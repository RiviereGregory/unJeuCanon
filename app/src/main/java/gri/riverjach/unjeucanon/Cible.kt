package gri.riverjach.unjeucanon

import android.graphics.*

class Cible(
    var cibleDistance: Float,
    var cibleDebut: Float,
    var cibleFin: Float,
    var cibleVitesseInitiale: Float,
    var width: Float,
    var view: CanonView
) {

    val CIBLE_PIECES = 7
    val cible = RectF(cibleDistance, cibleDebut, cibleDistance + width, cibleFin)
    val cibleTouchee = BooleanArray(CIBLE_PIECES)
    val ciblePaint = Paint()
    var longueurPiece = 0f
    var cibleVitesse = cibleVitesseInitiale
    var nbCiblesTouchees = 0

    fun draw(canvas: Canvas) {
        val currentPoint = PointF()
        currentPoint.x = cible.left
        currentPoint.y = cible.top
        for (i in 0 until CIBLE_PIECES) {
            if (i % 2 != 0) {
                ciblePaint.color = Color.BLUE
            } else {
                ciblePaint.color = Color.YELLOW
            }
            canvas.drawRect(
                currentPoint.x,
                currentPoint.y,
                cible.right,
                currentPoint.y + longueurPiece,
                ciblePaint
            )
            currentPoint.y += longueurPiece
        }
    }

    fun setRect() {
        cible.set(cibleDistance, cibleDebut, cibleDistance + width, cibleFin)
        cibleVitesse = cibleVitesseInitiale
        longueurPiece = (cibleFin - cibleDebut) / CIBLE_PIECES
    }

    fun update(interval: Double) {
        var up = (interval * cibleVitesse).toFloat()
        cible.offset(0f, up)
        if (cible.top < 0 || cible.bottom > view.screenHeight) {
            cibleVitesse *= -1f
            up = (interval * 3 * cibleVitesse).toFloat()
            cible.offset(0f, up)
        }
    }

    fun detectChoc(balle: BalleCanon) {
        val section = ((balle.canonball.y - cible.top) / longueurPiece).toInt()
        if (section >= 0 && section < CIBLE_PIECES && !cibleTouchee[section]) {
            cibleTouchee[section] = true
            balle.resetCanonBall()
            view.increaseTimeLeft()
            nbCiblesTouchees++
            if (nbCiblesTouchees == CIBLE_PIECES) {
                view.gameOver()
            }
        }
    }

    fun resetCible() {
        for (i in 0 until CIBLE_PIECES) {
            cibleTouchee[i] = false
        }
        nbCiblesTouchees = 0
        cibleVitesse = cibleVitesseInitiale
        cible.set(cibleDistance, cibleDebut, cibleDistance + width, cibleFin)
    }
}