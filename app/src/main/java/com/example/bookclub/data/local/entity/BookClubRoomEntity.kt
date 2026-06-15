package com.example.bookclub.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rooms",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerUserId")]
)
data class BookClubRoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val bookTitle: String,
    val bookAuthor: String,
    val bookCoverUrl: String?,
    val description: String,
    val isPrivate: Boolean,
    val accessCode: String?,
    val ownerUserId: Long,
    val createdAt: Long = System.currentTimeMillis()
)