package com.lab1.lab3

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class MyListAdapter(
    context: Activity,
    private val dataSource: MutableList<ListItem>,
    private val coroutineScope: CoroutineScope,
    private val database: AppDatabase
): ArrayAdapter<ListItem>(context, R.layout.list_item, dataSource) {  // Передаем dataSource в конструктор суперкласса

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): ListItem {
        return dataSource[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)

        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val deleteButton = view.findViewById<Button>(R.id.delete_button)

        val item = getItem(position)

        checkBox.text = item.task
        checkBox.isChecked = item.status

        deleteButton.setOnClickListener {
            coroutineScope.launch {
                database.listItemsDao().delete(item)
                dataSource.remove(item)
                notifyDataSetChanged()
            }
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            coroutineScope.launch {
                item.status = isChecked
                database.listItemsDao().update(item)
                // меняем цolor в зависимости от статуса
                if (isChecked) {
                    checkBox.setTextColor(context.resources.getColor(R.color.darkViolet))
                } else {
                    checkBox.setTextColor(context.resources.getColor(R.color.white))
                }

            }
        }

        return view
    }
}