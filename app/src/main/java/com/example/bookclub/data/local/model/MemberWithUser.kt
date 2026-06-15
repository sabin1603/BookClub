package com.example.bookclub.data.local.model

data class MemberWithUser(
    val userId: Long,
    val roomId: Long,
    val username: String,
    val email: String,
    val joinedAt: Long,
    val isAdmin: Boolean,
    val canMessage: Boolean
)