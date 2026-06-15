package com.example.bookclub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookclub.data.local.entity.RoomBanEntity

@Dao
interface RoomBanDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBan(ban: RoomBanEntity): Long

    @Query("""
        SELECT COUNT(*) FROM room_bans
        WHERE roomId = :roomId
        AND (
            bannedUserId = :userId
            OR LOWER(bannedEmail) = LOWER(:email)
        )
    """)
    suspend fun countBanForUserOrEmail(
        roomId: Long,
        userId: Long,
        email: String
    ): Int
}