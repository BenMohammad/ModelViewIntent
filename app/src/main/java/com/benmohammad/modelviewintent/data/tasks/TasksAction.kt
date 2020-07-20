package com.benmohammad.modelviewintent.data.tasks

import com.benmohammad.modelviewintent.data.Task
import com.benmohammad.modelviewintent.mvibase.MviAction

sealed class TasksAction: MviAction {

    data class LoadTasksAction(
        val forceUpdate: Boolean,
        val filterType: TasksFilterType?
    ): TasksAction()

    data class ActivateTaskAction(val task: Task): TasksAction()
    data class CompleteTaskAction(val task: Task): TasksAction()
    object ClearCompletedTaskAction: TasksAction()
}