package com.example.lab_test2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import java.util.Locale

class ExpenseAdapter(
    context: Context,
    private val expenses: List<Expense>
) : ArrayAdapter<Expense>(context, 0, expenses) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_expense, parent, false
        )

        val expense = getItem(position) ?: return view

        val imgThumbnail = view.findViewById<ImageView>(R.id.img_thumbnail)
        val txtTitle = view.findViewById<TextView>(R.id.txt_expense_title)
        val txtCategory = view.findViewById<TextView>(R.id.txt_expense_category)
        val txtAmount = view.findViewById<TextView>(R.id.txt_expense_amount)

        txtTitle.text = expense.title
        txtCategory.text = expense.category
        txtAmount.text = String.format(Locale.getDefault(), "₹%.2f", expense.amount)

        if (expense.imagePath.isNotEmpty()) {
            val bitmap = decodeSampledBitmapFromFile(expense.imagePath, 120, 120)
            if (bitmap != null) {
                imgThumbnail.setImageBitmap(bitmap)
            } else {
                imgThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            imgThumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        return view
    }

    private fun decodeSampledBitmapFromFile(pathName: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val file = File(pathName)
        if (!file.exists()) return null

        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(pathName, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            BitmapFactory.decodeFile(pathName, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
