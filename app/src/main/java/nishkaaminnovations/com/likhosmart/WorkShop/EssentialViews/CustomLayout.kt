package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatButton
import nishkaaminnovations.com.likhosmart.R

class CustomLayout @JvmOverloads constructor(context: Context,attr:AttributeSet?=null,defStyleAttr:Int=0): FrameLayout(context,attr,defStyleAttr),onChildViewClickListener {
    /*
    Tracks if one child view is enabled. Used to ensure only one child view is interactive at a time.
     */
    private var oneChildEnabled:Boolean=false
    /*
    Holds a reference to the currently active child view (e.g., CustomCanvasView, ResizeAbleImg, ResizableEdit).
     */
    private  var child: View?=null
    /*
    Detects user gestures like long presses. Used to show a popup with buttons (showButtonsAtLocation()
     */
    private lateinit var gestureDetector: GestureDetector
    /*
    Represents the popup window that appears on a long press, containing buttons to select different view types (canvas, image, or text).
     */
    private lateinit var popupWindow: PopupWindow
    /*
    Holds the reference to the canvas view added to the layout. It’s a custom view where drawing or other canvas-related operations are performed.
     */
    private lateinit var canvasView :CustomCanvasView
    /*
    A constant used to scale up or down the layout when zooming. Default value is 1.2f.
     */
    private val ZOOM_FACTOR:Float=1.2f
    /*
    Tracks the current zoom scale factor of the layout. Starts at 1.0f (no zoom), adjusted dynamically during zoom-in or zoom-out operations.
     */
    private var scaleFactor :Float=1.0f
    /*
    Represent the center of the view. Used to recenter the layout during zoom adjustments.
     */
    private var centerX:Float?=null
    private var centerY :Float?=null
    /*
    Store the initial translation values (X and Y) of the layout when it is first laid out.
    These values are used to reset or adjust translations during zoom-out
     */
    private var initialDx:Float=0f
    private  var initialDy :Float=0f
    /*
    Similar to initialDx and initialDy, these values store the total translation deltas at the initial state.
    Used to return to the original position during zoom-out.
     */
    private var initialTotalDx:Float=0f
    private var initialTotalDy:Float=0f
    /*
    Controls the gradual translation adjustment during zoom-out. Default is 0.2f (20%)
     */
    private val  translationStepPercentage:Float=0.2f
    /*
    Determines whether dragging is allowed for the active child view or layout. Set based on the interaction state.
     */
    private  var isDraggable:Boolean=false
    /*
    Store the raw X and Y coordinates of the user's last touch event during a drag.
     These are updated dynamically as the user drags.
     */
    private var lastTouchX:Float?=null
    private var lastTouchY:Float?=null
    /*
    Accumulate the total translation deltas (X and Y) applied to the layout during drag and zoom operations.
     */
    private var totalDx:Float=0f
    private var totalDy:Float=0f
    /*
    Store the initial translation offsets (X and Y) of the layout at the time it is created.
     Help ensure the layout remains within bounds during translations.
     */
    private var initialTranslationX:Float?=null
    private  var initialTranslationY:Float?=null
    /*
    Indicates whether a specific view type (e.g., IMAGE_VIEW, TEXT_VIEW) has been touched for the first time.
    Resets when switching view types.
     */
    private var firstTouched:Boolean=false
    /*
    Variable to represent the selected view type.
     */
    private  var selectedViewType:ViewType=ViewType.NONE
    /*
    This variable are used to check if
     */
    private var isDragging:Boolean=false
    /*
    Variables to define the text style enabled by the user.
     */
    private var isBoldEnabled:Boolean=false;
    private var isItalicEnabled:Boolean=false
    private var isUnderLineEnabled:Boolean=false
    private var isStrikeThroughEnabled:Boolean=false
    private var isAlignmentEnabled:Boolean=false
    private var fontFace:String="Default"
    private var fontSize:Int=10
    private var fontColor:String="#000000"
    private var isURLEnabled=false;
    private var linkText="Default"

    /*
       Enum to represent the selected view type
     */
     enum class ViewType {
        CANVAS,
        IMAGE_VIEW,
        TEXT_VIEW,
        NONE
     }
    /*
    Init block.
     */
    init{
        // Initialize the GestureDetector with a custom listener
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(event: MotionEvent) {
                // Get the x and y coordinates of the long press
                val x = event.x
                val y = event.y

                // Show buttons at this location
//                showButtonsPopUpAtLocation(x, y)
            }
        })
        addCanvasView()

    }
    /*
    Method to add canvas to the layout.
     */
    override fun onViewClicked(view: View?) {
        view?.isEnabled=true
        disableAllOther(view!!)
        oneChildEnabled = true
        child = view
        selectedViewType = ViewType.NONE
    }
    // Method to disable all child views except the specified one
    private fun disableAllOther(view: View) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != view) {
                child.isEnabled = false
            }
        }
    }
    // Set the selected view type
    fun setSelectedViewType(type: ViewType) {
        selectedViewType = type
        // Enable or disable views based on the selected type
        updateViewInteractivity()
    }
    // Method to dynamically add a canvas view
    private fun addCanvasView() {
        canvasView = CustomCanvasView(context, null)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ) // Set default size
        addView(canvasView, layoutParams)
        canvasView.isEnabled=false
    }
    /*
    Method to show pop up at user desired location.
     */
    private fun showButtonsAtLocation(x: Float, y: Float) {
        // Check if popupWindow already exists
        if (popupWindow == null) {
            // Inflate your button layout
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.poputlayout, null)

            // Create the PopupWindow (this will only happen once)
            popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            popupWindow?.apply {
                isFocusable = true
                isOutsideTouchable = true
                setBackgroundDrawable(ColorDrawable())

                // Set click listeners for buttons
                val draw: AppCompatButton = popupView.findViewById(R.id.button1)
                val text: AppCompatButton = popupView.findViewById(R.id.button2)
                val image: AppCompatButton = popupView.findViewById(R.id.button3)

                draw.setOnClickListener {
                    // Select Draw mode
                    selectedViewType = ViewType.CANVAS
                    // updateViewInteractivity() // Uncomment this if needed
                    popupWindow?.dismiss()
                }

                text.setOnClickListener {
                    // Select Text mode
                    selectedViewType = ViewType.TEXT_VIEW
                    firstTouched = false  // Reset firstTouched
                    popupWindow?.dismiss()
                }

                image.setOnClickListener {
                    // Select Image mode
                    selectedViewType = ViewType.IMAGE_VIEW
                    firstTouched = false  // Reset firstTouched
                    popupWindow?.dismiss()
                }
            }
        }

        // Show the popup at the new location
        popupWindow?.showAtLocation(this, Gravity.NO_GRAVITY, x.toInt(), y.toInt())
    }

    /*
    Method to update the child view interactivity.
     */
    private fun updateViewInteractivity() {
        // Loop through all child views and enable/disable them based on the selected view type
        for (i in 0 until childCount) {
            val child = getChildAt(i)

            // Enable or disable views based on their type
            when {
                child is CustomCanvasView && selectedViewType == ViewType.CANVAS -> {
                    child.isEnabled = true
                    child.isClickable = true
                }
                else -> {
                    // Disable interaction for other views
                    child.isEnabled = false
                    child.isClickable = false
                }
            }
        }
    }

    // Method to dynamically add an ImageView
    private fun addImageView(x: Float, y: Float) {
        firstTouched = true
        // Create the ImageView (Replace with your custom class if needed)
        val imageView = LikhoImageView(context, null)

        imageView.setOnChildClickListener(this)
        imageView.setImageResource(R.drawable.journal) // Set your image here

        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(imageView, layoutParams)

        imageView.x = x
        imageView.y = y // Set default size

        selectedViewType = ViewType.NONE
        child = imageView
    }

    // Method to dynamically add a TextView
    private fun addTextView(x: Float, y: Float) {
        firstTouched = true
        // Create the TextView (Replace with your custom class if needed)
        val textView = LikhoEditText(context)

        textView.setOnChildClickListener(this)
        textView.setText("Dynamic text")

        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(textView, layoutParams)

        textView.x = x
        textView.y = y
        selectedViewType = ViewType.NONE
        child = textView
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Record the initial touch positions
                lastTouchX = event.rawX
                lastTouchY = event.rawY
                isDragging = true
                val touchX = event.x
                val touchY = event.y

                // Add the corresponding view based on the selected type
                when (selectedViewType) {
                    ViewType.IMAGE_VIEW -> {
                        if (!firstTouched) {
                            isDraggable = false
                            addImageView(touchX, touchY)
                        }
                    }
                    ViewType.TEXT_VIEW -> {
                        if (!firstTouched) {
                            isDraggable = false
                            addTextView(touchX, touchY)
                        }
                    }
                    ViewType.NONE -> {
                        if (oneChildEnabled) {
                            child?.isEnabled = false
                            isDraggable = true
                        } else {
                            child?.isEnabled = false
                            isDraggable = true
                        }
                    }

                    ViewType.CANVAS -> TODO()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    // Calculate movement deltas using raw coordinates to avoid view hierarchy issues
                    val dx = event.rawX - lastTouchX!!
                    val dy = event.rawY - lastTouchY!!

                    // Accumulate the deltas for translation
                    totalDx +=dx
                    totalDy += dy
                    // Call the method to animate the translation with clamping
                    applyTranslationWithBounds(dx, dy)

                    // Update last touch positions
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }

        return true
    }


    private fun updateCenter() {
        centerX = (width / 2) - (totalDx!! * scaleFactor)
        centerY = (height / 2) - (totalDy!! * scaleFactor)
    }


    fun zoomIn() {
        scaleFactor *= ZOOM_FACTOR // Increase the scale factor
        scaleFactor = scaleFactor.coerceAtMost(10.0f) // Limit maximum scale

        this.animate()
            .scaleX(scaleFactor)
            .scaleY(scaleFactor)
            .setDuration(300) // Adjust duration as needed
            .withEndAction {
                // Adjust translation bounds after zooming in
                adjustTranslationAfterZoom()
            }
            .start()

        // Update center position after zoom
        updateCenter()
    }

    // Adjust translation to ensure the layout's center stays visible within bounds after zoom
    private fun adjustTranslationAfterZoom() {
        val maxDx = maxOf(0f, (canvasView.width * scaleFactor - width) / 2)
        val maxDy = maxOf(0f, (canvasView.height * scaleFactor - height) / 2)

        // Clamp totalDx and totalDy after zoom
        totalDx = totalDx.coerceIn(-maxDx, maxDx)
        totalDy = totalDy.coerceIn(-maxDy, maxDy)

        // Apply the adjusted translation
        this.translationX = totalDx
        this.translationY = totalDy

        // Ensure the center stays within the screen bounds after zoom
        updateCenter()
    }
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        // Store initial translation values
        if (changed) {
            initialDx = translationX
            initialDy = translationY
        }
    }

    private fun applyTranslationWithBounds(dx: Float, dy: Float) {
        // Get the boundaries for translation based on the canvas size and zoom level
        val maxDx = maxOf(0f, (canvasView.width * scaleFactor - width) / 2) // Horizontal boundary
        val maxDy = maxOf(0f, (canvasView.height * scaleFactor - height) / 2) // Vertical boundary

        // Clamp totalDx and totalDy to ensure the translation doesn't exceed bounds
        totalDx = totalDx.coerceIn(-maxDx, maxDx)
        totalDy = totalDy.coerceIn(-maxDy, maxDy)

        // Apply the translation within bounds
        this.translationX = totalDx
        this.translationY = totalDy
    }
    fun zoomOut() {
        // Calculate the new scale factor
        var newScaleFactor = scaleFactor / ZOOM_FACTOR
        scaleFactor = newScaleFactor.coerceAtLeast(1.0f) // Limit minimum scale

        // Update the translation to center it based on the new scale
        val adjustmentFactor = 1 - translationStepPercentage

        // Update totalDx and totalDy to gradually approach the initial position
        totalDx = initialTotalDx + (totalDx - initialTotalDx) * adjustmentFactor // Move towards initial position
        totalDy = initialTotalDy + (totalDy- initialTotalDy) * adjustmentFactor // Move towards initial position

        // Apply the new translation values
        applyTranslationWithBoundsZoomOut(totalDx, totalDy) // Apply the adjusted translation

        // Apply the scaling
        scaleX = scaleFactor
        scaleY = scaleFactor
    }

    private fun applyTranslationWithBoundsZoomOut(dx: Float, dy: Float) {
        // Get the boundaries for translation based on the canvas size and zoom level
        val maxDx = maxOf(0f, (canvasView.width * scaleFactor - width) / 2) // Horizontal boundary
        val maxDy = maxOf(0f, (canvasView.height * scaleFactor - height) / 2) // Vertical boundary

        // Clamp totalDx and totalDy to ensure the translation doesn't exceed bounds
        totalDx = dx.coerceIn(-maxDx, maxDx)
        totalDy = dy.coerceIn(-maxDy, maxDy)

        // Apply the translation within bounds
        this.translationX = totalDx
        this.translationY = totalDy
    }

    /*
    getter and setter method for text editing variable.
     */


}