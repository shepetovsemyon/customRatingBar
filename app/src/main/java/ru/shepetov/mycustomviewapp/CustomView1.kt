package ru.shepetov.mycustomviewapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import kotlin.math.cos
import kotlin.math.sin


class CustomView1 @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    @AttrRes defAttrStyle: Int = R.attr.filterStyle
) : View(context, attributeSet, defAttrStyle) {
    private val paint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
    }

    private val rect: CustomRect = CustomRect().apply {
        width = 300
        height = 300
    }

    private var area: Area? = null

    init {

        var lastX: Int? = null
        var lastY: Int? = null

        setOnTouchListener { _, motionEvent ->
            val x = motionEvent.x.toInt()
            val y = motionEvent.y.toInt()

            val area = area ?: return@setOnTouchListener false

            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!area.contains(x, y)) return@setOnTouchListener false
                    lastX = x
                    lastY = y
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!area.contains(x, y)) return@setOnTouchListener false

                    if (lastX == null || lastY == null) {
                        lastX = x
                        lastY = y

                        return@setOnTouchListener false
                    }

                    val dx = x - lastX!!
                    val dy = y - lastY!!

                    area.center.x += dx
                    area.center.y += dy

                    lastX = x
                    lastY = y

                    area.prepare()
                    invalidate()
                }
            }

            return@setOnTouchListener true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getMeasurementSize(widthMeasureSpec, DEFAULT_SIZE)
        val height = getMeasurementSize(heightMeasureSpec, DEFAULT_SIZE)

        setMeasuredDimension(width, height)

        initView(width, height)
    }

    private fun initView(width: Int, height: Int) {

        rect.apply {
            centerX = width / 2
            centerY = height / 2
        }

        area = rect.createArea()

        area?.angleGrad = 45f

        area?.prepare()
    }

    @Suppress("NAME_SHADOWING")
    override fun draw(canvas: Canvas?) = with(canvas) {
        super.draw(this)
        if (this == null) return@with

        area?.let{
            it.draw(this, paint)
            it.angleGrad += 5f
            it.prepare()
        }
    }

    private companion object {
        const val DEFAULT_SIZE = 500
    }
}

fun getMeasurementSize(measureSpec: Int, defaultSize: Int): Int {
    val mode = View.MeasureSpec.getMode(measureSpec)
    val size = View.MeasureSpec.getSize(measureSpec)
    return when (mode) {
        View.MeasureSpec.EXACTLY -> size
        View.MeasureSpec.AT_MOST -> Math.min(defaultSize, size)
        View.MeasureSpec.UNSPECIFIED -> defaultSize
        else -> defaultSize
    }
}

fun Float.gradToRadian() = this * (Math.PI / 180f).toFloat()

fun Point.rotate(angle: Float, center: Point? = null): Point {
    val x0 = x - (center?.x ?: 0)
    val y0 = y - (center?.y ?: 0)

    val x = (x0 * cos(angle) - y0 * sin(angle)).toInt()
    val y = (x0 * sin(angle) + y0 * cos(angle)).toInt()

    return Point(x, y)
}