package nishkaaminnovations.com.likhosmart.DataBase

import android.content.Context
import android.util.Log
import at.favre.lib.crypto.bcrypt.BCrypt
import io.appwrite.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.DataBase.appwrite.Appwrite


class DocumentRepository(var context: Context) {

    private val dataBase: DocumentDataBase
    private val docModelDAO: DocModelDAO
    private val userDAO: userDao
    /*
    Variable to represent the client of the app write sdk
     */
    private  lateinit var client:Client


    init {
        // Initialize the database instance and DAOs
        dataBase = DocumentDataBase.getDataBase(context)
        docModelDAO = dataBase.getDocumentDAO()
        userDAO = dataBase.getUserDAO()
        client=Appwrite.init(context)
    }
    /*
    Method to register user in AppWrite server.
     */
    suspend fun registerUserInApWrite(user: User): Boolean {
        return withContext(Dispatchers.IO){
           val result= Appwrite.register(user.userEmail, user.userPassword, user.userName)
            /*
           Hashing the password before saving it
            */
            val _key=encryptPassword(user.userPassword)
            try{
                insertUser(User(userName = result!!.name, userEmail = result!!.email, userPassword =_key))
            }
            catch (e:Exception){

                Log.d("mtag", "loginUserInApWrite:error = ${e.toString()}")
            }
            result!=null
        }
    }
    /*
  Method to register user in AppWrite server.
   */
    suspend fun loginUserInApWrite(email:String, password:String): Boolean {
        return withContext(Dispatchers.IO){
            val result=Appwrite.login(email,password)
            /*
            Hashing the password before saving it
             */
            val _key=encryptPassword(password)
            try{
                insertUser(User(userName = result!!.name, userEmail = result!!.email, userPassword =_key))
            }
            catch (e:Exception){

                Log.d("mtag", "loginUserInApWrite:error = ${e.toString()}")
            }
            result!=null
        }

    }
    /*
    Method to get user id logged in or not
     */
    suspend fun getUserLoggedIn():Boolean{
        return withContext(Dispatchers.IO){
            Appwrite.getLoggedIn()!=null
        }

    }


    /*
    Method to get all data form the DocTable,by name.
     */
    suspend fun getAllDataByName() {

//           docModelDAO.getdocListByName()

    }
    /*
    Method to get all data form the DocTable,by date.
     */
    suspend fun getAllDataByDate():List<docModel>{
        return docModelDAO.getdocListByDate()

    }

    /*
   Method to get all data form the DocTable,by date.
    */
    suspend fun getFolderData(userFolder: String):List<docModel>{
        return docModelDAO.getFolderData(userFolder)

    }
    /*
  Method to get all data form the DocTable,by date.
   */
    suspend fun searchDocsByName(searchString: String): List<docModel>{
        return withContext(Dispatchers.IO){
            docModelDAO.searchDocsByName(searchString)
        }

    }

    /*
    Method to get recent search suggestion.
     */
    suspend fun getRecentSearchSuggestion():List<docModel>{
        return withContext(Dispatchers.IO){
            docModelDAO.getTop10RecentSearchSuggestionByDate()
        }
    }
    /*
    Method to insert data in table.
     */
    suspend fun insertData(docModel: docModel){
        withContext(Dispatchers.IO) {
            docModelDAO.insertData(docModel)
        }
    }
    /*
   method to completely update the docModel
    */
    suspend fun UpdateDocData(docModel: docModel){
        withContext(Dispatchers.IO){
            docModelDAO.updateData(docModel)
        }
    }
    /*
    Method to extract maximum 15 last edited documents
     */
  suspend  fun getLastEdited():List<docModel>{
        return withContext(Dispatchers.IO){
            docModelDAO.getTop15DocsByDate()
        }
    }
    /*
   Method to Delete data in table.
    */
    suspend fun DeleteData(docModel: docModel){
        withContext(Dispatchers.IO){
            docModelDAO.deleteData(docModel)
        }
    }
    /*
    Method to get the specific doc form table by name.
     */
  suspend fun getDoc(docName: String):docModel?{
        return docModelDAO.getDoc(docName)

    }
    /*
   Method to update the color of a specific document
*/
   suspend fun updateDocColor(name: String, color: String){
       withContext(Dispatchers.IO){
           docModelDAO.updateDocColor(name,color)
       }
    }

    /*
Method to update the color of a specific document
*/
    suspend fun updateNoOfPages(name: String, noOfPages: Int){
        withContext(Dispatchers.IO){
            docModelDAO.updateDocNoOfPages(name,noOfPages)
        }
    }

    /*
    Method to update the document in database.
     */
    suspend fun upDateDoc(oldName:String,newName:String,docLocation:String){
        withContext(Dispatchers.IO){
            docModelDAO.updateDocDetails(oldName,newName,docLocation)
        }
    }

    // Insert user
    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
       withContext(Dispatchers.IO){
           Log.d("mtag", "insertUser: ")
           userDAO.insertUser(user)
       }
    }

    // Update entire user
    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        try{
            userDAO.updateUser(user)
            Log.d("mytag", "updateUser: ${user.userName}")
        }
        catch(c:Exception){
            Log.d("mytag", "updateUser: ${c.toString()}")
        }
    }

    // Delete user
    suspend fun deleteUser(user: User) = withContext(Dispatchers.IO) {
        userDAO.deleteUser(user)
    }

    // Get user by userName
    suspend fun getUserByUserName(userName: String): User? = withContext(Dispatchers.IO) {
        userDAO.getUserByUserName(userName)
    }

    // Get all users
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        userDAO.getAllUsers()
    }

    suspend fun updateUserEmail(userName: String, userEmail: String) = withContext(Dispatchers.IO) {
        userDAO.updateUserEmail(userName, userEmail)
    }

    suspend fun updateUserSmartPoints(userName: String, userSmartPoints: Int) = withContext(Dispatchers.IO) {
        userDAO.updateUserSmartPoints(userName, userSmartPoints)
    }

    suspend fun updateUserStreak(userName: String, userStreak: Int) = withContext(Dispatchers.IO) {
        userDAO.updateUserStreak(userName, userStreak)
    }

    suspend fun updateUserPassword(userName: String, userPassword: String) = withContext(Dispatchers.IO) {
        userDAO.updateUserPassword(userName, userPassword)
    }
    suspend fun updateLastEditedDocument(userName: String, lastEditedDocument: String?) = withContext(Dispatchers.IO) {
        userDAO.updateLastEditedDocument(userName, lastEditedDocument)
    }

    suspend fun updateNumberOfDocuments(userName: String, numberOfDocuments: Int) = withContext(Dispatchers.IO) {
        userDAO.updateNumberOfDocuments(userName, numberOfDocuments)
    }
    /*
    method to ahs the password
     */
    private fun encryptPassword(password:String):String{
        val hashedPassword: String =
            BCrypt.withDefaults().hashToString(12,password.toCharArray())
       return hashedPassword
    }
}