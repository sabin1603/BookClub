package com.example.bookclub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.BookClubApplication
import com.example.bookclub.data.local.entity.BookClubRoomEntity
import com.example.bookclub.data.local.model.MessageWithUser
import com.example.bookclub.data.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class RoomActionState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class RoomViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BookClubApplication

    private val repository = RoomRepository(
        roomDao = app.database.roomDao(),
        membershipDao = app.database.membershipDao(),
        messageDao = app.database.messageDao()
    )

    private val _actionState = MutableStateFlow(RoomActionState())
    val actionState: StateFlow<RoomActionState> = _actionState

    val loggedUsername: String
        get() = app.sessionManager.getUsername() ?: "Reader"

    private val loggedUserId: Long?
        get() = app.sessionManager.getUserId()

    fun observeVisibleRooms(): Flow<List<BookClubRoomEntity>> {
        val userId = loggedUserId ?: return flowOf(emptyList())
        return repository.observeVisibleRooms(userId)
    }

    fun observeRoom(roomId: Long): Flow<BookClubRoomEntity?> {
        return repository.observeRoom(roomId)
    }

    fun observeMessages(roomId: Long): Flow<List<MessageWithUser>> {
        return repository.observeMessages(roomId)
    }

    fun createRoom(
        title: String,
        bookTitle: String,
        bookAuthor: String,
        bookCoverUrl: String?,
        description: String,
        isPrivate: Boolean,
        accessCode: String?,
        onSuccess: () -> Unit
    ) {
        val userId = loggedUserId
        if (userId == null) {
            _actionState.value = RoomActionState(errorMessage = "You must be logged in.")
            return
        }

        viewModelScope.launch {
            _actionState.value = RoomActionState(isLoading = true)

            val result = repository.createRoom(
                title = title,
                bookTitle = bookTitle,
                bookAuthor = bookAuthor,
                bookCoverUrl = bookCoverUrl,
                description = description,
                isPrivate = isPrivate,
                accessCode = accessCode,
                ownerUserId = userId
            )

            result
                .onSuccess {
                    _actionState.value = RoomActionState()
                    onSuccess()
                }
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun joinPrivateRoom(
        accessCode: String
    ) {
        val userId = loggedUserId
        if (userId == null) {
            _actionState.value = RoomActionState(errorMessage = "You must be logged in.")
            return
        }

        viewModelScope.launch {
            _actionState.value = RoomActionState(isLoading = true)

            val result = repository.joinPrivateRoom(accessCode, userId)

            result
                .onSuccess {
                    _actionState.value = RoomActionState()
                }
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun sendMessage(
        roomId: Long,
        content: String
    ) {
        val userId = loggedUserId
        if (userId == null) {
            _actionState.value = RoomActionState(errorMessage = "You must be logged in.")
            return
        }

        viewModelScope.launch {
            val result = repository.sendMessage(
                roomId = roomId,
                userId = userId,
                content = content
            )

            result.onFailure { error ->
                _actionState.value = RoomActionState(errorMessage = error.message)
            }
        }
    }

    fun logout() {
        app.sessionManager.logout()
    }
}