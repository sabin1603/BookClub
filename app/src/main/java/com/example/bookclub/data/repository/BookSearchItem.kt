package com.example.bookclub.data.repository

data class BookSearchItem(
    val title: String,
    val author: String,
    val firstPublishYear: Int?,
    val coverUrl: String?,
    val openLibraryKey: String?
)