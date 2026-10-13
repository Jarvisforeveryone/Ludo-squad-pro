package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val uid: String = "",
    val name: String = "",
    val avatar: String? = null,
    val color: PlayerColor = PlayerColor.GREEN,
    val isHost: Boolean = false,
    val isOnline: Boolean = true,
    val pieces: List<Int> = listOf(0, 0, 0, 0)
)
