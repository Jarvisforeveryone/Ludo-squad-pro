package com.example.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoomStatus {
    WAITING,
    PLAYING,
    FINISHED,
    CANCELLED
}
