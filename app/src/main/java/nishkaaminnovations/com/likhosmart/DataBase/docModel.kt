package nishkaaminnovations.com.likhosmart.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey
import nishkaaminnovations.com.likhosmart.HomeScreen.DocType
import java.util.Date
@Entity()
data class docModel(@PrimaryKey val name:String,
                    val docType:DocType,
                    val createdDate: Date=Date(),
                    val docLocation:String="Null") {
}