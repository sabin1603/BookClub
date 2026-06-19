package com.example.bookclub.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bookclub.R
import com.example.bookclub.data.local.entity.BookClubRoomEntity
import com.example.bookclub.data.local.entity.RoomBookEntity
import com.example.bookclub.data.local.model.MessageWithUser
import com.example.bookclub.ui.theme.BookBackground
import com.example.bookclub.ui.theme.BookError
import com.example.bookclub.ui.theme.BookErrorContainer
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import com.example.bookclub.ui.components.UserAvatar

@Composable
fun RoomDetailsScreen(
    roomId: Long,
    onBack: () -> Unit,
    onAdminClick: () -> Unit,
    viewModel: RoomViewModel = viewModel()
) {
    val room by viewModel.observeRoom(roomId).collectAsState(initial = null)
    val books by viewModel.observeBooks(roomId).collectAsState(initial = emptyList())
    val messages by viewModel.observeMessages(roomId).collectAsState(initial = emptyList())
    val members by viewModel.observeMembers(roomId).collectAsState(initial = emptyList())
    val actionState by viewModel.actionState.collectAsState()



    val currentUserId = viewModel.currentUserId
    val currentMember = members.firstOrNull { it.userId == currentUserId }
    val isMuted = currentMember?.canMessage == false

    var messageText by remember { mutableStateOf("") }
    var showMutedBanner by remember { mutableStateOf(true) }

    val messageListState = rememberLazyListState()
    var didScrollToLatestMessages by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && !didScrollToLatestMessages) {
            messageListState.scrollToItem(messages.size)
            didScrollToLatestMessages = true
        }
    }

    Scaffold(
        containerColor = BookBackground,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            RoomDetailsTopBar(
                onBack = onBack,
                onSettingsClick = onAdminClick
            )
        },
        bottomBar = {
            MessageInputBar(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                isMuted = isMuted,
                onSend = {
                    val cleanText = messageText.trim()
                    if (cleanText.isNotBlank() && !isMuted) {
                        viewModel.sendMessage(roomId, cleanText)
                        messageText = ""
                    }
                }
            )
        }
    ) { padding ->
        if (room == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Room not found.",
                    color = BookOnSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )
            }
            return@Scaffold
        }

        val currentRoom = room!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            RoomInfoSection(
                room = currentRoom,
                books = books
            )

            if (isMuted && showMutedBanner) {
                MutedBanner(
                    onDismiss = { showMutedBanner = false }
                )
            }

            actionState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = BookError,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            LazyColumn(
                state = messageListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DateDivider()
                }

                if (messages.isEmpty()) {
                    item {
                        EmptyMessagesState()
                    }
                } else {
                    items(
                        items = messages,
                        key = { it.id }
                    ) { message ->
                        MessageBubble(
                            message = message,
                            isMine = message.userId == currentUserId
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomDetailsTopBar(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        color = BookSurface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(70.dp)
                    .padding(horizontal = 18.dp),
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
                    text = "Book Club",
                    color = BookPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onSettingsClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Room settings",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            HorizontalDivider(
                color = BookOutlineVariant.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun RoomInfoSection(
    room: BookClubRoomEntity,
    books: List<RoomBookEntity>
) {
    Surface(
        color = BookSurface,
        tonalElevation = 0.dp
    ) {
        Column {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BookSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = BookOutlineVariant,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = room.title,
                                color = BookPrimary,
                                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "Room ID: #${room.id}",
                                color = BookOnSurfaceVariant,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        ReadingStatusChip()
                    }

                    if (room.description.isNotBlank()) {
                        Text(
                            text = room.description,
                            color = BookOnSurface,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (books.isNotEmpty()) {
                        BookPreviewCard(book = books.first())

                        if (books.size > 1) {
                            Text(
                                text = "+ ${books.size - 1} more ${if (books.size - 1 == 1) "book" else "books"} in this room",
                                color = BookOnSurfaceVariant,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = BookOutlineVariant.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun ReadingStatusChip() {
    Box(
        modifier = Modifier
            .background(
                color = BookSecondaryContainer.copy(alpha = 0.35f),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = BookSecondaryContainer,
                shape = CircleShape
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Active\nReading",
            color = BookSecondary,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BookPreviewCard(
    book: RoomBookEntity
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BookSurfaceContainerLow,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 46.dp, height = 58.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(BookSurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            if (!book.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = "${book.title} cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Book",
                    color = BookOutline,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = book.title,
                color = BookPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = book.author,
                color = BookOnSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MutedBanner(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(
                color = BookErrorContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_muted),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "You are currently muted by an admin.",
                color = BookError,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = onDismiss) {
                Text(
                    text = "DISMISS",
                    color = BookError,
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun DateDivider() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = BookSurfaceContainerLow,
                    shape = CircleShape
                )
                .padding(horizontal = 18.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Today",
                color = BookOnSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EmptyMessagesState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No messages yet. Start the discussion!",
            color = BookOnSurfaceVariant,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MessageBubble(
    message: MessageWithUser,
    isMine: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) {
            Arrangement.End
        } else {
            Arrangement.Start
        },
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMine) {
            UserAvatar(
                username = message.username,
                profileImageUri = message.profileImageUri,
                size = 36.dp
            )

            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isMine) {
                Alignment.End
            } else {
                Alignment.Start
            }
        ) {
            Text(
                text = if (isMine) {
                    "Me • ${formatMessageTime(message.createdAt)}"
                } else {
                    "${message.username} • ${formatMessageTime(message.createdAt)}"
                },
                color = BookOnSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    start = if (isMine) 0.dp else 8.dp,
                    end = if (isMine) 8.dp else 0.dp,
                    bottom = 4.dp
                )
            )

            Box(
                modifier = Modifier
                    .background(
                        color = if (isMine) {
                            BookPrimaryContainer
                        } else {
                            BookSurfaceContainerLow
                        },
                        shape = if (isMine) {
                            RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 4.dp
                            )
                        } else {
                            RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomEnd = 18.dp,
                                bottomStart = 4.dp
                            )
                        }
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (isMine) {
                        BookOnPrimary
                    } else {
                        BookOnSurface
                    },
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (isMine) {
            Spacer(modifier = Modifier.width(10.dp))

            UserAvatar(
                username = message.username,
                profileImageUri = message.profileImageUri,
                size = 36.dp
            )
        }
    }
}

@Composable
private fun MessageInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    isMuted: Boolean,
    onSend: () -> Unit
) {
    Surface(
        color = BookSurface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                enabled = !isMuted,
                placeholder = {
                    Text(
                        text = if (isMuted) "Message disabled (Muted)" else "Write a message",
                        color = BookOnSurfaceVariant.copy(alpha = 0.55f)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = BookOnSurface,
                    unfocusedTextColor = BookOnSurface,
                    disabledTextColor = BookOnSurfaceVariant,
                    focusedContainerColor = BookSurfaceContainerLow,
                    unfocusedContainerColor = BookSurfaceContainerLow,
                    disabledContainerColor = BookSurfaceContainerLow,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = BookPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = BookOutlineVariant.copy(alpha = 0.65f),
                        shape = CircleShape
                    )
            )

            IconButton(
                enabled = !isMuted && messageText.trim().isNotBlank(),
                onClick = onSend,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send_message),
                    contentDescription = "Send message",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    return try {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    } catch (_: Exception) {
        ""
    }
}