package com.benmohammad.modelviewintent.addedittask

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.benmohammad.modelviewintent.R

class AddEditTaskActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addtask_act)
    }


    companion object {
        const val REQUEST_ADD_TASK = 1

    }
}