package com.benmohammad.modelviewintent.taskdetail

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.benmohammad.modelviewintent.R

class TaskDetailActivity: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.taskdetail_act)
    }


    companion object {
        const val EXTRA_TASK_ID = "TASK_ID"
    }
}