package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses

/*
This class will help us save the Audio view and retrieve it .
 */
class likhoAudioSave {
    private var x: Float? = null
    private var y: Float? = null
    private var rotationalAngle: Float? = null
    private var uri: String? = null
    private var fileName: String? = null

    fun getX(): Float? {
        return x
    }

    fun setX(x: Float?) {
        this.x = x
    }

    fun getY(): Float? {
        return y
    }

    fun setY(y: Float?) {
        this.y = y
    }

    fun getRotationalAngle(): Float? {
        return rotationalAngle
    }

    fun setRotationalAngle(rotationalAngle: Float?) {
        this.rotationalAngle = rotationalAngle
    }

    fun getUri(): String? {
        return uri
    }

    fun setUri(uri:String?) {
        this.uri = uri
    }

    fun getFileName(): String? {
        return fileName
    }

    fun setFileName(fileName: String?) {
        this.fileName = fileName
    }
}