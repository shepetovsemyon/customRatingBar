package ru.shepetov.mycustomviewapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.graphics.drawable.toBitmap

private const val TAG = "MyRatingBar"

class MyRatingBar @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    @AttrRes defAttrStyle: Int = R.attr.filterStyle
) : View(context, attributeSet, defAttrStyle) {
    var maxScore = MAX_SCORE_DEFAULT
        get() = if (field >= minScore) field else minScore
        set(value) {
            field = if (value > 0) value else 1f
        }

    var minScore = MIN_SCORE_DEFAULT
        set(value) {
            field = if (value >= 0) value else 0f
        }

    var maxScale = MAX_SCALE_DEFAULT

    var activeDrawable: Drawable? = null
    var paddingStars = PADDING_STARS_DEFAULT
    var isIndicator = IS_INDICATOR_DEFAULT
    var score = SCORE_DEFAULT
        private set(value) {
            field = value
            onScoreChanged?.invoke(value.toInt())
        }

    private var onScoreChanged: ((Int) -> Unit)? = null
    private var onScoreSubmit: ((Int) -> Unit)? = null

    fun setOnScoreChanged(block: (Int) -> Unit) {
        onScoreChanged = block
    }

    fun setOnScoreSubmit(block: (Int) -> Unit) {
        onScoreSubmit = block
    }

    private var starWidth = 10
    private var rectsWithPaddings = listOf<Rect>()
    private var rects = listOf<Rect>()
    private val indicatorRect = Rect()

    private var isOnPressed = false
        set(value) {
            field = value
            if (field) return
            onScoreSubmit?.invoke(score.toInt())
        }

    private var activeBitmap: Bitmap? = null

    private val activePaint = Paint()
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

    private var scale = 1f
    private val animatorIn = ValueAnimator.ofFloat(1f, maxScale).apply {
        addUpdateListener {
            scale = it.animatedValue as Float
            invalidate()
        }
    }

    private val animatorOut = ValueAnimator.ofFloat(maxScale, 1f).apply {
        addUpdateListener {
            scale = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        val array = attributeSet?.let {
            context.obtainStyledAttributes(it, R.styleable.MyRatingBar)
        }.apply {
            if (this == null) return@apply

            minScore = getFloat(R.styleable.MyRatingBar_minScore, MIN_SCORE_DEFAULT)
            maxScore = getFloat(R.styleable.MyRatingBar_maxScore, MAX_SCORE_DEFAULT)
            score = getFloat(R.styleable.MyRatingBar_score, SCORE_DEFAULT)
            activeDrawable = getDrawable(R.styleable.MyRatingBar_activeDrawable)
            paddingStars = getDimension(R.styleable.MyRatingBar_starPadding, PADDING_STARS_DEFAULT)
            isIndicator = getBoolean(R.styleable.MyRatingBar_isIndicator, IS_INDICATOR_DEFAULT)
            maxScale = getFloat(R.styleable.MyRatingBar_maxScale, MAX_SCALE_DEFAULT)
        }

        array?.recycle()

        activeBitmap = activeDrawable?.toBitmap()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getMeasurementSize(widthMeasureSpec, SIZE_DEFAULT)
        val paddingsHorisontal = paddingStart + paddingEnd
        starWidth =
            ((width - paddingsHorisontal - paddingStars * (maxScore - 1)) / maxScore).toInt()

        val height = getMeasurementSize(heightMeasureSpec, starWidth + paddingsHorisontal)

        setMeasuredDimension(width, height)

        initView(width, height)
    }

    private fun initView(width: Int, height: Int) {
        rectsWithPaddings = (0 until maxScore.toInt()).map { i ->
            val left = i * (paddingStars + starWidth) + paddingStart / 2
            val top = 0
            val right = left + paddingStars + starWidth
            val bottom = height
            Rect(left.toInt(), top, right.toInt(), bottom)
        }.toList()

        rects = (0 until maxScore.toInt()).map { i ->
            val left = i * (paddingStars + starWidth) + paddingStart
            val top = paddingTop
            val right = left + starWidth
            val bottom = height - paddingBottom
            Rect(left.toInt(), top, right.toInt(), bottom)
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

        if (isIndicator) return

        var lastIndex = -1

        setOnTouchListener { _, event ->
            val x = event.x.toInt()
            val y = event.y.toInt()

//            Log.d(TAG, event.action.toString())

            when (event.action) {
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                    isOnPressed = true

                    var i = rectsWithPaddings.indexOfFirst {
                        it.contains(x, y)
                    }

                    if (i < minScore) i = minScore.toInt()

                    if (event.action == MotionEvent.ACTION_MOVE
                        && (i == -1 || i == lastIndex)
                    ) {
                        return@setOnTouchListener true
                    }

                    lastIndex = i
                    score = i.toFloat() + 1
                    animatorIn.start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val i = rectsWithPaddings.indexOfFirst {
                        it.contains(x, y)
                    }

                    score = if (i >= minScore) {
                        i.toFloat() + 1
                    } else {
                        minScore
                    }

                    isOnPressed = false
                    animatorOut.start()
                }
            }

            return@setOnTouchListener true
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        @Suppress("NAME_SHADOWING") val canvas = canvas ?: return

        if (isIndicator) {
            drawIndicator(canvas)
            return
        }

        drawNonIndicator(canvas)
    }

    private fun drawIndicator(canvas: Canvas) {
        rects.forEach { rect ->
            canvas.drawBitmap(
                activeBitmap!!,
                rect.left.toFloat(),
                rect.top.toFloat(),
                this.defaultPaint
            )
        }

        indicatorRect.set(0, 0, (width * (score / maxScore)).toInt(), height)

        canvas.clipRect(indicatorRect)

        rects.forEach { rect ->
            canvas.drawBitmap(
                activeBitmap!!,
                rect.left.toFloat(),
                rect.top.toFloat(),
                this.activePaint
            )
        }
    }

    private fun drawNonIndicator(canvas: Canvas) = rects.mapIndexed { i, rect ->

        if (scale != 1f && i == score.toInt() - 1) {
            return@mapIndexed rect
        }

        val paint = when {
            i < score && isOnPressed -> this.pressedPaint
            i < score && !isOnPressed && score != 0f-> this.activePaint
            else -> this.defaultPaint
        }

        canvas.drawBitmap(activeBitmap!!, rect.left.toFloat(), rect.top.toFloat(), paint)
        return@mapIndexed rect
    }.getOrNull(score.toInt() - 1)?.apply {
        if (scale == 1f) return@apply

        canvas.apply {
            save()
            scale(
                scale,
                scale,
                left.toFloat() + starWidth / 2 + paddingStart,
                starWidth / 2f + paddingTop
            )
            drawBitmap(activeBitmap!!, left.toFloat(), top.toFloat(), activePaint)
            restore()
        }
    }

    private companion object {
        const val SIZE_DEFAULT = 100
        const val MIN_SCORE_DEFAULT = 0f
        const val MAX_SCORE_DEFAULT = 5f
        const val SCORE_DEFAULT = 0f
        const val PADDING_STARS_DEFAULT = 0f
        const val MAX_SCALE_DEFAULT = 1.35f
        const val IS_INDICATOR_DEFAULT = false
    }
}