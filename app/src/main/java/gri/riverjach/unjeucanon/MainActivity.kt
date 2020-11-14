package gri.riverjach.unjeucanon

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var canonView: CanonView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        canonView = findViewById<CanonView>(R.id.vMain)
    }

    override fun onResume() {
        super.onResume()
        canonView.resume()
    }

    override fun onPause() {
        super.onPause()
        canonView.pause()
    }
}