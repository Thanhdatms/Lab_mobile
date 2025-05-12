package com.example.dictionarylab4

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DictionaryDbHelper(private val ctx: Context)
    : SQLiteOpenHelper(ctx, "dictionary.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // 1) Create the table
        db.execSQL("""
      CREATE TABLE ${DictionaryContract.WordEntry.TABLE_NAME} (
        ${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT,
        ${DictionaryContract.WordEntry.COLUMN_WORD} TEXT UNIQUE,
        ${DictionaryContract.WordEntry.COLUMN_DEFINITION} TEXT
      )
    """.trimIndent())

        // 2) Bulk-load from CSV
        loadFromCsv(db)
    }

    private fun loadFromCsv(db: SQLiteDatabase) {
        // Open the CSV in assets
        ctx.assets.open("dictionary.csv").bufferedReader().useLines { lines ->
            lines
                .drop(1)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { line ->
                    val (word, definition) = line.split(",", limit = 2)
                    ContentValues().apply {
                        put(DictionaryContract.WordEntry.COLUMN_WORD, word.trim())
                        put(DictionaryContract.WordEntry.COLUMN_DEFINITION, definition.trim())
                    }.also { cv ->
                        db.insert(
                            DictionaryContract.WordEntry.TABLE_NAME,
                            null,
                            cv
                        )
                    }
                }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // no-op for now
    }
}