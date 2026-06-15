package com.example.bookclub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookclub.data.local.entity.CachedBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedBookDao {

    @Insert
    suspend fun insertBook(book: CachedBookEntity): Long

    @Query("SELECT * FROM cached_books WHERE id = :id LIMIT 1")
    fun observeBook(id: Long): Flow<CachedBookEntity?>
}