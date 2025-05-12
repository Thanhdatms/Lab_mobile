package com.example.dictionarylab4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class WordsAdapter
    : ListAdapter<String, WordsAdapter.WordVH>(DIFF) {

    companion object {
        private val DIFF = object: DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(a: String, b: String) = a == b
            override fun areContentsTheSame(a: String, b: String) = a == b
        }
    }

    inner class WordVH(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val tv = itemView.findViewById<TextView>(R.id.tvWord)
        fun bind(word: String) { tv.text = word }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WordVH(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false))

    override fun onBindViewHolder(holder: WordVH, pos: Int) =
        holder.bind(getItem(pos))
}