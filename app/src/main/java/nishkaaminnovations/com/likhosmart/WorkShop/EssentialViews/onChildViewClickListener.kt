package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.view.View

public interface onChildViewClickListener {
    fun onViewClicked(view: View?, viewType:cl.ViewType, isLocked:Boolean, isChildSelected:Boolean, link:String, linkText:String, pathBottom:Float, pathRight:Float)
    fun isDrawingOn():Boolean
    fun getPageNumber():String
    fun getDocumenLocation():String

}