package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.GameBoardScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.viewmodel.LudoViewModel

import androidx.compose.runtime.LaunchedEffect

@Composable
fun LudoNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    ludoViewModel: LudoViewModel = viewModel(),
    startDestination: String = LudoNavRoute.Lobby.route
) {
    val uiState by ludoViewModel.uiState.collectAsStateWithLifecycle()

    // Auto-navigate all players (Host & Guests) when room status changes to PLAYING in Firebase
    LaunchedEffect(uiState.gameStarted, uiState.roomCode) {
        if (uiState.gameStarted && uiState.roomCode.isNotBlank()) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != LudoNavRoute.GameBoard.route) {
                navController.navigate(LudoNavRoute.GameBoard.createRoute(uiState.roomCode)) {
                    popUpTo(LudoNavRoute.Lobby.route) { inclusive = false }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(LudoNavRoute.Splash.route) {
            SplashScreen(
                initialName = uiState.currentUserName,
                initialAvatarUrl = uiState.currentUserAvatar,
                onContinue = { enteredName, avatarUrl ->
                    ludoViewModel.updatePlayerProfile(enteredName, avatarUrl)
                    navController.navigate(LudoNavRoute.Lobby.route) {
                        popUpTo(LudoNavRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(LudoNavRoute.Lobby.route) {
            LobbyScreen(
                state = uiState,
                onCreateRoom = {
                    ludoViewModel.createPrivateRoom { generatedCode ->
                        navController.navigate(LudoNavRoute.GameBoard.createRoute(generatedCode))
                    }
                },
                onOtpDigitChange = { index, digit ->
                    ludoViewModel.updateOtpDigit(index, digit)
                },
                onJoinRoom = {
                    ludoViewModel.joinRoomWithEnteredCode { joinedCode ->
                        navController.navigate(LudoNavRoute.GameBoard.createRoute(joinedCode))
                    }
                },
                onStartGame = {
                    ludoViewModel.startGame {
                        navController.navigate(LudoNavRoute.GameBoard.createRoute(uiState.roomCode))
                    }
                },
                onNavigateToRules = {
                    navController.navigate(LudoNavRoute.Rules.route)
                },
                onNavigateToSettings = {
                    navController.navigate(LudoNavRoute.Settings.route)
                },
                onDismissError = {
                    ludoViewModel.dismissError()
                }
            )
        }

        composable(
            route = LudoNavRoute.GameBoard.route,
            arguments = listOf(
                navArgument("roomCode") { type = NavType.StringType; defaultValue = "738429" }
            )
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: "738429"
            val messages by ludoViewModel.chatMessages.collectAsStateWithLifecycle()

            // Observe room state continuously when on GameBoard
            LaunchedEffect(roomCode) {
                ludoViewModel.observeRoomState(roomCode)
            }

            GameBoardScreen(
                roomCode = roomCode,
                currentUserId = uiState.currentUserId,
                activeRoom = uiState.activeRoom,
                chatMessages = messages,
                onRollDice = { rolledVal ->
                    ludoViewModel.rollDice(roomCode, rolledVal)
                },
                onPassTurn = { nextPlayerUid ->
                    ludoViewModel.passTurn(roomCode, nextPlayerUid)
                },
                onUpdatePawnPositions = { playerUid, pieces ->
                    ludoViewModel.updatePawnPositions(roomCode, playerUid, pieces)
                },
                onSendChatMessage = { text ->
                    ludoViewModel.sendChatMessage(roomCode, text)
                },
                onStartChatObservation = {
                    ludoViewModel.startObservingChat(roomCode)
                },
                onLeaveGame = {
                    ludoViewModel.leaveRoom(roomCode) {
                        navController.popBackStack(LudoNavRoute.Lobby.route, inclusive = false)
                    }
                }
            )
        }

        composable(LudoNavRoute.Rules.route) {
            RulesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(LudoNavRoute.Settings.route) {
            SettingsScreen(
                currentName = uiState.currentUserName,
                soundEnabled = uiState.soundEnabled,
                onSoundToggle = { ludoViewModel.toggleSound(it) },
                onUpdateName = { ludoViewModel.updatePlayerName(it) },
                onNavigateToRules = { navController.navigate(LudoNavRoute.Rules.route) },
                onClearData = {
                    ludoViewModel.clearData {
                        navController.navigate(LudoNavRoute.Splash.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
