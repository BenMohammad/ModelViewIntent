package com.benmohammad.modelviewintent.taskdetail

import com.benmohammad.modelviewintent.mvibase.MviIntent

sealed class TaskDetailIntent: MviIntent {

    data class InitialIntent(val taskID: String): TaskDetailIntent()
    data class DeleteTaskIntent(val taskId: String): TaskDetailIntent()
    data class ActivateTaskIntent(val taskId: String): TaskDetailIntent()
    data class CompleteTaskIntent(val taskId: String): TaskDetailIntent()
}