package com.example.capstone.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.data.local.entity.Vocabulary
import com.example.capstone.databinding.ItemWrongWordBinding

class WrongNoteAdapter(private var items: List<Vocabulary>) :
    RecyclerView.Adapter<WrongNoteAdapter.WrongNoteViewHolder>() {

    inner class WrongNoteViewHolder(private val binding: ItemWrongWordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Vocabulary) {
            binding.textWord.text = item.word
            binding.textMeaning.text = item.meaning
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WrongNoteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWrongWordBinding.inflate(inflater, parent, false)
        return WrongNoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WrongNoteViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    // ✅ 정렬된 리스트로 교체하고 화면 갱신
    fun updateData(newItems: List<Vocabulary>) {
        items = newItems
        notifyDataSetChanged()
    }
}

