package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView

/*
This class will represent the imageview of our app.
 */
class LikhoImageView @JvmOverloads constructor(context: Context, attr:AttributeSet?=null, defStyleAttr:Int=0) :AppCompatImageView(context,attr,defStyleAttr) {
    /*
    It's used to ensure that the rectangle is drawn inside the image view with a certain amount of space from its edges.
     */
    private val rectMargin:Float=30f
    /*
    A Paint object for drawing the dashed rectangle around the image.
     */
    private lateinit var rectPaint: Paint
    /*
    Purpose: A Paint object for drawing the resizing handles at the corners of the image.
     */
    private lateinit var  resizerPaint:Paint
    /*
    Purpose: The distance between the touch point on the screen and the X position of the view.
    Usage: It is used to calculate the movement of the view when the user is dragging it. It helps to translate the view across the screen.
     */
    private var dX:Float?=null
    /*
    Purpose: The distance between the touch point on the screen and the Y position of the view.
    Usage: Similar to dX, it helps in calculating the vertical translation of the view during drag operations.
     */
    private var dY:Float?=null
    /*
    Purpose: The radius of the circular handles used for resizing the image.
    Usage: It defines how large the resizer handles (circles) are when drawn on the canvas.
     */
    private val HANDLE_RADIUS:Int=20
    /*
    Purpose: A flag that indicates whether the image is currently being resized.
    Usage: This is set to true when the user touches a resizing handle and is actively resizing the image. It helps to manage the resizing logic in the touch event handling.
     */
    private var isResizing:Boolean=false
    /*
    Purpose: A flag that indicates whether the image is currently being rotated.
    Usage: This is set to true when the user touches the rotation handle (top-right corner) and starts rotating the image. It ensures that the image rotation logic is executed.
     */
    private var isRotating:Boolean=false
    /*
    Purpose: The initial X position of the touch event when starting the resize operation.
    Usage: It is used to calculate the difference in touch positions (deltaX) during resizing.
     */
    private var initialX:Float?=null
    /*
    Purpose: The initial Y position of the touch event when starting the resize operation.
    Usage: Like initialX, it is used to calculate the difference in touch positions during resizing.
     */
    private var initialY:Float?=null
    /*
    Purpose: The initial width of the image when resizing starts.
    Usage: It helps to calculate the change in width during the resizing operation.
     */
    private var initialWidth:Float?=null
    /*
    Purpose: The initial height of the image when resizing starts.
    Usage: It helps to calculate the change in height during the resizing operation.
     */
    private var initialHeight:Float?=null

    /*
    Purpose: Identifies which corner of the image is being used for resizing (top-left, top-right, bottom-left, or bottom-right).
    Usage: This variable helps to handle the resizing logic by specifying which corner is being dragged.
    */
    private var resizeCorner = -1

    /*
    Purpose: The radius of the rotation handle (circle) located at the top-right corner of the image.
    Usage: It is used to draw the rotation handle and helps to define how large the handle appears on the canvas.
    */
    private val rotateRadius = 26
    /*
     Purpose: A Paint object used to define the appearance of the rotation handle (a filled circle).
     Usage: It is used to draw the rotation handle and defines the color, style, and stroke width of the handle.
    */
        private var rotatePaint: Paint? = null

    /*
    Purpose: Stores the current rotation angle of the image.
    Usage: This value is updated during the rotation operation and is used to apply the rotation to the image.
    */
    private var rotationAngle = 0f

    /*
    Purpose: Defines the tolerance around the corners for detecting whether a user is touching near the corner for resizing or rotation.
    Usage: It is used to detect whether the touch event is near one of the corners of the image (where the resize handles or rotation handle are located).
    */
    private val cornerTolerance = 50f

    /*
    Purpose: Stores the last X position of the touch event during rotation.
    Usage: It is used to calculate the change in angle between the previous and current touch positions for rotation.
    */
        private var lastTouchX = 0f

    /*
    Purpose: Stores the last Y position of the touch event during rotation.
    Usage: It is used to calculate the change in angle during the rotation process.
    */
    private var lastTouchY = 0f

    /*
    Purpose: The X coordinate of the center of the image, used for rotation.
    Usage: It is used as the pivot point for the rotation transformation, ensuring the image rotates around its center.
    */
    private var centerX = 0f

    /*
    Purpose: The Y coordinate of the center of the image, used for rotation.
    Usage: Like centerX, it is used as the pivot point for rotating the image.
    */
    private var centerY = 0f

    /*
        Purpose: An interface for handling click events on the view.
    Usage: This is used to notify when the view is clicked (e.g., when resizing or rotating is completed). The listener provides a method (onViewClicked) that is invoked when the view is clicked.
    */
    private lateinit var onChildClickListener: onChildViewClickListener

    // Initialize a Matrix object for image transformation
    private val matrix = Matrix()

    // Array to store the matrix values (9 values representing transformation parameters)
    private val matrixValues = FloatArray(9)
    /*
    Variable to tell if the vie is being dragged or not
     */
    private var isDragging=false;
    private val DRAG_AREA_MARGIN: Int = 35 // Margin to avoid rectangle and circles
    /*
    Variable for the utility class.
     */
    private var utility: ViewUtility?=null

    /*
    This is the initialisation block that is used to initialised the paint objects and set layout params.
     */
    init {
        utility= ViewUtility(this)
        // Set the scale type of the image to FIT_CENTER. This means the image will be scaled to fit within the bounds of the ImageView,
        // while maintaining its aspect ratio. It ensures that the image is displayed fully and centered within the ImageView.
        scaleType = ImageView.ScaleType.FIT_CENTER

        // Set padding around the ImageView. This creates space between the edge of the ImageView and its contents (the image).
        setPadding(40, 40, 40, 40)

        // Set a minimum width for the ImageView. This ensures that the ImageView can't be resized smaller than 200px in width.
        minimumWidth = 200

        // Initialize rectPaint for drawing the dashed rectangle around the image.
        rectPaint = Paint().apply {
            color = Color.BLUE // Set the color of the rectangle to blue.
            style = Paint.Style.STROKE // Set the style to STROKE, which means it will only draw the outline of the rectangle.
            strokeWidth = 5f // Set the width of the rectangle's stroke to 5px.
            val intervals = floatArrayOf(10f, 20f) // Define the dash pattern.
            pathEffect = DashPathEffect(intervals, 0f) // Apply the dash pattern to the rectangle.
        }

        // Initialize resizerPaint for drawing resize handles (the small circles at the corners).
        resizerPaint = Paint().apply {
            color = Color.BLUE // Set the color of the resizer handles to blue.
            style = Paint.Style.STROKE // Set the style to STROKE for just the outline.
            strokeWidth = 5f // Set the stroke width to 5px.
        }

        // Initialize rotatePaint for drawing the rotation handle (a circle).
        rotatePaint = Paint().apply {
            color = Color.BLACK // Set the color of the rotation handle to black.
            style = Paint.Style.FILL_AND_STROKE // Set the style to fill the circle and also outline it.
            strokeWidth = 5f // Set the stroke width to 5px.
        }
    }
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        // Custom logic for enabling/disabling the view
        if (!enabled) {
            utility = ViewUtility(this)
        } else {
            utility = null
        }
    }


    /*
    Method to draw rectangle on the canvas
     */
    private fun drawRectangle(canvas: Canvas) {
        // Get the image size using the custom getImageSize() method.
        val imageSize = getImageSize()
        if (imageSize == null) {
            return // If image size is null, exit the function.
        }

        // Draw a rectangle around the image with a margin (rectMargin).
        canvas.drawRect(
            rectMargin, rectMargin,
            (width - rectMargin),
            (height - rectMargin),
            rectPaint // Use the rectPaint object for styling the rectangle.
        )
    }

    /*
    Method to draw the circles on the canvas
     */
    private fun drawCircle(canvas: Canvas) {
        // Draw circles at the four corners for resizing handles (top-left, top-right, bottom-left, bottom-right).
        // These circles are drawn with the resizerPaint object for style and color.
        canvas.drawCircle(
            rectMargin.toFloat(), rectMargin.toFloat(),
            HANDLE_RADIUS.toFloat(), resizerPaint
        )
        rotatePaint?.let {
            canvas.drawCircle(
                (width - rectMargin).toFloat(), rectMargin.toFloat(),
                rotateRadius.toFloat(), it // Rotation handle is drawn with rotatePaint.
            )
        }
        canvas.drawCircle(
            rectMargin.toFloat(), (height - rectMargin).toFloat(),
            HANDLE_RADIUS.toFloat(), resizerPaint
        )
        canvas.drawCircle(
            (width - rectMargin).toFloat(), (height - rectMargin).toFloat(),
            HANDLE_RADIUS.toFloat(), resizerPaint
        )
    }

    private fun getImageSize(): RectF? {
        // Check if the drawable is null, if it is return null
        if (drawable == null) {
            return null
        }

        // Get the intrinsic width and height of the drawable (the image)
        val intrinsicWidth = drawable.intrinsicWidth.toFloat()
        val intrinsicHeight = drawable.intrinsicHeight.toFloat()

        // Retrieve the matrix values (transformation matrix applied to the drawable)
        matrix.getValues(matrixValues)

        // Get the scaling factors applied to the image (horizontal and vertical)
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val scaleY = matrixValues[Matrix.MSCALE_Y]

        // Calculate the scaled width and height based on the intrinsic size and scale factors
        val scaledWidth = intrinsicWidth * scaleX
        val scaledHeight = intrinsicHeight * scaleY

        // Return a RectF representing the scaled size of the image
        return RectF(0f, 0f, scaledWidth, scaledHeight)
    }

    /*
    The ondraw method
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Check if the view is enabled before proceeding
        if (!isEnabled) {
            return
        }

        // Call the method to draw the rectangle on the canvas
        drawRectangle(canvas)

        // Call the method to draw the circles (resize and rotation handles) on the canvas
        drawCircle(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        // If there's a click listener and the view is disabled, trigger the onViewClicked method
        if (onChildClickListener != null && !isEnabled) {
            onChildClickListener?.onViewClicked(this)
        }

        // Handle different touch actions
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                utility!!.resizeCorner=utility!!.getResizeCorner(event)
                // If the top-right corner is being touched, initiate rotation
                if (utility!!.resizeCorner == 1) { // Top-right corner for rotation
                    utility!!.lastTouchX = event.rawX
                    utility!!.lastTouchY = event.rawY
                    utility!!.centreX = (x + width) / 2
                    utility!!.centreY = (y + height) / 2
                    isRotating = true

                } else if (utility!!.resizeCorner != -1) { // Resizing handles
                    utility!!.initialHeight = height.toFloat()
                    utility!!.initialWidth = width.toFloat()
                    utility!!.initialX = event.rawX
                    isResizing = true
                } else if (utility!!.isDraggableArea(event,DRAG_AREA_MARGIN)) {
                    utility!!.dragOffsetX = event.rawX - x
                    utility!!.dragOffsetY = event.rawY - y
                    isDragging = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // If resizing is active, handle the resizing logic
                if (isResizing && isEnabled) {
                    utility!!.handleResize(event)
                    invalidate()
                }
                // If rotating is active, handle the rotation logic
                else if (isRotating && isEnabled) {
                    utility!!.handleRotation(event)
                } else {
                    // Dragging logic: Move the view based on the touch position
                    utility!!.handleDrag(event)
                    invalidate() // Redraw the view
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Reset resizing and rotating states
                isResizing = false
                isRotating = false
            }
        }
        return true
    }
    // Setter for the interface listener
    fun setOnChildClickListener(listener: onChildViewClickListener) {
        this.onChildClickListener = listener
    }

}