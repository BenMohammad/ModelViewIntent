package com.benmohammad.modelviewintent.util

fun String?.isNullOrEmpty() = this == null || this.isEmpty()
fun String?.isNotNullNorEmpty() = !this.isNullOrEmpty()