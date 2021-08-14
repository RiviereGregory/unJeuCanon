package gri.riverjach.unjeucanon

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity


class CanonView @JvmOverloads constructor(
    context: Context,
    attributes: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attributes, defStyleAttr), SurfaceHolder.Callback, Runnable {
    lateinit var canvas: Canvas
    val backgroundPaint = Paint()
    val textPaint = Paint()
    var screenWidth = 0f
    var screenHeight = 0f
    var drawing = false
    lateinit var thread: Thread
    val canon = Canon(0f, 0f, 0f, 0f, this)
    val obstacle = Obstacle(0f, 0f, 0f, 0f, 0f, this)
    val cible = Cible(0f, 0f, 0f, 0f, 0f, this)
    val balle = BalleCanon(this, obstacle, cible)
    var shotsFired = 0
    var timeLeft = 0.0
    val MISS_PENALTY = 2
    val HIT_REWARD = 3
    var gameOver = false
    var totalElapsedTime = 0.0
    val activity = context as FragmentActivity
    val soundPool: SoundPool
    val sounMap: SparseIntArray

    init {
        backgroundPaint.color = Color.WHITE
        textPaint.textSize = screenHeight / 20
        textPaint.color = Color.BLACK
        timeLeft = 10.0

        soundPool =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes((audioAttributes))
                    .build()
            } else {
                SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            }

        sounMap = SparseIntArray(3)
        sounMap.put(0, soundPool.load(context, R.raw.target_hit, 1))
        sounMap.put(1, soundPool.load(context, R.raw.canon_fire, 1))
        sounMap.put(2, soundPool.load(context, R.raw.blocker_hit, 1))
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
            totalElapsedTime += elapsedTimeMS / 1000.0
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
        textPaint.setTextSize(w / 20f)
        textPaint.isAntiAlias = true
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
            val formatted = String.format("%.2f", timeLeft)
            canvas.drawText("il reste $formatted seconds. ", 30f, 50f, textPaint)
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
        timeLeft -= interval
        if (timeLeft <= 0.0) {
            timeLeft = 0.0
            gameOver = true
            drawing = false
            showGameOverDialog(R.string.lose)
        }
    }

    fun showGameOverDialog(messageId: Int) {
        class GameResult : DialogFragment() {
            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                val builder = AlertDialog.Builder(activity)
                builder.setTitle(resources.getString(messageId))
                builder.setMessage(
                    resources.getString(
                        R.string.results_format,
                        shotsFired,
                        totalElapsedTime
                    )
                )
                builder.setPositiveButton(
                    R.string.reset_game,
                    DialogInterface.OnClickListener { _, _ -> newGame() })
                return builder.create()
            }
        }

        activity.runOnUiThread(Runnable {
            val ft = activity.supportFragmentManager.beginTransaction()
            val prev = activity.supportFragmentManager.findFragmentByTag("dialog")
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            val gameResult = GameResult()
            gameResult.isCancelable = false
            gameResult.show(ft, "dialog")
        })

    }

    fun newGame() {
        cible.resetCible()
        obstacle.resetObstacle()
        timeLeft = 10.0
        balle.resetCanonBall()
        shotsFired = 0
        totalElapsedTime = 0.0
        drawing = true
        if (gameOver) {
            gameOver = false
            thread = Thread(this)
            thread.start()
        }
    }

    fun reduceTimeLeft() {
        timeLeft -= MISS_PENALTY
    }

    fun increaseTimeLeft() {
        timeLeft += HIT_REWARD
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
            soundPool.play(sounMap.get(1), 1f, 1f, 1, 0, 1f)
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


    fun gameOver() {
        drawing = false
        showGameOverDialog(R.string.win)
        gameOver = true
    }

    fun playCibleSound() {
        soundPool.play(sounMap.get(0), 1f, 1f, 1, 0, 1f)
    }

    fun playObstacleSound() {
        soundPool.play(sounMap.get(2), 1f, 1f, 1, 0, 1f)
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        // Not used
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        // Not used
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        // Not used
    }
}