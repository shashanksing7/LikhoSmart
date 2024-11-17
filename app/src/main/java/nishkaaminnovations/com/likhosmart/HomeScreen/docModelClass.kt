package nishkaaminnovations.com.likhosmart.HomeScreen

enum class DocType{
    Create_New,
    PDF_Doc,
    Folder_Doc,
    Image_doc
}

enum class chipIds(val chipTYpe:String) {
    CHIP_DATE("Date"),
    CHIP_NAME("Name"),
    CHIP_TYPE("Type")
}