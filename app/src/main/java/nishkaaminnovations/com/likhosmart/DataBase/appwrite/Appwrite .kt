package nishkaaminnovations.com.likhosmart.DataBase.appwrite

import android.content.Context
import android.util.Log
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.User
import io.appwrite.services.Account



/*
Singleton object for app write client.
 */
object Appwrite {
    private const val ENDPOINT = "https://cloud.appwrite.io/v1"
    private const val PROJECT_ID = "6718f32a00385d9505f5"

    private var client: Client? = null
    private lateinit var account: Account

    fun init(context: Context): Client {
        // Initialize client if not already done
        if (client == null) {
            synchronized(this) {
                if (client == null) {
                    client = Client(context)
                        .setEndpoint(ENDPOINT)
                        .setProject(PROJECT_ID)
                }
            }
        }
        // Initialize account after client
        account = Account(client!!)
        return client!!
    }
    // Register a new user, create their account and login automatically
    suspend fun register(email: String, password: String, name: String):User<Map<String, Any>>?{
        return try {
            // Create user
            account.create(ID.unique(), email, password, name)
            // Log the user in after successful registration
            login(email, password)
        } catch (e: AppwriteException) {
            // Handle registration error (e.g., user already exists)
            null
        }
    }
    // Login a user with email and password
    suspend fun login(email: String, password: String):User<Map<String, Any>>?{
        return try {
            // Create a session using email and password
            account.createEmailPasswordSession(email, password)
            // Return the logged-in user
            getLoggedIn()
        } catch (e: AppwriteException) {
            // Handle login error (e.g., incorrect credentials)
            Log.d("mytag", "login: ${e.toString()}")
            null
        }
    }

    // Get details of the currently logged-in user
    suspend fun getLoggedIn():
            User<Map<String, Any>>?{
        return try {
            // Fetch the logged-in user's information
           account.get()
        } catch (e: AppwriteException) {
            // Handle error (e.g., not logged in)
           null
        }
    }
    // Logout the user and delete the session
    suspend fun logout(): Boolean {
        return try {
            account.deleteSession("current")  // Logout the current session
            true
        } catch (e: AppwriteException) {
            false
        }
    }

    suspend fun updateUserDetails( newName: String, newEmail: String, currentPassword: String,isEmailSame:Boolean):Boolean {
        // Update Name
        try {
            account.updateName(
                name = newName
            )
            return if(isEmailSame){
                true
            } else{
                updateEmail(newEmail,currentPassword)
            }



        } catch (e: AppwriteException) {
            Log.d("mytag", "updateEmail: ${e.toString()}")
            return false
        }
    }

    private suspend fun updateEmail(newEmail: String, currentPassword: String):Boolean {
        try {
             val user=  account.updateEmail(
                email = newEmail,
                password = currentPassword
            )
             return  user!=null

        } catch (e: AppwriteException) {
            Log.d("mytag", "updateEmail: ${e.toString()}")
           return false
        }
    }

    /*
    Method to update user password.
     */
    suspend fun updateUserPassword(oldPassword:String,newPassword:String):Boolean{
        return try{
           val result= account.updatePassword(
                password = newPassword,
                oldPassword = oldPassword
            )
            true
        }
        catch (e:Exception){
            Log.d("mytag ", "updateUserPassword: ${e.toString()}")
            false
        }
    }

}
