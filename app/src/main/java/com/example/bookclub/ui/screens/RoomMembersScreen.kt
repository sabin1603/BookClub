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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bookclub.data.local.model.MemberWithUser
import com.example.bookclub.ui.theme.BookBackground
import com.example.bookclub.ui.theme.BookError
import com.example.bookclub.ui.theme.BookOnSurface
import com.example.bookclub.ui.theme.BookOnSurfaceVariant
import com.example.bookclub.ui.theme.BookOutlineVariant
import com.example.bookclub.ui.theme.BookPrimary
import com.example.bookclub.ui.theme.BookSecondary
import com.example.bookclub.ui.theme.BookSecondaryContainer
import com.example.bookclub.ui.theme.BookSurface
import com.example.bookclub.ui.theme.BookSurfaceContainerHigh
import com.example.bookclub.ui.theme.BookSurfaceContainerLow
import com.example.bookclub.viewmodel.RoomViewModel

@Composable
fun RoomMembersScreen(
    roomId: Long,
    onBack: () -> Unit,
    onLeaveRoomSuccess: () -> Unit,
    viewModel: RoomViewModel = viewModel()
) {
    val room by viewModel.observeRoom(roomId).collectAsState(initial = null)
    val members by viewModel.observeMembers(roomId).collectAsState(initial = emptyList())
    val actionState by viewModel.actionState.collectAsState()

    Scaffold(
        containerColor = BookBackground,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            MembersTopBar(onBack = onBack)
        },
        bottomBar = {
            LeaveRoomBottomBar(
                onLeaveRoom = {
                    viewModel.leaveRoom(
                        roomId = roomId,
                        onSuccess = onLeaveRoomSuccess
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Room Members",
                        color = BookPrimary,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
                    )

                    Text(
                        text = room?.title ?: "Book Club room",
                        color = BookOnSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Read-only member list",
                        color = BookOnSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }

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
                HorizontalDivider(
                    color = BookOutlineVariant.copy(alpha = 0.65f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (members.isEmpty()) {
                item {
                    EmptyMembersCard()
                }
            } else {
                items(
                    items = members,
                    key = { it.userId }
                ) { member ->
                    ReadOnlyMemberCard(
                        member = member,
                        currentUserId = viewModel.currentUserId
                    )
                }
            }
        }
    }
}

@Composable
private fun MembersTopBar(
    onBack: () -> Unit
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
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                color = BookOutlineVariant.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun ReadOnlyMemberCard(
    member: MemberWithUser,
    currentUserId: Long?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = BookSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = BookOutlineVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemberAvatar(username = member.username)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = buildString {
                        append(member.username)
                        if (member.userId == currentUserId) append("  •  You")
                    },
                    color = BookOnSurface,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = member.email,
                    color = BookOnSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "User ID: ${member.userId}",
                    color = BookOnSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RoleChip(isAdmin = member.isAdmin)
                MessagingChip(canMessage = member.canMessage)
            }
        }
    }
}

@Composable
private fun MemberAvatar(
    username: String
) {
    val initial = username.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = Modifier
            .background(
                color = BookSecondaryContainer,
                shape = CircleShape
            )
            .padding(horizontal = 14.dp, vertical = 11.dp),
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

@Composable
private fun RoleChip(
    isAdmin: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isAdmin) {
                    BookSecondaryContainer.copy(alpha = 0.45f)
                } else {
                    BookSurfaceContainerHigh
                },
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isAdmin) "Admin" else "Member",
            color = if (isAdmin) BookSecondary else BookOnSurfaceVariant,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun MessagingChip(
    canMessage: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = if (canMessage) {
                    BookSurfaceContainerLow
                } else {
                    androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
                },
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (canMessage) "Can message" else "Muted",
            color = if (canMessage) {
                BookOnSurfaceVariant
            } else {
                androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
            },
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun EmptyMembersCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BookOutlineVariant,
                shape = RoundedCornerShape(18.dp)
            )
            .background(
                color = BookSurface,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No members found.",
            color = BookOnSurfaceVariant,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun LeaveRoomBottomBar(
    onLeaveRoom: () -> Unit
) {
    Surface(
        color = BookSurface,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            OutlinedButton(
                onClick = onLeaveRoom,
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.3.dp, BookPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BookPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "Leave Room",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}