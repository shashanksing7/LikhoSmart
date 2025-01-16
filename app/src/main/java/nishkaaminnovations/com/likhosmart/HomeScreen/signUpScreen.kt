package nishkaaminnovations.com.likhosmart.HomeScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModelFactory
import nishkaaminnovations.com.likhosmart.DataBase.User
import nishkaaminnovations.com.likhosmart.databinding.FragmentSignUpScreenBinding


class signUpScreen : Fragment() {

        /*
    Variable that will represent the binding of the login screen
     */
    private lateinit var binding: FragmentSignUpScreenBinding
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
        binding=FragmentSignUpScreenBinding.inflate(inflater,container,false)
        /*
        Setting the click listener to the button.
         */
        binding.userSignUpEButton.setOnClickListener{
            toggleViews(true)
            toggleViewState(binding.userName, binding.userSignUpEmail,binding.userSignUpPassword,binding.userLoginText, binding.userSignUpEButton)
            if(verifyInput(binding.userName.text.toString(),binding.userSignUpEmail.text.toString(),binding.userSignUpPassword.text.toString())){
                lifecycleScope.launch {

                    if( viewModel.registerUser(User(userName =binding.userName.text.toString() ,userEmail=binding.userSignUpEmail.text.toString(),userPassword=binding.userSignUpPassword.text.toString()))){
                        viewModel.insertUser(
                            User(
                                userName = binding.userName.text.toString(),
                                userEmail = binding.userSignUpEmail.text.toString(),
                                userPassword = binding.userSignUpPassword.text.toString()
                            )
                        )
                            takeToHomeScreen()
                    }
                    else{
                        toggleViewState(binding.userName, binding.userSignUpEmail,binding.userSignUpPassword,binding.userLoginText, binding.userSignUpEButton)
                        toggleViews(false)
                            showSnackbar("Failed")
                        }
                    }
                }
            else{
                toggleViewState(binding.userName, binding.userSignUpEmail,binding.userSignUpPassword,binding.userLoginText, binding.userSignUpEButton)
                toggleViews(false)
            }
        }

        return binding.root
    }

    /*
    Method to verify the user input.
     */
    private fun verifyInput(  userName: String, userEmail: String , userPassword: String): Boolean {

        // Check if any field is empty
        if (TextUtils.isEmpty(userName)) {
            showSnackbar("Name cannot be empty")
            return false
        }

        if (userEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            showSnackbar("Invalid email address")
            return false
        }
        if (userPassword.isEmpty() || userPassword.length < 8) {
            showSnackbar("Password must be more than 8 words")
            return false
        }

        // You can add more validation like email format, password strength, etc.
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
           binding.signuploadinglayout.visibility = View.VISIBLE
            binding.userLoginText.visibility = View.GONE
        } else {
            // Hide LinearLayout and show TextView
            binding.signuploadinglayout.visibility = View.GONE
            binding.userLoginText.visibility = View.VISIBLE
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