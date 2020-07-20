package com.benmohammad.modelviewintent.Injection

import android.content.Context
import com.benmohammad.modelviewintent.data.source.TasksRepository
import com.benmohammad.modelviewintent.data.source.local.TasksLocalDataSource
import com.benmohammad.modelviewintent.data.source.remote.TasksRemoteDataSource
import com.benmohammad.modelviewintent.util.schedulers.BaseSchedulerProvider
import com.benmohammad.modelviewintent.util.schedulers.SchedulerProvider

object Injection {

    fun provideTaskRepository(context: Context): TasksRepository {
        return TasksRepository.getInstance(
            TasksRemoteDataSource,
            TasksLocalDataSource.getInstance(context, provideSchedulerProvider())
        )
    }

    fun provideSchedulerProvider(): BaseSchedulerProvider = SchedulerProvider
}