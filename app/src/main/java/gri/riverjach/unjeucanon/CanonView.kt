package gri.riverjach.unjeucanon

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
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
        while (drawing) {
            draw()
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
            holder.unlockCanvasAndPost(canvas)
        }
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