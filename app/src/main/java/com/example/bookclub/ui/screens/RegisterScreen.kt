package com.example.bookclub.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bookclub.R
import com.example.bookclub.ui.theme.BookBackground
import com.example.bookclub.ui.theme.BookOnPrimary
import com.example.bookclub.ui.theme.BookOnSurface
import com.example.bookclub.ui.theme.BookOnSurfaceVariant
import com.example.bookclub.ui.theme.BookOutlineVariant
import com.example.bookclub.ui.theme.BookPrimary
import com.example.bookclub.ui.theme.BookPrimaryContainer
import com.example.bookclub.ui.theme.BookSurface
import com.example.bookclub.ui.theme.BookSurfaceContainerHighest
import com.example.bookclub.ui.theme.BookSurfaceContainerLow
import com.example.bookclub.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = BookBackground,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BookBackground)
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp)
                    .padding(top = 90.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SmallBookLogo()

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Book Club",
                        style = MaterialTheme.typography.displayLarge,
                        color = BookPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Create an account to start reading.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BookOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BookSurface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = BookOutlineVariant.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 28.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            RegisterInputGroup(
                                label = "Username",
                                value = username,
                                onValueChange = {
                                    username = it
                                    localError = null
                                },
                                placeholder = "e.g. Reader123",
                                keyboardType = KeyboardType.Text
                            )

                            RegisterInputGroup(
                                label = "Email",
                                value = email,
                                onValueChange = {
                                    email = it
                                    localError = null
                                },
                                placeholder = "name@example.com",
                                keyboardType = KeyboardType.Email
                            )

                            RegisterInputGroup(
                                label = "Password",
                                value = password,
                                onValueChange = {
                                    password = it
                                    localError = null
                                },
                                placeholder = "••••••••",
                                keyboardType = KeyboardType.Password,
                                visualTransformation = if (passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    PasswordVisibilityButton(
                                        isVisible = passwordVisible,
                                        onClick = { passwordVisible = !passwordVisible }
                                    )
                                }
                            )

                            RegisterInputGroup(
                                label = "Confirm Password",
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    localError = null
                                },
                                placeholder = "••••••••",
                                keyboardType = KeyboardType.Password,
                                visualTransformation = if (confirmPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    PasswordVisibilityButton(
                                        isVisible = confirmPasswordVisible,
                                        onClick = {
                                            confirmPasswordVisible = !confirmPasswordVisible
                                        }
                                    )
                                }
                            )

                            val displayedError = localError ?: state.errorMessage

                            displayedError?.let { error ->
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    localError = null

                                    when {
                                        username.isBlank() -> {
                                            localError = "Username is required."
                                        }

                                        email.isBlank() -> {
                                            localError = "Email is required."
                                        }

                                        password.isBlank() -> {
                                            localError = "Password is required."
                                        }

                                        password != confirmPassword -> {
                                            localError = "Passwords do not match."
                                        }

                                        else -> {
                                            viewModel.register(
                                                username = username,
                                                email = email,
                                                password = password,
                                                onSuccess = onRegisterSuccess
                                            )
                                        }
                                    }
                                },
                                enabled = !state.isLoading,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BookPrimaryContainer,
                                    contentColor = BookOnPrimary,
                                    disabledContainerColor = BookPrimaryContainer.copy(alpha = 0.55f),
                                    disabledContentColor = BookOnPrimary.copy(alpha = 0.75f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .shadow(
                                        elevation = 2.dp,
                                        shape = CircleShape
                                    )
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp,
                                        color = BookOnPrimary
                                    )
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Register",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(44.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = BookOnSurfaceVariant
                        )

                        TextButton(
                            onClick = onLoginClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Login",
                                style = MaterialTheme.typography.labelLarge,
                                color = BookPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterInputGroup(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = BookOnSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BookOutlineVariant
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = BookOnSurface,
                unfocusedTextColor = BookOnSurface,
                focusedContainerColor = BookSurfaceContainerLow,
                unfocusedContainerColor = BookSurfaceContainerLow,
                disabledContainerColor = BookSurfaceContainerLow,
                focusedIndicatorColor = BookPrimary,
                unfocusedIndicatorColor = BookSurfaceContainerLow,
                focusedLabelColor = BookPrimary,
                unfocusedLabelColor = BookOnSurfaceVariant,
                cursorColor = BookPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )
    }
}

@Composable
private fun PasswordVisibilityButton(
    isVisible: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(
                id = if (isVisible) {
                    R.drawable.ic_visibility_off
                } else {
                    R.drawable.ic_visibility
                }
            ),
            contentDescription = if (isVisible) {
                "Hide password"
            } else {
                "Show password"
            },
            tint = BookOnSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SmallBookLogo() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(
                color = BookSurfaceContainerLow,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_book_outline),
            contentDescription = "Book Club logo",
            colorFilter = ColorFilter.tint(BookPrimary),
            modifier = Modifier.size(42.dp)
        )
    }
}