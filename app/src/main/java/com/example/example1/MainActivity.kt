package com.example.travelpackinglist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


// SCREEN ONE - Main Activity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"

        // Four parallel arrays to store packing list data
        val itemNames = mutableListOf("T-Shirts and pants", "Toothbrush", "Shoes", "Passport")
        val itemCategories = mutableListOf("Clothing", "Toiletries", "Clothing", "Documents")
        val itemQuantities = mutableListOf(5, 1, 2, 1)
        val itemComments = mutableListOf(
            "Comfortable for travel",
            "Essential for hygiene",
            "Walking and smart casual",
            "Don't forget this!"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Build Screen One layout programmatically
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Travel Packing List"
            textSize = 22f
            setPadding(0, 0, 0, 40)
        }

        val btnAdd = Button(this).apply { text = "Add to Packing List" }
        val btnView = Button(this).apply { text = "View Packing List" }
        val btnExit = Button(this).apply { text = "Exit" }

        layout.addView(title)
        layout.addView(btnAdd)
        layout.addView(btnView)
        layout.addView(btnExit)
        setContentView(layout)

        Log.d(TAG, "MainActivity created. Items in list: ${itemNames.size}")

        btnAdd.setOnClickListener {
            Log.d(TAG, "Add button clicked")
            showAddItemDialog()
        }

        btnView.setOnClickListener {
            Log.d(TAG, "View List button clicked")
            startActivity(Intent(this, ListActivity::class.java))
        }

        btnExit.setOnClickListener {
            Log.d(TAG, "Exit button clicked")
            finish()
        }
    }

    // Shows a dialog asking the user to enter item details
    private fun showAddItemDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val etName     = EditText(this).apply { hint = "Item Name" }
        val etCategory = EditText(this).apply { hint = "Category" }
        val etQuantity = EditText(this).apply { hint = "Quantity"; inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        val etComments = EditText(this).apply { hint = "Comments" }

        dialogLayout.addView(etName)
        dialogLayout.addView(etCategory)
        dialogLayout.addView(etQuantity)
        dialogLayout.addView(etComments)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Packing Item")
            .setView(dialogLayout)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()

        dialog.show()

        // Override positive button to validate before closing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = etName.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val quantityStr = etQuantity.text.toString().trim()
            val comments = etComments.text.toString().trim()

            if (validateInput(name, category, quantityStr, comments)) {
                addItem(name, category, quantityStr.toInt(), comments)
                dialog.dismiss()
            }
        }
    }

    // Validates input and shows error messages for bad input
    private fun validateInput(name: String, category: String, quantityStr: String, comments: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Error: Item name cannot be empty.", Toast.LENGTH_SHORT).show()
            Log.w("Validation", "Name is empty")
            return false
        }
        if (category.isEmpty()) {
            Toast.makeText(this, "Error: Category cannot be empty.", Toast.LENGTH_SHORT).show()
            Log.w("Validation", "Category is empty")
            return false
        }
        val qty = quantityStr.toIntOrNull()
        if (qty == null || qty <= 0) {
            Toast.makeText(this, "Error: Quantity must be a positive number.", Toast.LENGTH_SHORT).show()
            Log.w("Validation", "Invalid quantity: $quantityStr")
            return false
        }
        if (comments.isEmpty()) {
            Toast.makeText(this, "Error: Comments cannot be empty.", Toast.LENGTH_SHORT).show()
            Log.w("Validation", "Comments are empty")
            return false
        }
        return true
    }

    // Adds the new item to all four parallel arrays
    private fun addItem(name: String, category: String, quantity: Int, comments: String) {
        itemNames.add(name)
        itemCategories.add(category)
        itemQuantities.add(quantity)
        itemComments.add(comments)
        Log.i("MainActivity", "Item added: $name | $category | $quantity | $comments")
        Toast.makeText(this, "'$name' added!", Toast.LENGTH_SHORT).show()
    }
}


// SCREEN TWO 

class ListActivity : AppCompatActivity() {

    private val TAG = "ListActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Packing List"
            textSize = 22f
            setPadding(0, 0, 0, 20)
        }

        val btnShowAll = Button(this).apply { text = "Show All Items" }
        val btnShowHighQty = Button(this).apply { text = "Show Items with Qty ≥ 2" }
        val btnBack = Button(this).apply { text = "Back to Main Screen" }

        val tvResults = TextView(this).apply {
            textSize = 14f
            setPadding(0, 20, 0, 20)
        }

        layout.addView(title)
        layout.addView(btnShowAll)
        layout.addView(btnShowHighQty)

        // Wrap results in a ScrollView in case the list is long
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        scrollView.addView(tvResults)
        layout.addView(scrollView)
        layout.addView(btnBack)
        setContentView(layout)

        Log.d(TAG, "ListActivity created")

        btnShowAll.setOnClickListener {
            Log.d(TAG, "Show All clicked")
            tvResults.text = buildFullList()
        }

        btnShowHighQty.setOnClickListener {
            Log.d(TAG, "Show High Qty clicked")
            tvResults.text = buildHighQuantityList()
        }

        btnBack.setOnClickListener {
            Log.d(TAG, "Back clicked")
            finish()
        }
    }

    // Loops through all parallel arrays and displays every item
    private fun buildFullList(): String {
        if (MainActivity.itemNames.isEmpty()) return "No items in your packing list."

        val sb = StringBuilder("=== All Items ===\n\n")
        for (i in MainActivity.itemNames.indices) {
            sb.appendLine("${i + 1}. ${MainActivity.itemNames[i]}")
            sb.appendLine("   Category : ${MainActivity.itemCategories[i]}")
            sb.appendLine("   Quantity : ${MainActivity.itemQuantities[i]}")
            sb.appendLine("   Comments : ${MainActivity.itemComments[i]}\n")
        }
        return sb.toString()
    }

    // Loops through parallel arrays and only shows items with quantity >= 2
    private fun buildHighQuantityList(): String {
        val sb = StringBuilder("=== Items with Qty ≥ 2 ===\n\n")
        var found = false

        for (i in MainActivity.itemNames.indices) {
            if (MainActivity.itemQuantities[i] >= 2) {
                sb.appendLine("• ${MainActivity.itemNames[i]} (Qty: ${MainActivity.itemQuantities[i]})")
                Log.d(TAG, "High qty item: ${MainActivity.itemNames[i]}")
                found = true
            }
        }

        return if (found) sb.toString() else "No items with quantity of 2 or more."
    }
}
