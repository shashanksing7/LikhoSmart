package nishkaaminnovations.com.likhosmart.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/*
this class represents the database that will be used to perform the operations on the
room data base.it will return it's singleton object,and the objects of the DAO.
 */
@Database(entities =[docModel::class], version = 1)
@TypeConverters(Converters::class)
abstract class DocumentDataBase :RoomDatabase(){
    /*
    This will make sure that only one instance of the data base is created.
     */
    companion object{
        @Volatile
       private var instance:DocumentDataBase?=null;

        fun getDataBase(context: Context):DocumentDataBase{
            return  instance?: synchronized(this){
                val docDB = Room.databaseBuilder(
                    context.applicationContext,
                    DocumentDataBase::class.java,
                    "user_database"
                ).build()
                instance = docDB
                docDB
            }
        }
    }
    /*
    Method to make return the object of DocumentDAO.
     */
    abstract  fun getDocumentDAO():DocModelDAO
}