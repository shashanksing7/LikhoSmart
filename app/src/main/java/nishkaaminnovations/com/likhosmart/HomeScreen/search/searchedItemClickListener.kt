package nishkaaminnovations.com.likhosmart.HomeScreen.search

import nishkaaminnovations.com.likhosmart.DataBase.docModel

/*
This interface will be used to open the user selected document form search in workshop.
 */

interface searchedItemClickListener {
    fun itemClicked(docModel: docModel)
}