package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class LudoRoom(
    val roomCode: String = "",
    val status: RoomStatus = RoomStatus.WAITING,
    val hostId: String = "",
    val currentTurn: String = "",
    val diceValue: Int = 1,
    val createdAt: Long = 0L,
    val players: Map<String, Player> = emptyMap(),
    val rankings: Map<String, String> = emptyMap()
)
