package nishkaaminnovations.com.likhosmart.DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,  // Default value of 0
    var userName: String,
    var userEmail: String,
       var userSmartPoints: Int=0,
    var userStreak: Int=0,
    var userPassword: String,
    var lastEditedDocument: String?="No Documents Created",
    var numberOfDocuments: Int=0
)
