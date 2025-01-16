package nishkaaminnovations.com.likhosmart.DataBase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/*
This interface is used to represent the DAO of the docModel class that will provide implementation for the
methods to perform the operations on database.
 */
@Dao
 interface DocModelDAO {
    /*
    Method to insert data into the table.
     */
     @Insert
    suspend fun insertData(docModel:docModel);

    /*
    Method to delete The row from the table .
     */
    @Delete
   suspend fun deleteData(docModel: docModel)
    /*
    Method to update the row.
     */
    @Update
   suspend fun updateData(docModel: docModel)
   /*
   Method to extract specific doc
    */
   @Query("select * from docModel where name=:name")
  suspend fun getDoc(name:String):docModel?
        /*
     Method to get all the docs ordered by date.
      */
    @Query("select * from docModel Where folder = :folderName order by createdDate ASC ")
    suspend fun getdocListByDate(folderName:String=""):List<docModel>

    /*
  Method to get all the folder data.
*/
    @Query("SELECT * FROM docModel WHERE folder = :userFolder ORDER BY createdDate ASC")
    suspend fun getFolderData(userFolder: String): List<docModel>

    /*
   Method to update the color of a specific document
*/
    @Query("UPDATE docModel SET color = :color WHERE name = :name")
   suspend fun updateDocColor(name: String, color: String)
    /*
    Method to update the number of pages of a specific document
 */
    @Query("UPDATE docModel SET noOfPages = :noOfPages WHERE name = :name")
    suspend fun updateDocNoOfPages(name: String, noOfPages: Int)

        /*
    Method to update the name and document location.
    */
    @Query("UPDATE docModel SET name = :newName, docLocation = :newDocLocation WHERE name = :oldName")
    suspend fun updateDocDetails(oldName: String, newName: String, newDocLocation: String)
    /*
   Method to return a list of docModels that contain the given string in their name.
    */
    @Query("SELECT * FROM docModel WHERE name LIKE '%' || :searchString || '%'")
    suspend fun searchDocsByName(searchString: String): List<docModel>

    /*
  Method to get a maximum of 15 docs ordered by createdDate.
  */
    @Query("SELECT * FROM docModel ORDER BY createdDate DESC LIMIT 15")
    suspend fun getTop15DocsByDate(): List<docModel>

    /*
Method to get a maximum of 15 docs ordered by createdDate.
*/
    @Query("SELECT * FROM docModel WHERE isSearched=1 ORDER BY createdDate DESC LIMIT 10")
    suspend fun getTop10RecentSearchSuggestionByDate(): List<docModel>


}