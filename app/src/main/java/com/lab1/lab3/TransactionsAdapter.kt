package com.lab1.lab3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TransactionsAdapter(
    private var data: List<TransactionEntity>,
    private val onItemClick: (TransactionEntity) -> Unit
) : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private var categories: List<CategoryEntity> = emptyList()

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = data[position]
        holder.tvAmount.text = "Сумма: ${item.amount}"
        holder.tvDate.text = "Дата: ${dateFormat.format(Date(item.date))}"
        holder.tvType.text = "Тип: ${if (item.type == "income") "Доход" else "Расход"}"
        val cat = categories.find { it.id == item.categoryId }
        holder.tvCategory.text = "Категория: ${cat?.name ?: "?"}"

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    fun updateData(newData: List<TransactionEntity>, categories: List<CategoryEntity>) {
        this.data = newData
        this.categories = categories
        notifyDataSetChanged()
    }
}
