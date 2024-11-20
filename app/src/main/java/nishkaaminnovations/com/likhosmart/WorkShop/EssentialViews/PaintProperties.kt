package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
/*
This class  will represent the paint properties of the path
 */
class PaintProperties (   val color: Int = Color.BLACK,
                          val strokeWidth: Float = 5f,
                          val strokeStyle: Paint.Style = Paint.Style.STROKE,
                          val alpha: Int = 255,
                          val pathEffect: PathEffect? = null,
                          val shader: Shader? = null,
                          val cap: Paint.Cap = Paint.Cap.ROUND,
                          val join: Paint.Join = Paint.Join.ROUND,
                          val xfermode: PorterDuffXfermode? = null ) {


}