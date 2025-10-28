package nishkaaminnovations.com.likhosmart.WorkShop

enum class textEditingButtonType(val value:String?) {
    BOLD(null),
    ITALIC(null),
    FONT("DEFAULT"),
    COLOR(null),
    ALIGNMENT(null),
    HYPERLINK(null),
    STRIKE_THROUGH(null),
    UNDERLINE(null);
    /*
    muted variable to hold the actual value.
     */
    private var dynamicValue:String?=value;
    /*
    method to set the dynamic value.
     */
    private  fun setDynamicValue(newValue:String){
        dynamicValue=newValue
    }

    /*
    method to get the value variable.
     */
//    private fun getDynamicValue()=dynamicValue
}