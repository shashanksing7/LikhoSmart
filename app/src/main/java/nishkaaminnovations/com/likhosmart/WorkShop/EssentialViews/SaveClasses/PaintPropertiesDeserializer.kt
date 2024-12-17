package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.graphics.drawable.shapes.Shape
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.RadialGradient
import com.google.gson.*
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.PaintProperties
import java.lang.reflect.Type

class PaintPropertiesDeserializer : JsonDeserializer<PaintProperties> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PaintProperties {
        val jsonObject = json.asJsonObject
        val color = jsonObject.get("color").asInt
        val strokeWidth = jsonObject.get("strokeWidth").asFloat
        val strokeStyle = Paint.Style.valueOf(jsonObject.get("strokeStyle").asString)
        val alpha = jsonObject.get("alpha").asInt

        // Deserialize PathEffect
        val pathEffect: PathEffect? = jsonObject.getAsJsonObject("pathEffect")?.let { pathEffectJson ->
            when (pathEffectJson.get("type").asString) {
                "DashPathEffect" -> {
                    val intervals = pathEffectJson.getAsJsonArray("intervals").map { it.asFloat }.toFloatArray()
                    val phase = pathEffectJson.get("phase").asFloat
                    DashPathEffect(intervals, phase)
                }
                else -> null
            }
        }

        // Deserialize PorterDuffXfermode (only handles CLEAR or null)
        val xfermode: PorterDuffXfermode? = jsonObject.get("porterDuffMode")?.let {
            if (it.asString == "CLEAR") PorterDuffXfermode(PorterDuff.Mode.CLEAR) else null
        }

        val cap = Paint.Cap.valueOf(jsonObject.get("cap").asString)
        val join = Paint.Join.valueOf(jsonObject.get("join").asString)

        return PaintProperties(
            color = color,
            strokeWidth = strokeWidth,
            strokeStyle = strokeStyle,
            alpha = alpha,
            pathEffect = pathEffect,
            cap = cap,
            join = join,
            xfermode = xfermode
        )
    }
}
