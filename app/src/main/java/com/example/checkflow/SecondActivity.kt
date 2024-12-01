package com.example.checkflow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {

    private lateinit var editTextTask: EditText
    private lateinit var btnSave: Button
    private var taskId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_activity)

        editTextTask = findViewById(R.id.etTask)
        btnSave = findViewById(R.id.btnAddTask)

        // Verificar si estamos editando una tarea existente
        val bundle = intent.extras
        if (bundle != null) {
            taskId = bundle.getLong("taskId", -1)
            val taskText = bundle.getString("taskText", "")
            if (taskId != -1L) {
                editTextTask.setText(taskText)
            }
        }

        btnSave.setOnClickListener {
            val taskText = editTextTask.text.toString().trim()
            if (taskText.isEmpty()) {
                Toast.makeText(this, "Task Can’t Be Empty", Toast.LENGTH_SHORT).show()
            } else {
                val resultIntent = Intent()
                resultIntent.putExtra("taskText", taskText)

                if (taskId != -1L) {
                    resultIntent.putExtra("taskId", taskId)
                    setResult(Activity.RESULT_OK, resultIntent)
                } else {
                    resultIntent.putExtra("newTask", taskText)
                    setResult(Activity.RESULT_OK, resultIntent)
                }

                finish()
            }
        }
    }
}
