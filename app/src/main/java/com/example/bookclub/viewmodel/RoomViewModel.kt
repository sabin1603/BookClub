package com.example.bookclub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.BookClubApplication
import com.example.bookclub.data.local.entity.BookClubRoomEntity
import com.example.bookclub.data.local.entity.RoomBookEntity
import com.example.bookclub.data.local.model.MemberWithUser
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
        roomBookDao = app.database.roomBookDao(),
        membershipDao = app.database.membershipDao(),
        messageDao = app.database.messageDao(),
        userDao = app.database.userDao(),
        roomBanDao = app.database.roomBanDao()
    )

    private val _actionState = MutableStateFlow(RoomActionState())
    val actionState: StateFlow<RoomActionState> = _actionState

    private val loggedUserId: Long?
        get() = app.sessionManager.getUserId()

    val currentUserId: Long?
        get() = loggedUserId

    val loggedUsername: String
        get() = app.sessionManager.getUsername() ?: "Reader"

    fun observeVisibleRooms(): Flow<List<BookClubRoomEntity>> {
        val userId = loggedUserId ?: return flowOf(emptyList())
        return repository.observeVisibleRooms(userId)
    }

    fun observeRoom(roomId: Long): Flow<BookClubRoomEntity?> {
        return repository.observeRoom(roomId)
    }

    fun observeBooks(roomId: Long): Flow<List<RoomBookEntity>> {
        return repository.observeBooks(roomId)
    }

    fun observeMessages(roomId: Long): Flow<List<MessageWithUser>> {
        return repository.observeMessages(roomId)
    }

    fun observeMembers(roomId: Long): Flow<List<MemberWithUser>> {
        return repository.observeMembers(roomId)
    }

    fun observeIsCurrentUserAdmin(roomId: Long): Flow<Boolean> {
        val userId = loggedUserId ?: return flowOf(false)
        return repository.observeIsAdmin(roomId, userId)
    }

    fun createRoom(
        title: String,
        description: String,
        isPrivate: Boolean,
        accessCode: String?,
        books: List<RoomBookEntity>,
        onSuccess: () -> Unit
    ) {
        val userId = loggedUserId ?: run {
            _actionState.value = RoomActionState(errorMessage = "You must be logged in.")
            return
        }

        viewModelScope.launch {
            _actionState.value = RoomActionState(isLoading = true)

            repository.createRoom(
                title = title,
                description = description,
                isPrivate = isPrivate,
                accessCode = accessCode,
                ownerUserId = userId,
                books = books
            )
                .onSuccess {
                    _actionState.value = RoomActionState()
                    onSuccess()
                }
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun joinRoomById(roomIdText: String, accessCode: String?) {
        val userId = loggedUserId ?: run {
            _actionState.value = RoomActionState(errorMessage = "You must be logged in.")
            return
        }

        viewModelScope.launch {
            _actionState.value = RoomActionState(isLoading = true)

            repository.joinRoomById(roomIdText, accessCode, userId)
                .onSuccess {
                    _actionState.value = RoomActionState()
                }
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun sendMessage(roomId: Long, content: String) {
        val userId = loggedUserId ?: run {
            _actionState.value = RoomActionState(errorMessage = "You must be logged in.")
            return
        }

        viewModelScope.launch {
            repository.sendMessage(roomId, userId, content)
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun updateRoomSettings(
        roomId: Long,
        title: String,
        description: String,
        isPrivate: Boolean,
        accessCode: String?
    ) {
        val userId = loggedUserId ?: return

        viewModelScope.launch {
            repository.updateRoomSettings(roomId, userId, title, description, isPrivate, accessCode)
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun deleteRoom(roomId: Long, onSuccess: () -> Unit) {
        val userId = loggedUserId ?: return

        viewModelScope.launch {
            repository.deleteRoom(roomId, userId)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun addBook(roomId: Long, book: RoomBookEntity) {
        val userId = loggedUserId ?: return

        viewModelScope.launch {
            repository.addBook(roomId, userId, book)
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun updateBook(roomId: Long, book: RoomBookEntity) {
        val userId = loggedUserId ?: return

        viewModelScope.launch {
            repository.updateBook(roomId, userId, book)
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun deleteBook(roomId: Long, bookId: Long) {
        val userId = loggedUserId ?: return

        viewModelScope.launch {
            repository.deleteBook(roomId, userId, bookId)
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun setAdmin(roomId: Long, targetUserId: Long, isAdmin: Boolean) {
        val userId = loggedUserId ?: return

        viewModelScope.launch {
            repository.setAdmin(roomId, userId, targetUserId, isAdmin)
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun setCanMessage(roomId: Long, targetUserId: Long, canMessage: Boolean) {
        val userId = loggedUserId ?: return

        viewModelScope.launch {
            repository.setCanMessage(roomId, userId, targetUserId, canMessage)
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun removeMember(roomId: Long, targetUserId: Long) {
        val userId = loggedUserId ?: return

        viewModelScope.launch {
            repository.removeMember(roomId, userId, targetUserId)
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun banMember(roomId: Long, targetUserId: Long) {
        val userId = loggedUserId ?: return

        viewModelScope.launch {
            repository.banMember(roomId, userId, targetUserId)
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun banEmail(roomId: Long, email: String) {
        val userId = loggedUserId ?: return

        viewModelScope.launch {
            repository.banEmail(roomId, userId, email)
                .onFailure { error ->
                    _actionState.value = RoomActionState(errorMessage = error.message)
                }
        }
    }

    fun logout() {
        app.sessionManager.logout()
    }
}