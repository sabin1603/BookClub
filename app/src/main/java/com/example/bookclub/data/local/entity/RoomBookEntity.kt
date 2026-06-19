package com.example.bookclub.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "room_books",
    foreignKeys = [
        ForeignKey(
            entity = BookClubRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roomId")]
)
data class RoomBookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long,
    val title: String,
    val author: String,
    val firstPublishYear: Int?,
    val coverUrl: String?,
    val openLibraryKey: String?,
    val description: String?,
    val displayOrder: Int = 0
)