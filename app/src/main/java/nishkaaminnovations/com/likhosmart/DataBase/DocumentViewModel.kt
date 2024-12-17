package nishkaaminnovations.com.likhosmart.DataBase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/*
This class is a view model for the document data base.it will use coroutines to
perform database operations .
 */
class DocumentViewModel(application: Application) :AndroidViewModel(application) {
    /*
    This  variable will be used to store the database instance
     */
    private val  docRepository:DocumentRepository
    /*
    Variable for sending data back to the user.
     */
    private var _docData: MutableStateFlow<List<docModel>> = MutableStateFlow(emptyList())
    val docData: StateFlow<List<docModel>> = _docData

    init{
        docRepository=DocumentRepository(application)
    }
    /*
    Method to insert data into the database.
     */
    fun insertData(docModel: docModel){
        viewModelScope.launch {
            docRepository.insertData(docModel)
        }
    }
    /*
    Method to get the data from the database.
     */
   suspend fun getDoc(name:String): docModel? {
        return docRepository.getDoc(name)
    }

   suspend fun getDataByDate():LiveData<List<docModel>> = liveData(Dispatchers.IO) {
        val data = docRepository.getAllDataByDate() // Assume this function exists in the repository
        emit(data)
    }

    /*
 Method to update the color of a specific document
*/
   suspend fun updateDocColor(name: String, color: String){
        docRepository.updateDocColor(name,color)
    }
}