    package com.example.capstone.data.api.service

    import retrofit2.http.Body
    import retrofit2.http.POST

    data class ThesaurusRequest(val word: String, val lang: String)
    data class ThesaurusResponse(val synonyms: List<String>, val antonyms: List<String>)

    interface FlaskThesaurusService {
        @POST("synonyms")
        suspend fun getRelations(@Body request: ThesaurusRequest): ThesaurusResponse
    }
