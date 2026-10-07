package com.example.capstone.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.api.RetrofitClient
import com.example.capstone.data.api.service.ArticleBatchRequest
import com.example.capstone.data.api.service.GNewsApiService
import com.example.capstone.data.model.GNewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.capstone.data.local.UserLevelStore
import com.example.capstone.data.model.UserLevel

class EsNewsViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _articles = MutableStateFlow<List<GNewsArticle>>(emptyList())
    val articles: StateFlow<List<GNewsArticle>> = _articles

    private val _primaryArticles = MutableStateFlow<List<GNewsArticle>>(emptyList())
    val primaryArticles: StateFlow<List<GNewsArticle>> = _primaryArticles

    private val _secondaryArticles = MutableStateFlow<List<GNewsArticle>>(emptyList())
    val secondaryArticles: StateFlow<List<GNewsArticle>> = _secondaryArticles

    private val _selectedCategory = MutableStateFlow("사회")
    val selectedCategory: StateFlow<String> = _selectedCategory

    // 저장된 레벨(레벨 테스트 또는 직접 선택). 기존에는 "beginner" 로 고정이었다.
    private val levelFlow = UserLevelStore.level("es")
    val userLevel: String
        get() = when (levelFlow.value) {
            UserLevel.BEGINNER -> "beginner"
            UserLevel.INTERMEDIATE -> "intermediate"
            UserLevel.ADVANCED -> "advanced"
        }

    init {
        // 레벨이 바뀌면 이미 받아 둔 기사를 다시 나눈다 (네트워크 재요청 없음)
        viewModelScope.launch {
            levelFlow.collect { filterArticlesByLevel(_articles.value) }
        }
    }

    private val articleCache = mutableMapOf<String, List<GNewsArticle>>()
    private val cacheTimestamps = mutableMapOf<String, Long>()
    private val fetchStatus = mutableMapOf<String, Boolean>()
    private val CACHE_TTL_MS = 10 * 60 * 1000L

    private val categoryToTopic = mapOf(
        "정치" to "nation",
        "경제" to "business",
        "사회" to "world",
        "기술" to "technology",
        "과학" to "science",
        "건강" to "health",
        "스포츠" to "sports",
        "연예" to "entertainment"
    )

    fun hasFetched(category: String): Boolean {
        val topic = categoryToTopic[category] ?: "world"
        return fetchStatus["es|$topic"] == true
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category

        val topic = categoryToTopic[category] ?: "world"
        val cacheKey = "es|$topic"
        val cached = articleCache[cacheKey]

        if (cached != null) {
            _articles.value = cached
            filterArticlesByLevel(cached)
        }

        fetchSpanishNews(category, forceRefresh = false)
    }

    fun fetchSpanishNews(category: String, forceRefresh: Boolean = false) {
        val topic = categoryToTopic[category] ?: "world"
        val cacheKey = "es|$topic"
        val now = System.currentTimeMillis()
        val lastFetched = cacheTimestamps[cacheKey]

        val isCacheValid = !forceRefresh &&
                articleCache.containsKey(cacheKey) &&
                lastFetched != null &&
                now - lastFetched < CACHE_TTL_MS


        if (isCacheValid) {
            val cached = articleCache[cacheKey] ?: emptyList()
            _articles.value = cached
            filterArticlesByLevel(cached)
            fetchStatus[cacheKey] = true
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        _articles.value = emptyList()
        _primaryArticles.value = emptyList()
        _secondaryArticles.value = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("EsNewsViewModel", "🌐 스페인어 GNews 요청: $cacheKey")
                val response = GNewsApiService.api.getTopHeadlines(
                    lang = "es",
                    topic = topic,
                    token = GNewsApiService.apiKey,
                    max = 10
                )

                val articles = response.articles

                // 기사마다 '본문 추출 → 분석' 2번씩 순차 요청하던 것을 배치 요청 1번으로
                val urls = articles.mapNotNull { it.url }
                val batch = RetrofitClient.articleBatchService.analyze(ArticleBatchRequest(lang = "es", urls = urls))
                val resultByUrl = if (batch.isSuccessful) {
                    batch.body()?.results.orEmpty().associateBy { it.url }
                } else {
                    Log.e("EsNewsViewModel", "배치 분석 실패: HTTP ${batch.code()}")
                    emptyMap()
                }

                val analyzed = articles.mapNotNull { article ->
                    val result = resultByUrl[article.url] ?: return@mapNotNull null
                    val content = result.text ?: return@mapNotNull null
                    val level = result.level ?: return@mapNotNull null

                    GNewsArticle(
                        title = article.title ?: "제목 없음",
                        description = article.description ?: content.take(100),
                        url = article.url,
                        image = article.image,
                        publishedAt = article.publishedAt ?: "",
                        difficulty = level,
                        content = content
                    )
                }

                _articles.value = analyzed
                articleCache[cacheKey] = analyzed
                cacheTimestamps[cacheKey] = now
                fetchStatus[cacheKey] = true
                filterArticlesByLevel(analyzed)

            } catch (e: Exception) {
                Log.e("EsNewsViewModel", "❌ 스페인어 뉴스 수집 실패: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }


    private fun filterArticlesByLevel(all: List<GNewsArticle>) {
        val valid = all.filter { it.difficulty in listOf("초급", "중급", "고급") }

        val (primary, secondary) = when (userLevel) {
            "beginner" -> valid.partition { it.difficulty == "초급" }
            "intermediate" -> valid.partition { it.difficulty == "중급" }
            else -> valid.partition { it.difficulty == "고급" }
        }

        _primaryArticles.value = primary
        _secondaryArticles.value = secondary
    }
}
