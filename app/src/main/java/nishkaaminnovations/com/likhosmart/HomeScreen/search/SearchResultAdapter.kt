package nishkaaminnovations.com.likhosmart.HomeScreen.search

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.HomeScreen.Documents.docRecyclerCallBack
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.databinding.SearchresultlayoutBinding

class SearchResultAdapter(private val docList:MutableList<docModel>,private val docRecyclerCallBack: docRecyclerCallBack) : RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = SearchresultlayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding,docRecyclerCallBack)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val doc = docList[position]
        holder.bind(doc)
    }

    override fun getItemCount(): Int {
        return docList.size
    }
    fun setData(newDocList: List<docModel>){
        docList.clear()
        docList.addAll(newDocList)
        notifyDataSetChanged()
    }

    class SearchResultViewHolder(private val binding: SearchresultlayoutBinding,private val docRecyclerCallBack: docRecyclerCallBack) : RecyclerView.ViewHolder(binding.root) {
        fun bind(doc: docModel) {
            // Set the image, text, and other attributes here
            binding.searchresulttext.text = doc.name
            if(doc.isSearched){
                binding.searchresultimage.setImageResource(R.drawable.history_svgrepo_com)
            }
            else{
                binding.searchresultimage.setImageResource(R.drawable.search_magnifying_glass_svgrepo_com)

            }
            binding.root.setOnClickListener{
                docRecyclerCallBack.openDoc(doc)
            }

        }
    }
}
