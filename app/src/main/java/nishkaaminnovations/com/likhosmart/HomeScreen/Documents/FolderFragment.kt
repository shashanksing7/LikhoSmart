package nishkaaminnovations.com.likhosmart.HomeScreen.Documents

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModelFactory
import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.HomeScreen.DocRecyclerAdapter
import nishkaaminnovations.com.likhosmart.HomeScreen.DocType
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.WorkShop.WorkShopArgs
import nishkaaminnovations.com.likhosmart.databinding.FragmentFolderBinding
import java.io.File
import java.util.Date


class FolderFragment : Fragment(),docRecyclerCallBack  {
    /*
    Variable to represent the binding of the folder fragment
     */
    private lateinit var binding:FragmentFolderBinding
    /*
    Variable to represent the name of the folder
     */
    private var folderName="Folder Name"
    // Access the arguments using Safe Args
    private val args by navArgs<FolderFragmentArgs>()
    /*
    Variable to represent the file object of the current folder
     */
    private lateinit var folderFILE:File
    /*
   Required variables
    */
    private val viewModel: DocumentViewModel by viewModels {
        DocumentViewModelFactory(
            requireActivity().application
        )
    }

    private lateinit var docRecyclerAdapter: DocRecyclerAdapter

    /*
 Folders name
  */
    private val typeCanvasName = "likhoCanvas"
    private val typeAudioName = "likhoAudio"
    private val typeImageName = "likhoImage"
    private var typeEditTextName: String = "likhoEdit"

    private var documentList: MutableList<docModel> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentFolderBinding.inflate(inflater,container,false)

        /*
        Retrieving the folder name
         */
        folderName=args.folderNAME
        folderFILE=File(requireContext().filesDir, "Notes" + File.separator + folderName)
        binding.folderName.setText(folderName)

        /*
          setting up Recycler View
        */
        val numberOfColumns = 3 // Change this number to set how many columns you want in your grid
        binding.folderRecycler.layoutManager = GridLayoutManager(context, numberOfColumns)
        docRecyclerAdapter = DocRecyclerAdapter(documentList, this,requireContext())
       binding.folderRecycler.adapter = docRecyclerAdapter

        /*
      Getting folder data
       */
        CoroutineScope(Dispatchers.Main).launch{
            getFolderData()
        }
        return  binding.root
    }

    override suspend fun addNewDoc(docName: String, docType: DocType) {
        val baseFolder = File(folderFILE,docName)
        Log.d("", "addNewDoc: ")
        val subFolders = listOf(typeImageName, typeEditTextName, typeCanvasName, typeAudioName)

        // Create base folder and subfolders
        if (baseFolder.mkdirs()) {
            subFolders.forEach { folderName ->
                File(baseFolder, folderName).mkdirs()
            }
            // Create and insert document model
            val docModel = docModel(name = docName, docType =  docType, createdDate = Date(), docLocation =  baseFolder.absolutePath, noOfPages = 1, color = "#FF8A65", folder = folderName)
            viewModel.insertData(docModel)
            CoroutineScope(Dispatchers.Main).launch{
                viewModel.isInserted.collect{
                        isInserted->
                    if(isInserted){
                        getFolderData()
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
                val action =FolderFragmentDirections.actionFolderFragmentToWorkShop(docModel)
                findNavController().navigate(action)
            }
            DocType.Create_New -> TODO()
            DocType.Folder -> {
                val action =FolderFragmentDirections.actionFolderFragmentSelf(docModel.name)
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
                    getFolderData()
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
                    getFolderData()
                }
            }
        }
    }
    override fun updateDocColor(name: String, color: String) {
        viewModel.updateDocColor(name,color)
        CoroutineScope(Dispatchers.Main).launch{
            getFolderData()
        }
    }
    /*
    Method to retract the
     */
    /*
Method to get data from the database by sorting according to date and
always add a specific item at the beginning.
*/
    private  fun getFolderData() {
        viewModel.getFolderData(folderName).observe(viewLifecycleOwner) { doctList ->
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false) // Reverts to default behavior

    }

}