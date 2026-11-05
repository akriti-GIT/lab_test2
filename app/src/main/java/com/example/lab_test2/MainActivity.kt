package com.example.lab_test2

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lab_test2.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var dbHelper: ExpenseDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = ExpenseDatabaseHelper(this)

        adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Ensure page offscreen limit keeps fragments in memory for direct refresh
        binding.viewPager.offscreenPageLimit = 2

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Expenses"
                1 -> "Add Expense"
                2 -> "Summary"
                else -> ""
            }
        }.attach()

        checkBudgetAlert()
    }

    fun switchToTab(position: Int) {
        binding.viewPager.currentItem = position
    }

    fun refreshAllPages() {
        adapter.expenseListFragment.refreshExpenses()
        adapter.summaryFragment.refreshSummary()
    }

    fun checkBudgetAlert() {
        val sharedPref = getSharedPreferences("TripExpensePrefs", Context.MODE_PRIVATE)
        val budget = sharedPref.getFloat("trip_budget", -1f)
        if (budget >= 0) {
            val totalSpent = dbHelper.getTotalSpent()
            if (totalSpent > budget) {
                val message = String.format(
                    Locale.getDefault(),
                    "⚠️ BUDGET ALERT: Total spent (₹%.2f) has crossed your trip budget (₹%.2f)!",
                    totalSpent,
                    budget
                )
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
