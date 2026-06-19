package com.example.bookclub.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "room_bans",
    foreignKeys = [
        ForeignKey(
            entity = BookClubRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("roomId"),
        Index("bannedUserId"),
        Index("bannedEmail")
    ]
)
data class RoomBanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long,
    val bannedUserId: Long?,
    val bannedEmail: String?,
    val bannedByUserId: Long,
    val reason: String?,
    val createdAt: Long = System.currentTimeMillis()
)