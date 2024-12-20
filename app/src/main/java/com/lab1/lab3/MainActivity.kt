package com.lab1.lab3

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: TransactionsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: ImageButton
    private lateinit var btnReports: ImageButton
    private lateinit var btnCategories: ImageButton

    private var categories: List<CategoryEntity> = emptyList()
    private var transactions: List<TransactionEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = TransactionsAdapter(emptyList()) { transaction ->
            // При клике редактируем транзакцию
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("transaction_id", transaction.id)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerViewTransactions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabAdd = findViewById(R.id.fabAddTransaction)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        btnReports = findViewById(R.id.btnReports)
        btnReports.setOnClickListener {
            showReportDialog()
        }

        btnCategories = findViewById(R.id.btnCategories)
        btnCategories.setOnClickListener {
            manageCategoriesDialog()
        }

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        Thread {
            val dao = MyApp.db.transactionDao()
            categories = dao.getAllCategories()
            // Проверяем, есть ли выбранный период
            val tr = if (MyApp.currentStartDate != null && MyApp.currentEndDate != null) {
                dao.getTransactionsByPeriod(MyApp.currentStartDate!!, MyApp.currentEndDate!!)
            } else {
                dao.getAllTransactions()
            }
            transactions = tr
            runOnUiThread {
                adapter.updateData(transactions, categories)
                // Добавлено: отображение отчёта, если установлен фильтр
                if (MyApp.currentStartDate != null && MyApp.currentEndDate != null) {
                    val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
                    val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
                    val total = totalIncome - totalExpense
                    showSummaryDialog(totalIncome, totalExpense, total)
                }
            }
        }.start()
    }

    private fun showReportDialog() {
        val options = arrayOf(
            "На конкретную дату",
            "На конкретный месяц",
            "На конкретный год",
            "Произвольный период",
            "Сбросить фильтр"
        )
        AlertDialog.Builder(this)
            .setTitle("Выберите период")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickExactDate()
                    1 -> pickMonth()
                    2 -> pickYear()
                    3 -> pickCustomPeriod()
                    4 -> {
                        MyApp.currentStartDate = null
                        MyApp.currentEndDate = null
                        loadData()
                    }
                }
            }
            .show()
    }

    private fun pickExactDate() {
        val calendar = Calendar.getInstance()
        val dp = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val start = Calendar.getInstance().apply { set(year, month, dayOfMonth, 0, 0, 0) }.timeInMillis
            val end = Calendar.getInstance().apply { set(year, month, dayOfMonth, 23, 59, 59) }.timeInMillis
            MyApp.currentStartDate = start
            MyApp.currentEndDate = end
            loadData()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dp.show()
    }

    private fun pickMonth() {
        val calendar = Calendar.getInstance()
        val dp = DatePickerDialog(this, { _, year, month, _ ->
            val start = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0) }.timeInMillis
            val daysInMonth = Calendar.getInstance().apply { set(year, month, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
            val end = Calendar.getInstance().apply { set(year, month, daysInMonth, 23, 59, 59) }.timeInMillis
            MyApp.currentStartDate = start
            MyApp.currentEndDate = end
            loadData()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        // Скрываем выбор дня
        dp.datePicker.findViewById<View>(resources.getIdentifier("android:id/day", null, null))?.visibility = View.GONE
        dp.show()
    }

    private fun pickYear() {
        val calendar = Calendar.getInstance()
        val dp = DatePickerDialog(this, { _, year, _, _ ->
            val start = Calendar.getInstance().apply { set(year, 0, 1, 0, 0, 0) }.timeInMillis
            val end = Calendar.getInstance().apply { set(year, 11, 31, 23, 59, 59) }.timeInMillis
            MyApp.currentStartDate = start
            MyApp.currentEndDate = end
            loadData()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        // Скрываем выбор месяца и дня
        dp.datePicker.findViewById<View>(resources.getIdentifier("android:id/day", null, null))?.visibility = View.GONE
        dp.datePicker.findViewById<View>(resources.getIdentifier("android:id/month", null, null))?.visibility = View.GONE
        dp.show()
    }

    private fun pickCustomPeriod() {
        Toast.makeText(this, "Выберите начальную дату", Toast.LENGTH_SHORT).show()
        val calendar = Calendar.getInstance()
        val dpStart = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val start = Calendar.getInstance().apply { set(year, month, dayOfMonth, 0, 0, 0) }.timeInMillis
            Toast.makeText(this, "Выберите конечную дату", Toast.LENGTH_SHORT).show()
            val dpEnd = DatePickerDialog(this, { _, ye, mo, da ->
                val end = Calendar.getInstance().apply { set(ye, mo, da, 23, 59, 59) }.timeInMillis
                MyApp.currentStartDate = start
                MyApp.currentEndDate = end
                loadData()
            }, year, month, dayOfMonth)
            dpEnd.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dpStart.show()
    }

    private fun manageCategoriesDialog() {
        Thread {
            val dao = MyApp.db.transactionDao()
            val cats = dao.getAllCategories()
            runOnUiThread {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Категории")
                val arr = cats.map { "${it.name} (${it.type})" }.toTypedArray()
                builder.setItems(arr) { _, which ->
                    val selectedCat = cats[which]
                    // Диалог для редактирования или удаления
                    editCategoryDialog(selectedCat)
                }
                builder.setPositiveButton("Добавить категорию") { _, _ ->
                    addCategoryDialog()
                }
                builder.setNegativeButton("Закрыть", null)
                builder.show()
            }
        }.start()
    }

    private fun addCategoryDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 50, 50, 50)
        val etName = EditText(this)
        etName.hint = "Название категории"
        val etType = EditText(this)
        etType.hint = "Тип: income или expense"
        layout.addView(etName)
        layout.addView(etType)

        AlertDialog.Builder(this)
            .setTitle("Новая категория")
            .setView(layout)
            .setPositiveButton("Добавить") { _, _ ->
                val name = etName.text.toString()
                val type = etType.text.toString()
                if (name.isNotBlank() && (type == "income" || type == "expense")) {
                    Thread {
                        MyApp.db.transactionDao().insertCategory(CategoryEntity(name = name, type = type))
                        runOnUiThread {
                            Toast.makeText(this, "Добавлено", Toast.LENGTH_SHORT).show()
                            loadData() // Обновляем список категорий
                        }
                    }.start()
                } else {
                    Toast.makeText(this, "Некорректные данные", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun editCategoryDialog(cat: CategoryEntity) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 50, 50, 50)
        val etName = EditText(this)
        etName.setText(cat.name)
        val etType = EditText(this)
        etType.setText(cat.type)
        layout.addView(etName)
        layout.addView(etType)

        AlertDialog.Builder(this)
            .setTitle("Редактировать категорию")
            .setView(layout)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = etName.text.toString()
                val type = etType.text.toString()
                if (name.isNotBlank() && (type == "income" || type == "expense")) {
                    val updated = cat.copy(name = name, type = type)
                    Thread {
                        MyApp.db.transactionDao().updateCategory(updated)
                        runOnUiThread {
                            Toast.makeText(this, "Обновлено", Toast.LENGTH_SHORT).show()
                            loadData() // Обновляем список категорий
                        }
                    }.start()
                } else {
                    Toast.makeText(this, "Некорректные данные", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Удалить") { _, _ ->
                Thread {
                    MyApp.db.transactionDao().deleteCategory(cat)
                    runOnUiThread {
                        Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show()
                        loadData() // Обновляем список категорий
                    }
                }.start()
            }
            .setNeutralButton("Отмена", null)
            .show()
    }

    // Добавленный метод для отображения итогового отчёта
    private fun showSummaryDialog(income: Double, expense: Double, total: Double) {
        val message = "Доход: $income\nРасход: $expense\nИтого: $total"
        AlertDialog.Builder(this)
            .setTitle("Отчёт за выбранный период")
            .setMessage(message)
            .setPositiveButton("ОК", null) // Диалог закрывается по нажатию "ОК"
            .show()
    }
}
