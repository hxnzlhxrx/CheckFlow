package com.example.checkflow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var addTaskButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var imageViewArrow: View
    private lateinit var imageViewLogo: View
    private val items = mutableListOf<MyItem>()
    private lateinit var taskDao: TaskDao
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addTaskButton = findViewById(R.id.btnNewTask)
        recyclerView = findViewById(R.id.recyclerView)
        imageViewArrow = findViewById(R.id.imageViewArrow)
        imageViewLogo = findViewById(R.id.imageViewLogo)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter(items, { task, action -> handleTaskAction(task, action) })
        recyclerView.adapter = adapter

        recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE

        taskDao = AppDatabase.getDatabase(this).taskDao()

        CoroutineScope(Dispatchers.IO).launch {
            val tasks = taskDao.getAllTasks()
            runOnUiThread {
                items.addAll(tasks.map { MyItem(it.id, it.task) })
                adapter.notifyDataSetChanged()
                checkEmpty()
            }
        }

        addTaskButton.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1 -> {
                    val task = data?.getStringExtra("newTask")
                    if (task != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val taskId = taskDao.insertTask(TaskEntity(task = task))
                            runOnUiThread {
                                items.add(MyItem(taskId, task))
                                adapter.notifyDataSetChanged()
                                checkEmpty()
                            }
                        }
                    }
                }
                2 -> {
                    val taskId = data?.getLongExtra("taskId", -1L) ?: -1L
                    val taskText = data?.getStringExtra("taskText")
                    if (taskId != -1L && taskText != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            taskDao.updateTask(TaskEntity(taskId, taskText))
                            runOnUiThread {
                                val task = items.find { it.id == taskId }
                                task?.task = taskText
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleTaskAction(task: MyItem, action: String) {
        when (action) {
            "update" -> {
                val intent = Intent(this, SecondActivity::class.java)
                intent.putExtra("taskId", task.id)
                intent.putExtra("taskText", task.task)
                startActivityForResult(intent, 2)
            }
            "delete" -> {
                val position = items.indexOf(task)
                adapter.notifyItemChanged(position)

                items.remove(task)
                adapter.notifyItemRemoved(position)
                checkEmpty()

                Snackbar.make(recyclerView, "TASK DELETED!", 6000)
                    .setAction("UNDO") {
                        // Reinserción de la tarea en la lista
                        items.add(position, task)
                        adapter.notifyItemInserted(position)
                        recyclerView.visibility = View.VISIBLE
                        imageViewArrow.visibility = View.GONE
                        imageViewLogo.visibility = View.GONE
                        recyclerView.scrollToPosition(position)
                    }
                    .show()

                handler.postDelayed({
                    CoroutineScope(Dispatchers.IO).launch {
                        taskDao.deleteTask(TaskEntity(task.id, task.task))
                    }
                }, 10000)
            }
        }
    }

    private fun checkEmpty() {
        if (items.isEmpty()) {
            recyclerView.visibility = View.GONE
            imageViewArrow.visibility = View.VISIBLE
            imageViewLogo.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            imageViewArrow.visibility = View.GONE
            imageViewLogo.visibility = View.GONE
        }
    }
}
