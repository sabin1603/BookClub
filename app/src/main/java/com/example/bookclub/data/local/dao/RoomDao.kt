package com.example.bookclub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookclub.data.local.entity.BookClubRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    @Insert
    suspend fun insertRoom(room: BookClubRoomEntity): Long

    @Query("""
        SELECT * FROM rooms
        WHERE ownerUserId = :userId
        OR id IN (
            SELECT roomId FROM memberships WHERE userId = :userId
        )
        ORDER BY createdAt DESC
    """)
    fun observeJoinedRooms(userId: Long): Flow<List<BookClubRoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :roomId LIMIT 1")
    fun observeRoom(roomId: Long): Flow<BookClubRoomEntity?>

    @Query("SELECT * FROM rooms WHERE id = :roomId LIMIT 1")
    suspend fun findRoomById(roomId: Long): BookClubRoomEntity?
}