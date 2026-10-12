package com.example.capstone.screen.article


import java.util.Locale
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.capstone.viewmodel.SharedTextViewModel
import com.example.capstone.viewmodel.SharedUrlViewModel
import com.example.capstone.viewmodel.WordViewModel
import com.example.capstone.data.api.service.TranslateRequest
import com.example.capstone.data.api.service.ThesaurusRequest
import kotlinx.coroutines.launch
import com.example.capstone.data.api.RetrofitClient
import com.example.capstone.data.api.RetrofitTranslateClient
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ArticleDetailScreenEs(
    sharedUrlViewModel: SharedUrlViewModel,
    sharedTextViewModel: SharedTextViewModel,
    wordViewModel: WordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val textState by sharedTextViewModel.text.collectAsState()
    val titleState by sharedTextViewModel.title.collectAsState()
    val language = "es"

    var translatedText by remember { mutableStateOf<String?>(null) }
    var translatedTitle by remember { mutableStateOf<String?>(null) }
    var isTranslated by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val displayedText = if (isTranslated) translatedText ?: "번역 실패" else textState
    val displayedTitle = if (isTranslated) translatedTitle ?: "번역 실패" else titleState

    val highlightedSentences = remember { mutableStateListOf<Int>() }
    val translatedSentences = remember { mutableStateMapOf<Int, String>() }

    val showSynonymSheet = remember { mutableStateOf(false) }
    val showTranslationSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val synonymList = remember { mutableStateListOf<String>() }

    val selectedWord = remember { mutableStateOf<String?>(null) }
    val lastTranslated = remember { mutableStateOf<String>("") }

    val ttsReady = remember { mutableStateOf(false) }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) {

        var ttsEngine: TextToSpeech? = null

        ttsEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = ttsEngine?.setLanguage(Locale("es"))
                if (result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsReady.value = true
                }
            }
        }

        tts.value = ttsEngine
    }


    val sentenceList = displayedText.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val textResponse = RetrofitTranslateClient.translateService
                                    .translateText(TranslateRequest(textState))
                                val titleResponse = RetrofitTranslateClient.translateService
                                    .translateText(TranslateRequest(titleState))
                                if (textResponse.isSuccessful && titleResponse.isSuccessful) {
                                    translatedText = textResponse.body()?.translated_text ?: "번역 실패"
                                    translatedTitle = titleResponse.body()?.translated_text ?: "번역 실패"
                                    isTranslated = true
                                } else {
                                    Toast.makeText(context, "번역 실패: 서버 오류", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "번역 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isTranslated && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4169E1))
                ) {
                    Text("번역 요청", color = Color.White)
                }

                Button(
                    onClick = { isTranslated = false },
                    enabled = isTranslated && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4169E1))
                ) {
                    Text("원문 보기", color = Color.White)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            highlightedSentences.forEach { index ->
                                if (translatedSentences[index] == null) {
                                    val response = RetrofitTranslateClient.translateService
                                        .translateText(TranslateRequest(sentenceList[index]))
                                    if (response.isSuccessful) {
                                        translatedSentences[index] =
                                            response.body()?.translated_text ?: "번역 실패"
                                    }
                                }
                            }
                            showTranslationSheet.value = true
                        }
                    },
                    enabled = highlightedSentences.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("하이라이트 번역", color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = displayedTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            sentenceList.forEachIndexed { index, sentence ->
                val isHighlighted = highlightedSentences.contains(index)

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isHighlighted) highlightedSentences.remove(index)
                            else highlightedSentences.add(index)
                        }
                        .padding(vertical = 4.dp)
                ) {
                    sentence.trim().split(" ").forEach { word ->
                        AnimatedClickableWordEs(
                            word = word,
                            isHighlighted = isHighlighted,
                            onClick = {
                                coroutineScope.launch {
                                    val response = RetrofitTranslateClient.translateService
                                        .translateText(TranslateRequest(word))
                                    if (response.isSuccessful) {
                                        val translated = response.body()?.translated_text ?: ""
                                        lastTranslated.value = translated
                                        Toast.makeText(context, "'$word' → '$translated'", Toast.LENGTH_SHORT).show()
                                        if (ttsReady.value) {
                                            tts.value?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
                                        }
                                    }
                                }
                            },
                            onLongClick = {
                                selectedWord.value = word
                                coroutineScope.launch {
                                    // 유의어 조회가 실패해도(키 없음 등) 단어 저장 창은 열려야 한다.
                                    // 기존에는 실패하면 창이 열리지 않아 단어를 저장할 수 없었다.
                                    synonymList.clear()
                                    try {
                                        val response = RetrofitTranslateClient.translateService
                                            .translateText(TranslateRequest(word))
                                        if (response.isSuccessful) {
                                            lastTranslated.value = response.body()?.translated_text ?: ""
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "번역 실패", Toast.LENGTH_SHORT).show()
                                    }
                                    try {
                                        val result = RetrofitClient.flaskService.getRelations(
                                            ThesaurusRequest(word, language)
                                        )
                                        synonymList.addAll(result.synonyms)
                                    } catch (e: Exception) {
                                        // 유의어는 부가 기능: 목록만 비워 두고 계속 진행
                                    }
                                    showSynonymSheet.value = true
                                }
                            }
                        )
                    }
                }
            }

            if (showSynonymSheet.value) {
                ModalBottomSheet(
                    onDismissRequest = { showSynonymSheet.value = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("🔁 유의어", style = MaterialTheme.typography.titleMedium)
                        if (synonymList.isNotEmpty()) {
                            synonymList.forEach { Text("• $it") }
                        } else Text("없음")

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                selectedWord.value?.let { word ->
                                    coroutineScope.launch {
                                        wordViewModel.saveWord(word, language, lastTranslated.value, "")
                                        Toast.makeText(context, "단어 저장됨", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("단어 저장")
                        }
                    }
                }
            }

            if (showTranslationSheet.value) {
                ModalBottomSheet(
                    onDismissRequest = { showTranslationSheet.value = false },
                    sheetState = sheetState
                ) {
                    Column(Modifier.padding(16.dp)) {
                        highlightedSentences.forEach { index ->
                            val original = sentenceList[index]
                            val translated = translatedSentences[index] ?: "번역 준비 중..."
                            Text("• $original", style = MaterialTheme.typography.bodySmall)
                            Text("→ $translated", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun AnimatedClickableWordEs(
    word: String,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var isClicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isClicked) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 250),
        finishedListener = { isClicked = false }
    )

    Text(
        text = "$word ",
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(
                if (isHighlighted) Color.Yellow.copy(alpha = 0.3f) else Color.Transparent
            )
            .padding(end = 2.dp)
            .combinedClickable(
                onClick = {
                    isClicked = true
                    onClick()
                },
                onLongClick = onLongClick
            ),
        style = MaterialTheme.typography.bodyMedium
    )
}

