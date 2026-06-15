package com.example.bookclub.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookclub.data.local.entity.RoomBookEntity
import com.example.bookclub.data.local.model.MemberWithUser
import com.example.bookclub.viewmodel.RoomViewModel

private const val ROOM_TITLE_MAX_LENGTH = 60
private const val ROOM_DESCRIPTION_MAX_LENGTH = 300

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomAdminScreen(
    roomId: Long,
    onBack: () -> Unit,
    onRoomDeleted: () -> Unit,
    viewModel: RoomViewModel = viewModel()
) {
    val room by viewModel.observeRoom(roomId).collectAsState(initial = null)
    val books by viewModel.observeBooks(roomId).collectAsState(initial = emptyList())
    val members by viewModel.observeMembers(roomId).collectAsState(initial = emptyList())
    val actionState by viewModel.actionState.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var accessCode by remember { mutableStateOf("") }

    var editingBookId by remember { mutableStateOf<Long?>(null) }
    var bookTitle by remember { mutableStateOf("") }
    var bookAuthor by remember { mutableStateOf("") }
    var bookYear by remember { mutableStateOf("") }

    var banEmail by remember { mutableStateOf("") }

    LaunchedEffect(room?.id) {
        room?.let {
            title = it.title.take(ROOM_TITLE_MAX_LENGTH)
            description = it.description.take(ROOM_DESCRIPTION_MAX_LENGTH)
            isPrivate = it.isPrivate
            accessCode = it.accessCode ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin panel") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Room settings",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            actionState.errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        if (it.length <= ROOM_TITLE_MAX_LENGTH) {
                            title = it
                        }
                    },
                    label = { Text("Room title") },
                    supportingText = {
                        Text("${title.length}/$ROOM_TITLE_MAX_LENGTH")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        if (it.length <= ROOM_DESCRIPTION_MAX_LENGTH) {
                            description = it
                        }
                    },
                    label = { Text("Description") },
                    supportingText = {
                        Text("${description.length}/$ROOM_DESCRIPTION_MAX_LENGTH")
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Private room")
                    Switch(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it }
                    )
                }
            }

            if (isPrivate) {
                item {
                    OutlinedTextField(
                        value = accessCode,
                        onValueChange = { accessCode = it },
                        label = { Text("Access code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.updateRoomSettings(
                            roomId = roomId,
                            title = title,
                            description = description,
                            isPrivate = isPrivate,
                            accessCode = accessCode
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save room settings")
                }
            }

            item {
                HorizontalDivider()
                Text(
                    text = "Books",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            items(books) { book ->
                BookAdminCard(
                    book = book,
                    onEdit = {
                        editingBookId = book.id
                        bookTitle = book.title
                        bookAuthor = book.author
                        bookYear = book.firstPublishYear?.toString() ?: ""
                    },
                    onDelete = {
                        viewModel.deleteBook(roomId, book.id)
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = bookTitle,
                    onValueChange = { bookTitle = it },
                    label = { Text("Book title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = bookAuthor,
                    onValueChange = { bookAuthor = it },
                    label = { Text("Book author") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = bookYear,
                    onValueChange = { bookYear = it },
                    label = { Text("First publish year optional") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = {
                        val book = RoomBookEntity(
                            id = editingBookId ?: 0,
                            roomId = roomId,
                            title = bookTitle.trim(),
                            author = bookAuthor.trim(),
                            firstPublishYear = bookYear.toIntOrNull(),
                            coverUrl = null,
                            openLibraryKey = null,
                            description = null,
                            displayOrder = books.size
                        )

                        if (editingBookId == null) {
                            viewModel.addBook(roomId, book)
                        } else {
                            viewModel.updateBook(roomId, book)
                        }

                        editingBookId = null
                        bookTitle = ""
                        bookAuthor = ""
                        bookYear = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (editingBookId == null) "Add book" else "Update book")
                }
            }

            item {
                HorizontalDivider()
                Text(
                    text = "Members",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            items(members) { member ->
                MemberAdminCard(
                    member = member,
                    currentUserId = viewModel.currentUserId,
                    onToggleAdmin = {
                        viewModel.setAdmin(roomId, member.userId, !member.isAdmin)
                    },
                    onToggleMessaging = {
                        viewModel.setCanMessage(roomId, member.userId, !member.canMessage)
                    },
                    onRemove = {
                        viewModel.removeMember(roomId, member.userId)
                    },
                    onBan = {
                        viewModel.banMember(roomId, member.userId)
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = banEmail,
                    onValueChange = { banEmail = it },
                    label = { Text("Ban by email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = {
                        viewModel.banEmail(roomId, banEmail)
                        banEmail = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ban email")
                }
            }

            item {
                HorizontalDivider()
                Button(
                    onClick = {
                        viewModel.deleteRoom(roomId, onRoomDeleted)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete room")
                }
            }
        }
    }
}

@Composable
private fun BookAdminCard(
    book: RoomBookEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text("by ${book.author}")

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }

                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun MemberAdminCard(
    member: MemberWithUser,
    currentUserId: Long?,
    onToggleAdmin: () -> Unit,
    onToggleMessaging: () -> Unit,
    onRemove: () -> Unit,
    onBan: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = member.username,
                style = MaterialTheme.typography.titleMedium
            )

            Text(member.email)
            Text("User ID: ${member.userId}")

            Text(
                text = buildString {
                    append(if (member.isAdmin) "Admin" else "Member")
                    append(" • ")
                    append(if (member.canMessage) "Can message" else "Muted")
                    if (member.userId == currentUserId) append(" • You")
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onToggleAdmin) {
                    Text(if (member.isAdmin) "Revoke admin" else "Make admin")
                }

                TextButton(onClick = onToggleMessaging) {
                    Text(if (member.canMessage) "Mute" else "Unmute")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onRemove) {
                    Text("Remove")
                }

                TextButton(onClick = onBan) {
                    Text("Ban")
                }
            }
        }
    }
}