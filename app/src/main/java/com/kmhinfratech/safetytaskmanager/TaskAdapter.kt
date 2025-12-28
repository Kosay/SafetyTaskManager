package com.kmhinfratech.safetytaskmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private var tasks: List<Task>,
    private val onClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTaskTitle)
        val projectName: TextView = view.findViewById(R.id.tvProjectName)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val riskLevel: TextView = view.findViewById(R.id.tvRiskLevel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]

        holder.title.text = task.title
        holder.projectName.text = "Project: ${task.projectName}"
        holder.status.text = task.status.uppercase()
        holder.riskLevel.text = "Risk: ${task.riskCategory}"

        // Fixes the "Cannot infer type" error by explicitly setting the listener
        holder.itemView.setOnClickListener { onClick(task) }
    }

    override fun getItemCount() = tasks.size

    // Fixes "Unresolved reference: updateData"
    fun updateData(newTasks: List<Task>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }
}