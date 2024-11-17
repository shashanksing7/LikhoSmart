package nishkaaminnovations.com.likhosmart.HomeScreen


import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.R
import androidx.core.content.ContextCompat


class DocRecyclerAdapter(private val documentList: MutableList<docModel>):
    RecyclerView.Adapter<DocRecyclerAdapter.DocRecyclerHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocRecyclerHolder {
       val itemView=LayoutInflater.from(parent.context).inflate(
           R.layout.docsitemlayout
       ,parent,false)
        return DocRecyclerHolder(itemView)
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


    class DocRecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private  lateinit var popupWindow: PopupWindow
        private val coverImage: ImageView = itemView.findViewById<ImageView>(R.id.addImage)
        private val titleText: TextView = itemView.findViewById<TextView>(R.id.titleText)
        private val favoriteButton: ImageButton =
            itemView.findViewById<AppCompatImageButton>(R.id.favoriteButton)
        private val createDoc=itemView.findViewById<AppCompatImageButton>(R.id.addImage)

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
                createDoc.isEnabled=false
                createDoc.visibility=View.GONE
                titleText.visibility=View.GONE
            }

                // Set favorite button click listener
                favoriteButton.setOnClickListener { v: View? -> }
                /*
                setting the click listener on the create new Doc button.
                 */
                createDoc.setOnClickListener{view->
                    showDialog(view)
//
                }
        }

        /*
        Method to create a dialogBox that will be used to create a new document.
         */
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

            // Show the dialog
            dialog.show()
        }


    }

}