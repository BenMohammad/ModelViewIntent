package com.benmohammad.modelviewintent.data.tasks

import com.benmohammad.modelviewintent.data.Task
import com.benmohammad.modelviewintent.mvibase.MviResult

sealed class TasksResult: MviResult {

    sealed class LoadTaskResult: TasksResult() {
        data class Success(val tasks: List<Task>, val filterType: TasksFilterType?): LoadTaskResult()
        data class Failure(val error: Throwable): LoadTaskResult()
        object InFlight: LoadTaskResult()
    }

    sealed class ActivateTaskResult: TasksResult() {
        data class Success(val tasks: List<Task>): ActivateTaskResult()
        data class Failure(val error: Throwable): ActivateTaskResult()
        object InFlight: ActivateTaskResult()
        object HideUiNotification: ActivateTaskResult()
    }

    sealed class CompleteTaskResult: TasksResult() {
        data class Success(val tasks: List<Task>): CompleteTaskResult()
        data class Failure(val error: Throwable): CompleteTaskResult()
        object InFlight: CompleteTaskResult()
        object HideUiNotification: CompleteTaskResult()
    }

    sealed class ClearCompletedTasksResult: TasksResult() {
        data class Success(val tasks: List<Task>): ClearCompletedTasksResult()
        data class Failure(val error: Throwable): ClearCompletedTasksResult()
        object InFlight: ClearCompletedTasksResult()
        object HideUiNotification: ClearCompletedTasksResult()
    }
}