package com.example.bookclub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookclub.data.local.entity.MembershipEntity
import com.example.bookclub.data.local.model.MemberWithUser
import kotlinx.coroutines.flow.Flow

@Dao
interface MembershipDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMembership(membership: MembershipEntity)

    @Query(
        """
        SELECT 
            memberships.userId AS userId,
            memberships.roomId AS roomId,
            users.username AS username,
            users.email AS email,
            memberships.joinedAt AS joinedAt,
            memberships.isAdmin AS isAdmin,
            memberships.canMessage AS canMessage,
            users.profileImageUri AS profileImageUri
        FROM memberships
        INNER JOIN users ON users.id = memberships.userId
        WHERE memberships.roomId = :roomId
        ORDER BY memberships.isAdmin DESC, users.username ASC
        """
    )
    fun observeMembersForRoom(roomId: Long): Flow<List<MemberWithUser>>

    @Query(
        """
        SELECT COUNT(*) FROM memberships
        WHERE roomId = :roomId
        AND userId = :userId
        AND isAdmin = 1
        """
    )
    suspend fun isAdminCount(roomId: Long, userId: Long): Int

    @Query(
        """
        SELECT COUNT(*) FROM memberships
        WHERE roomId = :roomId
        AND userId = :userId
        AND isAdmin = 1
        """
    )
    fun observeAdminCount(roomId: Long, userId: Long): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM memberships
        WHERE roomId = :roomId
        AND isAdmin = 1
        """
    )
    suspend fun countAdmins(roomId: Long): Int

    @Query(
        """
        SELECT * FROM memberships
        WHERE roomId = :roomId
        AND userId = :userId
        LIMIT 1
        """
    )
    suspend fun findMembership(
        roomId: Long,
        userId: Long
    ): MembershipEntity?

    @Query(
        """
        UPDATE memberships
        SET isAdmin = :isAdmin
        WHERE roomId = :roomId
        AND userId = :targetUserId
        """
    )
    suspend fun updateAdmin(
        roomId: Long,
        targetUserId: Long,
        isAdmin: Boolean
    )

    @Query(
        """
        UPDATE memberships
        SET canMessage = :canMessage
        WHERE roomId = :roomId
        AND userId = :targetUserId
        """
    )
    suspend fun updateCanMessage(
        roomId: Long,
        targetUserId: Long,
        canMessage: Boolean
    )

    @Query(
        """
        DELETE FROM memberships
        WHERE roomId = :roomId
        AND userId = :targetUserId
        """
    )
    suspend fun removeMembership(
        roomId: Long,
        targetUserId: Long
    )
}