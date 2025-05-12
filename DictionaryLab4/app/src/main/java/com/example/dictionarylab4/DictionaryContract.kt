package com.example.dictionarylab4

import android.provider.BaseColumns

object DictionaryContract {
    object WordEntry : BaseColumns {
        const val TABLE_NAME       = "dictionary"
        const val COLUMN_WORD      = "word"
        const val COLUMN_DEFINITION = "definition"
    }
}