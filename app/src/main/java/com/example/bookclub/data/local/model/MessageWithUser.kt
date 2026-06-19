package com.example.bookclub.data.local.model

data class MessageWithUser(
    val id: Long,
    val roomId: Long,
    val userId: Long,
    val content: String,
    val createdAt: Long,
    val username: String,
    val profileImageUri: String? = null
)