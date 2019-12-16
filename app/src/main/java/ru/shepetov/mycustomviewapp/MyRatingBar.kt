package ru.shepetov.mycustomviewapp

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.graphics.drawable.toBitmap


class MyRatingBar @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    @AttrRes defAttrStyle: Int = R.attr.filterStyle
) : View(context, attributeSet, defAttrStyle) {
    val paint = Paint()

    var maxScore = 5
        set(value) {
            field = if (value > 0) value else 1
        }

    var minScore = 0
    var activeDrawable: Drawable? = null
    var padding = 10
    var starWidth = 10

    var score = minScore
        private set

    private var rects = listOf<Rect>()
    private var isOnPressed = false
    private var activeBitmap: Bitmap? = null

    private val defaultPaint = Paint().apply {
        val defaultColorMatrix = ColorMatrix().apply {
            setSaturation(0f)
        }

        colorFilter = ColorMatrixColorFilter(defaultColorMatrix)
    }

    private val pressedPaint = Paint().apply {
        val cmData = floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 1f, 0.1f, 0f,
            0f, 0f, 0.5f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )

        val defaultColorMatrix = ColorMatrix(cmData)

        colorFilter = ColorMatrixColorFilter(defaultColorMatrix)
    }

    init {
        val array = attributeSet?.let {
            context.obtainStyledAttributes(it, R.styleable.MyRatingBar)
        }

        minScore = array?.getInt(R.styleable.MyRatingBar_minScore, 0) ?: 0
        maxScore = array?.getInt(R.styleable.MyRatingBar_maxScore, 5) ?: 5
        score = array?.getInt(R.styleable.MyRatingBar_score, 0) ?: 0
        activeDrawable = array?.getDrawable(R.styleable.MyRatingBar_activeDrawable)
        padding = array?.getInt(R.styleable.MyRatingBar_starPadding, 10) ?: 10

        activeBitmap = activeDrawable?.toBitmap()

        array?.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getMeasurementSize(widthMeasureSpec, DEFAULT_SIZE)
        starWidth = (width - padding * (maxScore - 1)) / maxScore

        val height = getMeasurementSize(heightMeasureSpec, starWidth)

        setMeasuredDimension(width, height)

        initView(width, height)
    }

    private fun initView(width: Int, height: Int) {

        rects = (0 until maxScore).map { i ->
            val left = i * (padding + starWidth)
            Rect(left, 0, left + starWidth, height)
        }.toList()

        val matrixActive = Matrix().apply {
            postScale(
                starWidth.toFloat() / activeBitmap!!.width,
                starWidth.toFloat() / activeBitmap!!.height
            )
        }

        activeBitmap = Bitmap.createBitmap(
            activeBitmap!!,
            0,
            0,
            activeBitmap!!.width,
            activeBitmap!!.height,
            matrixActive,
            true
        )

        setOnTouchListener { _, event ->
            val x = event.x.toInt()
            val y = event.y.toInt()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isOnPressed = true
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    rects.forEachIndexed { i, rect ->
                        if (rect.contains(x, y)) {
                            score = i
                            invalidate()
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    rects.forEachIndexed { i, rect ->
                        if (rect.contains(x, y)) {
                            score = i
                            invalidate()
                        }
                    }
                    isOnPressed = false
                    invalidate()
                }
            }

            return@setOnTouchListener true
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        rects.forEachIndexed { i, rect ->

            val paint = when {
                i <= score && isOnPressed -> this.pressedPaint
                i <= score && !isOnPressed -> this.paint
                else -> this.defaultPaint
            }

            canvas?.drawBitmap(activeBitmap!!, rect.left.toFloat(), rect.top.toFloat(), paint)
        }
    }

    private companion object {
        const val DEFAULT_SIZE = 500
    }
}