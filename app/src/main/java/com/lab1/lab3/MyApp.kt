package com.lab1.lab3

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

class MyApp : Application() {

    companion object {
        lateinit var db: AppDatabase
        var currentStartDate: Long? = null
        var currentEndDate: Long? = null
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "transactions_db"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Предустановленные категории доходов и расходов
                val incomeCategories = listOf(
                    CategoryEntity(name = "Заработная плата", type = "income"),
                    CategoryEntity(name = "Премия", type = "income"),
                    CategoryEntity(name = "Подработка", type = "income"),
                    CategoryEntity(name = "Донат", type = "income"),
                    CategoryEntity(name = "Кредит", type = "income"),
                    CategoryEntity(name = "Прочее.", type = "income")

                )
                val expenseCategories = listOf(
                    CategoryEntity(name = "Продукты", type = "expense"),
                    CategoryEntity(name = "Транспорт", type = "expense"),
                    CategoryEntity(name = "Онлайн-сервисы", type = "expense"),
                    CategoryEntity(name = "Кредиты", type = "expense"),
                    CategoryEntity(name = "Обучение", type = "expense"),
                    CategoryEntity(name = "Ремонт", type = "expense"),                    CategoryEntity(name = "Продукты", type = "expense"),
                    CategoryEntity(name = "Отпуск", type = "expense"),                    CategoryEntity(name = "Продукты", type = "expense"),
                    CategoryEntity(name = "Хобби,", type = "expense")
                )
                Executors.newSingleThreadExecutor().execute {
                    MyApp.db.transactionDao().insertCategories(incomeCategories + expenseCategories)
                }
            }
        })
            .fallbackToDestructiveMigration()
            .build()
    }
}
