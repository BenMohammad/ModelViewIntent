package com.benmohammad.modelviewintent.addedittask

import com.benmohammad.modelviewintent.mvibase.MviIntent

sealed class AddEditTaskIntent: MviIntent {

    data class InitialIntent(val taskId: String?): AddEditTaskIntent()

    data class SaveTask(
        val taskId: String?,
        val title: String,
        val description: String
    ): AddEditTaskIntent()
}