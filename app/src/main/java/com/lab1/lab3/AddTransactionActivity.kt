package com.lab1.lab3

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var radioIncome: RadioButton
    private lateinit var radioExpense: RadioButton
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnAdd: Button

    private var transactionId: Int? = null
    private var categories: List<CategoryEntity> = emptyList()
    private var selectedCategoryId: Int? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        etAmount = findViewById(R.id.etAmount)
        radioIncome = findViewById(R.id.radioIncome)
        radioExpense = findViewById(R.id.radioExpense)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnAdd = findViewById(R.id.btnAddTransaction)

        transactionId = intent.getIntExtra("transaction_id", -1)
        if (transactionId != null && transactionId != -1) {
            isEditMode = true
        }

        loadCategories {
            if (isEditMode) {
                loadTransaction(transactionId!!)
                btnAdd.text = "Сохранить изменения"
            } else {
                btnAdd.text = "Добавить"
            }
        }

        btnAdd.setOnClickListener {
            val amountStr = etAmount.text.toString()
            val amount = amountStr.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val type = if (radioIncome.isChecked) "income" else "expense"
            val catId = selectedCategoryId ?: run {
                Toast.makeText(this, "Выберите категорию", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount == 0.0) {
                // Обработка транзакций с суммой 0
                if (isEditMode) {
                    // В режиме редактирования удаляем транзакцию
                    Thread {
                        val dao = MyApp.db.transactionDao()
                        val existing = dao.getTransactionById(transactionId!!)
                        if (existing != null) {
                            dao.deleteTransaction(existing)
                            runOnUiThread {
                                Toast.makeText(this, "Транзакция с нулевой суммой удалена", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "Ошибка: транзакция не найдена", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.start()
                } else {
                    // В режиме добавления не сохраняем транзакцию с суммой 0
                    Toast.makeText(this, "Транзакция с нулевой суммой не добавляется", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@setOnClickListener
            }

            if (!isEditMode) {
                // Добавление новой транзакции
                val transaction = TransactionEntity(
                    amount = amount,
                    date = System.currentTimeMillis(),
                    type = type,
                    categoryId = catId
                )
                Thread {
                    MyApp.db.transactionDao().insertTransaction(transaction)
                    runOnUiThread {
                        Toast.makeText(this, "Транзакция добавлена", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }.start()
            } else {
                // Редактирование существующей транзакции
                Thread {
                    val dao = MyApp.db.transactionDao()
                    val existing = dao.getTransactionById(transactionId!!)
                    if (existing != null) {
                        val updated = existing.copy(
                            amount = amount,
                            type = type,
                            categoryId = catId
                        )
                        dao.updateTransaction(updated)
                        runOnUiThread {
                            Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Ошибка: транзакция не найдена", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            }
        }
    }

    private fun loadCategories(onLoaded: () -> Unit) {
        Thread {
            val dao = MyApp.db.transactionDao()
            categories = dao.getAllCategories()
            runOnUiThread {
                val categoryNames = categories.map { "${it.name} (${it.type})" }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter
                spinnerCategory.setSelection(0)
                selectedCategoryId = categories.firstOrNull()?.id
                spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedCategoryId = categories[position].id
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
                onLoaded()
            }
        }.start()
    }

    private fun loadTransaction(id: Int) {
        Thread {
            val dao = MyApp.db.transactionDao()
            val tr = dao.getTransactionById(id)
            if (tr != null) {
                val cat = dao.getCategoryById(tr.categoryId)
                runOnUiThread {
                    etAmount.setText(tr.amount.toString())
                    if (tr.type == "income") radioIncome.isChecked = true else radioExpense.isChecked = true
                    if (cat != null) {
                        val index = categories.indexOfFirst { it.id == cat.id }
                        if (index >= 0) {
                            spinnerCategory.setSelection(index)
                            selectedCategoryId = cat.id
                        }
                    }
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Транзакция не найдена", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
