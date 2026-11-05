package com.example.lab_test2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripExpenseUnitTest {

    @Test
    fun testExpenseModelCreation() {
        val expense = Expense(
            id = 1,
            title = "Flight Ticket",
            amount = 4500.0,
            category = "Travel",
            imagePath = "/storage/emulated/0/proof1.jpg"
        )
        assertEquals(1L, expense.id)
        assertEquals("Flight Ticket", expense.title)
        assertEquals(4500.0, expense.amount, 0.01)
        assertEquals("Travel", expense.category)
        assertEquals("/storage/emulated/0/proof1.jpg", expense.imagePath)
    }

    @Test
    fun testCategoryTotalCalculation() {
        val expenses = listOf(
            Expense(1, "Lunch", 350.0, "Food", "img1.jpg"),
            Expense(2, "Dinner", 650.0, "Food", "img2.jpg"),
            Expense(3, "Taxi", 500.0, "Travel", "img3.jpg")
        )

        val totalSpent = expenses.sumOf { it.amount }
        assertEquals(1500.0, totalSpent, 0.01)

        val foodTotal = expenses.filter { it.category == "Food" }.sumOf { it.amount }
        assertEquals(1000.0, foodTotal, 0.01)

        val travelTotal = expenses.filter { it.category == "Travel" }.sumOf { it.amount }
        assertEquals(500.0, travelTotal, 0.01)
    }

    @Test
    fun testBudgetAlertCondition() {
        val tripBudget = 1000.0
        val totalSpent = 1200.0

        val isBudgetCrossed = totalSpent > tripBudget
        assertTrue(isBudgetCrossed)
    }
}
