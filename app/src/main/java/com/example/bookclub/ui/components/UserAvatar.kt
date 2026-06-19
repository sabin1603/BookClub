package com.example.bookclub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bookclub.ui.theme.BookSecondary
import com.example.bookclub.ui.theme.BookSecondaryContainer

@Composable
fun UserAvatar(
    username: String,
    profileImageUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val initial = username
        .trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString()
        ?: "?"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(BookSecondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!profileImageUri.isNullOrBlank()) {
            AsyncImage(
                model = profileImageUri,
                contentDescription = "$username profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initial,
                color = BookSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}