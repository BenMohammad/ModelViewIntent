package com.benmohammad.modelviewintent.util

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

@SuppressLint("CommitTransaction")
fun addFragmentToActivity(
    fragmentManager: FragmentManager,
    fragment: Fragment,
    frameId: Int
) {
    fragmentManager.beginTransaction().run {
        add(frameId, fragment).commit()
    }
}