package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val messageId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
