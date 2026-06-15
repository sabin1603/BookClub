package com.example.bookclub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.BookClubApplication
import com.example.bookclub.data.local.entity.CachedBookEntity
import com.example.bookclub.data.network.RetrofitClient
import com.example.bookclub.data.repository.BookRepository
import com.example.bookclub.data.repository.BookSearchItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BookSearchUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val books: List<BookSearchItem> = emptyList()
)

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BookClubApplication

    private val repository = BookRepository(
        api = RetrofitClient.openLibraryApi,
        cachedBookDao = app.database.cachedBookDao()
    )

    private val _searchState = MutableStateFlow(BookSearchUiState())
    val searchState: StateFlow<BookSearchUiState> = _searchState

    fun searchBooks(query: String) {
        viewModelScope.launch {
            _searchState.value = BookSearchUiState(isLoading = true)

            val result = repository.searchBooks(query)

            result
                .onSuccess { books ->
                    _searchState.value = BookSearchUiState(books = books)
                }
                .onFailure { error ->
                    _searchState.value = BookSearchUiState(errorMessage = error.message)
                }
        }
    }

    fun cacheSelectedBook(
        book: BookSearchItem,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            _searchState.value = _searchState.value.copy(isLoading = true)

            val result = repository.cacheSelectedBook(book)

            result
                .onSuccess { bookId ->
                    _searchState.value = _searchState.value.copy(isLoading = false)
                    onSuccess(bookId)
                }
                .onFailure { error ->
                    _searchState.value = _searchState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun observeBook(id: Long): Flow<CachedBookEntity?> {
        return repository.observeBook(id)
    }
}