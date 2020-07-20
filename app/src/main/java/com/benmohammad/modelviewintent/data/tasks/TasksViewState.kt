package com.benmohammad.modelviewintent.data.tasks

import com.benmohammad.modelviewintent.data.Task
import com.benmohammad.modelviewintent.mvibase.MviViewState

data class TasksViewState(
    val isLoading: Boolean,
    val tasksFilterType: TasksFilterType,
    val tasks: List<Task>,
    val error: Throwable?,
    val uiNotification: UiNotification?
): MviViewState {

    enum class UiNotification {
        TASK_COMPLETE,
        TASK_ACTIVATED,
        COMPLETE_TASKS_CLEARED
    }

    companion object {
        fun idle(): TasksViewState {
            return TasksViewState(
                isLoading = false,
                tasksFilterType = TasksFilterType.ALL_TASKS,
                tasks = emptyList(),
                error = null,
                uiNotification = null
            )
        }
    }


}