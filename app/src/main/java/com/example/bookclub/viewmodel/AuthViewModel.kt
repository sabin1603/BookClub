package com.example.bookclub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.BookClubApplication
import com.example.bookclub.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BookClubApplication
    private val repository = AuthRepository(app.database.userDao())

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val result = repository.login(email, password)

            result
                .onSuccess { user ->
                    app.sessionManager.saveSession(user.id, user.username)
                    _uiState.value = AuthUiState()
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState(errorMessage = error.message)
                }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val result = repository.register(username, email, password)

            result
                .onSuccess { user ->
                    app.sessionManager.saveSession(user.id, user.username)
                    _uiState.value = AuthUiState()
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState(errorMessage = error.message)
                }
        }
    }
}