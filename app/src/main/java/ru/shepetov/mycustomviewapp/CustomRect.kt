package ru.shepetov.mycustomviewapp

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import kotlin.math.cos
import kotlin.math.sin

class CustomRect {
    var angle = 0f
    var centerX = 0
    var centerY = 0
    var width = 100
    var height = 100

    var rect = Rect()
        private set

    fun draw(canvas: Canvas, paint: Paint) {
        rect.left = centerX - width / 2
        rect.top = centerY - height / 2
        rect.right = centerX + width / 2
        rect.bottom = centerY + height / 2

        with(canvas) {
            save()
            rotate(angle, centerX.toFloat(), centerY.toFloat())
            drawRect(rect, paint)
            restore()
        }
    }

    fun contains(x: Int, y: Int): Boolean {
        val x0 = x - centerX
        val y0 = y - centerY

        val x1 = x0 * cos(angle) - y0 * sin(
            angle
        )
        val y1 = x0 * sin(angle) + y0 * cos(
            angle
        )

        //Rect(rect.left - centerX, rect.top - centerY)
        return rect.contains(x1.toInt(), y1.toInt())
    }

    fun createArea(): Area {
        val center = Point(centerX, centerY)

        val points = mutableListOf<Point>()
        points.add(Point(-width / 2, -height / 2))
        points.add(Point(width / 2, -height / 2))
        points.add(Point(width / 2, height / 2))
        points.add(Point(-width / 2, height / 2))

        return Area(points, center)
    }
}