package com.example.bookclub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_books")
data class CachedBookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val firstPublishYear: Int?,
    val coverUrl: String?,
    val openLibraryKey: String?,
    val description: String?
)