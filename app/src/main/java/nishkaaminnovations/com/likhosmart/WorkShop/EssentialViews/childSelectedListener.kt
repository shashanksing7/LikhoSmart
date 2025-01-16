package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

/*
This interface will be usd to represent the interface that will act as a call back
when ever a child is selected.
 */
interface childSelectedListener{
    fun getSelectedChild(currentChildType:cl.ViewType, isChildSelected:Boolean, isLocked:Boolean)
    fun addPopUpChild(currentChildType:cl.ViewType)
}