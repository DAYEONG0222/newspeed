package com.example.capstone.data.api.service

import retrofit2.http.GET
import retrofit2.http.Query

data class WordRelation(val word: String)

interface DatamuseService {
    @GET("words")
    suspend fun getSynonyms(@Query("rel_syn") word: String): List<WordRelation>

    @GET("words")
    suspend fun getAntonyms(@Query("rel_ant") word: String): List<WordRelation>
}
