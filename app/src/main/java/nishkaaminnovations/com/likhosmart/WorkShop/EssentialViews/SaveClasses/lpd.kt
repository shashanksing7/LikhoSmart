package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses


import android.graphics.Path
import com.google.gson.*
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.PaintProperties
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.likhoPath
import java.lang.reflect.Type


class lpd : JsonDeserializer<likhoPath> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): likhoPath {
        val jsonObject = json.asJsonObject
        return likhoPath(
            path = Path(), // Deserialize properly based on the custom path representation
            properties = context.deserialize(jsonObject.get("properties"), PaintProperties::class.java),
            isLocked = jsonObject.get("isLocked").asBoolean,
            linkName = jsonObject.get("linkName").asString,
            linkUrl = jsonObject.get("linkUrl").asString,
            xOffSet = jsonObject.get("xOffSet").asFloat,
            yOffSet = jsonObject.get("yOffSet").asFloat,
            prevXOffSet = jsonObject.get("prevXOffSet").asFloat,
            prevYOffSet = jsonObject.get("prevYOffSet").asFloat,
            xml = jsonObject.get("xml").asString,
            pathName = jsonObject.get("pathName").asString
        )
    }
}