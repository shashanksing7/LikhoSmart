package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Editable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.CustomCanvasView.PenType
import nishkaaminnovations.com.likhosmart.WorkShop.MusicPlayer.WaveformWithPlayPause

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
     var firstTouched:Boolean=false
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
    private var fontFace:String="serif"
    private var fontSize:Float=1.5f
    private var fontColor:String="#000000"
    private var isURLEnabled=false;
    private var linkText="Default"
    /*
    Variable to represent the current edit text.
     */
    private var currentEditext: LikhoEditText? = null
    /*
    Variable to represent the childSelectedListener
     */
    private  lateinit var childSelectedListener:childSelectedListener
    /*
    This variable is used represent the copied view.
     */
    private  var copiedView:View?=null
    private var copiedViewLayoutParams:FrameLayout.LayoutParams?=null
    private var clickedChildType=ViewType.NONE
    /*
    This variable represents if the drawing mode is on or not.
     */
    private var isDrawingOn:Boolean=false;

    /*
       Enum to represent the selected view type
     */
     enum class ViewType {
        CANVAS,
        IMAGE_VIEW,
        TEXT_VIEW,
        AUDIO,
        NONE
     }
    /*
    Init block.
     */
    init{

        /*
        Initialising the pop up view
         */
        InitialiseButtonsAtLocation()
        // Initialize the GestureDetector with a custom listener
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(event: MotionEvent) {
                // Get the x and y coordinates of the long press
                val x = event.x
                val y = event.y

                // Show buttons at this location
               showLongPressPopUp(x,y)
            }
        })
        addCanvasView()
    }
    /*
    Method to add canvas to the layout.
     */
    override fun onViewClicked(view: View?,viewType:ViewType,isLocked:Boolean,isChildSelected:Boolean,link:String,linkText:String,pathBottom:Float,pathRight:Float) {

        /*
        Using the if block because we need to call the getChild() listener even when the child is enabled
         */
        if(!view!!.isEnabled){
            view?.isEnabled=true
            disableAllOther(view!!)
            oneChildEnabled = true
            child = view
            selectedViewType = ViewType.NONE
            clickedChildType=viewType
            childSelectedListener.getSelectedChild(viewType,true,isLocked)
            Log.d("mytag", "onViewClicked: child"+viewType)
            if(link!="noURL"){
                showLinkPopup(view,link,linkText,pathBottom, pathRight)
            }
            if(child!=null&&viewType==ViewType.TEXT_VIEW){
                currentEditext= child!! as LikhoEditText

            }
            Log.d("tagmy", "onViewClicked: if block")
        }
        else if(viewType==ViewType.CANVAS){
            Log.d("tagmy", "onViewClicked: else block")
            childSelectedListener.getSelectedChild(viewType,isChildSelected,isLocked)
            clickedChildType=viewType
            if(child!=null&&viewType==ViewType.TEXT_VIEW){
                currentEditext= child!! as LikhoEditText
            }
            child = view
            if((link!="noURL" )){
                showLinkPopup(view,link,linkText,pathBottom, pathRight)
            }
        }
    }
/*
This method returns if the drawing mode in on or not
 */
    override fun isDrawingOn(): Boolean {
        return isDrawingOn
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
        canvasView.setOnChildClickListener(this)
        canvasView.getisReadingModeOn()
    }
    /*
    Method to show pop up at user desired location.
     */
    private fun InitialiseButtonsAtLocation() {
        // Check if popupWindow already exists
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
                     updateViewInteractivity() // Uncomment this if needed
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
    /*
    Show buttons at location method
     */
    private fun showLongPressPopUp(x:Float,y:Float){
        popupWindow?.showAtLocation(this, Gravity.NO_GRAVITY, x.toInt(), y.toInt())
    }

    /*
    Method to update the child view interactivity based on selected view type.
*/
    private fun updateViewInteractivity() {
        // Loop through all child views and enable/disable them based on the selected view type
        for (i in 0 until childCount) {
            val child = getChildAt(i)

            // Enable or disable views based on their type and the selected view type
            when (selectedViewType) {
                ViewType.CANVAS -> {
                    child.isEnabled = child is CustomCanvasView
                    child.isClickable = child is CustomCanvasView
                }
                ViewType.IMAGE_VIEW -> {
                    child.isEnabled = child is LikhoImageView
                    child.isClickable = child is LikhoImageView
                }
                ViewType.TEXT_VIEW -> {
                    child.isEnabled = child is LikhoEditText
                    child.isClickable = child is LikhoEditText
                }
                ViewType.AUDIO -> {
                    child.isEnabled = child is WaveformWithPlayPause // Replace with your custom audio view class
                    child.isClickable = child is WaveformWithPlayPause
                }
                ViewType.NONE -> {
                    // Disable interaction for all views
                    child.isEnabled = false
                    child.isClickable = false
                }
            }
        }
    }


    // Method to dynamically add an ImageView
     fun addImageView(uri:Uri) {
        firstTouched = true
        // Create the ImageView (Replace with your custom class if needed)
        val imageView = LikhoImageView(context, null)
        imageView.setUri(uri)
        imageView.setOnChildClickListener(this)
        imageView.setImageURI(uri)

//        val layoutParams = FrameLayout.LayoutParams(300, 300) // Set width and height to 300
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(imageView, layoutParams)
        imageView.x = 100f
        imageView.y = 100f


        selectedViewType = ViewType.NONE
        clickedChildType=ViewType.IMAGE_VIEW
        child = imageView
        childSelectedListener.getSelectedChild(ViewType.IMAGE_VIEW,true,false)
    }

    // Method to dynamically add a TextView
     fun addTextView(x: Float, y: Float):LikhoEditText {
        firstTouched = true
        // Create the TextView
        val textView = LikhoEditText(context)
        textView.init(this)
        textView.setOnChildClickListener(this)
        textView.hint="Dynamic Text"
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(textView, layoutParams)
        textView.x = x
        textView.y = y
        selectedViewType = ViewType.NONE
        currentEditext=textView
        child = textView
        clickedChildType=ViewType.TEXT_VIEW
        childSelectedListener.getSelectedChild(ViewType.TEXT_VIEW,true,false)
        return textView
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
//                            addImageView(touchX, touchY)
                        }
                    }
                    ViewType.TEXT_VIEW -> {
                        if (!firstTouched) {
                            isDraggable = false
                            addTextView(touchX, touchY)
                        }
                    }
                    ViewType.NONE -> {
                        Log.d("mytag", "onTouchEvent:none ")
                        if (oneChildEnabled) {
                            child?.isEnabled = false
                            isDraggable = true
                            childSelectedListener.getSelectedChild(ViewType.NONE,false,true)
                            clickedChildType=ViewType.NONE
                            popupWindow.dismiss()

                        } else {
                            child?.isEnabled = false
                            isDraggable = true
                            childSelectedListener.getSelectedChild(ViewType.NONE,false,true)
                            clickedChildType=ViewType.NONE
                            popupWindow.dismiss()

                        }
                    }

                    ViewType.CANVAS -> TODO()
                    else->{

                    }
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
    Getter and Setter.
     */
    // Public getters and setters
    // Getter and Setter for isBoldEnabled
    fun getIsBoldEnabled(): Boolean {
        return isBoldEnabled
    }

    fun setIsBoldEnabled(value: Boolean) {
        isBoldEnabled = value
    }

    // Getter and Setter for isItalicEnabled
    fun getIsItalicEnabled(): Boolean {
        return isItalicEnabled
    }

    fun setIsItalicEnabled(value: Boolean) {
        isItalicEnabled = value
    }

    // Getter and Setter for isUnderLineEnabled
    fun getIsUnderLineEnabled(): Boolean {
        return isUnderLineEnabled
    }

    fun setIsUnderLineEnabled(value: Boolean) {
        isUnderLineEnabled = value
    }

    // Getter and Setter for isStrikeThroughEnabled
    fun getIsStrikeThroughEnabled(): Boolean {
        return isStrikeThroughEnabled
    }

    fun setIsStrikeThroughEnabled(value: Boolean) {
        isStrikeThroughEnabled = value
    }

    // Getter and Setter for isAlignmentEnabled
    fun getIsAlignmentEnabled(): Boolean {
        return isAlignmentEnabled
    }

    fun setIsAlignmentEnabled(value: Boolean) {
        isAlignmentEnabled = value
    }

    // Getter and Setter for fontFace
    fun getFontFace(): String {
        return fontFace
    }

    fun setFontFace(value: String) {
        fontFace = value
    }

    // Getter and Setter for fontSize
    fun getFontSize(): Float {
        return fontSize
    }

    fun setFontSize(value: Float) {
        fontSize = value
    }

    // Getter and Setter for fontColor
    fun getFontColor(): String {
        return fontColor
    }

    fun setFontColor(value: String) {
        fontColor = value
    }

    // Getter and Setter for isURLEnabled
    fun getIsURLEnabled(): Boolean {
        return isURLEnabled
    }

    fun setIsURLEnabled(value: Boolean) {
        isURLEnabled = value
    }

    // Getter and Setter for linkText
    fun getLinkText(): String {
        return linkText
    }

    fun setLinkText(value: String) {
        linkText = value
    }

    /*
    Method to set the brushes of the canvas.
     */
    fun setBrushToErase(strokeWidth: Float) {
        Log.d("mytag", "layout eraser: ")
        canvasView.apply {
            setStrokeWidth(strokeWidth)
            setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR)) // Clear mode for erasing
            setColor(Color.TRANSPARENT) // Transparent color isn't mandatory here but aligns intent
            setStrokeStyle(Paint.Style.STROKE) // Ensure the paint style matches your drawing
            setAlpha(255) // Full effect (CLEAR mode handles transparency separately)
            invalidate() // Redraw after setting up eraser
        }
        canvasView.setLaserOn(false)
        canvasView.setEraserOn(true)
        Log.d("mytag", "latout: "+canvasView.isEraserOn())

    }

    fun setBrushToDashed(strokeWidth:Float,color:Int) {
        canvasView.setColor(color)       // Default color for normal brush
        canvasView.setStrokeWidth(strokeWidth)           // Standard stroke width
        canvasView.setStrokeStyle(Paint.Style.STROKE) // Stroke style
        canvasView.setAlpha(255)               // Full opacity
        canvasView.setPathEffect(DashPathEffect(floatArrayOf(10f, 10f), 0f))  // Dashed line effect
        canvasView.setShader(null)             // No gradient or pattern
        canvasView.setCap(Paint.Cap.ROUND)     // Rounded end for dashed line
        canvasView.setJoin(Paint.Join.MITER)   // Mitered join for dashed line
        canvasView.setXfermode(null)           // Normal blending mode
        canvasView.setLaserOn(false)
        canvasView.setEraserOn(false)
    }

    fun setBrushToHighlighter(strokeWidth:Float,color:Int) {
        canvasView.setColor(color)       // Default color for normal brush
        canvasView.setStrokeWidth(strokeWidth)          // Standard stroke width
        canvasView.setStrokeStyle(Paint.Style.STROKE) // Stroke style
        canvasView.setAlpha(80)               // Full opacity
        canvasView.setPathEffect(null)         // No dashed effect
        canvasView.setShader(null)             // No gradient or pattern
        canvasView.setCap(Paint.Cap.ROUND)     // Rounded end for normal brush
        canvasView.setJoin(Paint.Join.ROUND)   // Rounded join for normal brush
        canvasView.setXfermode(null)           // Normal blending mode
        canvasView.setLaserOn(false)
        canvasView.setEraserOn(false)
    }

    fun setBrushToNormal(strokeWidth:Float,color:Int) {
        canvasView.setColor(color)       // Default color for normal brush
        canvasView.setStrokeWidth(strokeWidth)          // Standard stroke width
        canvasView.setStrokeStyle(Paint.Style.STROKE) // Stroke style
        canvasView.setAlpha(255)               // Full opacity
        canvasView.setPathEffect(null)         // No dashed effect
        canvasView.setShader(null)             // No gradient or pattern
        canvasView.setCap(Paint.Cap.ROUND)     // Rounded end for normal brush
        canvasView.setJoin(Paint.Join.ROUND)   // Rounded join for normal brush
        canvasView.setXfermode(null)           // Normal blending mode
        canvasView.setLaserOn(false)
        canvasView.setEraserOn(false)
    }

    fun setBrushToLaser() {
        canvasView.setLaserOn(!canvasView.isLaserOn())
    }

    fun addMusic(wave:WaveformWithPlayPause){
        wave.x=100f
        wave.y=100f
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        wave.setOnChildClickListener(this)
        addView(wave,layoutParams)
        selectedViewType = ViewType.NONE
        child =wave
        childSelectedListener.getSelectedChild(ViewType.AUDIO,true,false)
        clickedChildType=ViewType.AUDIO
    }
    /*
    Method to get the current selected child of the layout
     */
    fun getCurrentEditText():LikhoEditText?{
        if (currentEditext==null) {
            return null
        }
        return currentEditext
    }
    /*
    Methods to set the pen color and pen stroke width
     */
    fun setNormalPaintColor(penColor:Int){
        canvasView.setColor(penColor)
    }
    fun  setPenStrokeWidth(penStrokeWidth:Float){
        canvasView.setStrokeWidth(penStrokeWidth)
    }
    /*
    Method to set the childSelectedListener.
     */
    fun setChildSelectedListener(childSelectedListener:childSelectedListener){
        this.childSelectedListener=childSelectedListener
    }
    /*
    Method to copy the currently selected child
     */
    fun copyChild(viewType: ViewType){
        when(viewType){
            ViewType.TEXT_VIEW ->{
                val selectedChild=child as LikhoEditText
               val copiedEditText=LikhoEditText(context)
                copiedEditText.x=selectedChild.x+100f
                copiedEditText.y=selectedChild.y+100f
                copiedEditText.rotation=selectedChild.rotation
                copiedEditText.init(this)
                copiedEditText.setOnChildClickListener(this)
                copiedEditText.hint="Dynamic Text"
                val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                /*
                Getting the editable from the selected edittext
                 */
                val editable:Editable=selectedChild.editableText
                copiedEditText.setText(editable)
                copiedViewLayoutParams=layoutParams
                copiedView=copiedEditText
                firstTouched = true
                selectedViewType = ViewType.NONE
                child!!.isEnabled=false
                child=copiedEditText
           }
            ViewType.IMAGE_VIEW->{
                val selectedChild=child as LikhoImageView
                val copiedImageView=LikhoImageView(context)
                copiedImageView.x=selectedChild.x+100f
                copiedImageView.y=selectedChild.y+100f
                copiedImageView.rotation=selectedChild.rotation
                copiedImageView.setOnChildClickListener(this)
                /*
                Getting and setting the uri
                 */
                val imageUri=selectedChild.getUri()
                copiedImageView.setImageURI(imageUri)
                copiedImageView.setUri(imageUri)
                val layoutParams = FrameLayout.LayoutParams(selectedChild.width, selectedChild.height)
                copiedViewLayoutParams=layoutParams
                copiedView=copiedImageView
                firstTouched = true
                selectedViewType = ViewType.NONE
                child!!.isEnabled=false
                child=copiedImageView

            }
            ViewType.AUDIO->{
                val selectedChild=child as WaveformWithPlayPause
                val audioUri=selectedChild.getAudioUri()
                val copiedWaveAudio=WaveformWithPlayPause(context,null,0,audioUri)
                copiedWaveAudio.x=selectedChild.x+100f
                copiedWaveAudio.y=selectedChild.y+100f
                copiedWaveAudio.rotation=selectedChild.rotation
                val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                copiedWaveAudio.setOnChildClickListener(this)
                /*
                Setting the values
                 */
                copiedViewLayoutParams=layoutParams
                copiedView=copiedWaveAudio
                child!!.isEnabled=false
                selectedViewType = ViewType.NONE
                child =copiedWaveAudio
            }
            ViewType.CANVAS->{
                child!!.invalidate()
                (child as CustomCanvasView).copySelectedPaths()
            }

            else->{

            }
        }

    }

    /*
    Method to paste the child.
     */
    fun pasteChild(){
        if(copiedView!=null&&copiedViewLayoutParams!=null){

                addView(copiedView,copiedViewLayoutParams)
                copiedView=null
                copiedViewLayoutParams=null
                Log.d("mytag", "pasteChild: not calling the canvas ")
        }else{
            if(clickedChildType==ViewType.CANVAS){
                canvasView.invalidate()
                canvasView.pasteCopiedPaths()
                Log.d("mytag", "pasteChild: calling the canvas ")
            }
        }


    }
    /*
    method to remove view.
     */
    fun deleteView(){
        if(child!=null&&clickedChildType!==ViewType.CANVAS){
            if(clickedChildType==ViewType.AUDIO){
                val musicUri=(child as WaveformWithPlayPause).getAudioUri()
                deleteFileAtUri(context,musicUri)
            }
            removeView(child)
        }
        else{
            canvasView.invalidate()
            canvasView.deleteSelectedPaths()
        }
    }
    fun lockUnlockChild(lock:Boolean){
        if(child!=null){
            Log.d("mytag", "lockUnlockChild: ")
            when(clickedChildType){
                ViewType.TEXT_VIEW ->{
                    child!!.invalidate()
                    (child as LikhoEditText).locked=lock
                    Log.d("mytag", "lockUnlockChild: text ")
                }
                ViewType.IMAGE_VIEW->{
                    child!!.invalidate()
                    (child as LikhoImageView).setLocked(lock)

                }
                ViewType.AUDIO->{
                    child!!.invalidate()
                    (child as WaveformWithPlayPause).setLocked(lock)
                }
                ViewType.CANVAS->{
                    child!!.invalidate()
                    (child as CustomCanvasView).lockSelectedPaths()
                }
                else->{
                    Log.d("mytag", "lockUnlockChild: none ")
                }
            }
        }
    }

    /*
    Method to delete the file at uri
     */
    fun deleteFileAtUri(context: Context, uri: Uri): Boolean {
        return try {
            // Attempt to delete the file using the ContentResolver
            val contentResolver = context.contentResolver
            val rowsDeleted = contentResolver.delete(uri, null, null)
            rowsDeleted > 0 // Return true if at least one row was deleted
        } catch (e: Exception) {
            // Handle any exceptions (e.g., security exceptions or invalid URI)
            e.printStackTrace()
            false
        }
    }

    private fun showLinkPopup(anchorView: View, url: String,linkName:String,pathBottom:Float,pathRight:Float) {
        // Inflate the popup layout
        val popupView = LayoutInflater.from(anchorView.context).inflate(R.layout.linkpopuplayout, null)
        Log.d("mytag", "showLinkPopup: ")
        // Create a PopupWindow
        val popupWindow = PopupWindow(popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true)

        // Set the link TextView
        val tvLink: TextView = popupView.findViewById(R.id.tv_link)
        tvLink.text=linkName
        tvLink.setOnClickListener {
            // Open the link in a browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            anchorView.context.startActivity(intent)
            popupWindow.dismiss()
        }

        // Show the popup
        popupWindow.elevation = 10f
        if(clickedChildType!=ViewType.CANVAS){
            popupWindow.showAsDropDown(anchorView, 0, 0) // Adjust position if needed
        }
        else{
            popupWindow!!.showAtLocation(popupView, Gravity.CENTER, pathRight.toInt(), pathBottom.toInt())
        }


    }
    /*
    Method to set the link in the view.
     */
    fun setLinkInChild(linkName:String,linkURL:String,childType: ViewType){
        if(child!=null){
            when(childType){
                ViewType.TEXT_VIEW ->{
                    child!!.invalidate()
                    (child as LikhoEditText).linkName=linkName
                    (child as LikhoEditText).linkUrl=linkURL
                }
                ViewType.IMAGE_VIEW->{
                    child!!.invalidate()
                    (child as LikhoImageView).setLinkName(linkName)
                    (child as LikhoImageView).setLinkUrl(linkURL)
                }
                ViewType.AUDIO->{
                    child!!.invalidate()
                    (child as WaveformWithPlayPause).setLinkName(linkName)
                    (child as WaveformWithPlayPause).setLinkUrl(linkURL)
                }
                ViewType.CANVAS->{
                    child!!.invalidate()
                    (child as CustomCanvasView).setPathLink(linkURL,linkName)
                }
                else->{
                    Log.d("mytag", "lockUnlockChild: none ")
                }
            }
        }
    }

    /*
    Method to set the isDrawingOn variable.
     */
    fun setIsDrawingOn(drawingMode:Boolean){
        isDrawingOn=drawingMode
    }
    /*
Method to set is lasso selected .
 */
    fun setIsLassoSelected(isLassoSelected:Boolean){
        canvasView.setIsLassoSelected(isLassoSelected)
        canvasView.setLaserOn(false)
        canvasView.setEraserOn(false)
    }
    /*
Method to and set the pen Type.
 */
    fun getPenType(): PenType {
        return canvasView.getPenType()
    }
    fun setPenType(type: PenType){
        canvasView.setPenType(type)
    }

    fun undoCanvas(){
        canvasView.undoAction()
    }
    fun redoCanvas(){
        canvasView.redoAction()
    }

    /*
    Method to redo and undo imageView, textView,WaveMusicView
     */
    fun remainingViewUndo(viewType: ViewType){
        Log.d("undotag", " layout")
        when(viewType){
            ViewType.TEXT_VIEW->{
                (child as LikhoEditText).undo()
                Log.d("undotag", "undo: layout")
            }
            ViewType.IMAGE_VIEW->{
                (child as LikhoImageView).undo()
            }
            ViewType.AUDIO->{
                (child as WaveformWithPlayPause).undo()
            }
            else->{

            }
        }
    }

    fun remainingViewRedo(viewType: ViewType){
        Log.d("redotag", "redolayout: ")
        when(viewType){
            ViewType.TEXT_VIEW->{
                (child as LikhoEditText).redo()
            }
            ViewType.IMAGE_VIEW->{
                Log.d("redotag", "redo:layout image ")
                (child as LikhoImageView).redo()
            }
            ViewType.AUDIO->{
                (child as WaveformWithPlayPause).redo()
            }
            else->{

            }
        }
    }
    /*
    Method to return the canvas view.
     */
    fun getCanvasView():CustomCanvasView{
        return canvasView
    }
}
