package com.example.lab_test2

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lab_test2.databinding.FragmentAddExpenseBinding
import java.io.File
import java.io.FileOutputStream

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: ExpenseDatabaseHelper
    private var selectedImagePath: String = ""

    private val categories = arrayOf("Food", "Travel", "Stay", "Shopping")

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = saveImageToInternalStorage(it)
            if (savedPath != null) {
                selectedImagePath = savedPath
                val bitmap = BitmapFactory.decodeFile(savedPath)
                binding.imgPreview.setImageBitmap(bitmap)
            } else {
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        selectImageLauncher.launch("image/*")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = ExpenseDatabaseHelper(requireContext())

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        binding.spinnerCategory.adapter = spinnerAdapter

        binding.btnSelectImage.setOnClickListener {
            checkPermissionAndOpenPicker()
        }

        binding.btnSampleBmtc.setOnClickListener {
            attachBmtcSampleProof()
        }

        binding.btnSaveExpense.setOnClickListener {
            saveExpense()
        }
    }

    private fun checkPermissionAndOpenPicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            selectImageLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun attachBmtcSampleProof() {
        val path = generateBmtcSampleImage()
        selectedImagePath = path
        val bitmap = BitmapFactory.decodeFile(path)
        binding.imgPreview.setImageBitmap(bitmap)

        binding.edtTitle.setText("BMTC Bus Ticket")
        binding.edtAmount.setText("6")
        binding.spinnerCategory.setSelection(1) // Travel

        Toast.makeText(requireContext(), "BMTC (₹6) Payment Proof attached!", Toast.LENGTH_SHORT).show()
    }

    private fun generateBmtcSampleImage(): String {
        val bitmap = Bitmap.createBitmap(600, 750, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Avatar Circle
        paint.color = Color.parseColor("#C0392B")
        canvas.drawCircle(300f, 90f, 45f, paint)

        paint.color = Color.WHITE
        paint.textSize = 45f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        canvas.drawText("B", 300f, 106f, paint)

        // "To BMTC"
        paint.color = Color.BLACK
        paint.textSize = 34f
        canvas.drawText("To BMTC", 300f, 185f, paint)

        // "₹6"
        paint.textSize = 70f
        canvas.drawText("₹6", 300f, 275f, paint)

        // Ref ID
        paint.color = Color.parseColor("#555555")
        paint.textSize = 22f
        canvas.drawText("Payment for 26j29E1117866mdR", 300f, 335f, paint)

        // Status
        paint.color = Color.parseColor("#2E7D32")
        paint.textSize = 28f
        canvas.drawText("✓ Completed", 300f, 395f, paint)

        // Date
        paint.color = Color.GRAY
        paint.textSize = 22f
        canvas.drawText("29 Oct 2026, 9:41am", 300f, 440f, paint)

        // Bank details
        paint.color = Color.BLACK
        paint.textSize = 24f
        canvas.drawText("State Bank of India 1961", 300f, 520f, paint)

        val file = File(requireContext().filesDir, "sample_bmtc_proof.jpg")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos.close()
        return file.absolutePath
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().filesDir, "expense_img_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveExpense() {
        val title = binding.edtTitle.text.toString().trim()
        val amountStr = binding.edtAmount.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem?.toString() ?: "Food"

        if (title.isEmpty()) {
            binding.edtTitle.error = "Please enter title"
            return
        }

        if (amountStr.isEmpty()) {
            binding.edtAmount.error = "Please enter amount"
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.edtAmount.error = "Please enter a valid amount"
            return
        }

        if (selectedImagePath.isEmpty()) {
            Toast.makeText(requireContext(), "Please attach a payment screenshot proof", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            title = title,
            amount = amount,
            category = category,
            imagePath = selectedImagePath
        )

        val id = dbHelper.insertExpense(expense)
        if (id > 0) {
            Toast.makeText(requireContext(), "Expense saved successfully!", Toast.LENGTH_SHORT).show()
            
            // Reset fields
            binding.edtTitle.text?.clear()
            binding.edtAmount.text?.clear()
            binding.spinnerCategory.setSelection(0)
            binding.imgPreview.setImageResource(android.R.drawable.ic_menu_gallery)
            selectedImagePath = ""

            // Notify activity to check budget alert and refresh screens
            (activity as? MainActivity)?.checkBudgetAlert()
            (activity as? MainActivity)?.refreshAllPages()
            (activity as? MainActivity)?.switchToTab(0)
        } else {
            Toast.makeText(requireContext(), "Failed to save expense", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
