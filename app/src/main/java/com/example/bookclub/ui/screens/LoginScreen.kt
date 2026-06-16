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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.sp
import com.example.bookclub.R
import com.example.bookclub.ui.theme.BookBackground
import com.example.bookclub.ui.theme.BookOnPrimary
import com.example.bookclub.ui.theme.BookOnSurface
import com.example.bookclub.ui.theme.BookOnSurfaceVariant
import com.example.bookclub.ui.theme.BookOutlineVariant
import com.example.bookclub.ui.theme.BookPrimary
import com.example.bookclub.ui.theme.BookPrimaryContainer
import com.example.bookclub.ui.theme.BookSurfaceContainerHighest
import com.example.bookclub.ui.theme.BookSurfaceContainerLow
import com.example.bookclub.viewmodel.AuthViewModel
import androidx.compose.material3.Icon

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BookLogo()

                    Spacer(modifier = Modifier.height(38.dp))

                    Text(
                        text = "Book Club",
                        style = MaterialTheme.typography.displayLarge,
                        color = BookPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Welcome back to your reading journey.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BookOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(52.dp))

                    LoginTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email address",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LoginTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (passwordVisible) {
                                            R.drawable.ic_visibility_off
                                        } else {
                                            R.drawable.ic_visibility
                                        }
                                    ),
                                    contentDescription = if (passwordVisible) {
                                        "Hide password"
                                    } else {
                                        "Show password"
                                    },
                                    tint = BookOnSurfaceVariant,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                // Visual only for now.
                            },
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Forgot password?",
                                style = MaterialTheme.typography.labelLarge,
                                color = BookPrimary
                            )
                        }
                    }

                    state.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            viewModel.login(
                                email = email,
                                password = password,
                                onSuccess = onLoginSuccess
                            )
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
                                    text = "Login",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(78.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BookOnSurfaceVariant
                        )

                        TextButton(
                            onClick = onRegisterClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Register",
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
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = BookOnSurface,
            unfocusedTextColor = BookOnSurface,
            focusedContainerColor = BookSurfaceContainerHighest,
            unfocusedContainerColor = BookSurfaceContainerHighest,
            disabledContainerColor = BookSurfaceContainerHighest,
            focusedIndicatorColor = BookPrimary,
            unfocusedIndicatorColor = BookOutlineVariant,
            focusedLabelColor = BookPrimary,
            unfocusedLabelColor = BookOnSurfaceVariant,
            cursorColor = BookPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    )
}

@Composable
private fun BookLogo() {
    Box(
        modifier = Modifier
            .size(128.dp)
            .shadow(
                elevation = 3.dp,
                shape = CircleShape
            )
            .background(
                color = BookSurfaceContainerLow,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = BookOutlineVariant.copy(alpha = 0.35f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_book_outline),
            contentDescription = "Book Club logo",
            colorFilter = ColorFilter.tint(BookPrimary),
            modifier = Modifier.size(72.dp)
        )
    }
}