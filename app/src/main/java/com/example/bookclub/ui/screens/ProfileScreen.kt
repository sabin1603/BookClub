package com.example.bookclub.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bookclub.R
import com.example.bookclub.ui.theme.BookBackground
import com.example.bookclub.ui.theme.BookError
import com.example.bookclub.ui.theme.BookOnPrimary
import com.example.bookclub.ui.theme.BookOnSurface
import com.example.bookclub.ui.theme.BookOnSurfaceVariant
import com.example.bookclub.ui.theme.BookOutlineVariant
import com.example.bookclub.ui.theme.BookPrimary
import com.example.bookclub.ui.theme.BookPrimaryContainer
import com.example.bookclub.ui.theme.BookSecondary
import com.example.bookclub.ui.theme.BookSecondaryContainer
import com.example.bookclub.ui.theme.BookSurface
import com.example.bookclub.ui.theme.BookSurfaceContainerHigh
import com.example.bookclub.ui.theme.BookSurfaceContainerLow
import com.example.bookclub.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    onClubsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(state.user?.id) {
        state.user?.let {
            username = it.username
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateProfilePicture(it)
        }
    }

    Scaffold(
        containerColor = BookBackground,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            ProfileTopBar()
        },
        bottomBar = {
            ProfileBottomBar(
                onClubsClick = onClubsClick,
                onSearchClick = onSearchClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp),
            contentPadding = PaddingValues(top = 28.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                ProfileHeader(
                    username = state.user?.username ?: "Reader",
                    createdAt = state.user?.createdAt,
                    profileImageUri = state.profileImageUri,
                    onChangePhoto = {
                        imagePickerLauncher.launch("image/*")
                    }
                )
            }

            state.successMessage?.let { message ->
                item {
                    FeedbackText(
                        text = message,
                        isError = false
                    )
                }
            }

            state.errorMessage?.let { error ->
                item {
                    FeedbackText(
                        text = error,
                        isError = true
                    )
                }
            }

            item {
                AccountSettingsCard(
                    username = username,
                    onUsernameChange = { username = it },
                    onSaveUsername = {
                        viewModel.updateUsername(username)
                    },
                    currentPassword = currentPassword,
                    onCurrentPasswordChange = { currentPassword = it },
                    newPassword = newPassword,
                    onNewPasswordChange = { newPassword = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    onChangePassword = {
                        viewModel.changePassword(
                            currentPassword = currentPassword,
                            newPassword = newPassword,
                            confirmPassword = confirmPassword
                        )

                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                    }
                )
            }

            item {
                LogoutButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileTopBar() {
    Surface(
        color = BookBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(70.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Profile",
                color = BookPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(2f)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProfileHeader(
    username: String,
    createdAt: Long?,
    profileImageUri: String?,
    onChangePhoto: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileAvatar(
            username = username,
            profileImageUri = profileImageUri
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = username,
            color = BookPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                text = "Member since ${formatMemberSince(createdAt)}",
                color = BookOnSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onChangePhoto,
            shape = CircleShape,
            border = BorderStroke(1.2.dp, BookPrimary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = BookPrimary
            )
        ) {
            Text(
                text = "Change profile picture",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    username: String,
    profileImageUri: String?
) {
    Box(
        modifier = Modifier
            .size(142.dp)
            .background(
                color = BookSurfaceContainerLow,
                shape = CircleShape
            )
            .border(
                width = 4.dp,
                color = BookSurfaceContainerLow,
                shape = CircleShape
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!profileImageUri.isNullOrBlank()) {
            AsyncImage(
                model = profileImageUri,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = username.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "R",
                color = BookSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FeedbackText(
    text: String,
    isError: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isError) {
                    androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
                } else {
                    BookSurfaceContainerLow
                },
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                color = if (isError) {
                    BookError.copy(alpha = 0.45f)
                } else {
                    BookOutlineVariant.copy(alpha = 0.65f)
                },
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = text,
            color = if (isError) BookError else BookOnSurfaceVariant,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AccountSettingsCard(
    username: String,
    onUsernameChange: (String) -> Unit,
    onSaveUsername: () -> Unit,
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onChangePassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
                    color = BookOutlineVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Account",
                color = BookPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Edit username",
                color = BookOnSurface,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onSaveUsername,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BookPrimaryContainer,
                    contentColor = BookOnPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save username")
            }

            HorizontalDivider(
                color = BookOutlineVariant.copy(alpha = 0.65f),
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Text(
                text = "Change password",
                color = BookOnSurface,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                label = { Text("Current password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = { Text("New password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirm new password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onChangePassword,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BookPrimaryContainer,
                    contentColor = BookOnPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change password")
            }
        }
    }
}

@Composable
private fun LogoutButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, BookError.copy(alpha = 0.45f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = BookError
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_logout),
                contentDescription = null,
                tint = BookError,
                modifier = Modifier.size(28.dp)
            )

            Text(
                text = "LOGOUT",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileBottomBar(
    onClubsClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    NavigationBar(
        containerColor = BookSurfaceContainerHigh,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onClubsClick,
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nav_clubs),
                        contentDescription = null,
                        tint = BookOnSurfaceVariant,
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nav_search),
                        contentDescription = null,
                        tint = BookOnSurfaceVariant,
                        modifier = Modifier.size(28.dp)
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
            selected = true,
            onClick = {},
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nav_profile),
                        contentDescription = null,
                        tint = BookSecondary,
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

private fun formatMemberSince(createdAt: Long?): String {
    if (createdAt == null) {
        return "today"
    }

    return try {
        SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(createdAt))
    } catch (_: Exception) {
        "today"
    }
}