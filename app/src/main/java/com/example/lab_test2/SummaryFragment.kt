package com.example.lab_test2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lab_test2.databinding.FragmentSummaryBinding
import java.util.Locale

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: ExpenseDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = ExpenseDatabaseHelper(requireContext())

        binding.btnSaveBudget.setOnClickListener {
            val budgetStr = binding.edtBudget.text.toString().trim()
            val budget = budgetStr.toDoubleOrNull()
            if (budget != null && budget >= 0) {
                saveBudget(budget)
                Toast.makeText(requireContext(), "Budget set to ₹${String.format(Locale.getDefault(), "%.2f", budget)}", Toast.LENGTH_SHORT).show()
                refreshSummary()
                (activity as? MainActivity)?.checkBudgetAlert()
            } else {
                binding.edtBudget.error = "Enter a valid budget"
            }
        }

        refreshSummary()
    }

    override fun onResume() {
        super.onResume()
        refreshSummary()
    }

    fun refreshSummary() {
        if (_binding == null || !isAdded || !::dbHelper.isInitialized) return
        val ctx = context ?: return

        val sharedPref = ctx.getSharedPreferences("TripExpensePrefs", Context.MODE_PRIVATE)
        val budget = sharedPref.getFloat("trip_budget", -1f)
        val totalSpent = dbHelper.getTotalSpent()

        // Total Spent
        binding.txtTotalSpent.text = String.format(Locale.getDefault(), "₹%.2f", totalSpent)

        // Budget Status
        if (budget >= 0) {
            binding.edtBudget.setText(String.format(Locale.getDefault(), "%.2f", budget))
            val status = if (totalSpent > budget) {
                String.format(Locale.getDefault(), "Budget: ₹%.2f | WARNING: Exceeded by ₹%.2f!", budget, totalSpent - budget)
            } else {
                String.format(Locale.getDefault(), "Budget: ₹%.2f | Remaining: ₹%.2f", budget, budget - totalSpent)
            }
            binding.txtBudgetStatus.text = status
            binding.txtBudgetStatus.setTextColor(
                if (totalSpent > budget) 0xFFD32F2F.toInt() else 0xFF2E7D32.toInt()
            )
        } else {
            binding.txtBudgetStatus.text = "Budget not set"
            binding.txtBudgetStatus.setTextColor(0xFF757575.toInt())
        }

        // Category Totals using SQL SUM + GROUP BY
        val categoryTotals = dbHelper.getCategoryTotals()
        binding.containerCategoryTotals.removeAllViews()

        if (categoryTotals.isEmpty()) {
            binding.txtNoCategoryData.visibility = View.VISIBLE
        } else {
            binding.txtNoCategoryData.visibility = View.GONE
            for (catTotal in categoryTotals) {
                val itemView = LayoutInflater.from(ctx).inflate(
                    R.layout.item_category_summary,
                    binding.containerCategoryTotals,
                    false
                )
                val txtCategoryName = itemView.findViewById<TextView>(R.id.txt_category_name)
                val txtCategoryTotal = itemView.findViewById<TextView>(R.id.txt_category_total)

                txtCategoryName.text = catTotal.category
                txtCategoryTotal.text = String.format(Locale.getDefault(), "₹%.2f", catTotal.totalAmount)

                binding.containerCategoryTotals.addView(itemView)
            }
        }
    }

    private fun saveBudget(budget: Double) {
        val ctx = context ?: return
        val sharedPref = ctx.getSharedPreferences("TripExpensePrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putFloat("trip_budget", budget.toFloat()).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
