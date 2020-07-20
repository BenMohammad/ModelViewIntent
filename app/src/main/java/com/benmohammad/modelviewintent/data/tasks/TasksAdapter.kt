package com.benmohammad.modelviewintent.data.tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.benmohammad.modelviewintent.R
import com.benmohammad.modelviewintent.data.Task
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class TasksAdapter(tasks: List<Task>): BaseAdapter()  {

    private val taskCLickSubject = PublishSubject.create<Task>()
    private val taskToggleSubject = PublishSubject.create<Task>()
    private lateinit var tasks: List<Task>

    val taskClickObservable: Observable<Task>
    get() = taskClickObservable

    val taskToggleObservable: Observable<Task>
    get() = taskToggleObservable

    init {
        setList(tasks)
    }

    fun replaceData(tasks: List<Task>) {
        setList(tasks)
        notifyDataSetChanged()
    }

    private fun setList(tasks: List<Task>) {
        this.tasks = tasks
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView: View = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)

        val task = getItem(position)
        rowView.findViewById<TextView>(R.id.title).text = task.titleFOrList
        val completeCB = rowView.findViewById<CheckBox>(R.id.complete)
        completeCB.isChecked = task.completed


        completeCB.setOnClickListener { taskToggleSubject.onNext(task) }
        rowView.setOnClickListener { taskCLickSubject.onNext(task) }

        return rowView

    }

    override fun getItem(position: Int): Task = tasks[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = tasks.size
}