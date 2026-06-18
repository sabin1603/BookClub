package com.example.bookclub.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bookclub.R
import com.example.bookclub.data.local.entity.RoomBookEntity
import com.example.bookclub.data.local.model.MemberWithUser
import com.example.bookclub.ui.theme.BookBackground
import com.example.bookclub.ui.theme.BookError
import com.example.bookclub.ui.theme.BookOnPrimary
import com.example.bookclub.ui.theme.BookOnSurface
import com.example.bookclub.ui.theme.BookOnSurfaceVariant
import com.example.bookclub.ui.theme.BookOutline
import com.example.bookclub.ui.theme.BookOutlineVariant
import com.example.bookclub.ui.theme.BookPrimary
import com.example.bookclub.ui.theme.BookPrimaryContainer
import com.example.bookclub.ui.theme.BookSecondary
import com.example.bookclub.ui.theme.BookSecondaryContainer
import com.example.bookclub.ui.theme.BookSurface
import com.example.bookclub.ui.theme.BookSurfaceContainerHigh
import com.example.bookclub.ui.theme.BookSurfaceContainerLow
import com.example.bookclub.viewmodel.RoomViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.ColumnScope

private const val ROOM_TITLE_MAX_LENGTH = 60
private const val ROOM_DESCRIPTION_MAX_LENGTH = 300

@Composable
fun RoomAdminScreen(
    roomId: Long,
    onBack: () -> Unit,
    onRoomDeleted: () -> Unit,
    onLeaveRoomSuccess: () -> Unit,
    viewModel: RoomViewModel = viewModel()
) {
    val room by viewModel.observeRoom(roomId).collectAsState(initial = null)
    val books by viewModel.observeBooks(roomId).collectAsState(initial = emptyList())
    val members by viewModel.observeMembers(roomId).collectAsState(initial = emptyList())
    val actionState by viewModel.actionState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showSuccess(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

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
        containerColor = BookBackground,
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            AdminTopBar(onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            actionState.errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        color = BookError,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                RoomDetailsAdminCard(
                    title = title,
                    onTitleChange = {
                        if (it.length <= ROOM_TITLE_MAX_LENGTH) {
                            title = it
                        }
                    },
                    description = description,
                    onDescriptionChange = {
                        if (it.length <= ROOM_DESCRIPTION_MAX_LENGTH) {
                            description = it
                        }
                    },
                    isPrivate = isPrivate,
                    onPrivateChange = { isPrivate = it },
                    accessCode = accessCode,
                    onAccessCodeChange = { accessCode = it },
                    onSave = {
                        viewModel.updateRoomSettings(
                            roomId = roomId,
                            title = title,
                            description = description,
                            isPrivate = isPrivate,
                            accessCode = accessCode,
                            onSuccess = {
                                showSuccess("Room details saved.")
                            }
                        )
                    }
                )
            }

            item {
                ManageBooksCard(
                    books = books,
                    editingBookId = editingBookId,
                    bookTitle = bookTitle,
                    onBookTitleChange = { bookTitle = it },
                    bookAuthor = bookAuthor,
                    onBookAuthorChange = { bookAuthor = it },
                    bookYear = bookYear,
                    onBookYearChange = { bookYear = it },
                    onEditBook = { book ->
                        editingBookId = book.id
                        bookTitle = book.title
                        bookAuthor = book.author
                        bookYear = book.firstPublishYear?.toString() ?: ""
                    },
                    onDeleteBook = { book ->
                        viewModel.deleteBook(
                            roomId = roomId,
                            bookId = book.id,
                            onSuccess = {
                                showSuccess("Book deleted.")
                            }
                        )
                    },
                    onSaveBook = {
                        val cleanTitle = bookTitle.trim()
                        val cleanAuthor = bookAuthor.trim()

                        if (cleanTitle.isBlank() || cleanAuthor.isBlank()) {
                            showSuccess("Book title and author are required.")
                            return@ManageBooksCard
                        }

                        val existingBook = books.firstOrNull { it.id == editingBookId }

                        val book = RoomBookEntity(
                            id = editingBookId ?: 0,
                            roomId = roomId,
                            title = cleanTitle,
                            author = cleanAuthor,
                            firstPublishYear = bookYear.toIntOrNull(),
                            coverUrl = existingBook?.coverUrl,
                            openLibraryKey = existingBook?.openLibraryKey,
                            description = existingBook?.description,
                            displayOrder = existingBook?.displayOrder ?: books.size
                        )

                        if (editingBookId == null) {
                            viewModel.addBook(
                                roomId = roomId,
                                book = book,
                                onSuccess = {
                                    editingBookId = null
                                    bookTitle = ""
                                    bookAuthor = ""
                                    bookYear = ""
                                    showSuccess("Book added.")
                                }
                            )
                        } else {
                            viewModel.updateBook(
                                roomId = roomId,
                                book = book,
                                onSuccess = {
                                    editingBookId = null
                                    bookTitle = ""
                                    bookAuthor = ""
                                    bookYear = ""
                                    showSuccess("Book updated.")
                                }
                            )
                        }
                    },
                    onCancelEdit = {
                        editingBookId = null
                        bookTitle = ""
                        bookAuthor = ""
                        bookYear = ""
                    }
                )
            }

            item {
                ManageMembersCard(
                    members = members,
                    currentUserId = viewModel.currentUserId,
                    onToggleAdmin = { member ->
                        viewModel.setAdmin(
                            roomId = roomId,
                            targetUserId = member.userId,
                            isAdmin = !member.isAdmin,
                            onSuccess = {
                                showSuccess("Admin rights updated.")
                            }
                        )
                    },
                    onToggleMessaging = { member ->
                        viewModel.setCanMessage(
                            roomId = roomId,
                            targetUserId = member.userId,
                            canMessage = !member.canMessage,
                            onSuccess = {
                                showSuccess("Messaging rights updated.")
                            }
                        )
                    },
                    onRemove = { member ->
                        viewModel.removeMember(
                            roomId = roomId,
                            targetUserId = member.userId,
                            onSuccess = {
                                showSuccess("Member removed.")
                            }
                        )
                    },
                    onBan = { member ->
                        viewModel.banMember(
                            roomId = roomId,
                            targetUserId = member.userId,
                            onSuccess = {
                                showSuccess("Member banned.")
                            }
                        )
                    }
                )
            }

            item {
                BanMemberCard(
                    banEmail = banEmail,
                    onBanEmailChange = { banEmail = it },
                    onBan = {
                        viewModel.banEmail(
                            roomId = roomId,
                            email = banEmail,
                            onSuccess = {
                                banEmail = ""
                                showSuccess("Email banned.")
                            }
                        )
                    }
                )
            }

            item {
                HorizontalDivider(
                    color = BookOutlineVariant.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                DangerZoneCard(
                    onLeaveRoom = {
                        viewModel.leaveRoom(
                            roomId = roomId,
                            onSuccess = onLeaveRoomSuccess
                        )
                    },
                    onDeleteRoom = {
                        viewModel.deleteRoom(
                            roomId = roomId,
                            onSuccess = onRoomDeleted
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminTopBar(
    onBack: () -> Unit
) {
    Surface(
        color = BookBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(
                    text = "Back",
                    color = BookPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }

            Text(
                text = "Room Admin",
                color = BookPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RoomDetailsAdminCard(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    isPrivate: Boolean,
    onPrivateChange: (Boolean) -> Unit,
    accessCode: String,
    onAccessCodeChange: (String) -> Unit,
    onSave: () -> Unit
) {
    AdminSectionCard {
        Text(
            text = "Room Details",
            color = BookPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        LabeledAdminField(
            label = "Room Title",
            value = title,
            onValueChange = onTitleChange,
            counter = "${title.length}/$ROOM_TITLE_MAX_LENGTH",
            singleLine = true
        )

        LabeledAdminField(
            label = "Description",
            value = description,
            onValueChange = onDescriptionChange,
            counter = "${description.length}/$ROOM_DESCRIPTION_MAX_LENGTH",
            singleLine = false,
            minLines = 3
        )

        HorizontalDivider(color = BookOutlineVariant.copy(alpha = 0.65f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Private Room",
                    color = BookPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Only approved members can join",
                    color = BookOnSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }

            Switch(
                checked = isPrivate,
                onCheckedChange = onPrivateChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BookOnPrimary,
                    checkedTrackColor = BookPrimaryContainer,
                    uncheckedThumbColor = Color(0xFF8D7D78),
                    uncheckedTrackColor = BookSurfaceContainerHigh
                )
            )
        }

        if (isPrivate) {
            LabeledAdminField(
                label = "Access Code",
                value = accessCode,
                onValueChange = onAccessCodeChange,
                counter = null,
                singleLine = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onSave,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BookPrimaryContainer,
                    contentColor = BookOnPrimary
                ),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Save Details",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ManageBooksCard(
    books: List<RoomBookEntity>,
    editingBookId: Long?,
    bookTitle: String,
    onBookTitleChange: (String) -> Unit,
    bookAuthor: String,
    onBookAuthorChange: (String) -> Unit,
    bookYear: String,
    onBookYearChange: (String) -> Unit,
    onEditBook: (RoomBookEntity) -> Unit,
    onDeleteBook: (RoomBookEntity) -> Unit,
    onSaveBook: () -> Unit,
    onCancelEdit: () -> Unit
) {
    AdminSectionCard {
        Text(
            text = "Manage Books",
            color = BookPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        if (books.isEmpty()) {
            Text(
                text = "No books in this room yet.",
                color = BookOnSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        } else {
            books.forEach { book ->
                BookAdminRow(
                    book = book,
                    onEdit = { onEditBook(book) },
                    onDelete = { onDeleteBook(book) }
                )
            }
        }

        HorizontalDivider(
            color = BookOutlineVariant.copy(alpha = 0.65f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = if (editingBookId == null) "Add New Book" else "Edit Book",
            color = BookPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge
        )

        LabeledAdminField(
            label = "Title",
            value = bookTitle,
            onValueChange = onBookTitleChange,
            counter = null,
            singleLine = true
        )

        LabeledAdminField(
            label = "Author",
            value = bookAuthor,
            onValueChange = onBookAuthorChange,
            counter = null,
            singleLine = true
        )

        LabeledAdminField(
            label = "First publish year optional",
            value = bookYear,
            onValueChange = onBookYearChange,
            counter = null,
            singleLine = true,
            keyboardType = KeyboardType.Number
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
        ) {
            if (editingBookId != null) {
                TextButton(onClick = onCancelEdit) {
                    Text("Cancel", color = BookOnSurfaceVariant)
                }
            }

            Button(
                onClick = onSaveBook,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BookPrimaryContainer,
                    contentColor = BookOnPrimary
                )
            ) {
                Text(if (editingBookId == null) "Add Book" else "Save Book")
            }
        }
    }
}

@Composable
private fun BookAdminRow(
    book: RoomBookEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isManualBook = book.openLibraryKey.isNullOrBlank() && book.coverUrl.isNullOrBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BookOutlineVariant.copy(alpha = 0.75f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookCoverThumbnail(
            coverUrl = book.coverUrl,
            title = book.title
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = book.title,
                color = BookOnSurface,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = book.author,
                color = BookOnSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isManualBook) {
            IconButton(onClick = onEdit) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit_book),
                    contentDescription = "Edit book",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete_book),
                contentDescription = "Delete book",
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun BookCoverThumbnail(
    coverUrl: String?,
    title: String
) {
    Box(
        modifier = Modifier
            .size(width = 58.dp, height = 78.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(BookSurfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        if (!coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = "$title cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "No\nCover",
                color = BookOutline,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ManageMembersCard(
    members: List<MemberWithUser>,
    currentUserId: Long?,
    onToggleAdmin: (MemberWithUser) -> Unit,
    onToggleMessaging: (MemberWithUser) -> Unit,
    onRemove: (MemberWithUser) -> Unit,
    onBan: (MemberWithUser) -> Unit
) {
    AdminSectionCard {
        Text(
            text = "Manage Members",
            color = BookPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        if (members.isEmpty()) {
            Text(
                text = "No members found.",
                color = BookOnSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        } else {
            members.forEach { member ->
                MemberAdminRow(
                    member = member,
                    currentUserId = currentUserId,
                    onToggleAdmin = { onToggleAdmin(member) },
                    onToggleMessaging = { onToggleMessaging(member) },
                    onRemove = { onRemove(member) },
                    onBan = { onBan(member) }
                )
            }
        }
    }
}

@Composable
private fun MemberAdminRow(
    member: MemberWithUser,
    currentUserId: Long?,
    onToggleAdmin: () -> Unit,
    onToggleMessaging: () -> Unit,
    onRemove: () -> Unit,
    onBan: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BookOutlineVariant.copy(alpha = 0.75f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemberInitialAvatar(member.username)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = member.username,
                    color = BookOnSurface,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (member.userId == currentUserId) {
                    Text(
                        text = "  • You",
                        color = BookOnSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text(
                text = member.email,
                color = BookOnSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = buildString {
                    append(if (member.isAdmin) "Admin" else "Member")
                    append(" • ")
                    append(if (member.canMessage) "Can message" else "Muted")
                },
                color = BookOnSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_member_more),
                    contentDescription = "Member options",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(width = 18.dp, height = 28.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = BookSurface
            ) {
                DropdownMenuItem(
                    text = {
                        Text(if (member.isAdmin) "Revoke admin" else "Make admin")
                    },
                    onClick = {
                        menuExpanded = false
                        onToggleAdmin()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(if (member.canMessage) "Mute" else "Unmute")
                    },
                    onClick = {
                        menuExpanded = false
                        onToggleMessaging()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Remove") },
                    onClick = {
                        menuExpanded = false
                        onRemove()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Ban") },
                    onClick = {
                        menuExpanded = false
                        onBan()
                    }
                )
            }
        }
    }
}

@Composable
private fun BanMemberCard(
    banEmail: String,
    onBanEmailChange: (String) -> Unit,
    onBan: () -> Unit
) {
    AdminSectionCard {
        Text(
            text = "Ban Member",
            color = BookPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Prevent a specific email address from joining this room.",
            color = BookOnSurfaceVariant,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = banEmail,
                onValueChange = onBanEmailChange,
                placeholder = {
                    Text(
                        text = "Enter email address",
                        color = BookOutline
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = BookOnSurface,
                    unfocusedTextColor = BookOnSurface,
                    focusedContainerColor = BookSurfaceContainerLow,
                    unfocusedContainerColor = BookSurfaceContainerLow,
                    focusedIndicatorColor = BookPrimary,
                    unfocusedIndicatorColor = BookOutline,
                    cursorColor = BookPrimary
                ),
                modifier = Modifier.weight(1f)
            )

            OutlinedButton(
                onClick = onBan,
                shape = CircleShape,
                border = BorderStroke(1.2.dp, BookError),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BookError
                )
            ) {
                Text("Ban")
            }
        }
    }
}

@Composable
private fun DangerZoneCard(
    onLeaveRoom: () -> Unit,
    onDeleteRoom: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onLeaveRoom,
            shape = CircleShape,
            border = BorderStroke(1.3.dp, BookPrimary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = BookPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Leave Room",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )
        }

        Button(
            onClick = onDeleteRoom,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC9181F),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete_room),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = "Delete Room",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }
        }

        Text(
            text = "This action cannot be undone.",
            color = BookOnSurfaceVariant,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun AdminSectionCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = BookSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = BookOutlineVariant.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
private fun LabeledAdminField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    counter: String?,
    singleLine: Boolean,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = BookOnSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )

            if (counter != null) {
                Text(
                    text = counter,
                    color = BookOnSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                )
            }
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedTextColor = BookOnSurface,
                unfocusedTextColor = BookOnSurface,
                focusedContainerColor = BookSurfaceContainerLow,
                unfocusedContainerColor = BookSurfaceContainerLow,
                focusedIndicatorColor = BookPrimary,
                unfocusedIndicatorColor = BookOutline,
                cursorColor = BookPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MemberInitialAvatar(
    username: String
) {
    val initial = username.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = BookSecondaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = BookSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}