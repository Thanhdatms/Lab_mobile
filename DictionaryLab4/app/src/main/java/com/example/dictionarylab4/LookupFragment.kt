package com.example.dictionarylab4

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LookupFragment: Fragment(R.layout.fragment_lookup) {

    private lateinit var dbHelper: DictionaryDbHelper
    private lateinit var etWord: EditText
    private lateinit var btnLookup: Button
    private lateinit var tvDefinition: TextView
    private lateinit var rvWords: RecyclerView
    private val adapter = WordsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DictionaryDbHelper(requireContext())
        etWord       = view.findViewById(R.id.etWord)
        btnLookup    = view.findViewById(R.id.btnLookup)
        tvDefinition = view.findViewById(R.id.tvDefinition)
        rvWords = view.findViewById<RecyclerView>(R.id.rvWords)
        rvWords.layoutManager = LinearLayoutManager(requireContext())
        rvWords.adapter       = adapter

        btnLookup.setOnClickListener {
            val q = etWord.text.toString().trim()
            if (q.isNotEmpty()) lookup(q)
        }
    }
    private fun lookup(query: String) {
        val db = dbHelper.readableDatabase

        db.query(
            DictionaryContract.WordEntry.TABLE_NAME,
            arrayOf(DictionaryContract.WordEntry.COLUMN_DEFINITION),
            "${DictionaryContract.WordEntry.COLUMN_WORD} = ?",
            arrayOf(query), null, null, null
        ).use { c ->
            if (c.moveToFirst()) {
                tvDefinition.text = c.getString(0)
                tvDefinition.visibility = VISIBLE
                rvWords.visibility = GONE
                return
            }
        }

        val matches = mutableListOf<String>()
        db.query(
            DictionaryContract.WordEntry.TABLE_NAME,
            arrayOf(DictionaryContract.WordEntry.COLUMN_WORD),
            "${DictionaryContract.WordEntry.COLUMN_WORD} LIKE ?",
            arrayOf("%$query%"), null, null, null
        ).use { c ->
            while (c.moveToNext()) {
                matches += c.getString(0)
            }
        }

        if (matches.isEmpty()) {
            tvDefinition.text = "No matches found"
            tvDefinition.visibility = VISIBLE
            rvWords.visibility = GONE
        } else {
            adapter.submitList(matches)
            rvWords.visibility      = VISIBLE
            tvDefinition.visibility = GONE
        }
    }
}
