package com.benmohammad.modelviewintent.taskdetail

import com.benmohammad.modelviewintent.mvibase.MviAction

sealed class TaskDetailAction: MviAction {

    data class PopulateTaskAction(val taskId: String): TaskDetailAction()
    data class DeleteTaskAction(val taskId: String): TaskDetailAction()
    data class ActivateTaskAction(val taskId: String): TaskDetailAction()
    data class CompleteTaskAction(val taskID: String): TaskDetailAction()
}