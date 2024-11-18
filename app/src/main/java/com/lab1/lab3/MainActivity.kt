package com.lab1.lab3

import android.app.ActivityManager.AppTask
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.*

import android.widget.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@Entity(tableName = "to_do_list")
data class ListItem(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val task: String,
    var status: Boolean
)

@Dao
interface ListItemDao {

    @Update
    suspend fun update(listItem: ListItem)

    @Delete
    suspend fun delete(listItem: ListItem)

    @Insert
    suspend fun insert(listItem: ListItem)

    @Query("SELECT * FROM to_do_list")
    suspend fun getAllItems(): List<ListItem>

    @Query("DELETE FROM to_do_list")
    suspend fun deleteAll()
}

@Database(
    version = 1,
    entities = [ListItem::class]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun listItemsDao(): ListItemDao
}


class MainActivity : AppCompatActivity() {

    private lateinit var listItems: MutableList<ListItem>
    private lateinit var adapter: MyListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val database = (application as DataBaseInit).database
        val listItemDao = database.listItemsDao()

        val logo_text = findViewById<TextView>(R.id.logo_text)
        val user_input = findViewById<EditText>(R.id.user_input)
        val add_button = findViewById<Button>(R.id.add_button)
        val list_view = findViewById<ListView>(R.id.list_view)

        listItems = mutableListOf()
        adapter = MyListAdapter(this, listItems, lifecycleScope, database)
        list_view.adapter = adapter

        add_button.setOnClickListener {
            val taskText = user_input.text.toString()
            if (taskText.isNotBlank()) {
                val newItem = ListItem(task = taskText, status = false)
                lifecycleScope.launch {
                    listItemDao.insert(newItem)
                    listItems.add(newItem)
                    adapter.notifyDataSetChanged()
                    user_input.setText("")
                }
                Toast.makeText(this, "Added new item: $newItem. List size: ${listItems.size}", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Введите задачу", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            listItems.addAll(listItemDao.getAllItems())
            adapter.notifyDataSetChanged()
        }

        // Добавляем обработчик нажатия на кнопку "Сжечь всё"
        val clear_all_button = findViewById<Button>(R.id.clear_all_button)

        clear_all_button.setOnClickListener {
            lifecycleScope.launch {
                listItemDao.deleteAll()
                listItems.clear()
                adapter.notifyDataSetChanged()

                Toast.makeText(this@MainActivity, "Список очищен", Toast.LENGTH_SHORT).show()
            }
        }
    }




}