package com.example.bookclub.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import com.example.bookclub.viewmodel.BookViewModel
import com.example.bookclub.viewmodel.RoomViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    bookId: Long?,
    onBack: () -> Unit,
    onSearchBook: () -> Unit,
    onRoomCreated: () -> Unit,
    roomViewModel: RoomViewModel = viewModel(),
    bookViewModel: BookViewModel = viewModel()
) {
    val actionState by roomViewModel.actionState.collectAsState()

    val selectedBook by if (bookId != null) {
        bookViewModel.observeBook(bookId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    var title by remember { mutableStateOf("") }
    var bookTitle by remember { mutableStateOf("") }
    var bookAuthor by remember { mutableStateOf("") }
    var bookCoverUrl by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var accessCode by remember { mutableStateOf("") }

    LaunchedEffect(selectedBook) {
        selectedBook?.let { book ->
            bookTitle = book.title
            bookAuthor = book.author
            bookCoverUrl = book.coverUrl
            if (description.isBlank()) {
                description = book.description ?: ""
            }
            if (title.isBlank()) {
                title = "${book.title} Club"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create room") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = onSearchBook,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search and select book online")
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Room title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = bookTitle,
                onValueChange = { bookTitle = it },
                label = { Text("Book title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = bookAuthor,
                onValueChange = { bookAuthor = it },
                label = { Text("Book author") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Room description") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Private room",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Users need a code to join.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it }
                )
            }

            if (isPrivate) {
                OutlinedTextField(
                    value = accessCode,
                    onValueChange = { accessCode = it },
                    label = { Text("Access code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            actionState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    roomViewModel.createRoom(
                        title = title,
                        bookTitle = bookTitle,
                        bookAuthor = bookAuthor,
                        bookCoverUrl = bookCoverUrl,
                        description = description,
                        isPrivate = isPrivate,
                        accessCode = accessCode,
                        onSuccess = onRoomCreated
                    )
                },
                enabled = !actionState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (actionState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Save room")
                }
            }
        }
    }
}