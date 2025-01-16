package nishkaaminnovations.com.likhosmart.DataBase

import androidx.room.*

@Dao
interface userDao {

    // Insert and replace user if conflict occurs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM user_table WHERE userName = :userName")
    suspend fun getUserByUserName(userName: String): User?

    @Query("SELECT * FROM user_table")
    suspend fun getAllUsers(): List<User>

    @Query("UPDATE user_table SET userEmail = :userEmail WHERE userName = :userName")
    suspend fun updateUserEmail(userName: String, userEmail: String)

    @Query("UPDATE user_table SET userSmartPoints = :userSmartPoints WHERE userName = :userName")
    suspend fun updateUserSmartPoints(userName: String, userSmartPoints: Int)

    @Query("UPDATE user_table SET userStreak = :userStreak WHERE userName = :userName")
    suspend fun updateUserStreak(userName: String, userStreak: Int)

    @Query("UPDATE user_table SET userPassword = :userPassword WHERE userName = :userName")
    suspend fun updateUserPassword(userName: String, userPassword: String)

    @Query("UPDATE user_table SET lastEditedDocument = :lastEditedDocument WHERE userName = :userName")
    suspend fun updateLastEditedDocument(userName: String, lastEditedDocument: String?)

    @Query("UPDATE user_table SET numberOfDocuments = :numberOfDocuments WHERE userName = :userName")
    suspend fun updateNumberOfDocuments(userName: String, numberOfDocuments: Int)
}
