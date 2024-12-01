package com.example.checkflow

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(
    private val items: List<MyItem>,
    private val onTaskAction: (MyItem, String) -> Unit
) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTask: TextView = itemView.findViewById(R.id.tvTask)
        val cardView: View = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTask.text = item.task

        // Configurar menú contextual
        holder.cardView.setOnCreateContextMenuListener { menu, v, menuInfo ->
            val editItem = menu.add(Menu.NONE, 1, 1, "Edit")
            editItem.setOnMenuItemClickListener {
                onTaskAction(item, "update")
                true
            }
            val deleteItem = menu.add(Menu.NONE, 2, 2, "Delete")
            deleteItem.setOnMenuItemClickListener {
                onTaskAction(item, "delete")
                true
            }
        }

        // Configurar listener para mostrar el menú contextual
        holder.cardView.setOnLongClickListener {
            holder.cardView.showContextMenu()
            true
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
