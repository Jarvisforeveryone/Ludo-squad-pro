package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseLudoRepository
import com.example.data.UserPreferencesRepository
import com.example.model.Player
import com.example.model.PlayerColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.model.LudoRoom
import com.example.model.RoomStatus

data class LobbyUiState(
    val currentUserId: String = "",
    val currentUserName: String = "Player",
    val currentUserAvatar: String? = null,
    val isAuthLoading: Boolean = true,
    val isProfileSetupRequired: Boolean = false,
    val roomCode: String = "",
    val otpDigits: List<String> = listOf("", "", "", "", "", ""),
    val isHost: Boolean = true,
    val players: List<Player> = emptyList(),
    val activeRoom: LudoRoom? = null,
    val gameStarted: Boolean = false,
    val hostLeftMessage: String? = null,
    val soundEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val canStartGame: Boolean = false,
    val errorMessage: String? = null
)

class LudoViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val firebaseRepository = FirebaseLudoRepository()

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    init {
        initializeUserAndAuth()
    }

    /**
     * Step 1 Logic: Authenticates anonymously with Firebase Auth on app launch,
     * loads local profile from DataStore, saves profile to Realtime Database users/{uid},
     * and persists the UID locally.
     */
    fun initializeUserAndAuth() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isAuthLoading = true) }

            val authResult = firebaseRepository.authenticateAnonymously()
            val uid = authResult.getOrNull()

            val profile = userPreferencesRepository.userProfileFlow.first()
            val finalUid = uid ?: profile.uid

            val name = profile.name.ifEmpty { "Player" }
            val avatar = profile.avatarUrl

            // Persist UID and profile locally
            userPreferencesRepository.saveProfile(uid = finalUid, name = name, avatarUrl = avatar)

            // Save user profile to Firebase Realtime Database
            firebaseRepository.saveUserProfile(uid = finalUid, name = name, avatarUrl = avatar)

            val initialPlayer = Player(
                uid = finalUid,
                name = name,
                avatar = avatar,
                color = PlayerColor.GREEN,
                isHost = true,
                isOnline = true
            )

            _uiState.update { current ->
                current.copy(
                    currentUserId = finalUid,
                    currentUserName = name,
                    currentUserAvatar = avatar,
                    soundEnabled = profile.soundEnabled,
                    isAuthLoading = false,
                    isProfileSetupRequired = profile.name.isEmpty(),
                    players = listOf(initialPlayer),
                    canStartGame = false
                )
            }
        }
    }

    /**
     * Updates user profile (Name & Avatar), persisting to DataStore and Realtime Database.
     */
    fun updatePlayerProfile(newName: String, avatarUrl: String? = null) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val uid = _uiState.value.currentUserId
            val updatedAvatar = avatarUrl ?: _uiState.value.currentUserAvatar

            userPreferencesRepository.saveProfile(uid = uid, name = trimmed, avatarUrl = updatedAvatar)
            firebaseRepository.saveUserProfile(uid = uid, name = trimmed, avatarUrl = updatedAvatar)

            _uiState.update { current ->
                val updatedPlayers = current.players.map { player ->
                    if (player.uid == current.currentUserId) {
                        player.copy(name = trimmed, avatar = updatedAvatar)
                    } else player
                }
                current.copy(
                    currentUserName = trimmed,
                    currentUserAvatar = updatedAvatar,
                    isProfileSetupRequired = false,
                    players = updatedPlayers
                )
            }
        }
    }

    fun updatePlayerName(newName: String) {
        updatePlayerProfile(newName = newName, avatarUrl = _uiState.value.currentUserAvatar)
    }

    fun createPrivateRoom(onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            var uid = _uiState.value.currentUserId
            if (uid.isBlank()) {
                uid = firebaseRepository.currentAuthUid ?: ""
            }
            if (uid.isBlank()) {
                val authRes = firebaseRepository.authenticateAnonymously()
                uid = authRes.getOrDefault("")
            }

            if (uid.isBlank()) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = "Authentication in progress. Please try creating room again."
                    )
                }
                return@launch
            }

            val name = _uiState.value.currentUserName
            val avatar = _uiState.value.currentUserAvatar

            try {
                val result = firebaseRepository.createRoom(hostUid = uid, hostName = name, hostAvatar = avatar)
                result.onSuccess { code ->
                    android.util.Log.d("LudoDebug", "Room created successfully: $code")
                    _uiState.update { current ->
                        current.copy(
                            currentUserId = uid,
                            roomCode = code,
                            isHost = true,
                            isLoading = false
                        )
                    }
                    observeRoomState(code)
                    launch(Dispatchers.Main) { onSuccess(code) }
                }.onFailure { error ->
                    android.util.Log.e("LudoDebug", "Room creation failed", error)
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to create room"
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LudoDebug", "Room creation failed with exception", e)
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "An unexpected error occurred while creating room"
                    )
                }
            }
        }
    }

    fun updateOtpDigit(index: Int, value: String) {
        if (index !in 0..5) return
        val currentDigits = _uiState.value.otpDigits.toMutableList()
        currentDigits[index] = value.take(1)
        _uiState.update { it.copy(otpDigits = currentDigits.toList()) }
    }

    fun joinRoomWithEnteredCode(onSuccess: (String) -> Unit) {
        val code = _uiState.value.otpDigits.joinToString("")
        if (code.length != 6) {
            _uiState.update { it.copy(errorMessage = "Please enter full 6-digit room code") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val uid = _uiState.value.currentUserId
            val name = _uiState.value.currentUserName
            val avatar = _uiState.value.currentUserAvatar

            val result = firebaseRepository.joinRoom(roomCode = code, uid = uid, name = name, avatar = avatar)
            result.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        roomCode = code,
                        isHost = false,
                        isLoading = false
                    )
                }
                observeRoomState(code)
                launch(Dispatchers.Main) { onSuccess(code) }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to join room"
                    )
                }
            }
        }
    }

    fun observeRoomState(code: String) {
        if (code.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.observeRoom(code).collect { room ->
                if (room != null) {
                    if (room.status == RoomStatus.CANCELLED) {
                        _uiState.update { current ->
                            current.copy(
                                activeRoom = null,
                                gameStarted = false,
                                hostLeftMessage = "Host closed the room",
                                errorMessage = "Host closed the room"
                            )
                        }
                        return@collect
                    }

                    val playerList = room.players.values.toList()
                    val isUserHost = room.hostId == _uiState.value.currentUserId
                    val canStart = isUserHost && playerList.size in 2..4 && room.status == RoomStatus.WAITING

                    _uiState.update { current ->
                        current.copy(
                            activeRoom = room,
                            players = playerList,
                            isHost = isUserHost,
                            canStartGame = canStart,
                            gameStarted = room.status == RoomStatus.PLAYING
                        )
                    }
                } else {
                    _uiState.update { current ->
                        current.copy(
                            activeRoom = null,
                            gameStarted = false,
                            errorMessage = if (current.activeRoom != null) "Host closed the room" else current.errorMessage
                        )
                    }
                }
            }
        }
    }

    /**
     * CRITICAL FIX #1: Starts the game in Firebase and updates room status to "playing".
     */
    fun startGame(onSuccess: () -> Unit = {}) {
        val roomCode = _uiState.value.roomCode
        val uid = _uiState.value.currentUserId
        if (roomCode.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val res = firebaseRepository.startGame(roomCode, uid)
            _uiState.update { it.copy(isLoading = false) }
            res.onSuccess {
                launch(Dispatchers.Main) { onSuccess() }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.localizedMessage ?: "Failed to start game") }
            }
        }
    }

    /**
     * CRITICAL FIX #2: Updates current turn in Firebase to next player's UID.
     */
    fun passTurn(roomCode: String, nextPlayerUid: String) {
        if (roomCode.isBlank() || nextPlayerUid.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.updateTurn(roomCode, nextPlayerUid)
        }
    }

    /**
     * CRITICAL FIX #3: Updates dice value in Firebase.
     */
    fun rollDice(roomCode: String, rolledValue: Int) {
        if (roomCode.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.updateDiceValue(roomCode, rolledValue)
        }
    }

    /**
     * CRITICAL FIX #4: Updates pawn positions in Firebase for a player.
     */
    fun updatePawnPositions(roomCode: String, playerUid: String, pieces: List<Int>) {
        if (roomCode.isBlank() || playerUid.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.updatePawnPositions(roomCode, playerUid, pieces)
        }
    }

    /**
     * CRITICAL FIX #5: Leaves or closes the room in Firebase.
     */
    fun leaveRoom(roomCode: String, onLeft: () -> Unit = {}) {
        val uid = _uiState.value.currentUserId
        val isHost = _uiState.value.isHost
        val code = roomCode.ifBlank { _uiState.value.roomCode }

        if (code.isBlank()) {
            onLeft()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.leaveRoom(code, uid, isHost)
            _uiState.update { current ->
                current.copy(
                    activeRoom = null,
                    gameStarted = false,
                    canStartGame = false
                )
            }
            launch(Dispatchers.Main) { onLeft() }
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.setSoundEnabled(enabled)
            _uiState.update { it.copy(soundEnabled = enabled) }
        }
    }

    fun clearData(onCleared: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.clearData()
            initializeUserAndAuth()
            onCleared()
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null, hostLeftMessage = null) }
    }

    val chatMessages: MutableStateFlow<List<com.example.model.ChatMessage>> = MutableStateFlow(emptyList())

    fun startObservingChat(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.observeChatMessages(code).collect { messages ->
                chatMessages.value = messages
            }
        }
    }

    fun sendChatMessage(roomCode: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val uid = _uiState.value.currentUserId
            val name = _uiState.value.currentUserName
            firebaseRepository.sendChatMessage(roomCode, uid, name, trimmed)
        }
    }
}
