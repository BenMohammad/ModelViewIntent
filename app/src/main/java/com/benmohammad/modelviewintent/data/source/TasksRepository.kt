package com.benmohammad.modelviewintent.data.source

import android.app.ActivityManager
import androidx.annotation.VisibleForTesting
import com.benmohammad.modelviewintent.data.Task
import com.benmohammad.modelviewintent.data.source.local.TasksLocalDataSource
import com.benmohammad.modelviewintent.util.SingletonHolderDoubleArg
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

open class TasksRepository private constructor(
    private val tasksRemoteDataSource: TasksDataSource,
    private val tasksLocalDataSource: TasksDataSource
): TasksDataSource {

    @VisibleForTesting
    var cachedTasks: MutableMap<String, Task>? = null
    @VisibleForTesting
    var cacheIsDirty = false

    private fun getAndCacheLocalTasks(): Single<List<Task>> {
        return tasksLocalDataSource.getTasks()
            .flatMap { tasks ->
                Observable.fromIterable(tasks)
                    .doOnNext{task -> cachedTasks!!.put(task.id, task)}
                    .toList()
            }
    }

    private fun getAndSaveRemoteTasks(): Single<List<Task>> {
        return tasksRemoteDataSource.getTasks()
            .flatMap { tasks ->
                Observable.fromIterable(tasks)
                    .doOnNext{task ->
                        tasksLocalDataSource.saveTask(task)
                        cachedTasks!!.put(task.id, task)
                    }.toList()
            }
            .doOnSuccess { cacheIsDirty = false }
    }

    override fun getTasks(): Single<List<Task>> {
        if(cachedTasks != null && !cacheIsDirty) {
            return Observable.fromIterable(cachedTasks!!.values).toList()
        } else if(cachedTasks == null) {
            cachedTasks = LinkedHashMap()
        }

        val remoteTasks = getAndSaveRemoteTasks()

        return if(cacheIsDirty) {
            remoteTasks
        } else {
            val localTasks = getAndCacheLocalTasks()
            Single.concat(localTasks, remoteTasks)
                .filter { tasks -> !tasks.isEmpty() }
                .firstOrError()
        }
    }

    override fun getTask(taskId: String): Single<Task> {
        val cachedTask = getTaskWithId(taskId)
        if(cachedTask == null) {
            cachedTasks = LinkedHashMap()
        }

        val localTask = getTaskWithidFromLocalRepository(taskId)
        val remoteTask = tasksRemoteDataSource.getTask(taskId)
            .doOnSuccess { task ->
                tasksLocalDataSource.saveTask(task)
                cachedTasks!!.put(task.id, task)
            }
        return Single.concat(localTask, remoteTask).firstOrError()
    }

    private fun getTaskWithId(id: String): Task? = cachedTasks?.get(id)

    private fun getTaskWithidFromLocalRepository(taskId: String): Single<Task> {
        return tasksLocalDataSource.getTask(taskId)
            .doOnSuccess { task -> cachedTasks!!.put(taskId, task)}
    }

    override fun saveTask(task: Task): Completable {
        tasksRemoteDataSource.saveTask(task)
        tasksLocalDataSource.saveTask(task)

        if(cachedTasks == null) {
            cachedTasks = LinkedHashMap()
        }

        cachedTasks!!.put(task.id, task)
        return Completable.complete()
    }

    override fun completeTask(task: Task): Completable {
        tasksRemoteDataSource.completeTask(task)
        tasksLocalDataSource.completeTask(task)

        val completedTask = Task(title = task.title!!, description = task.description, id = task.id, completed = true)

        if(cachedTasks == null) {
            cachedTasks = LinkedHashMap()
        }

        cachedTasks!!.put(task.id, completedTask)
        return Completable.complete()
    }

    override fun completeTask(taskId: String): Completable {
        val taskWithId = getTaskWithId(taskId)
        return if(taskWithId != null) {
                completeTask(taskWithId)
                } else {
                    Completable.complete()
        }
    }

    override fun activateTask(task: Task): Completable {
        tasksRemoteDataSource.activateTask(task)
        tasksLocalDataSource.activateTask(task)

        val activeTask = Task(title = task.title!!, description = task.description, id = task.id, completed = false)

        if(cachedTasks == null) {
            cachedTasks = LinkedHashMap()
        }

        cachedTasks!!.put(task.id, activeTask)
        return Completable.complete()
    }

    override fun activateTask(taskId: String): Completable {
        val taskWithId = getTaskWithId(taskId)
        return if(taskWithId != null) {
                    activateTask(taskWithId)
               } else {
                    Completable.complete()
        }
    }

    override fun clearCompletedTask(): Completable {
        tasksRemoteDataSource.clearCompletedTask()
        tasksLocalDataSource.clearCompletedTask()

        if(cachedTasks == null) {
            cachedTasks = LinkedHashMap()
        }
        val it = cachedTasks!!.entries.iterator()
        while(it.hasNext()) {
            val entry = it.next()
            if(entry.value.completed) {
                it.remove()
            }
        }
        return Completable.complete()
    }

    override fun refreshTasks() {
        cacheIsDirty = true
    }

    override fun deleteAllTasks() {
        tasksRemoteDataSource.deleteAllTasks()
        tasksLocalDataSource.deleteAllTasks()

        if(cachedTasks == null) {
            cachedTasks = LinkedHashMap()
        }
        cachedTasks!!.clear()
    }

    override fun deleteTask(taskId: String): Completable {
        tasksRemoteDataSource.deleteTask(taskId)
        tasksLocalDataSource.deleteTask(taskId)

        cachedTasks!!.remove(taskId)
        return Completable.complete()
    }



    companion object : SingletonHolderDoubleArg<TasksRepository, TasksDataSource, TasksDataSource>(
        ::TasksRepository
    )
}