package com.example.lab_test2

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lab_test2.databinding.FragmentExpenseListBinding
import java.io.File

class ExpenseListFragment : Fragment() {

    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: ExpenseDatabaseHelper
    private var expenseList = mutableListOf<Expense>()
    private lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = ExpenseDatabaseHelper(requireContext())

        adapter = ExpenseAdapter(requireContext(), expenseList)
        binding.listViewExpenses.adapter = adapter

        // Short click: View full payment screenshot
        binding.listViewExpenses.setOnItemClickListener { _, _, position, _ ->
            val expense = expenseList[position]
            showFullImageDialog(expense)
        }

        // Long click: Delete expense
        binding.listViewExpenses.setOnItemLongClickListener { _, _, position, _ ->
            val expense = expenseList[position]
            showDeleteConfirmationDialog(expense)
            true
        }

        refreshExpenses()
    }

    override fun onResume() {
        super.onResume()
        refreshExpenses()
    }

    fun refreshExpenses() {
        if (_binding == null || !isAdded || !::adapter.isInitialized || !::dbHelper.isInitialized) return
        expenseList.clear()
        expenseList.addAll(dbHelper.getAllExpenses())
        adapter.notifyDataSetChanged()

        if (expenseList.isEmpty()) {
            binding.txtEmptyState.visibility = View.VISIBLE
            binding.listViewExpenses.visibility = View.GONE
        } else {
            binding.txtEmptyState.visibility = View.GONE
            binding.listViewExpenses.visibility = View.VISIBLE
        }
    }

    private fun showFullImageDialog(expense: Expense) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_image_preview, null)
        val txtDialogTitle = dialogView.findViewById<TextView>(R.id.txt_dialog_title)
        val imgFullPreview = dialogView.findViewById<ImageView>(R.id.img_full_preview)
        val btnClose = dialogView.findViewById<View>(R.id.btn_close_dialog)

        txtDialogTitle.text = expense.title + " (" + expense.category + ")"

        if (expense.imagePath.isNotEmpty() && File(expense.imagePath).exists()) {
            val bitmap = BitmapFactory.decodeFile(expense.imagePath)
            imgFullPreview.setImageBitmap(bitmap)
        } else {
            imgFullPreview.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete '${expense.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                val rows = dbHelper.deleteExpense(expense.id)
                if (rows > 0) {
                    Toast.makeText(requireContext(), "Expense deleted", Toast.LENGTH_SHORT).show()
                    refreshExpenses()
                    (activity as? MainActivity)?.checkBudgetAlert()
                    (activity as? MainActivity)?.refreshAllPages()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
