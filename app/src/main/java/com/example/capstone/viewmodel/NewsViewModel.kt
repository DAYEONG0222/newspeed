package com.example.capstone.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.api.RetrofitClient
import com.example.capstone.data.api.model.ExtractResponse
import com.example.capstone.data.api.model.NlpResponse
import com.example.capstone.data.api.service.GNewsApiService
import com.example.capstone.data.api.service.NlpRequest
import com.example.capstone.data.model.GNewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel : ViewModel() {

    val isLoading = MutableStateFlow(true)

    private val _articles = MutableStateFlow<List<GNewsArticle>>(emptyList())
    val articles: StateFlow<List<GNewsArticle>> = _articles

    private val _selectedCategory = MutableStateFlow("world")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage

    // ✅ 수정 (동적 로드)
    private val _userLevel = MutableStateFlow("beginner")
    val userLevel: StateFlow<String> = _userLevel

    val primaryArticles = MutableStateFlow<List<GNewsArticle>>(emptyList())
    val secondaryArticles = MutableStateFlow<List<GNewsArticle>>(emptyList())

    private val articleCache = mutableMapOf<String, List<GNewsArticle>>()
    private val cacheTimestamps = mutableMapOf<String, Long>()
    private val difficultyCache = mutableMapOf<String, String>()
    private val fetchStatus = mutableMapOf<String, Boolean>()

    private val CACHE_TTL_MS = 10 * 60 * 1000L // 10분

    fun hasFetched(category: String, lang: String): Boolean {
        return fetchStatus["$lang|$category"] == true
    }

    fun setCategory(newCategory: String) {
        _selectedCategory.value = newCategory
    }

    fun fetchNews(language: String, topic: String = _selectedCategory.value, forceRefresh: Boolean = false) {
        _currentLanguage.value = language
        _selectedCategory.value = topic
        isLoading.value = true

        val cacheKey = "$language|$topic"
        val now = System.currentTimeMillis()

        val lastFetched = cacheTimestamps[cacheKey]
        val hasCache = articleCache.containsKey(cacheKey)
        val isCacheFresh = lastFetched != null && now - lastFetched < CACHE_TTL_MS

        Log.d("NewsViewModel", "💡 forceRefresh: $forceRefresh / hasCache: $hasCache / isCacheFresh: $isCacheFresh")

        if (!forceRefresh && hasCache && isCacheFresh) {
            Log.d("NewsViewModel", "✅ 캐시 사용됨: $cacheKey")
            _articles.value = articleCache[cacheKey]!!
            filterArticlesByUserLevel()
            isLoading.value = false
            return
        }
    
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("NewsViewModel", "🌐 GNews API 요청: $cacheKey")
                val response = GNewsApiService.api.getTopHeadlines(
                    lang = language,
                    topic = topic,
                    token = GNewsApiService.apiKey,
                    max = 10
                )

                val analyzedArticles = response.articles.map { article ->
                    async { analyzeDifficulty(article, forceRefresh = forceRefresh) }
                }.awaitAll()

                _articles.value = analyzedArticles
                articleCache[cacheKey] = analyzedArticles
                cacheTimestamps[cacheKey] = now
                fetchStatus[cacheKey] = true

                filterArticlesByUserLevel()

            } catch (e: Exception) {
                Log.e("NewsViewModel", "❌ 뉴스 요청 실패: ${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }

    suspend fun analyzeDifficulty(article: GNewsArticle, forceRefresh: Boolean = false): GNewsArticle {
        if (!forceRefresh && difficultyCache.containsKey(article.url)) {
            return article.copy(difficulty = difficultyCache[article.url])
        }

        return try {
            Log.d("Analyze", "📤 Flask 서버 분석 요청 → ${article.url}")

            val extractResponse: Response<ExtractResponse> =
                RetrofitClient.extractService.extractArticle(mapOf("url" to article.url))

            val extractedText = extractResponse.body()?.text
            if (!extractResponse.isSuccessful || extractedText.isNullOrBlank()) {
                return article.copy(difficulty = "분석불가")
            }

            val nlpResponse: Response<NlpResponse> =
                RetrofitClient.nlpService.analyzeText(NlpRequest(extractedText))

            val difficulty = if (nlpResponse.isSuccessful) {
                nlpResponse.body()?.difficulty ?: "분석불가"
            } else {
                "분석불가"
            }

            difficultyCache[article.url] = difficulty
            return article.copy(difficulty = difficulty)

        } catch (e: Exception) {
            Log.e("NLP_ANALYZE", "❌ 예외 발생: ${e.message}")
            return article.copy(difficulty = "에러")
        }
    }

    fun filterArticlesByUserLevel() {
        val all = _articles.value.filter {
            it.difficulty != null && it.difficulty != "분석불가" && it.difficulty != "에러"
        }

        val (primary, secondary) = when (userLevel.value) {  // .value 추가
            "beginner" -> Pair(
                all.filter { it.difficulty == "초급" },
                all.filter { it.difficulty != "초급" }
            )
            "intermediate" -> Pair(
                all.filter { it.difficulty == "중급" },
                all.filter { it.difficulty != "중급" }
            )
            else -> Pair(
                all.filter { it.difficulty == "고급" },
                all.filter { it.difficulty != "고급" }
            )
        }

        primaryArticles.value = primary
        secondaryArticles.value = secondary
    }
}
