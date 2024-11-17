package nishkaaminnovations.com.likhosmart

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import nishkaaminnovations.com.likhosmart.HomeScreen.HomeActivity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.renderscript.Element
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val logoImageView = findViewById<View>(R.id.logoImageView)
        // Set up listeners for continuous zoom in and out
        startZoomAnimation(logoImageView)
        takeToHomeScreen()

          var _data = MutableStateFlow<String>("String")
            var data: StateFlow<String> =_data

            fun fetchData() {

                    // Fetch your data here
                    val result = "hi"
                    _data.value = result

            }



    }

    private fun startZoomAnimation(view: View) {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f)
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f)

        // Set duration for each part of the zoom (1.5 seconds for zoom in, 1.5 seconds for zoom out)
        scaleUpX.duration = 1500
        scaleUpY.duration = 1500
        scaleDownX.duration = 1500
        scaleDownY.duration = 1500

        val animatorSet = AnimatorSet()
        animatorSet.play(scaleUpX).with(scaleUpY) // Zoom in
        animatorSet.play(scaleDownX).with(scaleDownY).after(scaleUpX) // Zoom out after zoom in
        animatorSet.interpolator = AccelerateDecelerateInterpolator()

        animatorSet.start()
    }

    private fun takeToHomeScreen() {
        Handler(Looper.getMainLooper()).postDelayed( {
            val intent = Intent(this,HomeActivity::class.java) // Replace MainActivity with your main activity class
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Optional: Call finish() if you want to close the current activity
        },3000)

    }

}