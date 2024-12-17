package nishkaaminnovations.com.likhosmart

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.google.mlkit.vision.digitalink.Ink
import nishkaaminnovations.com.likhosmart.HomeScreen.HomeActivity
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.InkRecognizer
import nishkaaminnovations.com.likhosmart.databinding.ActivityMainBinding
import top.defaults.colorpicker.ColorPickerView
import java.io.File


class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val logoImageView = findViewById<View>(R.id.logoImageView)
        // Set up listeners for continuous zoom in and out
        createNotesFolder()
        startZoomAnimation(logoImageView)
        takeToHomeScreen()
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

    /*
    Method to check and create the notes folder.
     */
    private fun createNotesFolder(){
        val notesFolder:File= File(filesDir,"Notes")
        if(!notesFolder.exists()){
            notesFolder.mkdir()
            Log.d("myfiles", "createNotesFolder: file path = ${notesFolder}")
        }
    }
}