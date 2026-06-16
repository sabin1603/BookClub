package com.example.bookclub.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bookclub.R
import com.example.bookclub.data.local.entity.BookClubRoomEntity
import com.example.bookclub.ui.theme.BookBackground
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width

@Composable
fun HomeScreen(
    onCreateRoomClick: () -> Unit,
    onBookSearchClick: () -> Unit,
    onRoomClick: (Long) -> Unit,
    onLogout: () -> Unit,
    viewModel: RoomViewModel = viewModel()
) {
    val rooms by viewModel.observeVisibleRooms().collectAsState(initial = emptyList())
    val actionState by viewModel.actionState.collectAsState()

    var showJoinDialog by remember { mutableStateOf(false) }
    var roomIdText by remember { mutableStateOf("") }
    var accessCode by remember { mutableStateOf("") }

    if (showJoinDialog) {
        AlertDialog(
            containerColor = BookSurface,
            onDismissRequest = { showJoinDialog = false },
            title = {
                Text(
                    text = "Join room",
                    color = BookPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = roomIdText,
                        onValueChange = { roomIdText = it },
                        label = { Text("Room ID") },
                        placeholder = { Text("Example: 3 or #3") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accessCode,
                        onValueChange = { accessCode = it },
                        label = { Text("Access code, only for private rooms") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.joinRoomById(
                            roomIdText = roomIdText,
                            accessCode = accessCode.ifBlank { null }
                        )
                        roomIdText = ""
                        accessCode = ""
                        showJoinDialog = false
                    }
                ) {
                    Text("Join", color = BookPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancel", color = BookOnSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        containerColor = BookBackground,
        topBar = {
            ClubsTopBar(
                onLogout = {
                    viewModel.logout()
                    onLogout()
                }
            )
        },
        bottomBar = {
            ClubsBottomBar(
                onSearchClick = onBookSearchClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp),
            contentPadding = PaddingValues(top = 34.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Hello, ${viewModel.loggedUsername}",
                        color = BookPrimary,
                        style = androidx.compose.material3.MaterialTheme.typography.displayLarge
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Welcome back to your reading community.",
                        color = BookOnSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onCreateRoomClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BookPrimaryContainer,
                            contentColor = BookOnPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape
                            )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_create_room),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )

                            Text(
                                text = "Create Room",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showJoinDialog = true },
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, BookPrimaryContainer),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BookPrimaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_join_room),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(width = 30.dp, height = 22.dp)
                            )

                            Text(
                                text = "Join by ID",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    TextButton(
                        onClick = onBookSearchClick,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_nav_search),
                                contentDescription = null,
                                tint = BookSecondary,
                                modifier = Modifier.size(18.dp)
                            )

                            Text(
                                text = "Search books online",
                                color = BookSecondary,
                                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            item {
                HorizontalDivider(
                    color = BookOutlineVariant.copy(alpha = 0.55f)
                )
            }

            item {
                Text(
                    text = "My Rooms",
                    color = BookPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
                )
            }

            actionState.errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (rooms.isEmpty()) {
                item {
                    EmptyRoomsCard()
                }
            } else {
                items(
                    items = rooms,
                    key = { it.id }
                ) { room ->
                    ClubRoomCard(
                        room = room,
                        viewModel = viewModel,
                        onClick = { onRoomClick(room.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClubsTopBar(
    onLogout: () -> Unit
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
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Book Club",
                    color = BookPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onLogout) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logout),
                        contentDescription = "Logout",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(34.dp)
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
private fun ClubRoomCard(
    room: BookClubRoomEntity,
    viewModel: RoomViewModel,
    onClick: () -> Unit
) {
    val books by viewModel.observeBooks(room.id).collectAsState(initial = emptyList())
    val members by viewModel.observeMembers(room.id).collectAsState(initial = emptyList())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = BookSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .border(
                    width = 1.dp,
                    color = Color(0xFFE6E3D8),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            if (!room.isPrivate) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF81C784))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = room.title,
                            color = BookOnSurface,
                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "ID #${room.id}",
                            color = BookOutline,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                    }

                    RoomVisibilityChip(isPrivate = room.isPrivate)
                }

                if (room.description.isNotBlank()) {
                    Text(
                        text = room.description,
                        color = BookOnSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider(
                    color = BookOutlineVariant.copy(alpha = 0.45f),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_room_members),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(width = 18.dp, height = 14.dp)
                        )

                        Text(
                            text = "${members.size} ${if (members.size == 1) "Member" else "Members"}",
                            color = BookOnSurfaceVariant,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_room_books),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(width = 18.dp, height = 16.dp)
                        )

                        Text(
                            text = "${books.size} ${if (books.size == 1) "Book" else "Books"}",
                            color = BookOnSurfaceVariant,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomVisibilityChip(
    isPrivate: Boolean
) {
    val backgroundColor = if (isPrivate) {
        BookSurfaceContainerHigh
    } else {
        BookSecondaryContainer.copy(alpha = 0.42f)
    }

    val textColor = if (isPrivate) {
        BookOnSurfaceVariant
    } else {
        BookSecondary
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isPrivate) "Private" else "Public",
            color = textColor,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun EmptyRoomsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = BookOutlineVariant,
                shape = RoundedCornerShape(18.dp)
            )
            .background(
                color = BookSurface.copy(alpha = 0.55f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No rooms joined yet. Start a new chapter by creating or joining a room!",
            color = BookOnSurfaceVariant,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ClubsBottomBar(
    onSearchClick: () -> Unit
) {
    NavigationBar(
        containerColor = BookSurfaceContainerHigh,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nav_clubs),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(width = 28.dp, height = 18.dp)
                    )

                    Text(
                        text = "Clubs",
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                    )
                }
            },
            label = null,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BookSecondary,
                selectedTextColor = BookSecondary,
                indicatorColor = BookSecondaryContainer,
                unselectedIconColor = BookOnSurfaceVariant,
                unselectedTextColor = BookOnSurfaceVariant
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = onSearchClick,
            icon = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nav_search),
                        contentDescription = null,
                        tint = BookOnSurfaceVariant,
                        modifier = Modifier.size(25.dp)
                    )

                    Text(
                        text = "Search",
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                    )
                }
            },
            label = null,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BookSecondary,
                selectedTextColor = BookSecondary,
                indicatorColor = BookSecondaryContainer,
                unselectedIconColor = BookOnSurfaceVariant,
                unselectedTextColor = BookOnSurfaceVariant
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nav_profile),
                        contentDescription = null,
                        tint = BookOnSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = "Profile",
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                    )
                }
            },
            label = null,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BookSecondary,
                selectedTextColor = BookSecondary,
                indicatorColor = BookSecondaryContainer,
                unselectedIconColor = BookOnSurfaceVariant,
                unselectedTextColor = BookOnSurfaceVariant
            )
        )
    }
}