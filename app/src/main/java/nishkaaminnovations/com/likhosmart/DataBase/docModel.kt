package nishkaaminnovations.com.likhosmart.DataBase

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import nishkaaminnovations.com.likhosmart.HomeScreen.DocType
import java.util.Date
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize // Using this for Parcelable implementation instead of @VersionedParcelize
data class docModel(
    @PrimaryKey var name: String,
    val docType: DocType,  // Ensure DocType is Parcelable or Serializable
    var createdDate: Date = Date(), // Store date as timestamp (Long)
    val docLocation: String = "Null",
    var noOfPages: Int = 1,
    var color: String = "#BCD1D2", // Default color set to white
    var isSearched:Boolean=false,
    var folder:String=""
) : Parcelable
