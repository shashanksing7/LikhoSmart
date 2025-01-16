package nishkaaminnovations.com.likhosmart.HomeScreen.search

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.HomeScreen.Documents.docRecyclerCallBack
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.databinding.LastediteddocitemBinding

class SearchLastDocumentAdapter (
    private val notebookList: MutableList<docModel>,private val docRecyclerCallBack: docRecyclerCallBack
) : RecyclerView.Adapter<SearchLastDocumentAdapter.SearchLastDocumentAdapter>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchLastDocumentAdapter {
        val binding = LastediteddocitemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SearchLastDocumentAdapter(binding,docRecyclerCallBack)
    }

    override fun onBindViewHolder(holder: SearchLastDocumentAdapter, position: Int) {
        val currentItem = notebookList[position]
        holder.addContentToUi(currentItem)
    }

    override fun getItemCount(): Int {
        return notebookList.size
    }
    fun setData(newDocList: List<docModel>){
        notebookList.clear()
        notebookList.addAll(newDocList)
        Log.d("mytag", "getLastEdited:customSearch newDoc size: ${newDocList.size}")
        Log.d("mytag", "getLastEdited:customSearch notebook size: ${notebookList.size}")
        notifyDataSetChanged()
    }


    // ViewHolder class to hold item layout\
    class SearchLastDocumentAdapter (private val binding: LastediteddocitemBinding,private  val docRecyclerCallBack: docRecyclerCallBack) : RecyclerView.ViewHolder(binding.root) {
        /*
        Method to add content to the items
         */
        fun addContentToUi(docModel: docModel){
            /*
              Parsing the color.
            */
            val colorString = docModel.color // This is the hex code from your docModel
            val parsedColor = Color.parseColor(colorString)
            /*
             Set data to views
             */
          binding.itemImage.setImageResource(R.drawable.notebookhh)
          binding.itemImage.imageTintList= ColorStateList.valueOf(parsedColor)
          binding.notebookname.text = docModel.name
          binding.notebookType.text = docModel.docType.toString()
          binding.lastEdietdDate.text = docModel.createdDate.toString()
          binding.root.setOnClickListener{
                docRecyclerCallBack.openDoc(docModel)
            }
        }
    }
}