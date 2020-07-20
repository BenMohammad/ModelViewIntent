package com.benmohammad.modelviewintent.util.schedulers

import io.reactivex.Scheduler

interface BaseSchedulerProvider {

    fun computation(): Scheduler
    fun io(): Scheduler
    fun ui(): Scheduler
}