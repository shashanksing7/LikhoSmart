package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses

/*
Helper class that will be used to save and retrieve the Likho edittext .
 */
class lis {
    private var x: Float? = null
    private var y: Float? = null
    private var rotationalAngle: Float? = null
    private var uri: String? = null
    private var fileName: String? = null
    private var linkUrl: String? = null
    private  var height:Int?=null
    private  var width:Int?=null
    private var linkName: String? = null

    fun getX(): Float? {
        return x
    }

    fun setX(x: Float?) {
        this.x = x
    }
    fun getheight(): Int? {
        return height
    }

    fun setheight(x: Int?) {
        height = x
    }
    fun getwidth(): Int? {
        return width
    }

    fun setwidth(x: Int?) {
        width = x
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
    fun getLinkUrl(): String {
        return linkUrl!!
    }

    fun setLinkUrl(linkUrl: String?) {
        this.linkUrl = linkUrl
    }

    fun getLinkName(): String {
        return linkName!!
    }

    fun setLinkName(linkName: String?) {
        this.linkName = linkName
    }
}