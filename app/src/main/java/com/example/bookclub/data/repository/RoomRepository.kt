package com.example.bookclub.data.repository

import com.example.bookclub.data.local.dao.MembershipDao
import com.example.bookclub.data.local.dao.MessageDao
import com.example.bookclub.data.local.dao.RoomDao
import com.example.bookclub.data.local.entity.BookClubRoomEntity
import com.example.bookclub.data.local.entity.MembershipEntity
import com.example.bookclub.data.local.entity.MessageEntity
import com.example.bookclub.data.local.model.MessageWithUser
import kotlinx.coroutines.flow.Flow

class RoomRepository(
    private val roomDao: RoomDao,
    private val membershipDao: MembershipDao,
    private val messageDao: MessageDao
) {

    fun observeVisibleRooms(userId: Long): Flow<List<BookClubRoomEntity>> {
        return roomDao.observeVisibleRooms(userId)
    }

    fun observeRoom(roomId: Long): Flow<BookClubRoomEntity?> {
        return roomDao.observeRoom(roomId)
    }

    fun observeMessages(roomId: Long): Flow<List<MessageWithUser>> {
        return messageDao.observeMessagesForRoom(roomId)
    }

    suspend fun createRoom(
        title: String,
        bookTitle: String,
        bookAuthor: String,
        bookCoverUrl: String?,
        description: String,
        isPrivate: Boolean,
        accessCode: String?,
        ownerUserId: Long
    ): Result<Long> {
        if (title.trim().length < 3) {
            return Result.failure(Exception("Room title must have at least 3 characters."))
        }

        if (bookTitle.trim().isBlank()) {
            return Result.failure(Exception("Book title is required."))
        }

        if (bookAuthor.trim().isBlank()) {
            return Result.failure(Exception("Book author is required."))
        }

        if (isPrivate && accessCode.isNullOrBlank()) {
            return Result.failure(Exception("Private rooms need an access code."))
        }

        return try {
            val room = BookClubRoomEntity(
                title = title.trim(),
                bookTitle = bookTitle.trim(),
                bookAuthor = bookAuthor.trim(),
                bookCoverUrl = bookCoverUrl,
                description = description.trim(),
                isPrivate = isPrivate,
                accessCode = if (isPrivate) accessCode?.trim() else null,
                ownerUserId = ownerUserId
            )

            val roomId = roomDao.insertRoom(room)

            membershipDao.insertMembership(
                MembershipEntity(
                    userId = ownerUserId,
                    roomId = roomId
                )
            )

            Result.success(roomId)
        } catch (e: Exception) {
            Result.failure(Exception("Could not create room."))
        }
    }

    suspend fun joinPrivateRoom(
        accessCode: String,
        userId: Long
    ): Result<Unit> {
        val cleanCode = accessCode.trim()

        if (cleanCode.isBlank()) {
            return Result.failure(Exception("Enter an access code."))
        }

        val room = roomDao.findPrivateRoomByAccessCode(cleanCode)
            ?: return Result.failure(Exception("No private room found with this code."))

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