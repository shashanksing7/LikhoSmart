package nishkaaminnovations.com.likhosmart.HomeScreen.UserProfile

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModelFactory
import nishkaaminnovations.com.likhosmart.DataBase.appwrite.Appwrite
import nishkaaminnovations.com.likhosmart.DataBase.User
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.databinding.FragmentEditUserProfileBinding
import nishkaaminnovations.com.likhosmart.databinding.ImagepickerdialoglayoutBinding
import java.io.File
import java.io.IOException


class EditUserProfile : Fragment() {
    /*
    binding
     */
    private lateinit var binding:FragmentEditUserProfileBinding
    /*
   ViewModel variable.
    */
    private val viewModel: DocumentViewModel by viewModels {
        DocumentViewModelFactory(
            requireActivity().application
        )
    }
    /*
    Variable to represent the user retrieved
     */
    private lateinit var appUser: User
    /*
    Activity Result Launcher.
     */
    private var  cameraResultLauncher:ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            if (data != null && data.extras != null) {
                val imageBitmap = data.extras?.get("data") as Bitmap
                /*
                Calling the ucrop by passing the image uri
                 */
                initialiseUcrop(createImageUri(imageBitmap))
            }
        }
    }
    /*
     Declare the UCrop launcher
     */
    private val uCropResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val resultUri = data?.let { UCrop.getOutput(it) }
                if (resultUri != null) {
                    binding.profileImage.setImageURI(resultUri)
                    loadProfileImage()
                } else {
                    showSnackbar("Unable to get cropped image")
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = result.data?.let { UCrop.getError(it) }
                cropError?.printStackTrace()
                showSnackbar("Error while cropping image")
            }
        }
    /*
    Image picker launcher.
     */
    private var imagePickerLauncher: ActivityResultLauncher<Intent>?=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        results->
        if(results.resultCode== RESULT_OK){
            val data=results.data
            val inputUri:Uri?=data?.data
            /*
            Creating a input and output stream to write the file in the image view
             */
            val inputStream=requireContext().contentResolver.openInputStream(inputUri!!)
            inputStream.use { input->
                imageFile.outputStream().use {
                    output->
                    input?.copyTo(output)
                    loadProfileImage()
                }
            }


        }
    }
    /*
    Variable to represent the location of the user image
     */
    private lateinit var imageFile:File
    /*
    Variable to represent the current photo path
     */
    private  var currentPhotoPath=""
    /*
    Variable to represent the bottom sheet dialog.
     */
    private  lateinit var bottomDialog:BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*
        Inflating
         */
        binding=FragmentEditUserProfileBinding.inflate(inflater,container,false)
        imageFile=File(requireContext().filesDir, "Notes" + File.separator + "userProfile.jpg")
        loadProfileImage()
        /*
        Getting user Details
         */
        getUserDetails()
        showImageDialog()
        /*
        Initialising the client and Account variable in AppWrite.
        */
        Appwrite.init(requireContext())
        /*
        Initialising the click listener for back to profile screen
         */
        binding.backtouserprofilebutton.setOnClickListener{
            findNavController().navigate(R.id.action_editUserProfile_to_userProfile)
        }
        /*
        Initialising the click listener for back to profile screen
         */
        binding.editProfileButton.setOnClickListener{
            bottomDialog.show()
        }
        /*
        Initialising the click listener
         */
        binding.saveChanges.setOnClickListener{
            /*
            validating user input
             */
            if(verifyInput(binding.firstNameInput.text.toString(),binding.emailInput.text.toString(),binding.confirmPassword.text.toString())){
                /*
                user input valid check password
                 */
               lifecycleScope.launch {
                   verifyUserDetails(binding.firstNameInput.text.toString(),binding.emailInput.text.toString(),binding.confirmPassword.text.toString(),binding.emailInput.text.toString()==appUser.userEmail)
               }
            }

        }
        return  binding.root
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

    /*
  method to verify user password
   */
    private suspend fun verifyUserDetails(newName:String,newEmail:String,password:String,isEmailSame:Boolean){
        var  updated=false
         /*
            Disabling views
                */
        toggleState(false)
            withContext(Dispatchers.Main){

                if(verifyUserPassword(binding.confirmPassword.text.toString(),appUser.userPassword)){
                    /*
                    Password match now changing on server
                     */
                    if(newName==appUser.userName){
                        showSnackbar("Enter new Details")
                        /*
                      Disabling views
                       */
                        toggleState(true)
                    }
                    else{
                         updated=withContext(Dispatchers.IO){
                            Appwrite.updateUserDetails(newName,newEmail,password,isEmailSame)
                        }
                    }
                    /*
                      Disabling views
                       */
                    toggleState(true)
                    if(updated){
                        viewModel.updateUser(User(id=appUser.id,userName = binding.firstNameInput.text.toString(), userEmail = binding.emailInput.text.toString(), userPassword = appUser.userPassword))
                        getUserDetails()
                        showSnackbar("Changes Applied")
                    }else{
                        showSnackbar("Unable to Update")
                    }
                    /*
                    Updating in database
                     */
                }
                else{
                    /*
                 Disabling views
                  */
                    toggleState(true)
                    showSnackbar("Password didn't match")
                }
            }
    }
    /*
Method to verify the user password
*/
    suspend fun verifyUserPassword(enteredPassword: String,_key:String): Boolean {
        return BCrypt.verifyer().verify(enteredPassword.toCharArray(), _key).verified;
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
                        binding.firstNameInput.setText(u.userName)
                        binding.emailInput.setText(u.userEmail)
                    }
                }

            }
        }
    }

    /*
    Method to toggle view visibility
     */
    private fun toggleState(enable: Boolean) {
        binding.firstNameInput.isEnabled = enable
        binding.emailInput.isEnabled = enable
        binding.confirmPassword.isEnabled = enable
        binding.saveChanges.isEnabled = enable
        binding.firstNameInput.alpha = if (enable) 1.0f else 0.5f
        binding.emailInput.alpha = if (enable) 1.0f else 0.5f
        binding.confirmPassword.alpha = if (enable) 1.0f else 0.5f
        binding.saveChanges.alpha = if (enable) 1.0f else 0.5f
        binding.loadinglayout.visibility = if (enable) View.INVISIBLE else View.VISIBLE
    }
    /*
    Method to create the image file,the image captured will be saved in this location
     */
    @Throws(IOException::class)
    private fun createImageUri(userImage:Bitmap):Uri{
        imageFile.outputStream().use{
            userImage.compress(Bitmap.CompressFormat.JPEG,100,it)
        }
      return Uri.fromFile(imageFile)
    }
    /*
    Method to initialise the Ucrop instance.
     */
    private fun initialiseUcrop(uri:Uri){
        val uCrop = UCrop.of(uri,uri)
            .withAspectRatio(1f, 1f) // Set aspect ratio (optional)
            .withMaxResultSize(1439, 3042) // Set max result size (optional)
            .withOptions(UCrop.Options().apply {
                setToolbarTitle("Crop Image")
                setStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.white))
                setToolbarColor(ContextCompat.getColor(requireActivity(), R.color.white))
                setActiveControlsWidgetColor(ContextCompat.getColor(requireActivity(), R.color.charcoal_black))
                setCropFrameColor(ContextCompat.getColor(requireActivity(), R.color.muted_blue))
                setCropGridColor(ContextCompat.getColor(requireActivity(), R.color.muted_blue))
                setCropGridCornerColor(ContextCompat.getColor(requireActivity(), R.color.muted_blue))
                setDimmedLayerColor(ContextCompat.getColor(requireActivity(), R.color.applightgrey))
                setLogoColor(ContextCompat.getColor(requireActivity(), R.color.black))
                setToolbarWidgetColor(ContextCompat.getColor(requireActivity(), R.color.black))
            })
            .start(requireContext(),uCropResultLauncher)
    }

    /*
    Method to load the user profile image
     */
    private fun  loadProfileImage(){
        if(imageFile.exists()){
            binding.profileImage.setImageResource(R.drawable.userprofile)
            binding.profileImage.setImageURI(Uri.fromFile(imageFile))
        }
    }
    /*
    Method to read image pick data.
     */
    private fun pickImage(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/*")
        imagePickerLauncher?.launch(intent)
    }
    /*
    Method to hsow the bottom sheet dialog view
     */
    private fun showImageDialog(){
        bottomDialog=BottomSheetDialog(requireContext())
        /*
        inflating the view for bottom
         */
        val view=ImagepickerdialoglayoutBinding.inflate(LayoutInflater.from(requireContext()),null,false)
        view.camerabutton.setOnClickListener{

            val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            bottomDialog.dismiss()
            cameraResultLauncher.launch(intent)


        }
        view.gallerybutton.setOnClickListener {
            bottomDialog.dismiss()
            pickImage()

        }
        view.cancelButton.setOnClickListener{
            bottomDialog.dismiss()
        }
        bottomDialog.setContentView(view.root)
        bottomDialog.setCancelable(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Reverts to default behavior

    }
}