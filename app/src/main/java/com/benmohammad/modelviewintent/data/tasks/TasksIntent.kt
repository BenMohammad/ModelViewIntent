package com.benmohammad.modelviewintent.data.tasks

import com.benmohammad.modelviewintent.data.Task
import com.benmohammad.modelviewintent.mvibase.MviIntent

sealed class TasksIntent: MviIntent {
    object InitialIntent: TasksIntent()

    data class RefreshIntent(val forceUpdate: Boolean): TasksIntent()
    data class ActivateTaskIntent(val task: Task): TasksIntent()
    data class CompleteTaskIntent(val task: Task): TasksIntent()
    object ClearCompletedTaskIntent: TasksIntent()
    data class ChangeFilterIntent(val filterType: TasksFilterType): TasksIntent()
}