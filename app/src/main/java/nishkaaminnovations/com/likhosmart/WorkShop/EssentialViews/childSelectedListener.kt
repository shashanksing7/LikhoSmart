package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.view.View
/*
This interface will be usd to represent the interface that will act as a call back
when ever a child is selected.
 */
interface childSelectedListener{
    fun getSelectedChild(currentChildType:CustomLayout.ViewType,isChildSelected:Boolean,isLocked:Boolean)
}