package com.example.bookclub.data.repository

import com.example.bookclub.data.local.dao.CachedBookDao
import com.example.bookclub.data.local.entity.CachedBookEntity
import com.example.bookclub.data.network.OpenLibraryApi
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val api: OpenLibraryApi,
    private val cachedBookDao: CachedBookDao
) {

    suspend fun searchBooks(query: String): Result<List<BookSearchItem>> {
        val cleanQuery = query.trim()

        if (cleanQuery.length < 2) {
            return Result.failure(Exception("Search text must have at least 2 characters."))
        }

        return try {
            val englishQuery = "$cleanQuery language:eng"
            val response = api.searchBooks(englishQuery)

            val books = response.docs
                .mapNotNull { dto ->
                    val englishEditionTitle = dto.editions
                        ?.docs
                        ?.firstOrNull { edition ->
                            edition.language?.contains("eng") == true && !edition.title.isNullOrBlank()
                        }
                        ?.title

                    val displayTitle = englishEditionTitle ?: dto.title ?: return@mapNotNull null
                    val author = dto.authorNames?.firstOrNull() ?: "Unknown author"

                    val coverUrl = dto.coverId?.let {
                        "https://covers.openlibrary.org/b/id/$it-M.jpg"
                    }

                    BookSearchItem(
                        title = displayTitle,
                        author = author,
                        firstPublishYear = dto.firstPublishYear,
                        coverUrl = coverUrl,
                        openLibraryKey = dto.key
                    )
                }
                .distinctBy { "${it.title.lowercase()}-${it.author.lowercase()}" }
                .take(20)

            Result.success(books)
        } catch (e: Exception) {
            Result.failure(Exception("Could not load books. Check your internet connection."))
        }
    }

    suspend fun cacheSelectedBook(book: BookSearchItem): Result<Long> {
        return try {
            val description = loadDescription(book.openLibraryKey)

            val id = cachedBookDao.insertBook(
                CachedBookEntity(
                    title = book.title,
                    author = book.author,
                    firstPublishYear = book.firstPublishYear,
                    coverUrl = book.coverUrl,
                    openLibraryKey = book.openLibraryKey,
                    description = description
                )
            )

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(Exception("Could not save selected book."))
        }
    }

    fun observeBook(id: Long): Flow<CachedBookEntity?> {
        return cachedBookDao.observeBook(id)
    }

    private suspend fun loadDescription(openLibraryKey: String?): String? {
        if (openLibraryKey.isNullOrBlank()) return null

        val workId = openLibraryKey.substringAfterLast("/")
        if (workId.isBlank()) return null

        val details = api.getWorkDetails(workId)

        return when (val description = details.description) {
            is String -> description
            is Map<*, *> -> description["value"] as? String
            else -> null
        }
    }
}