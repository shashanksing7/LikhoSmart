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
    @Query("select * from docModel order by createdDate ASC ")
    suspend fun getdocListByDate():List<docModel>



}