package com.example.navigation

sealed class LudoNavRoute(val route: String) {
    data object Splash : LudoNavRoute("splash")
    data object Lobby : LudoNavRoute("lobby")
    data object GameBoard : LudoNavRoute("game_board/{roomCode}") {
        fun createRoute(roomCode: String): String = "game_board/$roomCode"
    }
    data object Rules : LudoNavRoute("rules")
    data object Settings : LudoNavRoute("settings")
}
