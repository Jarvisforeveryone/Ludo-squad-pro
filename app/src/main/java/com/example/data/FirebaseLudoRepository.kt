package com.example.data

import com.example.model.LudoRoom
import com.example.model.Player
import com.example.model.PlayerColor
import com.example.model.RoomStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class FirebaseDbUser(
    val uid: String = "",
    val name: String = "",
    val avatar: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

class FirebaseLudoRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    val currentAuthUid: String? get() = auth.currentUser?.uid

    /**
     * Authenticates anonymously with Firebase Auth if not already signed in.
     */
    suspend fun authenticateAnonymously(): Result<String> = suspendCancellableCoroutine { continuation ->
        val currentUser = auth.currentUser
        if (currentUser != null) {
            continuation.resume(Result.success(currentUser.uid))
            return@suspendCancellableCoroutine
        }

        auth.signInAnonymously()
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    continuation.resume(Result.success(user.uid))
                } else {
                    continuation.resume(Result.failure(Exception("Firebase auth returned null user")))
                }
            }
            .addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
            }
    }

    /**
     * Saves user profile under users/{uid} in Firebase Realtime Database.
     */
    suspend fun saveUserProfile(uid: String, name: String, avatarUrl: String?): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val userRef = database.getReference("users").child(uid)
            val userMap = mapOf(
                "uid" to uid,
                "name" to name,
                "avatar" to avatarUrl,
                "createdAt" to System.currentTimeMillis()
            )

            userRef.setValue(userMap)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }

    /**
     * Fetches user profile from Realtime Database users/{uid}.
     */
    suspend fun fetchUserProfile(uid: String): Result<FirebaseDbUser> =
        suspendCancellableCoroutine { continuation ->
            val userRef = database.getReference("users").child(uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dbUser = snapshot.getValue(FirebaseDbUser::class.java)
                    if (dbUser != null) {
                        continuation.resume(Result.success(dbUser))
                    } else {
                        continuation.resume(Result.failure(Exception("User profile not found in Firebase")))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(error.toException()))
                }
            })
        }

    /**
     * Item 3: Creates a private room in Firebase Realtime Database under rooms/{roomCode}.
     */
    suspend fun createRoom(hostUid: String, hostName: String, hostAvatar: String?): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val effectiveUid = if (hostUid.isNotBlank()) hostUid else currentAuthUid ?: ""
            if (effectiveUid.isBlank()) {
                continuation.resume(Result.failure(Exception("Cannot create room: User is not authenticated. Please wait a moment and try again.")))
                return@suspendCancellableCoroutine
            }

            val roomCode = (100000..999999).random().toString()

            try {
                val roomRef = database.getReference("rooms").child(roomCode)
                val hostColor = PlayerColor.values().random().name.lowercase()
                val hostPlayerMap = mapOf(
                    "uid" to effectiveUid,
                    "name" to hostName.ifBlank { "Player" },
                    "avatar" to hostAvatar,
                    "color" to hostColor,
                    "isHost" to true,
                    "isOnline" to true,
                    "pieces" to listOf(0, 0, 0, 0),
                    "sixCount" to 0
                )

                val roomData = mapOf(
                    "roomCode" to roomCode,
                    "status" to "waiting",
                    "hostId" to effectiveUid,
                    "currentTurn" to effectiveUid,
                    "diceValue" to 1,
                    "createdAt" to System.currentTimeMillis(),
                    "players" to mapOf(effectiveUid to hostPlayerMap)
                )

                roomRef.setValue(roomData)
                    .addOnSuccessListener {
                        continuation.resume(Result.success(roomCode))
                    }
                    .addOnFailureListener { error ->
                        continuation.resume(Result.failure(error))
                    }
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }

    /**
     * Item 3: Joins an existing room in Firebase Realtime Database.
     */
    suspend fun joinRoom(roomCode: String, uid: String, name: String, avatar: String?): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val effectiveUid = if (uid.isNotBlank()) uid else currentAuthUid ?: ""
            if (effectiveUid.isBlank()) {
                continuation.resume(Result.failure(Exception("Cannot join room: User is not authenticated.")))
                return@suspendCancellableCoroutine
            }

            try {
                val roomRef = database.getReference("rooms").child(roomCode)

                roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            continuation.resume(Result.failure(Exception("Room $roomCode does not exist")))
                            return
                        }

                        val status = snapshot.child("status").getValue(String::class.java) ?: "waiting"
                        if (status != "waiting") {
                            continuation.resume(Result.failure(Exception("Game in room $roomCode has already started")))
                            return
                        }

                        val playersSnap = snapshot.child("players")
                        if (playersSnap.hasChild(effectiveUid)) {
                            // Player already in room, mark online
                            roomRef.child("players").child(effectiveUid).child("isOnline").setValue(true)
                            continuation.resume(Result.success(Unit))
                            return
                        }

                        val existingPlayerCount = playersSnap.childrenCount
                        if (existingPlayerCount >= 4) {
                            continuation.resume(Result.failure(Exception("Room $roomCode is full (max 4 players)")))
                            return
                        }

                        val takenColors = mutableListOf<String>()
                        for (child in playersSnap.children) {
                            child.child("color").getValue(String::class.java)?.let { takenColors.add(it.lowercase()) }
                        }

                        val availableColors = PlayerColor.values().map { it.name.lowercase() } - takenColors.toSet()
                        val assignedColor = availableColors.firstOrNull() ?: PlayerColor.RED.name.lowercase()

                        val newPlayerMap = mapOf(
                            "uid" to effectiveUid,
                            "name" to name.ifBlank { "Player" },
                            "avatar" to avatar,
                            "color" to assignedColor,
                            "isHost" to false,
                            "isOnline" to true,
                            "pieces" to listOf(0, 0, 0, 0),
                            "sixCount" to 0
                        )

                        roomRef.child("players").child(effectiveUid).setValue(newPlayerMap)
                            .addOnSuccessListener {
                                continuation.resume(Result.success(Unit))
                            }
                            .addOnFailureListener { error ->
                                continuation.resume(Result.failure(error))
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.failure(error.toException()))
                    }
                })
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }

    /**
     * Item 4: Real-time observation of room state from rooms/{roomCode}.
     */
    fun observeRoom(roomCode: String): Flow<LudoRoom?> = callbackFlow {
        if (roomCode.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val roomRef = database.getReference("rooms").child(roomCode)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }

                val code = snapshot.child("roomCode").getValue(String::class.java) ?: roomCode
                val statusStr = snapshot.child("status").getValue(String::class.java) ?: "waiting"
                val hostId = snapshot.child("hostId").getValue(String::class.java) ?: ""
                val currentTurn = snapshot.child("currentTurn").getValue(String::class.java) ?: ""
                val diceVal = snapshot.child("diceValue").getValue(Long::class.java)?.toInt() ?: 1
                val createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L

                val statusEnum = when (statusStr.lowercase()) {
                    "playing" -> RoomStatus.PLAYING
                    "finished" -> RoomStatus.FINISHED
                    "cancelled" -> RoomStatus.CANCELLED
                    else -> RoomStatus.WAITING
                }

                val playersMap = mutableMapOf<String, Player>()
                snapshot.child("players").children.forEach { child ->
                    val pUid = child.child("uid").getValue(String::class.java) ?: child.key ?: ""
                    val pName = child.child("name").getValue(String::class.java) ?: "Player"
                    val pAvatar = child.child("avatar").getValue(String::class.java)
                    val pColorStr = child.child("color").getValue(String::class.java) ?: "green"
                    val pIsHost = child.child("isHost").getValue(Boolean::class.java) ?: false
                    val pIsOnline = child.child("isOnline").getValue(Boolean::class.java) ?: true

                    val pColor = try {
                        PlayerColor.valueOf(pColorStr.uppercase())
                    } catch (e: Exception) {
                        PlayerColor.GREEN
                    }

                    val piecesList = mutableListOf<Int>()
                    child.child("pieces").children.forEach { pieceSnap ->
                        pieceSnap.getValue(Long::class.java)?.let { piecesList.add(it.toInt()) }
                    }
                    val finalPieces = if (piecesList.size == 4) piecesList else listOf(-1, -1, -1, -1)

                    val player = Player(
                        uid = pUid,
                        name = pName,
                        avatar = pAvatar,
                        color = pColor,
                        isHost = pIsHost,
                        isOnline = pIsOnline,
                        pieces = finalPieces
                    )
                    playersMap[pUid] = player
                }

                val room = LudoRoom(
                    roomCode = code,
                    status = statusEnum,
                    hostId = hostId,
                    currentTurn = currentTurn,
                    diceValue = diceVal,
                    createdAt = createdAt,
                    players = playersMap
                )
                trySend(room)
            }

            override fun onCancelled(error: DatabaseError) {
                // Ignore or log error
            }
        }

        roomRef.addValueEventListener(listener)
        awaitClose { roomRef.removeEventListener(listener) }
    }

    /**
     * CRITICAL FIX #1: Starts the game by updating status to "playing" and set currentTurn to hostUid.
     */
    suspend fun startGame(roomCode: String, hostUid: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val roomRef = database.getReference("rooms").child(roomCode)
            val updates = mapOf(
                "status" to "playing",
                "currentTurn" to hostUid
            )
            roomRef.updateChildren(updates)
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }

    /**
     * CRITICAL FIX #2: Updates current turn to next player UID in rooms/{roomCode}/currentTurn.
     */
    suspend fun updateTurn(roomCode: String, nextPlayerUid: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val roomRef = database.getReference("rooms").child(roomCode)
            roomRef.child("currentTurn").setValue(nextPlayerUid)
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }

    /**
     * CRITICAL FIX #3: Writes dice value to rooms/{roomCode}/diceValue.
     */
    suspend fun updateDiceValue(roomCode: String, value: Int): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val roomRef = database.getReference("rooms").child(roomCode)
            roomRef.child("diceValue").setValue(value)
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }

    /**
     * CRITICAL FIX #4: Updates pawn positions for player in rooms/{roomCode}/players/{uid}/pieces.
     */
    suspend fun updatePawnPositions(roomCode: String, playerUid: String, pieces: List<Int>): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val playerRef = database.getReference("rooms").child(roomCode).child("players").child(playerUid)
            playerRef.child("pieces").setValue(pieces)
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }

    /**
     * CRITICAL FIX #5: Leaves or deletes room. If host, updates status to "cancelled" then removes room.
     */
    suspend fun leaveRoom(roomCode: String, uid: String, isHost: Boolean): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val roomRef = database.getReference("rooms").child(roomCode)
            if (isHost) {
                roomRef.child("status").setValue("cancelled")
                    .addOnCompleteListener {
                        roomRef.removeValue()
                            .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                            .addOnFailureListener { continuation.resume(Result.failure(it)) }
                    }
            } else {
                roomRef.child("players").child(uid).removeValue()
                    .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                    .addOnFailureListener { continuation.resume(Result.failure(it)) }
            }
        }

    /**
     * Item 12: Sends a chat message to rooms/{roomCode}/chat. Keeps max 50 messages.
     */
    suspend fun sendChatMessage(roomCode: String, senderUid: String, senderName: String, text: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            if (roomCode.isBlank() || text.isBlank()) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            val chatRef = database.getReference("rooms").child(roomCode).child("chat")
            val newMsgRef = chatRef.push()
            val msgId = newMsgRef.key ?: System.currentTimeMillis().toString()

            val msgData = mapOf(
                "messageId" to msgId,
                "senderUid" to senderUid,
                "senderName" to senderName,
                "text" to text,
                "timestamp" to System.currentTimeMillis()
            )

            newMsgRef.setValue(msgData)
                .addOnSuccessListener {
                    // Trim chat history if > 50 messages
                    chatRef.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.childrenCount > 50) {
                                val toDeleteCount = snapshot.childrenCount - 50
                                var deleted = 0L
                                for (child in snapshot.children) {
                                    if (deleted < toDeleteCount) {
                                        child.ref.removeValue()
                                        deleted++
                                    } else break
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { error ->
                    continuation.resume(Result.failure(error))
                }
        }

    /**
     * Item 12: Observes chat messages for rooms/{roomCode}/chat in real time.
     */
    fun observeChatMessages(roomCode: String): Flow<List<com.example.model.ChatMessage>> = callbackFlow {
        if (roomCode.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val chatRef = database.getReference("rooms").child(roomCode).child("chat")
            .orderByChild("timestamp").limitToLast(50)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.model.ChatMessage>()
                for (child in snapshot.children) {
                    val msgId = child.child("messageId").getValue(String::class.java) ?: child.key ?: ""
                    val sUid = child.child("senderUid").getValue(String::class.java) ?: ""
                    val sName = child.child("senderName").getValue(String::class.java) ?: "Player"
                    val txt = child.child("text").getValue(String::class.java) ?: ""
                    val ts = child.child("timestamp").getValue(Long::class.java) ?: 0L

                    list.add(com.example.model.ChatMessage(msgId, sUid, sName, txt, ts))
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        chatRef.addValueEventListener(listener)
        awaitClose { chatRef.removeEventListener(listener) }
    }
}

