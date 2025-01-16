package nishkaaminnovations.com.likhosmart.HomeScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.appwrite.Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModelFactory
import nishkaaminnovations.com.likhosmart.DataBase.appwrite.Appwrite
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.databinding.FragmentLoginscreenBinding


class loginscreen : Fragment() {
    /*
    Variable that will represent the binding of the login screen
     */
    private lateinit var binding:FragmentLoginscreenBinding
    /*
    ViewModel variable.
     */
    private val viewModel: DocumentViewModel by viewModels {
        DocumentViewModelFactory(
            requireActivity().application
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentLoginscreenBinding.inflate(inflater,container,false)

        /*
        Login button listener
         */
        binding.userloginbutton.setOnClickListener{
            if(validateCredentials(binding.userEmail.text.toString(),binding.userPassword.text.toString())) {
                toggleViewState(binding.userEmail,binding.userPassword,binding.userloginbutton,binding.usersignuptext)
                toggleViews(true)
              /*
              Logging in the user.
               */
                lifecycleScope.launch {
                        if(viewModel.loginUser(binding.userEmail.text.toString(),binding.userPassword.text.toString())){
                                takeToHomeScreen()
                        }
                    else{
                            toggleViewState(binding.userEmail,binding.userPassword,binding.userloginbutton,binding.usersignuptext)
                            toggleViews(false)
                            showSnackbar("login failed")
                    }
                }

            }

        }
        binding.usersignuptext.setOnClickListener{
            findNavController().navigate(R.id.action_loginscreen_to_signUpScreen)
        }
        return binding.root
    }
    /*
    Method to verify the user email and password.
     */
    private fun validateCredentials(email: String, password: String): Boolean {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showSnackbar("Invalid email address")
            return false
        }
        if (password.isEmpty() || password.length < 8) {
            showSnackbar("Password must be more than 8 words")
            return false
        }
        return true
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    fun toggleViewState(vararg views: View) {
        views.forEach { view ->
            val isCurrentlyEnabled = view.isEnabled
            view.isEnabled = !isCurrentlyEnabled
            view.alpha = if (isCurrentlyEnabled) 0.5f else 1.0f // Dim for disabled, full opacity for enabled
        }
    }

    fun toggleViews(isLoading: Boolean) {
        if (isLoading) {
            // Show LinearLayout and hide TextView
            binding.loginloadinglayout.visibility = View.VISIBLE
            binding.usersignuptext.visibility = View.GONE
        } else {
            // Hide LinearLayout and show TextView
            binding.loginloadinglayout.visibility = View.GONE
            binding.usersignuptext.visibility = View.VISIBLE
        }
    }

    private fun takeToHomeScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(requireContext(), HomeActivity::class.java) // Correct usage of context for Intent
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            requireActivity().finish() // Close the current activity
        }, 3000)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Reverts to default behavior

    }


}