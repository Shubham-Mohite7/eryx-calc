package com.liquidglass.calculator.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class HistoryItem(
    val id: Long,
    val expression: String,
    val result: String,
    val timestamp: Long,
    val mode: String,
    val isPinned: Boolean,
    val isFavorite: Boolean
)

data class GraphFunctionItem(
    val id: Long,
    val expression: String,
    val colorHex: Long,
    val isVisible: Boolean
)

enum class HistoryFilter(val label: String) {
    ALL("All Time"),
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    PINNED("Pinned"),
    FAVORITES("Favorites")
}

class HistoryDatabase(context: Context) :
    SQLiteOpenHelper(context, "calculator_history.db", null, 1) {

    private val _historyFlow = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyFlow: Flow<List<HistoryItem>> = _historyFlow.asStateFlow()

    private val _graphFunctionsFlow = MutableStateFlow<List<GraphFunctionItem>>(emptyList())
    val graphFunctionsFlow: Flow<List<GraphFunctionItem>> = _graphFunctionsFlow.asStateFlow()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                expression TEXT NOT NULL,
                result TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                mode TEXT NOT NULL,
                is_pinned INTEGER NOT NULL DEFAULT 0,
                is_favorite INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_history_time ON history(timestamp DESC)")

        db.execSQL(
            """
            CREATE TABLE graph_functions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                expression TEXT NOT NULL,
                color_hex INTEGER NOT NULL,
                is_visible INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
        // Seed one clean default graph function matching the design
        val defaultFn = ContentValues().apply {
            put("expression", "sin(x)")
            put("color_hex", 0xFF22252A)
            put("is_visible", 1)
        }
        db.insert("graph_functions", null, defaultFn)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    suspend fun loadInitial() = withContext(Dispatchers.IO) {
        _historyFlow.value = queryHistory()
        _graphFunctionsFlow.value = queryGraphFunctions()
    }

    suspend fun insertHistory(
        expression: String,
        result: String,
        mode: CalculatorMode
    ) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("expression", expression)
            put("result", result)
            put("timestamp", System.currentTimeMillis())
            put("mode", mode.name)
            put("is_pinned", 0)
            put("is_favorite", 0)
        }
        db.insert("history", null, values)
        _historyFlow.value = queryHistory()
    }

    suspend fun togglePin(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.execSQL(
            "UPDATE history SET is_pinned = CASE WHEN is_pinned = 1 THEN 0 ELSE 1 END WHERE id = ?",
            arrayOf(id)
        )
        _historyFlow.value = queryHistory()
    }

    suspend fun toggleFavorite(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.execSQL(
            "UPDATE history SET is_favorite = CASE WHEN is_favorite = 1 THEN 0 ELSE 1 END WHERE id = ?",
            arrayOf(id)
        )
        _historyFlow.value = queryHistory()
    }

    suspend fun deleteHistory(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.delete("history", "id = ?", arrayOf(id.toString()))
        _historyFlow.value = queryHistory()
    }

    suspend fun clearAllHistory(keepPinned: Boolean = false) = withContext(Dispatchers.IO) {
        if (keepPinned) {
            writableDatabase.delete("history", "is_pinned = 0", null)
        } else {
            writableDatabase.delete("history", null, null)
        }
        _historyFlow.value = queryHistory()
    }

    suspend fun addGraphFunction(expression: String, colorHex: Long) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("expression", expression)
            put("color_hex", colorHex)
            put("is_visible", 1)
        }
        writableDatabase.insert("graph_functions", null, values)
        _graphFunctionsFlow.value = queryGraphFunctions()
    }

    suspend fun updateGraphFunction(id: Long, expression: String) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply { put("expression", expression) }
        writableDatabase.update("graph_functions", values, "id = ?", arrayOf(id.toString()))
        _graphFunctionsFlow.value = queryGraphFunctions()
    }

    suspend fun toggleGraphFunctionVisibility(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.execSQL(
            "UPDATE graph_functions SET is_visible = CASE WHEN is_visible = 1 THEN 0 ELSE 1 END WHERE id = ?",
            arrayOf(id)
        )
        _graphFunctionsFlow.value = queryGraphFunctions()
    }

    suspend fun deleteGraphFunction(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.delete("graph_functions", "id = ?", arrayOf(id.toString()))
        _graphFunctionsFlow.value = queryGraphFunctions()
    }

    private fun queryHistory(): List<HistoryItem> {
        val list = mutableListOf<HistoryItem>()
        readableDatabase.rawQuery(
            "SELECT id, expression, result, timestamp, mode, is_pinned, is_favorite FROM history ORDER BY is_pinned DESC, timestamp DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    HistoryItem(
                        id = cursor.getLong(0),
                        expression = cursor.getString(1),
                        result = cursor.getString(2),
                        timestamp = cursor.getLong(3),
                        mode = cursor.getString(4),
                        isPinned = cursor.getInt(5) == 1,
                        isFavorite = cursor.getInt(6) == 1
                    )
                )
            }
        }
        return list
    }

    private fun queryGraphFunctions(): List<GraphFunctionItem> {
        val list = mutableListOf<GraphFunctionItem>()
        readableDatabase.rawQuery(
            "SELECT id, expression, color_hex, is_visible FROM graph_functions ORDER BY id ASC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    GraphFunctionItem(
                        id = cursor.getLong(0),
                        expression = cursor.getString(1),
                        colorHex = cursor.getLong(2),
                        isVisible = cursor.getInt(3) == 1
                    )
                )
            }
        }
        return list
    }
}
