package com.benmohammad.modelviewintent.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.benmohammad.modelviewintent.Injection.Injection
import com.benmohammad.modelviewintent.taskdetail.TaskDetailActionProcessorHolder
import com.benmohammad.modelviewintent.taskdetail.TaskDetailViewModel
import com.benmohammad.modelviewintent.tasks.TasksActionProcessorHolder
import com.benmohammad.modelviewintent.tasks.TasksViewModel
import java.lang.IllegalArgumentException

class TodoViewModelFactory private constructor(
    private val applicationContext: Context
): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == TasksViewModel::class.java) {
            return TasksViewModel(
                TasksActionProcessorHolder(
                    Injection.provideTaskRepository(applicationContext),
                    Injection.provideSchedulerProvider())) as T
        } else if(modelClass == TaskDetailViewModel::class.java) {
            return TaskDetailViewModel(
                TaskDetailActionProcessorHolder(
                    Injection.provideTaskRepository(applicationContext),
                    Injection.provideSchedulerProvider())) as T
        }

        throw IllegalArgumentException("unknown model class: $modelClass")
    }

    companion object : SingletonHolderSingleArg<TodoViewModelFactory, Context>(
        ::TodoViewModelFactory
    )
}