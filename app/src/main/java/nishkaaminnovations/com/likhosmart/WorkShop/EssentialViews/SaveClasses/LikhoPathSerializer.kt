package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses


import android.graphics.Path
import com.google.gson.*
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.likhoPath
import java.lang.reflect.Type

class LikhoPathSerializer : JsonSerializer<likhoPath> {
    override fun serialize(src: likhoPath, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.add("path", JsonPrimitive(src.path.toString())) // Replace with a custom path representation
        jsonObject.add("properties", context.serialize(src.properties))
        jsonObject.addProperty("isLocked", src.isLocked)
        jsonObject.addProperty("linkName", src.linkName)
        jsonObject.addProperty("linkUrl", src.linkUrl)
        jsonObject.addProperty("xOffSet", src.xOffSet)
        jsonObject.addProperty("yOffSet", src.yOffSet)
        jsonObject.addProperty("prevXOffSet", src.prevXOffSet)
        jsonObject.addProperty("prevYOffSet", src.prevYOffSet)
        jsonObject.addProperty("xml", src.xml)
        jsonObject.addProperty("pathName", src.pathName)
        return jsonObject
    }
}