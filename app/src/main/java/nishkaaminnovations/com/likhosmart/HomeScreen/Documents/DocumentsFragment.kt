package nishkaaminnovations.com.likhosmart.HomeScreen.Documents

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModelFactory
import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.HomeScreen.DocRecyclerAdapter
import nishkaaminnovations.com.likhosmart.HomeScreen.DocType
import nishkaaminnovations.com.likhosmart.HomeScreen.chipIds
import nishkaaminnovations.com.likhosmart.databinding.FragmentDocumentsBinding
import java.io.File
import java.util.Date
import nishkaaminnovations.com.likhosmart.R

/*
This class wil represent the document screen of our App.
 */
class DocumentsFragment : Fragment(),docRecyclerCallBack {

    /*
    Required variables
     */
    private val viewModel: DocumentViewModel by viewModels {
        DocumentViewModelFactory(
            requireActivity().application
        )
    }
    /*
    Variable to represent the location of the user image
    */
    private lateinit var imageFile: File
    private lateinit var docRecyclerAdapter: DocRecyclerAdapter
    private var documentList: MutableList<docModel> = mutableListOf() // Proper initialization
    private lateinit var docBinding: FragmentDocumentsBinding

    /*
    Folders name
     */
    private val typeCanvasName = "likhoCanvas"
    private val typeAudioName = "likhoAudio"
    private val typeImageName = "likhoImage"
    private var typeEditTextName: String = "likhoEdit"

    /**
     * Array of permissions
     */
    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*
        Initialising the binding variable.
         */
        docBinding = FragmentDocumentsBinding.inflate(inflater, container, false)
        imageFile=File(requireContext().filesDir, "Notes" + File.separator + "userProfile.jpg")
        loadProfileImage()
        /*
       Getting user Details
        */
        getUserDetails()
        /*
        Initialising the user profile click button
         */
        docBinding.userProfileSettings.setOnClickListener{
            findNavController().navigate(R.id.action_documentsFragment_to_userProfile)
        }
        docBinding.searchDocumentCardView.setOnClickListener{
            findNavController().navigate(R.id.action_documentsFragment_to_searchview)
        }
        docBinding.smartinkcardview.setOnClickListener{
            findNavController().navigate(R.id.action_documentsFragment_to_smartink)
        }
        /*
        Asking for permission
         */
        requestPermission()
        CoroutineScope(Dispatchers.Main).launch{
            getDataByDate()
        }
        /*
          setting up Recycler View
        */
        val numberOfColumns = 3 // Change this number to set how many columns you want in your grid
        docBinding?.docRecycler?.layoutManager = GridLayoutManager(context, numberOfColumns)
        docRecyclerAdapter = DocRecyclerAdapter(documentList, this,requireContext())
        docBinding?.docRecycler?.adapter = docRecyclerAdapter
        initializeChipGroup()
        docBinding.getPointsCardView.setOnClickListener{
            findNavController().navigate(R.id.action_documentsFragment_to_smartpoints)
        }

        return docBinding.root
    }

    /*
    Method to initialize chip group
     */

    private fun initializeChipGroup() {
//        docBinding?.chipGroup?.setOnCheckedStateChangeListener { group, checkIds ->
//            if (checkIds.isNotEmpty()) {
//                val chipId = group.findViewById<Chip>(checkIds.first())
//
//                // Sort list based on selected chip
//                when (chipId.text) {
//                    chipIds.CHIP_DATE.chipTYpe -> {
//                        // Sort by date in descending order (latest first)
//                        documentList.sortByDescending { it.createdDate }
//                    }
//
//                    chipIds.CHIP_NAME.chipTYpe -> {
//                        // Sort by name alphabetically
//                        documentList.sortBy { it.name }
//                    }
//
//                    chipIds.CHIP_TYPE.chipTYpe -> {
//                        // Sort by type, you can define custom sorting logic if needed
//                        documentList.sortBy { it.docType }
//                    }
//                }
//                // Notify adapter of the data change after sorting
//                docRecyclerAdapter.notifyDataSetChanged()
//            }
//        }
    }

    /*
 Method to get data from the database by sorting according to date and
 always add a specific item at the beginning.
 */
    private  fun getDataByDate() {
        viewModel.getDataByDate().observe(viewLifecycleOwner) { doctList ->
            documentList.clear()  // Clear the existing list

            // Add the new item at the beginning
            val firstItem = docModel(name = "Create", docType =  DocType.Create_New,)
            documentList.add(0, firstItem)

            // Add the remaining items
            documentList.addAll(doctList)

            // Notify adapter of data change
            docRecyclerAdapter.notifyDataSetChanged()
        }
    }

    /*
    Method to check for permission .
     */
    private fun requestPermission() {
        Dexter.withContext(activity)
            .withPermissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()
    }

    override suspend fun addNewDoc(docName: String, docType: DocType) {
        val baseFolder = File(requireContext().filesDir, "Notes" + File.separator + docName)
        val subFolders = listOf(typeImageName, typeEditTextName, typeCanvasName, typeAudioName)


        // Create base folder and subfolders
        if (baseFolder.mkdirs()) {
            if(docType!=DocType.Folder){
                subFolders.forEach { folderName ->
                    File(baseFolder, folderName).mkdirs()
                }
            }
            // Create and insert document model
            val docModel = docModel(name = docName, docType =  docType, createdDate = Date(), docLocation =  baseFolder.absolutePath, noOfPages = 1, color = "#FF8A65",)
            viewModel.insertData(docModel)
            CoroutineScope(Dispatchers.Main).launch{
                viewModel.isInserted.collect{
                    isInserted->
                    if(isInserted){
                        getDataByDate()
                    }
                }
            }
        } else {
            // Handle error if base folder creation fails
        }
    }

    override suspend fun canDocNameBeAssigned(docName: String): Boolean {
        val doc = withContext(Dispatchers.IO) {
            viewModel.getDoc(docName)  // Suspend function call here
        }
        return doc == null  // Returns true if no document with the given name exists
    }

    override fun openDoc(docModel: docModel) {
        when(docModel.docType){
            DocType.NoteBook->{
                val action =DocumentsFragmentDirections.actionDocumentsFragmentToWorkShop(docModel)
                findNavController().navigate(action)
            }

            DocType.Create_New -> TODO()
            DocType.Folder -> {
                val action=DocumentsFragmentDirections.actionDocumentsFragmentToFolderFragment(docModel.name)
                findNavController().navigate(action)
            }

            DocType.Image -> TODO()
        }
    }
    override fun deleteDoc(docModel: docModel) {
        viewModel.deleteDoc(docModel)
        CoroutineScope(Dispatchers.Main).launch{
            viewModel.isDeleted.collect { isDeleted ->
                if (isDeleted) {
                    // Perform any necessary action after deletion is complete, e.g., refreshing the data
                    getDataByDate()

                }
            }
        }

    }
    override fun updateDoc(oldName:String,newName:String,docLocation:String){
        viewModel.updateDoc(oldName,newName,docLocation)
        CoroutineScope(Dispatchers.Main).launch{
           viewModel.isRenamed.collect{
               isRenamed->
               if(isRenamed){
                   Log.d("mytag", "updateDocColor: ")
                   getDataByDate()

               }
           }
        }
    }
    override fun updateDocColor(name: String, color: String) {
        viewModel.updateDocColor(name,color)
        CoroutineScope(Dispatchers.Main).launch{

            getDataByDate()

        }
    }
    /*
Method to load the user profile image
 */
    private fun  loadProfileImage(){
        if(imageFile.exists()){
           docBinding.profileImage.setImageURI(Uri.fromFile(imageFile))
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
                Log.d("mtag", "getUserDetails:${users} ")
                for (u in users){
                    withContext(Dispatchers.Main){
                        docBinding.userNameHome.setText(u.userName)

                    }
                }

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Reverts to default behavior

    }

}
