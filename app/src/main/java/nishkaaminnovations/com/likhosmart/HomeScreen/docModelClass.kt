package nishkaaminnovations.com.likhosmart.HomeScreen

enum class DocType{
    Create_New,
    NoteBook,
    Folder,
    Image
}

enum class chipIds(val chipTYpe:String) {
    CHIP_DATE("Date"),
    CHIP_NAME("Name"),
    CHIP_TYPE("Type")
}