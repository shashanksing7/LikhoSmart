package nishkaaminnovations.com.likhosmart.DataBase

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    // Create a StateFlow to track the deletion status
    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> get() = _isDeleted

    // Create a StateFlow to track the renaming status
    private val _isRenamed = MutableStateFlow(false)
    val isRenamed: StateFlow<Boolean> get() = _isRenamed

    // Create a StateFlow to track the updating status
    private val _isUpdated = MutableStateFlow(false)
    val isUpdated: StateFlow<Boolean> get() = _isUpdated

    // Create a StateFlow to track the inserting status
    private val _isInserted = MutableStateFlow(false)
    val isInserted: StateFlow<Boolean> get() = _isInserted

    // Create a StateFlow sned result of search suggestion
    private val _suggestionData:MutableStateFlow<List<docModel>> = MutableStateFlow(emptyList())
    val suggestionData: StateFlow<List<docModel>> get() = _suggestionData

    // Create a StateFlow sned result of search suggestion
    private val _recentSuggestionData:MutableStateFlow<List<docModel>> = MutableStateFlow(emptyList())
    val recentSuggestionData: StateFlow<List<docModel>> get() = _recentSuggestionData

    // Create a StateFlow sed result of last edited documents
    private val _LastEditedDocuments:MutableStateFlow<List<docModel>> = MutableStateFlow(emptyList())
    val LastEditedDocuments: StateFlow<List<docModel>> get() =_LastEditedDocuments

    init{
        docRepository=DocumentRepository(application)
    }
    /*
    Method to register the user in the appwrite server.
     */
   suspend fun registerUser(user:User):Boolean {
     return   withContext(Dispatchers.IO){
            docRepository.registerUserInApWrite(user)
        }
    }
    /*
Method to register the user in the appwrite server.
 */
  suspend  fun loginUser(email:String, password:String):Boolean{
            return withContext(Dispatchers.IO){
                docRepository.loginUserInApWrite(email, password)
            }
    }

    /*
    Method to check if user is loggedIn or not
     */
    suspend fun getUserLoggedIn():Boolean{
       return withContext(Dispatchers.IO){
           docRepository.getUserLoggedIn()
        }
    }
    /*
    Method to insert data into the database.
     */
    fun insertData(docModel: docModel){
        viewModelScope.launch {
            docRepository.insertData(docModel)
            _isInserted.value=true
        }
    }
    /*
    Method to get the data from the database.
     */
   suspend fun getDoc(name:String): docModel? {
        return docRepository.getDoc(name)
    }
    fun getDataByDate():LiveData<List<docModel>> = liveData(Dispatchers.IO) {
        val data = docRepository.getAllDataByDate() // Assume this function exists in the repository
        emit(data)
    }
    fun getFolderData(userFolder: String):LiveData<List<docModel>> = liveData(Dispatchers.IO) {
        val data = docRepository.getFolderData(userFolder) // Assume this function exists in the repository
        emit(data)
    }
    fun searchDocsByName(searchString: String){
        viewModelScope.launch {
           val data= docRepository.searchDocsByName(searchString)
            _suggestionData.value=data
        }
    }

    fun getRecentSearch(){
        viewModelScope.launch {
            val data=docRepository.getRecentSearchSuggestion()
            _recentSuggestionData.value=data
        }
    }
    /*
 Method to update the color of a specific document
*/
  fun updateDocColor(name: String, color: String){
       viewModelScope.launch {
           docRepository.updateDocColor(name,color)
       }

    }
        /*
    Method to update the color of a specific document
    */
    fun updateNoOfPages(name: String, noOfPages: Int){
        viewModelScope.launch {
            docRepository.updateNoOfPages(name,noOfPages)
        }
    }
    fun deleteDoc(docModel: docModel){
        viewModelScope.launch {
            docRepository.DeleteData(docModel)
            _isDeleted.value=true
        }
    }
    fun updateDoc(oldName:String,newName:String,docLocation:String){
        viewModelScope.launch {
            docRepository.upDateDoc(oldName,newName,docLocation)
            _isRenamed.value=true

        }
    }
    fun upDateDocCompletely(docModel: docModel){
        viewModelScope.launch {
            docRepository.UpdateDocData(docModel)
            _isUpdated.value=true
        }
    }
    /*
    Method to extract maximum 15 last edited documents
     */
    fun getLastEdited(){
        viewModelScope.launch {
          val data=  docRepository.getLastEdited()
         Log.d("mytag", "getLastEdited: size ${data.size}")
         _LastEditedDocuments.value=data
        }
    }

    /**
     *
     * For user table
     */
    // StateFlows for observing results
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> get() = _allUsers

    private val _updateResult = MutableStateFlow(false)
    val updateResult: StateFlow<Boolean> get() = _updateResult

    /*
    Method to insert user into the database.
    Updates the `updateResult` StateFlow.
    */
   suspend fun insertUser(user: User) {
            try {
                docRepository.insertUser(user)
            } catch (e: Exception) {

            }
    }

    /*
    Method to update an entire user in the database.
    Updates the `updateResult` StateFlow.
    */
    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                docRepository.updateUser(user)
                _updateResult.value = true
            } catch (e: Exception) {
                _updateResult.value = false
            }
        }
    }

    /*
    Method to delete a user from the database.
    Updates the `updateResult` StateFlow.
    */
    fun deleteUser(user: User) {
        viewModelScope.launch {
            try {
                docRepository.deleteUser(user)
                _updateResult.value = true
            } catch (e: Exception) {
                _updateResult.value = false
            }
        }
    }

    /*
    Method to fetch a user by username.
    Updates the `user` StateFlow.
    */
    fun getUserByUserName(userName: String) {
        viewModelScope.launch {
            _user.value = docRepository.getUserByUserName(userName)
        }
    }

    /*
    Method to fetch all users from the database.
    Updates the `allUsers` StateFlow.
    */
    fun getAllUsers() {
        viewModelScope.launch {
            _allUsers.value =docRepository.getAllUsers()
        }
    }

    /*
    Method to update individual properties.
    Each method updates the `updateResult` StateFlow.
    */
    fun updateUserEmail(userName: String, userEmail: String) {
        viewModelScope.launch {
            try {
                docRepository.updateUserEmail(userName, userEmail)
                _updateResult.value = true
            } catch (e: Exception) {
                _updateResult.value = false
            }
        }
    }

    fun updateUserSmartPoints(userName: String, userSmartPoints: Int) {
        viewModelScope.launch {
            try {
                docRepository.updateUserSmartPoints(userName, userSmartPoints)
                _updateResult.value = true
            } catch (e: Exception) {
                _updateResult.value = false
            }
        }
    }

    fun updateUserStreak(userName: String, userStreak: Int) {
        viewModelScope.launch {
            try {
                docRepository.updateUserStreak(userName, userStreak)
                _updateResult.value = true
            } catch (e: Exception) {
                _updateResult.value = false
            }
        }
    }

    fun updateUserPassword(userName: String, userPassword: String) {
        viewModelScope.launch {
            try {
                docRepository.updateUserPassword(userName, userPassword)
                _updateResult.value = true
            } catch (e: Exception) {
                _updateResult.value = false
            }
        }
    }

    fun updateLastEditedDocument(userName: String, lastEditedDocument: String?) {
        viewModelScope.launch {
            try {
                docRepository.updateLastEditedDocument(userName, lastEditedDocument)
                _updateResult.value = true
            } catch (e: Exception) {
                _updateResult.value = false
            }
        }
    }

    fun updateNumberOfDocuments(userName: String, numberOfDocuments: Int) {
        viewModelScope.launch {
            try {
                docRepository.updateNumberOfDocuments(userName, numberOfDocuments)
                _updateResult.value = true
            } catch (e: Exception) {
                _updateResult.value = false
            }
        }
    }

}