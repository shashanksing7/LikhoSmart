package nishkaaminnovations.com.likhosmart.DataBase

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentRepository(var context: Context) {

    private val  dataBase:DocumentDataBase;
    private  val docModelDAO:DocModelDAO;
    init {

        dataBase=DocumentDataBase.getDataBase(context);
        docModelDAO=dataBase.getDocumentDAO();
    }

    /*
    Method to get all data form the DocTable,by name.
     */
    suspend fun getAllDataByName() {
       return  withContext(Dispatchers.IO){
//           docModelDAO.getdocListByName()
       }
    }
    /*
    Method to get all data form the DocTable,by date.
     */
    suspend fun getAllDataByDate():List<docModel>{
        return  withContext(Dispatchers.IO){
            docModelDAO.getdocListByDate()
        }
    }
    /*
    Method to insert data in table.
     */
    suspend fun insertData(docModel: docModel){
        withContext(Dispatchers.IO){
            docModelDAO.insertData(docModel)
        }
    }
    /*
    Method to update data in table.
     */
    suspend fun UpDateData(docModel: docModel){
        withContext(Dispatchers.IO){
            docModelDAO.updateData(docModel)
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
        return withContext(Dispatchers.IO){
            docModelDAO.getDoc(docName)
        }
    }


}