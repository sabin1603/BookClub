package com.example.bookclub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookclub.data.local.entity.RoomBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomBookDao {

    @Insert
    suspend fun insertBook(book: RoomBookEntity): Long

    @Insert
    suspend fun insertBooks(books: List<RoomBookEntity>)

    @Query("""
        SELECT * FROM room_books
        WHERE roomId = :roomId
        ORDER BY displayOrder ASC, id ASC
    """)
    fun observeBooksForRoom(roomId: Long): Flow<List<RoomBookEntity>>
}