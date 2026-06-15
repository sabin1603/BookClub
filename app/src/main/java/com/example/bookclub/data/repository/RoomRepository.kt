package com.example.bookclub.data.repository

import android.util.Log
import com.example.bookclub.data.local.dao.MembershipDao
import com.example.bookclub.data.local.dao.MessageDao
import com.example.bookclub.data.local.dao.RoomBanDao
import com.example.bookclub.data.local.dao.RoomBookDao
import com.example.bookclub.data.local.dao.RoomDao
import com.example.bookclub.data.local.dao.UserDao
import com.example.bookclub.data.local.entity.BookClubRoomEntity
import com.example.bookclub.data.local.entity.MembershipEntity
import com.example.bookclub.data.local.entity.MessageEntity
import com.example.bookclub.data.local.entity.RoomBanEntity
import com.example.bookclub.data.local.entity.RoomBookEntity
import com.example.bookclub.data.local.model.MemberWithUser
import com.example.bookclub.data.local.model.MessageWithUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRepository(
    private val roomDao: RoomDao,
    private val roomBookDao: RoomBookDao,
    private val membershipDao: MembershipDao,
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val roomBanDao: RoomBanDao
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

    fun observeMembers(roomId: Long): Flow<List<MemberWithUser>> {
        return membershipDao.observeMembersForRoom(roomId)
    }

    fun observeIsAdmin(roomId: Long, userId: Long): Flow<Boolean> {
        return membershipDao.observeAdminCount(roomId, userId).map { it > 0 }
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

        if (title.trim().length > 60) {
            return Result.failure(Exception("Room title cannot be longer than 60 characters."))
        }

        if (description.trim().length > 300) {
            return Result.failure(Exception("Room description cannot be longer than 300 characters."))
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
                    roomId = roomId,
                    isAdmin = true,
                    canMessage = true
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

        val user = userDao.findById(userId)
            ?: return Result.failure(Exception("User not found."))

        val isBanned = roomBanDao.countBanForUserOrEmail(
            roomId = roomId,
            userId = userId,
            email = user.email
        ) > 0

        if (isBanned) {
            return Result.failure(Exception("You are banned from this room."))
        }

        if (room.isPrivate && room.accessCode != accessCode?.trim()) {
            return Result.failure(Exception("This private room needs the correct access code."))
        }

        membershipDao.insertMembership(
            MembershipEntity(
                userId = userId,
                roomId = room.id,
                isAdmin = false,
                canMessage = true
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

        val user = userDao.findById(userId)
            ?: return Result.failure(Exception("User not found."))

        val isBanned = roomBanDao.countBanForUserOrEmail(
            roomId = roomId,
            userId = userId,
            email = user.email
        ) > 0

        if (isBanned) {
            return Result.failure(Exception("You are banned from this room."))
        }

        val membership = membershipDao.findMembership(roomId, userId)
            ?: return Result.failure(Exception("You are not a member of this room."))

        if (!membership.canMessage) {
            return Result.failure(Exception("You are not allowed to send messages in this room."))
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

    suspend fun updateRoomSettings(
        roomId: Long,
        currentUserId: Long,
        title: String,
        description: String,
        isPrivate: Boolean,
        accessCode: String?
    ): Result<Unit> {
        if (!isAdmin(roomId, currentUserId)) {
            return Result.failure(Exception("Only admins can edit this room."))
        }

        if (title.trim().length < 3) {
            return Result.failure(Exception("Room title must have at least 3 characters."))
        }

        if (title.trim().length > 60) {
            return Result.failure(Exception("Room title cannot be longer than 60 characters."))
        }

        if (description.trim().length > 300) {
            return Result.failure(Exception("Room description cannot be longer than 300 characters."))
        }

        if (isPrivate && accessCode.isNullOrBlank()) {
            return Result.failure(Exception("Private rooms need an access code."))
        }

        roomDao.updateRoom(
            roomId = roomId,
            title = title.trim(),
            description = description.trim(),
            isPrivate = isPrivate,
            accessCode = if (isPrivate) accessCode?.trim() else null
        )

        return Result.success(Unit)
    }

    suspend fun deleteRoom(
        roomId: Long,
        currentUserId: Long
    ): Result<Unit> {
        if (!isAdmin(roomId, currentUserId)) {
            return Result.failure(Exception("Only admins can delete this room."))
        }

        roomDao.deleteRoom(roomId)
        return Result.success(Unit)
    }

    suspend fun addBook(
        roomId: Long,
        currentUserId: Long,
        book: RoomBookEntity
    ): Result<Unit> {
        if (!isAdmin(roomId, currentUserId)) {
            return Result.failure(Exception("Only admins can add books."))
        }

        if (book.title.isBlank() || book.author.isBlank()) {
            return Result.failure(Exception("Book title and author are required."))
        }

        roomBookDao.insertBook(book.copy(id = 0, roomId = roomId))
        return Result.success(Unit)
    }

    suspend fun updateBook(
        roomId: Long,
        currentUserId: Long,
        book: RoomBookEntity
    ): Result<Unit> {
        if (!isAdmin(roomId, currentUserId)) {
            return Result.failure(Exception("Only admins can edit books."))
        }

        if (book.title.isBlank() || book.author.isBlank()) {
            return Result.failure(Exception("Book title and author are required."))
        }

        roomBookDao.updateBook(
            bookId = book.id,
            title = book.title.trim(),
            author = book.author.trim(),
            firstPublishYear = book.firstPublishYear,
            coverUrl = book.coverUrl,
            openLibraryKey = book.openLibraryKey,
            description = book.description
        )

        return Result.success(Unit)
    }

    suspend fun deleteBook(
        roomId: Long,
        currentUserId: Long,
        bookId: Long
    ): Result<Unit> {
        if (!isAdmin(roomId, currentUserId)) {
            return Result.failure(Exception("Only admins can delete books."))
        }

        roomBookDao.deleteBook(bookId)
        return Result.success(Unit)
    }

    suspend fun setAdmin(
        roomId: Long,
        currentUserId: Long,
        targetUserId: Long,
        isAdmin: Boolean
    ): Result<Unit> {
        if (!isAdmin(roomId, currentUserId)) {
            return Result.failure(Exception("Only admins can change admin rights."))
        }

        val targetMembership = membershipDao.findMembership(roomId, targetUserId)
            ?: return Result.failure(Exception("Target user is not a room member."))

        if (!isAdmin && targetMembership.isAdmin && membershipDao.countAdmins(roomId) <= 1) {
            return Result.failure(Exception("A room must have at least one admin."))
        }

        membershipDao.updateAdmin(roomId, targetUserId, isAdmin)
        return Result.success(Unit)
    }

    suspend fun setCanMessage(
        roomId: Long,
        currentUserId: Long,
        targetUserId: Long,
        canMessage: Boolean
    ): Result<Unit> {
        if (!isAdmin(roomId, currentUserId)) {
            return Result.failure(Exception("Only admins can change messaging rights."))
        }

        membershipDao.updateCanMessage(roomId, targetUserId, canMessage)
        return Result.success(Unit)
    }

    suspend fun removeMember(
        roomId: Long,
        currentUserId: Long,
        targetUserId: Long
    ): Result<Unit> {
        if (!isAdmin(roomId, currentUserId)) {
            return Result.failure(Exception("Only admins can remove members."))
        }

        val targetMembership = membershipDao.findMembership(roomId, targetUserId)
            ?: return Result.failure(Exception("Target user is not a room member."))

        if (targetMembership.isAdmin && membershipDao.countAdmins(roomId) <= 1) {
            return Result.failure(Exception("Cannot remove the last admin."))
        }

        membershipDao.removeMembership(roomId, targetUserId)
        return Result.success(Unit)
    }

    suspend fun banMember(
        roomId: Long,
        currentUserId: Long,
        targetUserId: Long
    ): Result<Unit> {
        if (!isAdmin(roomId, currentUserId)) {
            return Result.failure(Exception("Only admins can ban members."))
        }

        if (currentUserId == targetUserId) {
            return Result.failure(Exception("You cannot ban yourself."))
        }

        val targetUser = userDao.findById(targetUserId)
            ?: return Result.failure(Exception("Target user not found."))

        val targetMembership = membershipDao.findMembership(roomId, targetUserId)

        if (targetMembership?.isAdmin == true && membershipDao.countAdmins(roomId) <= 1) {
            return Result.failure(Exception("Cannot ban the last admin."))
        }

        roomBanDao.insertBan(
            RoomBanEntity(
                roomId = roomId,
                bannedUserId = targetUserId,
                bannedEmail = targetUser.email,
                bannedByUserId = currentUserId,
                reason = null
            )
        )

        membershipDao.removeMembership(roomId, targetUserId)
        return Result.success(Unit)
    }

    suspend fun banEmail(
        roomId: Long,
        currentUserId: Long,
        email: String
    ): Result<Unit> {
        if (!isAdmin(roomId, currentUserId)) {
            return Result.failure(Exception("Only admins can ban users."))
        }

        val cleanEmail = email.trim()

        if (!cleanEmail.contains("@")) {
            return Result.failure(Exception("Enter a valid email address."))
        }

        val existingUser = userDao.findByEmail(cleanEmail)

        roomBanDao.insertBan(
            RoomBanEntity(
                roomId = roomId,
                bannedUserId = existingUser?.id,
                bannedEmail = cleanEmail,
                bannedByUserId = currentUserId,
                reason = null
            )
        )

        existingUser?.let {
            membershipDao.removeMembership(roomId, it.id)
        }

        return Result.success(Unit)
    }

    private suspend fun isAdmin(roomId: Long, userId: Long): Boolean {
        return membershipDao.isAdminCount(roomId, userId) > 0
    }
}