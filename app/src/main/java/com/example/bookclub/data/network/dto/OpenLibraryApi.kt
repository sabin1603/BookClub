package com.example.bookclub.data.network

import com.example.bookclub.data.network.dto.OpenLibrarySearchResponse
import com.example.bookclub.data.network.dto.OpenLibraryWorkDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApi {

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String
    ): OpenLibrarySearchResponse

    @GET("works/{workId}.json")
    suspend fun getWorkDetails(
        @Path("workId") workId: String
    ): OpenLibraryWorkDto
}