package com.example.capstone.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.api.service.GNewsApiService
import com.example.capstone.data.api.service.ArticleBatchRequest
import com.example.capstone.data.api.RetrofitClient
import com.example.capstone.data.model.GNewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JaNewsViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _articles = MutableStateFlow<List<GNewsArticle>>(emptyList())
    val articles: StateFlow<List<GNewsArticle>> = _articles

    private val _primaryArticles = MutableStateFlow<List<GNewsArticle>>(emptyList())
    val primaryArticles: StateFlow<List<GNewsArticle>> = _primaryArticles

    private val _secondaryArticles = MutableStateFlow<List<GNewsArticle>>(emptyList())
    val secondaryArticles: StateFlow<List<GNewsArticle>> = _secondaryArticles

    private val _selectedCategory = MutableStateFlow("国際")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val userLevel = "beginner" // 추후 동적 처리

    private val articleCache = mutableMapOf<String, List<GNewsArticle>>()
    private val cacheTimestamps = mutableMapOf<String, Long>()
    private val fetchStatus = mutableMapOf<String, Boolean>()
    private val CACHE_TTL_MS = 10 * 60 * 1000L

    private val categoryToKeyword = mapOf(
        "政治" to "政治 OR 首相",
        "経済" to "経済 OR 株式",
        "国際" to "国際 OR 外交",
        "技術" to "テクノロジー OR 技術",
        "科学" to "科学 OR 宇宙",
        "エンタメ" to "エンタメ OR 芸能",
        "健康" to "健康 OR 医療",
        "スポーツ" to "スポーツ OR 試合"
    )

    fun hasFetched(category: String): Boolean {
        val keyword = categoryToKeyword[category] ?: "国際"
        return fetchStatus["ja|$keyword"] == true
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category

        val keyword = categoryToKeyword[category] ?: "国際"
        val cacheKey = "ja|$keyword"
        val cached = articleCache[cacheKey]

        if (cached != null) {
            _articles.value = cached
            filterArticlesByLevel(cached)
        }

        fetchJapaneseNews(category, forceRefresh = false)
    }

    fun fetchJapaneseNews(category: String, forceRefresh: Boolean = false) {
        val keyword = categoryToKeyword[category] ?: "国際"
        val cacheKey = "ja|$keyword"
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

        // ✅ 네트워크 요청 시에만 상태 초기화
        _isLoading.value = true
        _articles.value = emptyList()
        _primaryArticles.value = emptyList()
        _secondaryArticles.value = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("JaNewsViewModel", "🌐 일본어 GNews 요청: $cacheKey")
                val response = GNewsApiService.api.searchJapaneseNews(keyword)
                val articles = response.articles.take(10)

                // 기사마다 '본문 추출 → 분석' 2번씩 순차 요청하던 것을 배치 요청 1번으로
                val urls = articles.mapNotNull { it.url }
                val batch = RetrofitClient.articleBatchService.analyze(ArticleBatchRequest(lang = "ja", urls = urls))
                val resultByUrl = if (batch.isSuccessful) {
                    batch.body()?.results.orEmpty().associateBy { it.url }
                } else {
                    Log.e("JaNewsViewModel", "배치 분석 실패: HTTP ${batch.code()}")
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
                Log.e("JaNewsViewModel", "❌ GNews 일본어 뉴스 수집 실패: ${e.message}")
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
