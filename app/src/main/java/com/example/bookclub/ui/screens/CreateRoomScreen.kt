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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookclub.data.local.entity.CachedBookEntity
import com.example.bookclub.data.local.entity.RoomBookEntity
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

    val selectedBookState = if (bookId != null) {
        bookViewModel.observeBook(bookId).collectAsState(initial = null)
    } else {
        remember {
            mutableStateOf<CachedBookEntity?>(null)
        }
    }

    val selectedBook = selectedBookState.value
    val selectedBooks = remember { mutableStateListOf<RoomBookEntity>() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var accessCode by remember { mutableStateOf("") }

    var manualBookTitle by remember { mutableStateOf("") }
    var manualBookAuthor by remember { mutableStateOf("") }

    LaunchedEffect(selectedBook?.id) {
        selectedBook?.let { book ->
            val alreadyAdded = selectedBooks.any {
                it.title.equals(book.title, ignoreCase = true) &&
                        it.author.equals(book.author, ignoreCase = true)
            }

            if (!alreadyAdded) {
                selectedBooks.add(
                    RoomBookEntity(
                        roomId = 0,
                        title = book.title,
                        author = book.author,
                        firstPublishYear = book.firstPublishYear,
                        coverUrl = book.coverUrl,
                        openLibraryKey = book.openLibraryKey,
                        description = book.description,
                        displayOrder = selectedBooks.size
                    )
                )
            }

            if (title.isBlank()) {
                title = "${book.title} Club"
            }
        }
    }

    fun addManualBook() {
        val cleanTitle = manualBookTitle.trim()
        val cleanAuthor = manualBookAuthor.trim()

        if (cleanTitle.isBlank() || cleanAuthor.isBlank()) {
            return
        }

        val alreadyAdded = selectedBooks.any {
            it.title.equals(cleanTitle, ignoreCase = true) &&
                    it.author.equals(cleanAuthor, ignoreCase = true)
        }

        if (!alreadyAdded) {
            selectedBooks.add(
                RoomBookEntity(
                    roomId = 0,
                    title = cleanTitle,
                    author = cleanAuthor,
                    firstPublishYear = null,
                    coverUrl = null,
                    openLibraryKey = null,
                    description = null,
                    displayOrder = selectedBooks.size
                )
            )
        }

        manualBookTitle = ""
        manualBookAuthor = ""

        if (title.isBlank()) {
            title = "$cleanTitle Club"
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FilledTonalButton(
                    onClick = onSearchBook,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Search and add book online")
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Room title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Books in this room",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (selectedBooks.isEmpty()) {
                item {
                    Text(
                        text = "No books added yet. Add at least one book.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(selectedBooks) { book ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "by ${book.author}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            TextButton(
                                onClick = {
                                    selectedBooks.remove(book)
                                }
                            ) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = manualBookTitle,
                    onValueChange = { manualBookTitle = it },
                    label = { Text("Manual book title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = manualBookAuthor,
                    onValueChange = { manualBookAuthor = it },
                    label = { Text("Manual book author") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                FilledTonalButton(
                    onClick = { addManualBook() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add manual book")
                }
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Room description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
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

            actionState.errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        roomViewModel.createRoom(
                            title = title,
                            description = description,
                            isPrivate = isPrivate,
                            accessCode = accessCode,
                            books = selectedBooks.toList(),
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
}