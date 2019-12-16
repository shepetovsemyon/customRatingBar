package ru.shepetov.mycustomviewapp

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Area(private val points: List<Point>, val center: Point = Point(0, 0)) {
    var angleGrad = 0f
        set(value) {
            field = value
            angle = value.gradToRadian()
        }

    private var angle = 0f

    val colors = listOf(
        Color.BLACK,
        Color.BLUE,
        Color.CYAN,
        Color.RED
    )

    private var _points = listOf<Point>()

   // private var dots = mutableListOf<Point>()

    fun prepare() {
        _points = updatePoints()
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (points.size < 2) return

        var x0 = _points[0].x
        var y0 = _points[0].y

        _points.forEachIndexed { i, p ->
            if (i == 0) return@forEachIndexed
//            paint.color = colors[i - 1]

            canvas.drawLine(x0.toFloat(), y0.toFloat(), p.x.toFloat(), p.y.toFloat(), paint)
            x0 = p.x
            y0 = p.y
        }

        paint.color = colors[3]
        canvas.drawLine(
            x0.toFloat(),
            y0.toFloat(),
            _points[0].x.toFloat(),
            _points[0].y.toFloat(),
            paint
        )

//        dots.forEach {
//            canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), paint)
//        }
    }

    private fun updatePoints(): List<Point> = points.map {

        val newPoint = it.rotate(angle)

        newPoint.x += center.x
        newPoint.y += center.y

        return@map newPoint
    }

    fun contains(x: Int, y: Int): Boolean {
        val points = _points
        var count = 0

        for (i in points.indices) {
            val crossPoint = cross(i, x, y) ?: continue
       //     dots.add(crossPoint)
            count++
        }

        return count % 2 == 1
    }

    private fun cross(j: Int, xh: Int, yh: Int): Point? {
        val points = _points

        val n = points.size
        val second = if (j == n - 1) 0 else j + 1

        val p0 = points[j]
        val p1 = points[second]

        val minimal = min(p0.x, p1.x)
        val maximal = max(p0.x, p1.x)

        if (xh < minimal || xh > maximal) return null

        val y = line(p0, p1)(xh)

        if (y < yh) return null

        return Point(xh, y)
    }

    fun clone(): Area = Area(points.toMutableList(), center)
        .apply {
            angle = angle
        }

    private fun line(p0: Point, p1: Point): (Int) -> Int {
        return {x ->
            var dx = (p1.x - p0.x).toFloat()
            if (dx == 0f) dx = 0.0001f
            val dy = (p1.y - p0.y).toFloat()

            val k = dy / dx
            val b = p1.y - k * p1.x

            (k * x + b).toInt()
        }
    }
}