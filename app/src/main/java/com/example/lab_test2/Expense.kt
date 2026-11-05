package com.example.lab_test2

data class Expense(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val imagePath: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CategoryTotal(
    val category: String,
    val totalAmount: Double
)
