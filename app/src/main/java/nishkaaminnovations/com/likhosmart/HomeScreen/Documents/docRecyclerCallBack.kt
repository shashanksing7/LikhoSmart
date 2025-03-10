package nishkaaminnovations.com.likhosmart.HomeScreen.Documents

import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.HomeScreen.DocType

interface docRecyclerCallBack {
   suspend fun addNewDoc(docName:String,docType: DocType)
   suspend fun canDocNameBeAssigned(docName: String):Boolean
   fun openDoc(docModel: docModel)
   fun deleteDoc(docModel: docModel)
   fun updateDoc(oldName:String,newName:String,docLocation:String)
   fun updateDocColor(name: String, color: String)


}