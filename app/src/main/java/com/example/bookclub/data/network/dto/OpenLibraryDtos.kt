package com.example.bookclub.data.network.dto

import com.google.gson.annotations.SerializedName

data class OpenLibrarySearchResponse(
    @SerializedName("docs")
    val docs: List<OpenLibraryBookDto> = emptyList()
)

data class OpenLibraryBookDto(
    @SerializedName("key")
    val key: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("author_name")
    val authorNames: List<String>? = null,

    @SerializedName("first_publish_year")
    val firstPublishYear: Int? = null,

    @SerializedName("cover_i")
    val coverId: Int? = null,

    @SerializedName("editions")
    val editions: OpenLibraryEditionsDto? = null
)

data class OpenLibraryEditionsDto(
    @SerializedName("docs")
    val docs: List<OpenLibraryEditionDto> = emptyList()
)

data class OpenLibraryEditionDto(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("language")
    val language: List<String>? = null
)

data class OpenLibraryWorkDto(
    @SerializedName("description")
    val description: Any? = null
)