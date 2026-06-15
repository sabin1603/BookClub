package com.example.bookclub.data.repository

import com.example.bookclub.data.local.dao.MembershipDao
import com.example.bookclub.data.local.dao.MessageDao
import com.example.bookclub.data.local.dao.RoomBookDao
import com.example.bookclub.data.local.dao.RoomDao
import com.example.bookclub.data.local.entity.BookClubRoomEntity
import com.example.bookclub.data.local.entity.MembershipEntity
import com.example.bookclub.data.local.entity.MessageEntity
import com.example.bookclub.data.local.entity.RoomBookEntity
import com.example.bookclub.data.local.model.MessageWithUser
import kotlinx.coroutines.flow.Flow
import android.util.Log

class RoomRepository(
    private val roomDao: RoomDao,
    private val roomBookDao: RoomBookDao,
    private val membershipDao: MembershipDao,
    private val messageDao: MessageDao
) {

    fun observeVisibleRooms(userId: Long): Flow<List<BookClubRoomEntity>> {
        return roomDao.observeJoinedRooms(userId)
    }

    fun observeBooks(roomId: Long): Flow<List<RoomBookEntity>> {
        return roomBookDao.observeBooksForRoom(roomId)
    }

    fun observeRoom(roomId: Long): Flow<BookClubRoomEntity?> {
        return roomDao.observeRoom(roomId)
    }

    fun observeMessages(roomId: Long): Flow<List<MessageWithUser>> {
        return messageDao.observeMessagesForRoom(roomId)
    }

    suspend fun createRoom(
        title: String,
        description: String,
        isPrivate: Boolean,
        accessCode: String?,
        ownerUserId: Long,
        books: List<RoomBookEntity>
    ): Result<Long> {
        if (title.trim().length < 3) {
            return Result.failure(Exception("Room title must have at least 3 characters."))
        }

        if (isPrivate && accessCode.isNullOrBlank()) {
            return Result.failure(Exception("Private rooms need an access code."))
        }

        if (books.isEmpty()) {
            return Result.failure(Exception("Add at least one book to the room."))
        }

        return try {
            val room = BookClubRoomEntity(
                title = title.trim(),
                description = description.trim(),
                isPrivate = isPrivate,
                accessCode = if (isPrivate) accessCode?.trim() else null,
                ownerUserId = ownerUserId
            )

            val roomId = roomDao.insertRoom(room)

            val booksForRoom = books.mapIndexed { index, book ->
                book.copy(
                    id = 0,
                    roomId = roomId,
                    displayOrder = index
                )
            }

            roomBookDao.insertBooks(booksForRoom)

            membershipDao.insertMembership(
                MembershipEntity(
                    userId = ownerUserId,
                    roomId = roomId
                )
            )

            Result.success(roomId)
        } catch (e: Exception) {
            Log.e("RoomRepository", "Could not create room", e)
            Result.failure(Exception("Could not create room: ${e.message}"))
        }
    }

    suspend fun joinRoomById(
        roomIdText: String,
        accessCode: String?,
        userId: Long
    ): Result<Unit> {
        val roomId = roomIdText
            .trim()
            .removePrefix("#")
            .toLongOrNull()
            ?: return Result.failure(Exception("Enter a valid room ID."))

        val room = roomDao.findRoomById(roomId)
            ?: return Result.failure(Exception("No room found with this ID."))

        if (room.isPrivate && room.accessCode != accessCode?.trim()) {
            return Result.failure(Exception("This private room needs the correct access code."))
        }

        membershipDao.insertMembership(
            MembershipEntity(
                userId = userId,
                roomId = room.id
            )
        )

        return Result.success(Unit)
    }

    suspend fun sendMessage(
        roomId: Long,
        userId: Long,
        content: String
    ): Result<Unit> {
        val cleanContent = content.trim()

        if (cleanContent.isBlank()) {
            return Result.failure(Exception("Message cannot be empty."))
        }

        messageDao.insertMessage(
            MessageEntity(
                roomId = roomId,
                userId = userId,
                content = cleanContent
            )
        )

        return Result.success(Unit)
    }
}