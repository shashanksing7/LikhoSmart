package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import com.google.gson.*
import java.lang.reflect.Type
class PaintPropertiesSerializer : JsonSerializer<PaintProperties> {
    override fun serialize(src: PaintProperties, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("color", src.color)
        jsonObject.addProperty("strokeWidth", src.strokeWidth)
        jsonObject.addProperty("strokeStyle", src.strokeStyle.name)
        jsonObject.addProperty("alpha", src.alpha)

        // Serialize PathEffect (handle DashPathEffect only)
        when (src.pathEffect) {
            is DashPathEffect -> {
                val intervalsAndPhase = getDashPathEffectData(src.pathEffect)
                jsonObject.add("pathEffect", JsonObject().apply {
                    addProperty("type", "DashPathEffect")
                    add("intervals", JsonArray().apply {
                        intervalsAndPhase.first.forEach { add(it) }
                    })
                    addProperty("phase", intervalsAndPhase.second)
                })
            }
            else -> jsonObject.add("pathEffect", JsonNull.INSTANCE)
        }

        jsonObject.addProperty("cap", src.cap.name)
        jsonObject.addProperty("join", src.join.name)

        // Serialize PorterDuff.Mode (only handles CLEAR or null)
        jsonObject.addProperty("porterDuffMode", if (src.xfermode != null) "CLEAR" else null)

        return jsonObject
    }

    private fun getDashPathEffectData(dashPathEffect: DashPathEffect): Pair<FloatArray, Float> {
        return Pair(floatArrayOf(10f, 10f), 0f) // Replace with actual values used to create the DashPathEffect
    }
}
