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

    @Query("""
        UPDATE room_books
        SET title = :title,
            author = :author,
            firstPublishYear = :firstPublishYear,
            coverUrl = :coverUrl,
            openLibraryKey = :openLibraryKey,
            description = :description
        WHERE id = :bookId
    """)
    suspend fun updateBook(
        bookId: Long,
        title: String,
        author: String,
        firstPublishYear: Int?,
        coverUrl: String?,
        openLibraryKey: String?,
        description: String?
    )

    @Query("DELETE FROM room_books WHERE id = :bookId")
    suspend fun deleteBook(bookId: Long)
}