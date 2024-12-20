package com.lab1.lab3

import androidx.room.*

@Dao
interface TransactionDao {

    // TRANSACTIONS
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByPeriod(startDate: Long, endDate: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun getTransactionById(id: Int): TransactionEntity?

    @Insert
    fun insertTransaction(transaction: TransactionEntity)

    @Update
    fun updateTransaction(transaction: TransactionEntity)

    @Delete
    fun deleteTransaction(transaction: TransactionEntity) // Добавленный метод

    // CATEGORIES
    @Insert
    fun insertCategories(categories: List<CategoryEntity>)

    @Insert
    fun insertCategory(category: CategoryEntity)

    @Update
    fun updateCategory(category: CategoryEntity)

    @Delete
    fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Int): CategoryEntity?
}
