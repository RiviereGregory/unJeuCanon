package gri.riverjach.unjeucanon

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class CanonView @JvmOverloads constructor(
    context: Context,
    attributes: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attributes, defStyleAttr), SurfaceHolder.Callback, Runnable {
    lateinit var canvas: Canvas
    val backgroundPaint = Paint()
    var screenWidth = 0f
    var screenHeight = 0f
    var drawing = false
    lateinit var thread: Thread
    val canon = Canon(0f, 0f, 0f, 0f, this)
    val obstacle = Obstacle(0f, 0f, 0f, 0f, 0f, this)
    val cible = Cible(0f, 0f, 0f, 0f, 0f, this)
    val balle = BalleCanon(this, obstacle, cible)
    var shotsFired = 0

    init {
        backgroundPaint.color = Color.WHITE
    }

    fun pause() {
        drawing = false
        thread.join()
    }

    fun resume() {
        drawing = true
        thread = Thread(this)
        thread.start()
    }

    override fun run() {
        var previousframeTime = System.currentTimeMillis()
        while (drawing) {
            val currentTime = System.currentTimeMillis()
            val elapsedTimeMS = (currentTime - previousframeTime).toDouble()
            updatePostions(elapsedTimeMS)
            draw()
            previousframeTime = currentTime
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w.toFloat()
        screenHeight = h.toFloat()
        canon.canonBaseRadius = (h / 18f)
        canon.canonLongueur = (w / 8f)
        canon.largeur = (w / 24f)
        canon.setFinCanon(h / 2f)
        balle.canonballRadius = (w / 36f)
        balle.canonballVitesse = (w * 3 / 2f)
        obstacle.obstacleDistance = (w * 5 / 8f)
        obstacle.obstacleDebut = (h / 8f)
        obstacle.obstacleFin = (h * 3 / 8f)
        obstacle.width = (w / 24f)
        obstacle.obstacleVitesseInitiale = (h / 2f)
        obstacle.setRect()
        cible.width = (w / 24f)
        cible.cibleDistance = (w * 7 / 8f)
        cible.cibleDebut = (h / 8f)
        cible.cibleFin = (h * 7 / 8f)
        cible.cibleVitesseInitiale = (-h / 4f)
        cible.setRect()
    }

    fun draw() {
        if (holder.surface.isValid) {
            canvas = holder.lockCanvas()
            canvas.drawRect(
                0f,
                0f,
                canvas.width.toFloat(),
                canvas.height.toFloat(),
                backgroundPaint
            )
            canon.draw(canvas)
            if (balle.canonballOnScreen) {
                balle.draw(canvas)
            }
            obstacle.draw(canvas)
            cible.draw(canvas)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun updatePostions(elapsedTimeMS: Double) {
        val interval = elapsedTimeMS / 1000.0
        obstacle.update(interval)
        cible.update(interval)
        balle.update(interval)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            fireCanonball(event)
        }
        return true
    }

    fun fireCanonball(event: MotionEvent) {
        if (!balle.canonballOnScreen) {
            val angle = alignCanon(event)
            balle.launch(angle)
            ++shotsFired
        }
    }

    fun alignCanon(event: MotionEvent): Double {
        val touchPoint = Point(event.x.toInt(), event.y.toInt())
        val centerMinusY = screenHeight / 2 - touchPoint.y
        var angle = 0.0
        if (centerMinusY != 0.0f) {
            angle = Math.atan((touchPoint.x).toDouble() / centerMinusY)
        }
        if (touchPoint.y > screenHeight / 2) {
            angle += Math.PI
        }
        canon.align(angle)
        return angle
    }


    override fun surfaceCreated(p0: SurfaceHolder?) {
        // Not used
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        // Not used
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        // Not used
    }


}