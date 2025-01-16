package nishkaaminnovations.com.likhosmart.HomeScreen

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.WindowCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.appwrite.Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModelFactory
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.databinding.FragmentStartUpScreenBinding
import java.io.File


class StartUpScreen : Fragment() {

    private  lateinit var  binding:FragmentStartUpScreenBinding
    private val viewModel: DocumentViewModel by viewModels { DocumentViewModelFactory(requireActivity().application) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentStartUpScreenBinding.inflate(inflater,container,false)
        val client = Client(requireContext()).setProject("6718f32a00385d9505f5")
        // Set up listeners for continuous zoom in and out
        /*
        Checking if user is logged in if yes then take to document screen.
        */
        lifecycleScope.launch {
        if(viewModel.getUserLoggedIn()){
              takeToHomeScreen()
         }
            else{
                takeToAppLogin(R.id.action_startUpScreen_to_loginscreen)
            }
        }
        createNotesFolder()
        startZoomAnimation(binding.logoImageView)
        return binding.root
    }


    private fun startZoomAnimation(view: View) {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f)
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f)

        // Set duration for each part of the zoom (1.5 seconds for zoom in, 1.5 seconds for zoom out)
        scaleUpX.duration = 1750
        scaleUpY.duration = 1750
        scaleDownX.duration = 1750
        scaleDownY.duration = 1750

        val animatorSet = AnimatorSet()
        animatorSet.play(scaleUpX).with(scaleUpY) // Zoom in
        animatorSet.play(scaleDownX).with(scaleDownY).after(scaleUpX) // Zoom out after zoom in
        animatorSet.interpolator = AccelerateDecelerateInterpolator()

        animatorSet.start()
    }

    private fun takeToAppLogin(id:Int) {
        Handler(Looper.getMainLooper()).postDelayed( {
            findNavController().navigate(id)
        },3000)

    }
    private fun takeToHomeScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(requireContext(), HomeActivity::class.java) // Correct usage of context for Intent
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            requireActivity().finish() // Close the current activity
        }, 3000)
    }
    /*
    Method to check and create the notes folder.
     */
    private fun createNotesFolder(){
        val notesFolder: File = File(requireContext().filesDir,"Notes")
        if(!notesFolder.exists()){
            notesFolder.mkdir()
            Log.d("myfiles", "createNotesFolder: file path = ${notesFolder}")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Reverts to default behavior

    }
}