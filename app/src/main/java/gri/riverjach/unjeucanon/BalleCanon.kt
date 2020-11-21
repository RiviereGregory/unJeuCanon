package gri.riverjach.unjeucanon

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF

class BalleCanon(var view: CanonView) {
    var canonball = PointF()
    var canonballVitesse = 0f
    var canonballVitesseX = 0f
    var canonballVitesseY = 0f
    var canonballOnScreen = true
    var canonballRadius = 0f
    var canonballPaint = Paint()

    init {
        canonballPaint.color = Color.RED
    }

    fun launch(angle: Double) {
        canonball.x = canonballRadius
        canonball.y = view.screenHeight / 2f
        canonballVitesseX = (canonballVitesse * Math.sin(angle)).toFloat()
        canonballVitesseY = (-canonballVitesse * Math.cos(angle)).toFloat()
        canonballOnScreen = true
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(canonball.x, canonball.y, canonballRadius, canonballPaint)
    }



}