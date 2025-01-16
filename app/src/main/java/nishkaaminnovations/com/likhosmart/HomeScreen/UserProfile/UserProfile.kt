package nishkaaminnovations.com.likhosmart.HomeScreen.UserProfile

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModelFactory
import nishkaaminnovations.com.likhosmart.DataBase.appwrite.Appwrite
import nishkaaminnovations.com.likhosmart.MainActivity
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.databinding.FragmentUserProfileBinding
import nishkaaminnovations.com.likhosmart.databinding.LogoutdialoglayoutBinding
import java.io.File

/*
This class will be used to handle the user profile operations.
 */

class UserProfile : Fragment() {

    /*
    Variable used for representing the binding of this frgament
     */
    private lateinit var binding:FragmentUserProfileBinding
    private val viewModel: DocumentViewModel by viewModels {
        DocumentViewModelFactory(
            requireActivity().application
        )
    }
    private  lateinit var  dialog:Dialog
    /*
   Variable to represent the location of the user image
    */
    private lateinit var imageFile: File
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentUserProfileBinding.inflate(inflater, container, false)
        imageFile=File(requireContext().filesDir, "Notes" + File.separator + "userProfile.jpg")
        loadProfileImage()
        /*
       Getting user Details
        */
        getUserDetails()
        binding.backtodocspagebutton.setOnClickListener{
            findNavController().navigate(R.id.action_userProfile_to_documentsFragment)
        }
        binding.editProfileButton.setOnClickListener{
            findNavController().navigate(R.id.action_userProfile_to_editUserProfile)
        }
        /*
        Taking user to change password screen
         */
        binding.userprofilepasswordbutton.setOnClickListener{
            findNavController().navigate(R.id.action_userProfile_to_changePassword)
        }
        binding.userprofilelogoutbutton.setOnClickListener{
            showLogoutDialog {
                if(withContext(Dispatchers.IO){
                    Appwrite.logout()
                    }){
                    showSnackbar("Logging out")
                   val intent= Intent(requireContext(),MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                else{
                    showSnackbar("Failed to log out")
                }
            }
        }
        return binding.root
    }

    /*
  Show a Snackbar for feedback messages
  */
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    /*
  Method to load the user profile image
   */
    private fun  loadProfileImage(){
        if(imageFile.exists()){
            binding.profileImage.setImageURI(Uri.fromFile(imageFile))
        }
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
                        binding.userprofilename.setText(u.userName)

                    }
                }

            }
        }
    }
    private fun showLogoutDialog(onLogout: suspend () -> Unit) {
        // Create a dialog
        val dialog = Dialog(requireContext(), R.style.CustomDialog)
        // Use View Binding to inflate the layout
        val binding = LogoutdialoglayoutBinding.inflate(LayoutInflater.from(requireContext()), null, false)
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        // Set the dialog window attributes
        dialog.window?.setLayout(
            1000,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(

            ContextCompat.getDrawable(requireContext(), R.drawable.dialogbg)
        )

        // Set click listeners
        binding.btnCancel.setOnClickListener {
            dialog.dismiss() // Close the dialog on Cancel
        }

        binding.btnLogout.setOnClickListener {
            dialog.dismiss() // Close the dialog
            // Use CoroutineScope to call the suspend function
            CoroutineScope(Dispatchers.Main).launch {
                onLogout() // Trigger logout action
            }
        }

        // Show the dialog
        dialog.show()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Reverts to default behavior

    }

}