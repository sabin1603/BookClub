package com.example.bookclub.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bookclub.R
import com.example.bookclub.data.repository.BookSearchItem
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
import com.example.bookclub.ui.theme.BookSurfaceContainerHighest
import com.example.bookclub.viewmodel.BookViewModel
import kotlinx.coroutines.delay

private data class BookCategory(
    val label: String,
    val query: String
)

@Composable
fun BookSearchScreen(
    onBack: () -> Unit,
    onBookSelected: (Long) -> Unit,
    viewModel: BookViewModel = viewModel()
) {
    val state by viewModel.searchState.collectAsState()

    val categories = remember {
        listOf(
            BookCategory("Fiction", "fiction"),
            BookCategory("Non-Fiction", "nonfiction"),
            BookCategory("Bestsellers", "bestseller"),
            BookCategory("Sci-Fi & Fantasy", "science fiction fantasy"),
            BookCategory("Biographies", "biography"),
            BookCategory("Mystery", "mystery"),
            BookCategory("Romance", "romance")
        )
    }

    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories[2]) }

    LaunchedEffect(query, selectedCategory) {
        delay(350)

        val cleanQuery = query.trim()
        val searchQuery = cleanQuery.ifBlank { selectedCategory.query }

        if (searchQuery.isNotBlank()) {
            viewModel.searchBooks(searchQuery)
        }
    }

    Scaffold(
        containerColor = BookBackground,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            SearchBooksTopBar(onBack = onBack)
        },
        bottomBar = {
            SearchBottomBar(
                onClubsClick = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp),
            contentPadding = PaddingValues(top = 28.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SearchTextBox(
                        query = query,
                        onQueryChange = { query = it }
                    )

                    CategoryTabs(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = {
                            selectedCategory = it
                            query = ""
                        }
                    )
                }
            }

            state.errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (state.isLoading && state.books.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = BookPrimary
                        )
                    }
                }
            }

            if (!state.isLoading && state.books.isEmpty()) {
                item {
                    EmptySearchState(
                        selectedCategory = selectedCategory.label
                    )
                }
            }

            items(
                items = state.books,
                key = { "${it.title}-${it.author}-${it.firstPublishYear ?: 0}" }
            ) { book ->
                BookResultCard(
                    book = book,
                    onAddClick = {
                        viewModel.cacheSelectedBook(book) { cachedBookId ->
                            onBookSelected(cachedBookId)
                        }
                    }
                )
            }

            if (state.isLoading && state.books.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = BookOutline,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBooksTopBar(
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
                        color = BookOnSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                    )
                }

                Text(
                    text = "Search Books",
                    color = BookPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
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
private fun SearchTextBox(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search titles, authors, or ISBN...",
                color = BookOutlineVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        singleLine = true,
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_nav_search),
                contentDescription = null,
                tint = BookPrimary,
                modifier = Modifier.size(28.dp)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = BookOnSurface,
            unfocusedTextColor = BookOnSurface,
            focusedContainerColor = BookSurfaceContainerLow,
            unfocusedContainerColor = BookSurfaceContainerLow,
            disabledContainerColor = BookSurfaceContainerLow,
            focusedIndicatorColor = BookPrimary,
            unfocusedIndicatorColor = BookPrimary,
            cursorColor = BookPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(
                width = 1.4.dp,
                color = BookPrimary,
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
            )
    )
}

@Composable
private fun CategoryTabs(
    categories: List<BookCategory>,
    selectedCategory: BookCategory,
    onCategorySelected: (BookCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->
            val selected = category == selectedCategory

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        color = if (selected) {
                            BookSecondaryContainer.copy(alpha = 0.45f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .border(
                        width = if (selected) 0.dp else 1.dp,
                        color = if (selected) Color.Transparent else BookOutlineVariant,
                        shape = CircleShape
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.label,
                    color = if (selected) BookSecondary else BookOnSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun BookResultCard(
    book: BookSearchItem,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = BookSurfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFE6E3D8)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BookCover(
                coverUrl = book.coverUrl,
                title = book.title
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = book.title,
                        color = BookOnSurface,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    book.firstPublishYear?.let { year ->
                        Box(
                            modifier = Modifier
                                .background(
                                    color = BookSurfaceContainerHighest,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = year.toString(),
                                color = BookOnSurfaceVariant,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = book.author,
                    color = BookOnSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    color = BookOutlineVariant.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onAddClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BookPrimaryContainer,
                            contentColor = BookOnPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add_book),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(14.dp)
                            )

                            Text(
                                text = "Add to Room",
                                style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
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
            .size(width = 106.dp, height = 156.dp)
            .clip(RoundedCornerShape(8.dp))
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = "▯",
                    color = BookOutlineVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "NO COVER\nAVAILABLE",
                    color = BookOutline,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EmptySearchState(
    selectedCategory: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BookOutlineVariant,
                shape = RoundedCornerShape(18.dp)
            )
            .background(
                color = BookSurfaceContainerLow,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No books found yet. Try searching a title, author, ISBN, or choose another category. Current category: $selectedCategory.",
            color = BookOnSurfaceVariant,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SearchBottomBar(
    onClubsClick: () -> Unit
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
            selected = true,
            onClick = {},
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nav_search),
                        contentDescription = null,
                        tint = BookSecondary,
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
            selected = false,
            onClick = {},
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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