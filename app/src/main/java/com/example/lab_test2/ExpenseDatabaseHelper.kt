package com.example.lab_test2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExpenseDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tripexpense.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_EXPENSES = "expenses"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IMAGE_PATH = "image_path"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_EXPENSES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_IMAGE_PATH TEXT NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        onCreate(db)
    }

    fun insertExpense(expense: Expense): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, expense.title)
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_CATEGORY, expense.category)
            put(COLUMN_IMAGE_PATH, expense.imagePath)
            put(COLUMN_TIMESTAMP, expense.timestamp)
        }
        val id = db.insert(TABLE_EXPENSES, null, values)
        db.close()
        return id
    }

    fun deleteExpense(id: Long): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_EXPENSES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }

    fun getAllExpenses(): List<Expense> {
        val list = mutableListOf<Expense>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EXPENSES,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_TIMESTAMP DESC"
        )

        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(COLUMN_TITLE)
            val amountIndex = cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)
            val categoryIndex = cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)
            val imagePathIndex = cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)
            val timestampIndex = cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)

            do {
                val expense = Expense(
                    id = cursor.getLong(idIndex),
                    title = cursor.getString(titleIndex),
                    amount = cursor.getDouble(amountIndex),
                    category = cursor.getString(categoryIndex),
                    imagePath = cursor.getString(imagePathIndex),
                    timestamp = cursor.getLong(timestampIndex)
                )
                list.add(expense)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun getTotalSpent(): Double {
        val db = readableDatabase
        var total = 0.0
        val cursor = db.rawQuery("SELECT SUM($COLUMN_AMOUNT) FROM $TABLE_EXPENSES", null)
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return total
    }

    fun getCategoryTotals(): List<CategoryTotal> {
        val list = mutableListOf<CategoryTotal>()
        val db = readableDatabase
        val query = """
            SELECT $COLUMN_CATEGORY, SUM($COLUMN_AMOUNT) AS total 
            FROM $TABLE_EXPENSES 
            GROUP BY $COLUMN_CATEGORY
            ORDER BY total DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val category = cursor.getString(0)
                val totalAmount = cursor.getDouble(1)
                list.add(CategoryTotal(category, totalAmount))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}
