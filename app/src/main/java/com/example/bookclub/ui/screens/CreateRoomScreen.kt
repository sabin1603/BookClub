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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bookclub.R
import com.example.bookclub.data.local.entity.CachedBookEntity
import com.example.bookclub.data.local.entity.RoomBookEntity
import com.example.bookclub.ui.theme.BookBackground
import com.example.bookclub.ui.theme.BookError
import com.example.bookclub.ui.theme.BookOnPrimary
import com.example.bookclub.ui.theme.BookOnSurface
import com.example.bookclub.ui.theme.BookOnSurfaceVariant
import com.example.bookclub.ui.theme.BookOutline
import com.example.bookclub.ui.theme.BookOutlineVariant
import com.example.bookclub.ui.theme.BookPrimary
import com.example.bookclub.ui.theme.BookPrimaryContainer
import com.example.bookclub.ui.theme.BookSurface
import com.example.bookclub.ui.theme.BookSurfaceContainerHigh
import com.example.bookclub.ui.theme.BookSurfaceContainerLow
import com.example.bookclub.ui.theme.BookSurfaceContainerLowest
import com.example.bookclub.viewmodel.BookViewModel
import com.example.bookclub.viewmodel.RoomViewModel

private const val ROOM_TITLE_MAX_LENGTH = 60
private const val ROOM_DESCRIPTION_MAX_LENGTH = 300

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
                title = "${book.title} Club".take(ROOM_TITLE_MAX_LENGTH)
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
            title = "$cleanTitle Club".take(ROOM_TITLE_MAX_LENGTH)
        }
    }

    Scaffold(
        containerColor = BookBackground,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            CreateRoomTopBar(onBack = onBack)
        },
        bottomBar = {
            SaveRoomBottomBar(
                isLoading = actionState.isLoading,
                onSave = {
                    roomViewModel.createRoom(
                        title = title,
                        description = description,
                        isPrivate = isPrivate,
                        accessCode = accessCode,
                        books = selectedBooks.toList(),
                        onSuccess = onRoomCreated
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            contentPadding = PaddingValues(top = 28.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            item {
                SearchBookButton(onClick = onSearchBook)
            }

            item {
                LabeledCounterTextField(
                    label = "Room Title",
                    value = title,
                    onValueChange = {
                        if (it.length <= ROOM_TITLE_MAX_LENGTH) {
                            title = it
                        }
                    },
                    placeholder = "E.g., Sunday Sci-Fi Readers",
                    counter = "${title.length}/$ROOM_TITLE_MAX_LENGTH",
                    singleLine = true
                )
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Books in this Room",
                        color = BookPrimary,
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                    )

                    if (selectedBooks.isEmpty()) {
                        EmptyBooksBox()
                    }
                }
            }

            if (selectedBooks.isNotEmpty()) {
                items(
                    items = selectedBooks,
                    key = { "${it.title}-${it.author}-${it.displayOrder}" }
                ) { book ->
                    AddedBookCard(
                        book = book,
                        onRemove = {
                            selectedBooks.remove(book)
                        }
                    )
                }
            }

            item {
                ManualBookCard(
                    manualBookTitle = manualBookTitle,
                    onManualBookTitleChange = { manualBookTitle = it },
                    manualBookAuthor = manualBookAuthor,
                    onManualBookAuthorChange = { manualBookAuthor = it },
                    onAddManualBook = { addManualBook() }
                )
            }

            item {
                LabeledCounterTextField(
                    label = "Room Description",
                    value = description,
                    onValueChange = {
                        if (it.length <= ROOM_DESCRIPTION_MAX_LENGTH) {
                            description = it
                        }
                    },
                    placeholder = "What is this room about?",
                    counter = "${description.length}/$ROOM_DESCRIPTION_MAX_LENGTH",
                    singleLine = false,
                    minLines = 5
                )
            }

            item {
                PrivacyCard(
                    isPrivate = isPrivate,
                    onPrivateChange = { isPrivate = it },
                    accessCode = accessCode,
                    onAccessCodeChange = { accessCode = it }
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
        }
    }
}

@Composable
private fun CreateRoomTopBar(
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
                    .height(64.dp)
                    .padding(horizontal = 20.dp),
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
                    text = "Create New Room",
                    color = BookPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.width(64.dp))
            }

            HorizontalDivider(
                color = BookOutlineVariant.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun SearchBookButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = BookOutlineVariant
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = BookSurfaceContainerLow,
            contentColor = BookPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .border(
                width = 1.dp,
                color = BookOutlineVariant.copy(alpha = 0.75f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_nav_search),
                contentDescription = null,
                tint = BookPrimary,
                modifier = Modifier.size(28.dp)
            )

            Text(
                text = "Search and add book",
                color = BookPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LabeledCounterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    counter: String,
    singleLine: Boolean,
    minLines: Int = 1
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                color = BookOnSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )

            Text(
                text = counter,
                color = BookOutline,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )
        }

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFF747B8B),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = BookOnSurface,
                unfocusedTextColor = BookOnSurface,
                focusedContainerColor = BookSurfaceContainerLow,
                unfocusedContainerColor = BookSurfaceContainerLow,
                disabledContainerColor = BookSurfaceContainerLow,
                focusedIndicatorColor = BookPrimary,
                unfocusedIndicatorColor = BookOutline,
                cursorColor = BookPrimary,
                focusedLabelColor = BookPrimary,
                unfocusedLabelColor = BookOnSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EmptyBooksBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BookOutlineVariant,
                shape = RoundedCornerShape(18.dp)
            )
            .background(
                color = BookSurface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No books added yet.",
            color = BookOnSurfaceVariant,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AddedBookCard(
    book: RoomBookEntity,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = BookSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            BookCover(
                coverUrl = book.coverUrl,
                title = book.title
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = book.title,
                    color = BookOnSurface,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    maxLines = 1
                )

                Text(
                    text = book.author,
                    color = BookOnSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_remove_book),
                    contentDescription = "Remove book",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun BookCover(
    coverUrl: String?,
    title: String
) {
    Box(
        modifier = Modifier
            .size(width = 58.dp, height = 82.dp)
            .clip(RoundedCornerShape(4.dp))
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
                text = "Book",
                color = BookOutline,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ManualBookCard(
    manualBookTitle: String,
    onManualBookTitleChange: (String) -> Unit,
    manualBookAuthor: String,
    onManualBookAuthorChange: (String) -> Unit,
    onAddManualBook: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = BookSurfaceContainerLow
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Add Book Manually",
                color = BookPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
            )

            SimpleInput(
                label = "Title",
                value = manualBookTitle,
                onValueChange = onManualBookTitleChange
            )

            SimpleInput(
                label = "Author",
                value = manualBookAuthor,
                onValueChange = onManualBookAuthorChange
            )

            OutlinedButton(
                onClick = onAddManualBook,
                shape = CircleShape,
                border = BorderStroke(1.5.dp, BookPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BookPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Add Manually",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SimpleInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = BookOnSurfaceVariant,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = BookOnSurface,
                unfocusedTextColor = BookOnSurface,
                focusedContainerColor = BookSurface,
                unfocusedContainerColor = BookSurface,
                disabledContainerColor = BookSurface,
                focusedIndicatorColor = BookPrimary,
                unfocusedIndicatorColor = BookOutline,
                cursorColor = BookPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PrivacyCard(
    isPrivate: Boolean,
    onPrivateChange: (Boolean) -> Unit,
    accessCode: String,
    onAccessCodeChange: (String) -> Unit
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
                    color = BookOutlineVariant,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Private Room",
                        color = BookOnSurface,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "Require an access code to join.",
                        color = BookOnSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                    )
                }

                Switch(
                    checked = isPrivate,
                    onCheckedChange = onPrivateChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BookOnPrimary,
                        checkedTrackColor = BookPrimaryContainer,
                        uncheckedThumbColor = BookOnPrimary,
                        uncheckedTrackColor = BookSurfaceContainerHigh
                    )
                )
            }

            if (isPrivate) {
                HorizontalDivider(
                    color = BookOutlineVariant.copy(alpha = 0.55f)
                )

                SimpleInput(
                    label = "Access Code",
                    value = accessCode,
                    onValueChange = onAccessCodeChange
                )
            }
        }
    }
}

@Composable
private fun SaveRoomBottomBar(
    isLoading: Boolean,
    onSave: () -> Unit
) {
    Surface(
        color = BookSurface,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onSave,
                enabled = !isLoading,
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
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = BookOnPrimary
                    )
                } else {
                    Text(
                        text = "Save Room",
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}