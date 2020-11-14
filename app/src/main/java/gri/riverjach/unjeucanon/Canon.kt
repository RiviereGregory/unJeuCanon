package gri.riverjach.unjeucanon

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF

class Canon(
    var canonBaseRadius: Float,
    var canonLongueur: Float,
    var hauteur: Float,
    var largeur: Float,
    val view: CanonView
) {
    val canonPaint = Paint()
    var finCanon = PointF(canonLongueur, hauteur)

    fun draw(canvas: Canvas) {
        canonPaint.strokeWidth = largeur * 1.5f
        canvas.drawLine(0f, view.screenHeight / 2, finCanon.x, finCanon.y, canonPaint)
        canvas.drawCircle(0f, view.screenHeight / 2, canonBaseRadius, canonPaint)
    }

    fun setFinCanon(hauteur: Float) {
        finCanon.set(canonLongueur, hauteur)
    }
}