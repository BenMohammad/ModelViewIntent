package com.benmohammad.modelviewintent.data.source.remote

import com.benmohammad.modelviewintent.data.Task
import com.benmohammad.modelviewintent.data.source.TasksDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

object TasksRemoteDataSource: TasksDataSource{

    private const val SERVICE_LATENCY_IN_MILLIS = 5000
    private val tasksServiceData: MutableMap<String, Task>

    init {
        tasksServiceData = LinkedHashMap()
        addTask("Ben Mohammad", "Live your Life!!!!!!")
        addTask("Jose Mourinho", "The special one!!!!")
    }

    private fun addTask(title: String, description: String) {
        val newTask = Task(title = title, description = description)
        tasksServiceData.put(newTask.id, newTask)
    }


    override fun getTasks(): Single<List<Task>> {
        return Observable.fromIterable(tasksServiceData.values)
            .delay(SERVICE_LATENCY_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
            .toList()
    }

    override fun getTask(taskId: String): Single<Task> {
        return Single.just<Task>(tasksServiceData[taskId])
            .delay(SERVICE_LATENCY_IN_MILLIS.toLong(), TimeUnit.MILLISECONDS)
    }

    override fun saveTask(task: Task): Completable {
        tasksServiceData.put(task.id, task)
        return Completable.complete()
    }

    override fun completeTask(task: Task): Completable {
        val completedTask = Task(task.title!!, task.description, task.id, true)
        tasksServiceData.put(task.id, completedTask)
        return Completable.complete()
    }

    override fun completeTask(taskId: String): Completable {
        return Completable.complete()
    }

    override fun activateTask(task: Task): Completable {
        val activatedTask = Task(title = task.title!!, description = task.description!!, id = task.id)
        tasksServiceData.put(task.id, activatedTask)
        return Completable.complete()
    }

    override fun activateTask(taskId: String): Completable {
        return Completable.complete()
    }

    override fun clearCompletedTask(): Completable {
        val it = tasksServiceData.entries.iterator()
        while(it.hasNext()) {
            val entry = it.next()
            if(entry.value.completed) {
                it.remove()
            }
        }
        return Completable.complete()
    }

    override fun refreshTasks() {
    }

    override fun deleteAllTasks() {
        tasksServiceData.clear()
    }

    override fun deleteTask(taskId: String): Completable {
        tasksServiceData.remove(taskId)
        return Completable.complete()
    }
}