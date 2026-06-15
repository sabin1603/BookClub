package com.example.bookclub.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bookclub.data.local.dao.CachedBookDao
import com.example.bookclub.data.local.dao.MembershipDao
import com.example.bookclub.data.local.dao.MessageDao
import com.example.bookclub.data.local.dao.RoomDao
import com.example.bookclub.data.local.dao.UserDao
import com.example.bookclub.data.local.entity.BookClubRoomEntity
import com.example.bookclub.data.local.entity.CachedBookEntity
import com.example.bookclub.data.local.entity.MembershipEntity
import com.example.bookclub.data.local.entity.MessageEntity
import com.example.bookclub.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        BookClubRoomEntity::class,
        MessageEntity::class,
        MembershipEntity::class,
        CachedBookEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun roomDao(): RoomDao
    abstract fun membershipDao(): MembershipDao
    abstract fun messageDao(): MessageDao
    abstract fun cachedBookDao(): CachedBookDao
}