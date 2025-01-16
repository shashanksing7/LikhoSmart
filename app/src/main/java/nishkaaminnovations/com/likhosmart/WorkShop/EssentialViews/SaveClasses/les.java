package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses;

/*
Helper class that will be used to save and retrieve the Likho edittext .
 */
public class les {
    private Float x;
    private Float y;
    private Float rotationalAngle;
    private  String text;
    private  String fileName;
    private String linkUrl;
    private String linkName;


    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Float getRotationalAngle() {
        return rotationalAngle;
    }

    public void setRotationalAngle(Float rotationalAngle) {
        this.rotationalAngle = rotationalAngle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }
}
