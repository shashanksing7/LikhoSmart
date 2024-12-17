package nishkaaminnovations.com.likhosmart.HomeScreen


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.R
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nishkaaminnovations.com.likhosmart.HomeScreen.Documents.docRecyclerCallBack


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

        fun bind(document:docModel) {
            /*
            Here Assigning the text to the  crete new element of the list and disabling
             the create button and text in rest
             */

            if(document.docType==DocType.Create_New){
                titleText.setText(document.docType.toString())
                favoriteButton.visibility=View.GONE

            }
            else{
//                val drawable = ContextCompat.getDrawable(context, R.drawable.notebook)  // Get Drawable
//                createDoc.setImageDrawable(drawable)
              createDoc.isEnabled=false
                createDoc.visibility=View.GONE
                titleText.visibility=View.GONE
                cardView.setBackgroundResource(R.drawable.notebook)
                // Set the background tint list of the CardView
                cardView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.soft_orange))
                relativeLayout.background = null

            }
            cardView.setOnClickListener {
                // Handle the click event here
                docRecyclerCallBack.openDoc(document)
            }
                // Set favorite button click listener
                favoriteButton.setOnClickListener { v: View? -> }
                /*
                setting the click listener on the create new Doc button.
                 */
                createDoc.setOnClickListener{view->
                    showDialog(view)

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
                showCreationDialog(it)
            }

            folderButton.setOnClickListener {
                // Handle folder creation

                dialog.dismiss()
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
        fun showCreationDialog(view: View) {
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
                        if(docRecyclerCallBack.canDocNameBeAssigned(notebookNameEditText.text.toString())) docRecyclerCallBack.addNewDoc(notebookNameEditText.text.toString(),DocType.PDF_Doc)
                    }

                    dialog.dismiss()
                } else {
                    // Show an error if the notebook name is empty
                }
            }

            // Display the dialog
            dialog.show()
        }

    }

}