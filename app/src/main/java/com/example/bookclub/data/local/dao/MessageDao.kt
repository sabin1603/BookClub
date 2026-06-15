package com.example.bookclub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bookclub.data.local.entity.MessageEntity
import com.example.bookclub.data.local.model.MessageWithUser
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("""
        SELECT 
            messages.id AS id,
            messages.roomId AS roomId,
            messages.userId AS userId,
            messages.content AS content,
            messages.createdAt AS createdAt,
            users.username AS username
        FROM messages
        INNER JOIN users ON users.id = messages.userId
        WHERE messages.roomId = :roomId
        ORDER BY messages.createdAt ASC
    """)
    fun observeMessagesForRoom(roomId: Long): Flow<List<MessageWithUser>>
}