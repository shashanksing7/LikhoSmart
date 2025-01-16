package nishkaaminnovations.com.likhosmart.HomeScreen


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.nikartm.button.FitButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.HomeScreen.Documents.docRecyclerCallBack
import nishkaaminnovations.com.likhosmart.R
import top.defaults.colorpicker.ColorPickerView
import java.io.File


class DocRecyclerAdapter(private val documentList: MutableList<docModel>,private val docRecyclerCallBack: docRecyclerCallBack,private val context: Context):
    RecyclerView.Adapter<DocRecyclerAdapter.DocRecyclerHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocRecyclerHolder {
       val itemView=LayoutInflater.from(parent.context).inflate(
           R.layout.docsitemlayout
       ,parent,false)
        return DocRecyclerHolder(itemView,docRecyclerCallBack,context)
    }

    override fun getItemCount(): Int {
        return  documentList.size

    }

    override fun onBindViewHolder(holder: DocRecyclerHolder, position: Int) {

        val document: docModel = documentList[position]
        holder.bind(document) // Bind the data to the ViewHolder
    }
    /*
    Function to set data.
     */
    fun setData(newData: List<docModel>) {
        documentList.clear()
        documentList.addAll(newData)
        notifyDataSetChanged() // Notify the adapter to refresh the RecyclerView
    }


    class DocRecyclerHolder(itemView: View,private val docRecyclerCallBack: docRecyclerCallBack,private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private  lateinit var popupWindow: PopupWindow
        private val coverImage: ImageView = itemView.findViewById<ImageView>(R.id.addImage)
        private val titleText: TextView = itemView.findViewById<TextView>(R.id.titleText)
        private val favoriteButton: ImageButton =
            itemView.findViewById<AppCompatImageButton>(R.id.favoriteButton)
        private val createDoc=itemView.findViewById<AppCompatImageButton>(R.id.addImage)
        private val cardView=itemView.findViewById<CardView>(R.id.cardView)
        private  val relativeLayout=itemView.findViewById<RelativeLayout>(R.id.relativeLayout)
        private val docEdit=itemView.findViewById<AppCompatImageButton>(R.id.editDoc)
        private  val docName=itemView.findViewById<TextView>(R.id.docName)

        fun bind(document:docModel) {
            /*
            Here Assigning the text to the  crete new element of the list and disabling
             the create button and text in rest
             */

            if(document.docType==DocType.Create_New){
                titleText.setText(document.docType.toString())
                favoriteButton.visibility=View.GONE
                docName.visibility=View.GONE
                docEdit.visibility=View.GONE

            }
            else{
                docName.visibility=View.VISIBLE
                docEdit.visibility=View.VISIBLE
              createDoc.isEnabled=false
                createDoc.visibility=View.GONE
                titleText.visibility=View.GONE
                docName.text=document.name
                when(document.docType){
                    DocType.Create_New -> {
                    }
                    DocType.NoteBook -> {
                        cardView.setBackgroundResource(R.drawable.notebookhh)
                    }
                    DocType.Folder ->{
                        cardView.setBackgroundResource(R.drawable.folder)
                    }
                    DocType.Image -> TODO()
                }
                // Set the background tint list of the CardView
                val colorString = document.color // This is the hex code from your docModel
                val parsedColor = Color.parseColor(colorString)
                cardView.backgroundTintList = ColorStateList.valueOf(parsedColor)
                relativeLayout.background = null

            }
            cardView.setOnClickListener {
                // Handle the click event here
                if (document.docType!==DocType.Create_New){
                    docRecyclerCallBack.openDoc(document)
                }
                else{
                    showDialog(it)
                }

            }
                // Set favorite button click listener
                favoriteButton.setOnClickListener { v: View? -> }
                /*
                setting the click listener on the create new Doc button.
                 */
                createDoc.setOnClickListener{view->
                    showDialog(view)

                }
            docEdit.setOnClickListener{
                showDocPopUp(document, favoriteButton)
            }
        }

        /*
        Method to create a dialogBox that will be used to create a new document.
         */
        @SuppressLint("MissingInflatedId")
        private fun showDialog(view: View) {
            // Create the dialog object with custom style
            val dialog = Dialog(view.context, R.style.CustomDialog)

            // Inflate the custom layout for the dialog
            val inflater = LayoutInflater.from(view.context)
            val dialogView = inflater.inflate(R.layout.createnewpopupwindow, null)

            // Set content view and style for the dialog
            dialog.setContentView(dialogView)
            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(view.context, R.drawable.dialogbg))

            // Make the dialog cancellable
            dialog.setCancelable(true)

            // Find buttons in the dialog layout and set click listeners
            val notebookButton = dialogView.findViewById<AppCompatImageButton>(R.id.journal)
            val folderButton = dialogView.findViewById<AppCompatImageButton>(R.id.folder)
            val imageButton = dialogView.findViewById<AppCompatImageButton>(R.id.image)
            val quickNoteButton = dialogView.findViewById<AppCompatImageButton>(R.id.quick)

            notebookButton.setOnClickListener {
                // Handle notebook creation

                dialog.dismiss()
                showCreationDialog(it,DocType.NoteBook)
            }

            folderButton.setOnClickListener {
                // Handle folder creation

                dialog.dismiss()
                showCreationDialog(it,DocType.Folder)
            }

            imageButton.setOnClickListener {
                // Handle image creation

                dialog.dismiss()
            }

            quickNoteButton.setOnClickListener {
                // Handle quick note creation

                dialog.dismiss()
            }

            // Show the dialog
            dialog.show()
        }
        fun showCreationDialog(view: View,docType:DocType) {
            // Create a dialog object with a custom style
            val dialog = Dialog(view.context, R.style.CustomDialog)

            // Inflate the custom layout
            val inflater = LayoutInflater.from(view.context)
            val dialogView = inflater.inflate(R.layout.newnotebooklayout, null)

            // Set the dialog's content view
            dialog.setContentView(dialogView)

            // Set the dialog window attributes
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawable(
                ContextCompat.getDrawable(view.context, R.drawable.popup_background)
            )
            dialog.setCancelable(false)

            // Get the buttons from the layout
            val cancelButton = dialogView.findViewById<AppCompatButton>(R.id.cancel)
            val createButton = dialogView.findViewById<AppCompatButton>(R.id.confirm)
            val notebookNameEditText = dialogView.findViewById<EditText>(R.id.notebook_name)

            // Set click listeners for the buttons
            cancelButton.setOnClickListener {
                // Dismiss the dialog when cancel button is clicked
                dialog.dismiss()
            }

            createButton.setOnClickListener {
                // Get the entered notebook name
                val notebookName = notebookNameEditText.text.toString().trim()

                if (notebookName.isNotEmpty()) {
                    // Perform the notebook creation logic
                    CoroutineScope(Dispatchers.Main).launch {
                        if(docRecyclerCallBack.canDocNameBeAssigned(notebookNameEditText.text.toString())) docRecyclerCallBack.addNewDoc(notebookNameEditText.text.toString(),docType)
                    }

                    dialog.dismiss()
                } else {
                    // Show an error if the notebook name is empty
                }
            }

            // Display the dialog
            dialog.show()
        }

        /*
        Method to show a pop up window to edit the document .
         */
        fun showDocPopUp(document:docModel, anchorView: View) {
            // Inflate the popup_layout
            val inflater = LayoutInflater.from(context)
            val popupView = inflater.inflate(R.layout.doceditlayout, null)

            // Create PopupWindow instance
            val popupWindow = PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true)

            // Set elevation for the popup
            popupWindow.elevation = 10f

            // Find buttons inside the popup layout
            val btnShare = popupView.findViewById<FitButton>(R.id.btn_share)
            val btnDelete = popupView.findViewById<FitButton>(R.id.btn_delete)
            val btnRename = popupView.findViewById<FitButton>(R.id.btn_rename)
            val btnColor = popupView.findViewById<FitButton>(R.id.btn_color)

            // Set click listeners for buttons
            btnShare.setOnClickListener {
                popupWindow.dismiss()
            }

            btnDelete.setOnClickListener {
                showEditDocPopup(context,"Delete",document)
                popupWindow.dismiss()
            }

            btnRename.setOnClickListener {
                showEditDocPopup(context,"Rename",document)
                popupWindow.dismiss()
            }

            btnColor.setOnClickListener {
                popupWindow.dismiss()
                showColorPickerPopup(document)
            }

            // Show the popup window anchored to the provided view
            popupWindow.showAsDropDown(anchorView, 0, 0) // Adjust offset if needed
        }


        @SuppressLint("MissingInflatedId")
        fun showEditDocPopup(context: Context, action:String,document: docModel) {
            // Inflate the custom layout
            val popupView = LayoutInflater.from(context).inflate(R.layout.newnotebooklayout, null)

            // Create the PopupWindow
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // Focusable so it handles clicks
            )
            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.viewpager2))
            // Set views and their behaviors inside the popup
            val notebookNameEditText = popupView.findViewById<EditText>(R.id.notebook_name)
            val cancelButton = popupView.findViewById<AppCompatButton>(R.id.cancel)
            val confirmButton = popupView.findViewById<AppCompatButton>(R.id.confirm)
            val titleText=popupView.findViewById<TextView>(R.id.titleText)


            if(action=="Delete"){
                confirmButton.text="Delete"
                titleText.setText("Are sure you want to delete "+document.name+"?")
                notebookNameEditText.isEnabled=false
                notebookNameEditText.visibility=View.GONE
            }
            else{
                confirmButton.text="Rename"
                titleText.setText("Are sure you want to Rename ?")
            }
            cancelButton.setOnClickListener {
                popupWindow.dismiss() // Close the popup
            }

            confirmButton.setOnClickListener {
                val notebookName = notebookNameEditText.text.toString().trim()
                if (notebookName.isEmpty()) {

                    if(action=="Delete"){
                        docRecyclerCallBack.deleteDoc(document)

                        val file=File(document.docLocation)
                        if (file.exists()) {

                            Log.d("deleteee", "File deleteed or not =${deleteFolder(file)}.")
                        } else {
                            Log.d("deleteee", "File does not exist.")
                        }

                        popupWindow.dismiss()
                    }

                } else {
                    val oldName=File(document.docLocation)
                    val newName = File(context.filesDir, "Notes" + File.separator +notebookName)
                    docRecyclerCallBack.updateDoc(document.name,notebookName,newName.absolutePath)
                    oldName.renameTo(newName)
                    popupWindow.dismiss()
                }
            }
            // Show the popup window in the middle of the screen
            val parentView = View(context) // Temporary view for the root
            popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0)
        }
        // Method to show the popup window
        private fun showColorPickerPopup(document: docModel) {
            // Inflate the popup layout
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R.layout.colorpickerdialog, null)
            // Create the PopupWindow
            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )
            /*
        Setting the color of global variable and the selected paint image button
         */
            var tempColor:Int=Color.BLACK
            // Set the background of the popup
            popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.viewpager2))

            // Show the PopupWindow at the center of the screen
            // Show the popup window in the middle of the screen
            val parentView = View(context)
            popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0)

            // Handle Save Button click
            val saveButton = popupView.findViewById<Button>(R.id.saveButton)
            saveButton.setOnClickListener { v: View? ->
                val colorString = "#${Integer.toHexString(tempColor)}"
                docRecyclerCallBack.updateDocColor(document.name,colorString)

                popupWindow.dismiss()
            }
            val coloPicker=popupView.findViewById<ColorPickerView>(R.id.colorpicker)
            coloPicker.subscribe{
                    color,fromUser,shouldPropagate->
                tempColor=color
            }
        }
        fun deleteFolder(fileOrDirectory: File): Boolean {
            if (fileOrDirectory.isDirectory) {
                val children = fileOrDirectory.listFiles()
                if (children == null) {
                    println("Failed to list files in: ${fileOrDirectory.absolutePath}")
                    return false
                }
                for (child in children) {
                    val success = deleteFolder(child)
                    if (!success) {
                        println("Failed to delete: ${child.absolutePath}")
                        return false
                    }
                }
            }
            val success = fileOrDirectory.delete()
            if (!success) {
                println("Failed to delete: ${fileOrDirectory.absolutePath}")
            }
            return success
        }

    }

}