package com.example.capstone.quiz

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.R
import com.example.capstone.data.local.entity.Vocabulary

enum class SortOption(val label: String) {
    LATEST("최신순"),
    OLDEST("오래된순"),
    ALPHABET("알파벳순")
}

class WrongNoteActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WrongNoteAdapter
    private lateinit var sortSpinner: Spinner
    private var wrongAnswers: List<Vocabulary> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong_note)

        recyclerView = findViewById(R.id.recyclerWrongNote)
        sortSpinner = findViewById(R.id.spinnerSort)

        // 오답 데이터 불러오기
        wrongAnswers = WrongNoteManager.getAll()

        // 초기 어댑터 설정 (기본은 최신순)
        adapter = WrongNoteAdapter(wrongAnswers.sortedByDescending { it.vocaId })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Spinner에 정렬 옵션 연결
        val sortOptions = SortOption.values().map { it.label }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = spinnerAdapter

        // Spinner 선택 리스너
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selected = SortOption.values()[position]
                applySort(selected)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun applySort(option: SortOption) {
        val sortedList = when (option) {
            SortOption.LATEST -> wrongAnswers.sortedByDescending { it.vocaId }
            SortOption.OLDEST -> wrongAnswers.sortedBy { it.vocaId }
            SortOption.ALPHABET -> wrongAnswers.sortedBy { it.word.lowercase() }
        }
        adapter.updateData(sortedList)
    }
}
