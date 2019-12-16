package ru.shepetov.mycustomviewapp

import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

@Suppress("DEPRECATION")
@ColorInt
fun View.color(@ColorRes resId: Int): Int =
    context?.let { ContextCompat.getColor(it, resId) } ?: resources.getColor(resId)

fun Int.pxToDp(context: Context): Int =
    (this / context.resources.displayMetrics.density + 0.5).toInt()

fun Int.dpToPx(context: Context): Int =
    (context.resources.displayMetrics.density / this + 0.5).toInt()
