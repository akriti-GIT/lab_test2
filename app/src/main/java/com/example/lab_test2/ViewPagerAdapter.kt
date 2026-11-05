package com.example.lab_test2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    val expenseListFragment = ExpenseListFragment()
    val addExpenseFragment = AddExpenseFragment()
    val summaryFragment = SummaryFragment()

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> expenseListFragment
            1 -> addExpenseFragment
            2 -> summaryFragment
            else -> expenseListFragment
        }
    }
}
