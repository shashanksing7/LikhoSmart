package nishkaaminnovations.com.likhosmart.HomeScreen.UserProfile

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModelFactory
import nishkaaminnovations.com.likhosmart.DataBase.User
import nishkaaminnovations.com.likhosmart.DataBase.appwrite.Appwrite
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.databinding.EntercurrentpasswordlayoutBinding
import nishkaaminnovations.com.likhosmart.databinding.FragmentChangePasswordBinding

class changePassword : Fragment() {
    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var dialog: Dialog
    private  var appUser:User?=null
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
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        getUserDetails()
        initialiseToggleButton()

        // Save changes button
        binding.saveChanges.setOnClickListener {
            if (validatePasswords()) {
                // Proceed to confirm current password
                initialisePasswordDialog()
            }
        }

        return binding.root
    }

    /*
    Generalized method to toggle password visibility
    */
    private fun togglePasswordVisibility(editText: android.widget.EditText, toggleButton: android.widget.ImageView, isVisible: Boolean): Boolean {
        val newVisibility = !isVisible
        if (newVisibility) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleButton.setImageResource(R.drawable.eye_svgrepo_com) // Replace with your visible icon
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleButton.setImageResource(R.drawable.eye_slash_svgrepo_com) // Replace with your hidden icon
        }
        editText.setSelection(editText.text?.length ?: 0) // Move cursor to the end
        return newVisibility
    }

    /*
    Initialise toggle buttons for new password and confirm password
    */
    private fun initialiseToggleButton() {
        var isNewPasswordVisible = false
        var isConfirmPasswordVisible = false

        // Toggle visibility for new password
        binding.toggleNewPassword.setOnClickListener {
            isNewPasswordVisible = togglePasswordVisibility(
                binding.newpassword,
                binding.toggleNewPassword,
                isNewPasswordVisible
            )
        }

        // Toggle visibility for confirm password
        binding.toggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = togglePasswordVisibility(
                binding.confirmPassword,
                binding.toggleConfirmPassword,
                isConfirmPasswordVisible
            )
        }
    }

    /*
    Validate password fields
    */
    private fun validatePasswords(): Boolean {
        val password = binding.newpassword.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()

        var isValid = true

        if (password.length < 8) {
            binding.textPasswordHint.text = "Password must be at least 8 characters."
            binding.textPasswordHint.setTextColor(Color.RED)
            isValid = false
            showSnackbar("Invalid password")
        } else {
            binding.textPasswordHint.text = "Password is valid."
            binding.textPasswordHint.setTextColor(Color.GREEN)
        }

        if (password != confirmPassword) {
            binding.textConfirmPasswordHint.text = "Passwords do not match."
            binding.textConfirmPasswordHint.setTextColor(Color.RED)
            isValid = false
            showSnackbar("Passwords do not match.")
        } else {
            binding.textConfirmPasswordHint.text = "Passwords match."
            binding.textConfirmPasswordHint.setTextColor(Color.GREEN)
        }

        return isValid
    }

    /*
    Show a Snackbar for feedback messages
    */
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    /*
    Dialog to re-enter current password
    */
    private fun initialisePasswordDialog() {
        dialog = Dialog(requireContext(),R.style.CustomDialog)
        val view = EntercurrentpasswordlayoutBinding.inflate(LayoutInflater.from(requireContext()), null, false)
        dialog.setContentView(view.root)
        // Set the dialog window attributes
        dialog.window?.setLayout(
            1000,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(

            ContextCompat.getDrawable(requireContext(), R.drawable.dialogbg)
        )
        var isPasswordVisible = false

        // Toggle visibility for password input
        view.togglePasswordVisibility.setOnClickListener {
            isPasswordVisible = togglePasswordVisibility(
                view.passwordInput,
                view.togglePasswordVisibility,
                isPasswordVisible
            )
        }

        // Close dialog
        view.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCancelable(false)

        view.confirmButton.setOnClickListener {
            var password = view.passwordInput.text.toString()
            if (password.length >= 8) {

                togglePasswordInputState(false,view)
                if (verifyUserPassword(password, appUser!!.userPassword)) {
                    hideKeyboard(requireContext(),it)

                    /*
                    now calling app write method to update password
                     */
                    lifecycleScope.launch {
                        if (withContext(Dispatchers.IO) {
                                Appwrite.updateUserPassword(password, binding.newpassword.text.toString())
                            }
                        ) {
                            // Hide input layout and show completed layout
                            view.passwordInputLayout.visibility = View.GONE
                            view.completedLayout.visibility = View.VISIBLE

                            // Update the user in ViewModel
                            withContext(Dispatchers.IO) {
                                viewModel.updateUser(
                                    User(
                                        id = appUser!!.id,
                                        userName = appUser!!.userName,
                                        userEmail = appUser!!.userEmail,
                                        userPassword = encryptPassword(binding.newpassword.text.toString())
                                    )
                                )

                                getUserDetails()
                            }
                            password=""
                            // Allow dialog to be cancellable
                            dialog.setCancelable(true)
                        }
                    }

                } else {
                    view.passwordInput.error="incorrect Password"
                    showSnackbar("Incorrect password.")
                    togglePasswordInputState(true,view)
                }
            } else {
                view.passwordInput.error = "Password must be at least 8 characters long"
                showSnackbar("Password must be at least 8 characters long")
            }
        }
        dialog.setOnDismissListener{
            if( view.completedLayout.visibility == View.VISIBLE){
                findNavController().navigate(R.id.action_changePassword_to_userProfile)
            }
        }

        dialog.show()
    }
    /*
  method to get user  user details
   */
    private  fun getUserDetails(){

        viewModel.getAllUsers()
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.allUsers.collect{
                    users->
                for (u in users){
                    withContext(Dispatchers.Main){
                        appUser=u

                    }
                }

            }
        }
    }
    /*
    Method to verify the user password
    */
   fun verifyUserPassword(enteredPassword: String,_key:String): Boolean {
        Log.d("mytag", "verifyUserPassword: ${encryptPassword(enteredPassword)},${_key}")
        return BCrypt.verifyer().verify(enteredPassword.toCharArray(), _key).verified;
    }
    /*
    Extension function to convert dp to pixels
*/
    private fun Int.dpToPx(): Int {
        return (this * requireContext().resources.displayMetrics.density).toInt()
    }

    /*
   Method to show and hide keyboard
    */
    fun hideKeyboard(context: Context, view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    /*
    Method to show keyboard.
     */
    fun showKeyboard(context: Context, editText: EditText) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        editText.requestFocus()
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
    /*
Method to toggle view states and visibility
*/
    private fun togglePasswordInputState(enable: Boolean,binding: EntercurrentpasswordlayoutBinding) {

        binding.passwordInput.isEnabled = enable
        binding.togglePasswordVisibility.isEnabled = enable
        binding.confirmButton.isEnabled = enable
        binding.confirmButton.alpha = if (enable) 1.0f else 0.5f
        binding.closeButton.isEnabled = enable
        binding.closeButton.alpha = if (enable) 1.0f else 0.5f

        // Toggle visibility of the loading layout
        binding.loadinglayout.visibility = if (enable) View.GONE else View.VISIBLE
    }

    /*
  method to ahs the password
   */
    private fun encryptPassword(password:String):String{
        val hashedPassword: String =
            BCrypt.withDefaults().hashToString(12,password.toCharArray())
        return hashedPassword
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Reverts to default behavior

    }

}
