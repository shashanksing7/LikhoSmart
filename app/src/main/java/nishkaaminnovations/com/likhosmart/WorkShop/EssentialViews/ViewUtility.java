package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatEditText;

/*
This class wil contain all the view
 */
public class ViewUtility {
    /*
    This variable will represent the actual view.
     */
    private View view;
    /*
    Variables that will be used to store the initialX,initialY,initialHeight,initialWidth of the view
     */
    private float initialX, initialY, initialWidth, initialHeight;
    /*
    This variable will determine which corner was tapped by the user
     */
    private  int resizeCorner=-1;
    /*
      Offsets for dragging
     */
    private float dragOffsetX, dragOffsetY;
    private float cornerTolerance = 50;
    /*
    Variable for the rotation of the view.
     */
    private float lastTouchX;
    private  float lastTouchY;
    private  float centreX;
    private  float centreY;
    private  float rotationalAngle;
    /*
    Setting the view in constructor
     */
    ViewUtility(View view){
        this.view=view;
    }
    /*
    Method to decide which corner the user touched
     */
    public int getResizeCorner(MotionEvent event) {
        if (event.getX() < cornerTolerance && event.getY() < cornerTolerance)
            return 0; // Top-left
        if (event.getX() > view.getWidth() - cornerTolerance+10 && event.getY() < cornerTolerance+10)
            return 1; // Top-right (for rotation)
        if (event.getX() < cornerTolerance && event.getY() > view.getHeight() - cornerTolerance)
            return 2; // Bottom-left
        if (event.getX() > view.getWidth() - cornerTolerance && event.getY() > view.getHeight() - cornerTolerance)
            return 3; // Bottom-right
        return -1; // Not near any corner
    }
    /*
    Method to Handle the resize of the view.
     */
    public void handleResize(MotionEvent event) {
        float deltaX = event.getRawX() - initialX;
        float deltaY = event.getRawY() - initialY;

        int newWidth = (int) initialWidth;
        int newHeight = (int) initialHeight;

        // Resize logic based on the corner being dragged
        if (resizeCorner == 0) { // Top-left
            newWidth = (int) (initialWidth - deltaX);
            newHeight = (int) (initialHeight - deltaY);
        } else if (resizeCorner == 2) { // Bottom-left
            newWidth = (int) (initialWidth - deltaX);
            newHeight = (int) (initialHeight + deltaY);
        } else if (resizeCorner == 3) { // Bottom-right
            newWidth = (int) (initialWidth + deltaX);
            newHeight = (int) (initialHeight + deltaY);
        }

        // Set maximum width and height if needed
        int maxWidth = ((ViewGroup) view.getParent()).getWidth(); // or specify a constant value
        int maxHeight = ((ViewGroup) view.getParent()).getHeight(); // or specify a constant value

        if(view instanceof  LikhoEditText){
            // Ensure newWidth and newHeight do not exceed max dimensions
        if (newWidth > maxWidth) newWidth = maxWidth;
        }
        if (newHeight < 50) newHeight = 200; // Prevent collapsing
        if (newWidth < 100) newWidth = 200; // Prevent collapsing

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = newWidth;
        params.height = newHeight;
        view.setLayoutParams(params);

    }

    /*
       Method to check if the user tap lies in drag area or not.
    */
    public boolean isDraggableArea(MotionEvent event ,int DRAG_AREA_MARGIN) {
        // Check if the touch event is within the draggable area, considering the margin
        return event.getX() > DRAG_AREA_MARGIN && event.getX() < (view.getWidth() - DRAG_AREA_MARGIN) &&
                event.getY() > DRAG_AREA_MARGIN && event.getY() < (view.getHeight() - DRAG_AREA_MARGIN);
    }
    /*
    Method to handle drag.
     */
    public void handleDrag(MotionEvent event) {
            // Dragging logic
            float newX = event.getRawX() - dragOffsetX;
            float newY = event.getRawY() - dragOffsetY;


            if(view instanceof AppCompatEditText){
                // Ensure the EditText stays within the parent view
                ViewGroup parent = (ViewGroup) view.getParent();
                if (newX < 0) newX = 0;
                // Uncomment to enable Y-axis boundaries
                // if (newY < 0) newY = 0;
                if (newX + view.getWidth() > parent.getWidth()) newX = parent.getWidth() - view.getWidth();
                // Uncomment to enable Y-axis boundaries
                // if (newY + getHeight() > parent.getHeight()) newY = parent.getHeight() - getHeight();
            }

            view.setX(newX);
            view.setY(newY);
    }

    // Handles the rotation logic based on touch events
    public void handleRotation(MotionEvent event) {
        float newTouchX = event.getRawX();
        float newTouchY = event.getRawY();

        // Calculate angle between the two points (new and old)
        double angle = Math.toDegrees(
                Math.atan2(newTouchY - centreY, newTouchX - centreX) -
                        Math.atan2(lastTouchY  -centreY, lastTouchX - centreX)
        );

        // Apply rotation to the view
        rotationalAngle += (float) angle;
        view.setRotation(rotationalAngle);

        // Update last touch points
        lastTouchX = newTouchX;
        lastTouchY = newTouchY;
        view.invalidate();

    }


    /*
    method to set the initial values
     */
    public void setInitialX(float initialX) {
        this.initialX = initialX;
    }
    public void setInitialY(float initialY) {
        this.initialY = initialY;
    }
    public void setInitialWidth(float initialWidth) {
        this.initialWidth = initialWidth;
    }

    public void setInitialHeight(float initialHeight) {
        this.initialHeight = initialHeight;
    }

    /*
         method to set the initial values
         */
    public float getInitialX() {
        return initialX;
    }

    public float getInitialY() {
        return initialY;
    }

    public float getInitialWidth() {
        return initialWidth;
    }

    public float getInitialHeight() {
        return initialHeight;
    }

    public int getResizeCorner() {
        return resizeCorner;
    }

    public void setResizeCorner(int resizeCorner) {
        this.resizeCorner = resizeCorner;
    }

    public float getDragOffsetX() {
        return dragOffsetX;
    }

    public void setDragOffsetX(float dragOffsetX) {
        this.dragOffsetX = dragOffsetX;
    }

    public float getDragOffsetY() {
        return dragOffsetY;
    }

    public void setDragOffsetY(float dragOffsetY) {
        this.dragOffsetY = dragOffsetY;
    }

    public float getLastTouchX() {
        return lastTouchX;
    }

    public void setLastTouchX(float lastTouchX) {
        this.lastTouchX = lastTouchX;
    }
    public float getLastTouchY() {
        return lastTouchY;
    }
    public void setLastTouchY(float lastTouchY) {
        this.lastTouchY = lastTouchY;
    }
    public float getCentreX() {
        return centreX;
    }
    public void setCentreX(float centreX) {
        this.centreX = centreX;
    }
    public float getCentreY() {
        return centreY;
    }
    public void setCentreY(float centreY) {
        this.centreY = centreY;
    }
    public float getRotationalAngle() {
        return rotationalAngle;
    }
    public void setRotationalAngle(float rotationalAngle) {
        this.rotationalAngle = rotationalAngle;
    }
}
