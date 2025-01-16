package nishkaaminnovations.com.likhosmart.HomeScreen.search

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.Nullable
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.DataBase.DocumentViewModel
import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.HomeScreen.DocType
import nishkaaminnovations.com.likhosmart.HomeScreen.Documents.docRecyclerCallBack
import nishkaaminnovations.com.likhosmart.databinding.CustomsearchviewBinding


class CustomSearchView : LinearLayout,docRecyclerCallBack {
    private lateinit var viewmodel:DocumentViewModel
    private lateinit var searchResultAdapter:SearchResultAdapter
    private val docList: MutableList<docModel> = mutableListOf() // or you can initialize it with some data
    private lateinit var searchItemClickListener: searchedItemClickListener
    private lateinit var docObj:docModel
    private var docLastEditedList:MutableList<docModel> = mutableListOf()
    private lateinit var searchLastDocumentAdapter:SearchLastDocumentAdapter

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context)
    }
    private  lateinit var binding:CustomsearchviewBinding
    private fun init(context: Context) {
        /*
         Inflate the custom layout
         */
         binding=CustomsearchviewBinding.inflate(LayoutInflater.from(context),this,true)

        /*
         Set click listener on search card view
         */
       binding.customsearchcardview.setOnClickListener{ toggleOverlay()}
        /*
         initialising the searchResultAdapter and setting the adatpter to the recyler view
         */
        searchResultAdapter=SearchResultAdapter(docList,this)
        binding.customsearchviewSearchsuggestion.layoutManager=LinearLayoutManager(context)
        binding.customsearchviewSearchsuggestion.adapter=searchResultAdapter
        /*
        Initialising the last EditedAdapter
         */
        searchLastDocumentAdapter=SearchLastDocumentAdapter(docLastEditedList,this)
        binding.RecentRecycler.layoutManager=LinearLayoutManager(context)
        binding.RecentRecycler.adapter=searchLastDocumentAdapter
        /*
        initialising the editext change listener
         */
        initialiseSuggestion()
        /*
        Method to hode the added layout buttons
         */
        hideSelectedNoteBookViews()
        /*
        Initialising the click listener on the added layout
         */
        binding.selectedNoteBookLayout.ladteditedmainlayout.setOnClickListener{
            searchItemClickListener.itemClicked(docObj)
        }
        /*
        Initialising the click listener on the .
         */
        binding.gobackButton.setOnClickListener{
            hideSearchLayout()
            binding.customsearchcardview.visibility = VISIBLE
            hideKeyboard(context,binding.searchText)
            docList.clear()
            searchResultAdapter.setData(docList)
        }
        /*
        Deleting the text from  search edittext
         */
        binding.customsearchviewclear.setOnClickListener{
            var text=""
            binding.searchText.setText(text)
        }
    }
    /*
    Method to extract last edited document from dataBase.
     */
    private  fun getLastEdited(){
        viewmodel.getLastEdited()
        CoroutineScope(Dispatchers.IO).launch {
            viewmodel.LastEditedDocuments.collect{
                data->
                withContext(Dispatchers.Main){
                    searchLastDocumentAdapter.setData(data)
                }
            }
        }
    }
    /*
    Method to toggle the view in layout
     */
    private fun toggleOverlay() {
        if (binding.overlayLayout!!.visibility == GONE) {
            /*
            Add an animation to fade in the view
             */
            val fadeIn = AlphaAnimation(0f, 1f)
            fadeIn.duration = 500 // Duration in milliseconds

            binding.overlayLayout!!.visibility = VISIBLE
            binding.overlayLayout.animation=fadeIn
            showKeyboard(context,binding.searchText)
            /*
            Hidin the search cardview
             */
            binding.customsearchcardview.visibility= GONE
        }
    }
    /*
    Method to set and get viewmodel.
     */
    fun setViewmodel(viewModel: DocumentViewModel){
        this.viewmodel=viewModel
        /*
        Method read last editedt documents
        */
       getLastEdited()
       getPreviousSearch()

    }
    /*
    Method to set and get searchItemClickListener.
     */
    fun setSearchItemClickListener(searchedItemClickListener: searchedItemClickListener){
        this.searchItemClickListener=searchedItemClickListener
    }
    private fun initialiseSuggestion() {
        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Called before the text is changed
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // Called as the text is being changed
            }

            override fun afterTextChanged(editable: Editable?) {
                // Called after the text has been changed
                // Check if the user has entered any text
                val query = editable.toString().trim()
                if (query.isNotEmpty()) {
                    // Call the method to search in the database
                    getSearchSuggestion(query)
                    binding.customsearchviewclear.visibility= VISIBLE
                    binding.searchresulttext.visibility= GONE
                }
                else{
                    getPreviousSearch()
                    binding.customsearchviewclear.visibility= GONE
                    binding.searchresulttext.visibility= VISIBLE
                }
            }
        })

    }
    /*
    Method to perform the database operation.
     */
    fun getSearchSuggestion(searchString: String) {
        viewmodel.searchDocsByName(searchString)
        CoroutineScope(Dispatchers.Main).launch {
            viewmodel.suggestionData.collect{
                suggestionData->
                searchResultAdapter.setData(suggestionData)
            }
        }
    }
    /*
    Method to get the previous search suggestion
     */
    private fun getPreviousSearch(){
        viewmodel.getRecentSearch()
        CoroutineScope(Dispatchers.IO).launch{
            viewmodel.recentSuggestionData.collect{
                recentSearchSuggestionData->
                withContext(Dispatchers.Main){
                    searchResultAdapter.setData(recentSearchSuggestionData)
                }
            }
        }
    }
    /*
    This method wiil be called when the user clicks on the suggested item
     */
    override fun openDoc(docModel: docModel)
    {
        if(binding.selectedNoteBookLayout.ladteditedmainlayout.isInvisible==false){
            searchItemClickListener.itemClicked(docModel)
            Log.d("mytag", "openDoc: ${binding.selectedNoteBookLayout.ladteditedmainlayout.isInvisible==false}")
        }
        else{
            docObj=docModel
            modifySelectedLayout(docModel)
            hideKeyboard(context,binding.searchText)
        }
    }
    /*
    Method to modify the selected layout
     */
    fun  modifySelectedLayout(docModel: docModel){
        /*
      Updating the document in database since it has been visited
       */
        updateDoc(docModel)
        CoroutineScope(Dispatchers.IO).launch {
            viewmodel.isUpdated.collect { isUpdated ->
                if (isUpdated) {
                    withContext(Dispatchers.Main) {
                        setViewsVisibility()
                        docList.clear()
                        searchResultAdapter.setData(docList)
                        /*
                        Parsing the color.
                         */
                        val colorString = docModel.color // This is the hex code from your docModel
                        val parsedColor = Color.parseColor(colorString)
                        /*
                        Setting the data in the added layout
                         */
                        binding.selectedNoteBookLayout.notebookname.text=docModel.name
                        binding.selectedNoteBookLayout.notebookType.text=docModel.docType.toString()
                        binding.selectedNoteBookLayout.lastEdietdDate.text=docModel.createdDate.toString()
                        binding.selectedNoteBookLayout.itemImage.imageTintList = ColorStateList.valueOf(parsedColor)
                    }
                }
            }
        }

    }
    /*
    Method to update the doc
     */
    fun updateDoc(docModel: docModel){
        docModel.isSearched=true
        viewmodel.upDateDocCompletely(docModel)
    }
    private fun setViewsVisibility() {
        /*
        Making the result text,added layut and search cardview visible
         */
        binding.resulttext.visibility =VISIBLE
        binding.selectedNoteBookLayout.ladteditedmainlayout.visibility= VISIBLE
        binding.customsearchcardview.visibility = VISIBLE
        hideSearchLayout()

    }

    /*
    Method to hide the search layout
     */
    private fun hideSearchLayout(){
        /*
Making the custom search view disappear and adding animation.
 */
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 500 // Duration in milliseconds
        binding.overlayLayout.visibility =GONE
    }

    /*
    Method to hide th added layout files
     */
    private fun hideSelectedNoteBookViews() {
        binding.selectedNoteBookLayout.ladteditedmainlayout.visibility= GONE
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
UnUsed methods
 */
    override suspend fun addNewDoc(docName: String, docType: DocType) {
        TODO("Not yet implemented")
    }

    override suspend fun canDocNameBeAssigned(docName: String): Boolean {
        TODO("Not yet implemented")
    }
    override fun deleteDoc(docModel: docModel) {
        TODO("Not yet implemented")
    }

    override fun updateDoc(oldName: String, newName: String, docLocation: String) {
        TODO("Not yet implemented")
    }

    override fun updateDocColor(name: String, color: String) {
        TODO("Not yet implemented")
    }

}
