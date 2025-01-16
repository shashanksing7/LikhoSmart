package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Editable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.ccv.PenType
import nishkaaminnovations.com.likhosmart.WorkShop.MusicPlayer.la
import java.io.File


class cl @JvmOverloads constructor(context: Context, attr:AttributeSet?=null, defStyleAttr:Int=0): FrameLayout(context,attr,defStyleAttr),onChildViewClickListener {
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
    private lateinit var canvasView :ccv
    /*
   A constant used to scale up or down the layout when zooming. Default value is 1.2f.
    */
    private val ZOOM_FACTOR:Float=2f
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
    private var currentEditext: let? = null
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
    /**
     * Vraibales for calling the
     */
    private var initialDistance = 0f
    private var isPinching = false
    private var isZoomInAble=true
    private var isZoomOutAble=true

    /*
       Enum to represent the selected view type
     */
     enum class ViewType {
        CANVAS,
        IMAGE_VIEW,
        TEXT_VIEW,
        AUDIO,
        NO_SELECTED,
        NONE
     }

    /**
     * Variable for zooming
     */
    private val mScaleGestureDetector: ScaleGestureDetector
    private val mGestureDetector: GestureDetector
    private val MIN_ZOOM = 1.0f
    private val MAX_ZOOM = 40.0f
    private var scale = 1f
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var distanceX = 0f
    private var distanceY = 0f
    private var contentSize: RectF? = null
    private val mDispatchTouchEventWorkingArray = FloatArray(2)
    private val mOnTouchEventWorkingArray = FloatArray(2)
    private val matrix = Matrix()
    private val matrixInverse = Matrix()
    private val savedMatrix = Matrix()
    private lateinit var mCanvas:ccv

    private val mScaleGestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            oldDist = detector.currentSpan
            if (oldDist > 10f) {
                savedMatrix.set(matrix)
                mid.set(detector.focusX, detector.focusY)
            }
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale = detector.scaleFactor
            return true
        }
    }
    /*
    Init block.
     */

    init{

        /*
        Initialising the pop up view
         */
        InitialiseButtonsAtLocation()

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
                currentEditext= child!! as let

            }
            Log.d("tagmy", "onViewClicked: if block")
        }
        else if(viewType==ViewType.CANVAS){
            Log.d("tagmy", "onViewClicked: else block")
            childSelectedListener.getSelectedChild(viewType,isChildSelected,isLocked)
            Log.d("tagmy", "child type ${viewType}")
            clickedChildType=viewType
            if(child!=null&&viewType==ViewType.TEXT_VIEW){
                currentEditext= child!! as let
            }
            child = view
            if((link!="noURL" )){
                showLinkPopup(view,link,linkText,pathBottom, pathRight)
            }
        }
        else if(viewType==ViewType.NO_SELECTED){
                childSelectedListener.getSelectedChild(viewType,isChildSelected,isLocked)
                clickedChildType=ViewType.CANVAS
             child = view
            if((link!="noURL" )){
                    showLinkPopup(view,link,linkText,pathBottom, pathRight)

            }

        }
    }

    private var documentLocation=""
    private  var docPageNumber=""
/*
This method returns if the drawing mode in on or not
 */
    override fun isDrawingOn(): Boolean {
        return isDrawingOn
    }

    override fun getPageNumber(): String {
        return docPageNumber
    }

    override fun getDocumenLocation(): String {
        return documentLocation
    }

    fun setDocumentLocation(docName:String){
        Log.d("savingError", "setDocumentName: doc name = ${docName}")
        documentLocation=docName
    }
    fun setPageNumber(pageNumber:String){
        docPageNumber=pageNumber
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
        canvasView = ccv(context, null)
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
                    childSelectedListener.addPopUpChild(ViewType.CANVAS)
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
                    childSelectedListener.addPopUpChild(ViewType.IMAGE_VIEW)
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
                    child.isEnabled = child is ccv
                    child.isClickable = child is ccv
                }
                ViewType.IMAGE_VIEW -> {
                    child.isEnabled = child is lei
                    child.isClickable = child is lei
                }
                ViewType.TEXT_VIEW -> {
                    child.isEnabled = child is let
                    child.isClickable = child is let
                }
                ViewType.AUDIO -> {
                    child.isEnabled = child is la // Replace with your custom audio view class
                    child.isClickable = child is la
                }
                ViewType.NONE -> {
                    // Disable interaction for all views
                    child.isEnabled = false
                    child.isClickable = false
                }
                ViewType.NO_SELECTED->{}

            }
        }
    }


    // Method to dynamically add an ImageView
     fun addImageView(uri:Uri):lei {
        firstTouched = true
        // Create the ImageView (Replace with your custom class if needed)
        val imageView = lei(context, null,0,this)
        imageView.setUri(uri)
        imageView.setImageURI(uri)
//        val layoutParams = FrameLayout.LayoutParams(300, 300) // Set width and height to 300
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(imageView, layoutParams)
        if(imageView.x ==100f
            &&  imageView.y == 100f){
            imageView.x = 100f
            imageView.y = 100f
        }
        selectedViewType = ViewType.NONE
        clickedChildType=ViewType.IMAGE_VIEW
        child = imageView
        childSelectedListener.getSelectedChild(ViewType.IMAGE_VIEW,true,false)
        return imageView
    }

    // Method to dynamically add a TextView
     fun addTextView(x: Float, y: Float): let {
        firstTouched = true
        // Create the TextView
        val textView =
            let(
                context
            )
        textView.init(this,this)
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
        if(isZoomInAble){
            canvasView.checkZoom(event)
        }
        var gestureDetected = mGestureDetector.onTouchEvent(event)

        // Handle multi-touch events for scaling
        if (event.pointerCount > 1) {
            gestureDetected = mScaleGestureDetector.onTouchEvent(event) || gestureDetected
            if (checkScaleBounds()) {
                matrix.postScale(scale, scale, mid.x, mid.y)
            }
        }
        // Handle single touch events
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
                            // Uncomment to add an ImageView when implemented
                            // addImageView(touchX, touchY)
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
                            childSelectedListener.getSelectedChild(ViewType.NONE, false, true)
                            clickedChildType = ViewType.NONE
                            popupWindow.dismiss()
                        } else {
                            child?.isEnabled = false
                            isDraggable = true
                            childSelectedListener.getSelectedChild(ViewType.NONE, false, true)
                            clickedChildType = ViewType.NONE
                            popupWindow.dismiss()
                        }
                    }
                    ViewType.CANVAS -> {
                        Log.d("mytag", "onTouchEvent: ")
                    }

                    else -> {
                        // Handle any other view types here
                    }
                }

            }

            MotionEvent.ACTION_MOVE -> {

                if (isDragging) {
                    // Calculate movement deltas using raw coordinates to avoid view hierarchy issues
                    val dx = event.rawX - lastTouchX!!
                    val dy = event.rawY - lastTouchY!!

                    // Accumulate the deltas for translation
                    totalDx += dx
                    totalDy += dy

                    // Call the method to animate the translation with clamping
                    // (Assuming a method exists to apply translation logic here)

                    // Update last touch positions
                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false

            }
        }

        // Finalize matrix updates
        matrix.invert(matrixInverse)
        savedMatrix.set(matrix)
        invalidate()

        return gestureDetected || true // Ensures true is returned for handled events
    }

    /**
     *
     * Methods for zoom in zoom out
     */


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        // Store initial translation values
        if (changed) {
            initialDx = translationX
            initialDy = translationY
        }
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
            }
        }

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
            setXfermode(null) // Clear mode for erasing
            setColor(Color.WHITE) // Transparent color isn't mandatory here but aligns intent
            setStrokeStyle(Paint.Style.STROKE) // Ensure the paint style matches your drawing
            setAlpha(255)
            canvasView.setCap(Paint.Cap.ROUND)     // Rounded end for normal brush
            canvasView.setJoin(Paint.Join.ROUND)   // Rounded join for normal brush// Full effect (CLEAR mode handles transparency separately)
            invalidate() // Redraw after setting up eraser
        }
        canvasView.setLaserOn(false)
        canvasView.setEraserOn(true)
        Log.d("mytag", "latout: "+canvasView.isEraserOn())
        Log.d("mytag", "setBrushToErase: eraser")

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
        Log.d("mytag", "setBrushToDashed: dashed")
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
        Log.d("mytag", "setBrushToHighlighter: highlighter")
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
        Log.d("mytag", "setBrushToNormal: normal")
    }

    fun setBrushToLaser() {
        canvasView.setTheValueOfIsLaserOn(true)
        canvasView.setEraserOn(false)
        canvasView.setLaserOn(true)
        Log.d("mytag", "setBrushToLaser: laser ")
    }
    fun setBrushWidth(width:Float){
        canvasView.setStrokeWidth(width)
    }

    fun addMusic(wave:la){
        wave.x=100f
        wave.y=100f
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        wave.setOnChildClickListener(this)
        addView(wave,layoutParams)
        child =wave
        childSelectedListener.getSelectedChild(ViewType.AUDIO,true,false)
        clickedChildType=ViewType.AUDIO
        selectedViewType = ViewType.NONE
        Log.d("selectedtype", "addMusic: ${selectedViewType}")
    }
    /*
    Method to get the current selected child of the layout
     */
    fun getCurrentEditText(): let?{
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
                val selectedChild=child as let
               val copiedEditText=
                   let(
                       context
                   )
                copiedEditText.x=selectedChild.x+100f
                copiedEditText.y=selectedChild.y+100f
                copiedEditText.rotation=selectedChild.rotation
                copiedEditText.init(this,this)
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
                val selectedChild=child as lei
                val copiedImageView=lei(context,null,0,this)
                copiedImageView.x=selectedChild.x+100f
                copiedImageView.y=selectedChild.y+100f
                copiedImageView.rotation=selectedChild.rotation
                /*
                Getting and setting the uri
                 */
                val imageUri=selectedChild.getUri()
                val name="copied_image_${System.currentTimeMillis()}.jpg"
                val outputFile=File(
                     getDocumenLocation() + File.separator + "likhoImage" + File.separator + name
                )
                val newImgUri=(copyURIContent(context,imageUri,outputFile))
                copiedImageView.setImageURI(newImgUri)
                copiedImageView.setUri(newImgUri)
                val layoutParams = FrameLayout.LayoutParams(selectedChild.width, selectedChild.height)
                copiedViewLayoutParams=layoutParams
                copiedView=copiedImageView
                firstTouched = true
                selectedViewType = ViewType.NONE
                child!!.isEnabled=false
                child=copiedImageView

            }
            ViewType.AUDIO->{
                val selectedChild=child as la
                val name="copied_audio_${System.currentTimeMillis()}.mp3"
                val audioFile = File(
                    getDocumenLocation() + File.separator + "likhoAudio" + File.separator + name
                )
                val audioUri=(copyURIContent(context,selectedChild.getAudioUri(),audioFile))
                val copiedWaveAudio=la(context,null,0,audioUri)
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
                (child as ccv).copySelectedPaths()
            }

            else->{

            }
        }

    }

    /*
    method to copy the content of the uri
     */
    private fun  copyURIContent(context:Context,uri:Uri,outFile:File):Uri{
        val inputStream=File(uri.path).inputStream()
        inputStream.use {
            input->
            outFile.outputStream().use {
                output->
                input?.copyTo(output)
            }
        }
        return  Uri.fromFile(outFile)
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
                Log.d("mytag", "deleteView: Audio")
                val musicUri=(child as la).getAudioUri()
                (child as la).delete()
                deleteFileAtUri(musicUri)
            }
            else if(clickedChildType==ViewType.TEXT_VIEW){
                (child as let).delete()
            }
            else if(clickedChildType==ViewType.IMAGE_VIEW){
                val imageURI=(child as lei).getUri()
                Log.d("mytag", "deleteView: ${(child as lei).getFileName()}")
                (child as lei).delete()
                deleteFileAtUri(imageURI)
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
                    (child as let).locked=lock
                    Log.d("mytag", "lockUnlockChild: text ")
                }
                ViewType.IMAGE_VIEW->{
                    child!!.invalidate()
                    (child as lei).setLocked(lock)

                }
                ViewType.AUDIO->{
                    child!!.invalidate()
                    (child as la).setLocked(lock)
                }
                ViewType.CANVAS->{
                    child!!.invalidate()
                    (child as ccv).lockSelectedPaths()
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
    fun deleteFileAtUri(uri: Uri): Boolean {
        return try {
            Log.d("mytag", "deleteFileAtUri: ${uri.path.toString()}")
            val deleteFile=File(uri.path)
            if(deleteFile.exists()){
                Log.d("mytag", "deleteFileAtUri: exista")
            }
            deleteFile.delete()
            true
        }
        catch(e:Exception){
            Log.d("mytag", "deleteFileAtUri: ${e.toString()}")
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
                    (child as let).linkName=linkName
                    (child as let).linkUrl=linkURL
                }
                ViewType.IMAGE_VIEW->{
                    child!!.invalidate()
                    (child as lei).setLinkName(linkName)
                    (child as lei).setLinkUrl(linkURL)
                }
                ViewType.AUDIO->{
                    child!!.invalidate()
                    (child as la).setLinkName(linkName)
                    (child as la).setLinkUrl(linkURL)
                }
                ViewType.CANVAS->{
                    child!!.invalidate()
                    (child as ccv).setPathLink(linkURL,linkName)
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
       if(isLassoSelected){
           canvasView.setLaserOn(false)
           canvasView.setEraserOn(false)
       }
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
//        canvasView.undoAction()
        canvasView.undoCanvasAction()
//        canvasView.undo()
    }
    fun redoCanvas(){
//        canvasView.redoAction()
        canvasView.redoCanvasAction()
//        canvasView.redo()
    }

    /*
    Method to redo and undo imageView, textView,WaveMusicView
     */
    fun remainingViewUndo(viewType: ViewType){
        Log.d("undotag", " layout")
        when(viewType){
            ViewType.TEXT_VIEW->{
                (child as let).undo()
                Log.d("undotag", "undo: layout")
            }
            ViewType.IMAGE_VIEW->{
                (child as lei).undo()
            }
            ViewType.AUDIO->{
                (child as la).undo()
            }
            else->{

            }
        }
    }

    fun remainingViewRedo(viewType: ViewType){
        Log.d("redotag", "redolayout: ")
        when(viewType){
            ViewType.TEXT_VIEW->{
                (child as let).redo()
            }
            ViewType.IMAGE_VIEW->{
                Log.d("redotag", "redo:layout image ")
                (child as lei).redo()
            }
            ViewType.AUDIO->{
                (child as la).redo()
            }
            else->{

            }
        }
    }
    /*
    Method to return the canvas view.
     */
    fun getCanvasView():ccv{
        return canvasView
    }

    /***
     *
     *
     * Zoom and panning code
     */


    private var isZoomOn=false
    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            savedMatrix.set(matrix)
            start.set(event.x, event.y)
            return true
        }
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dX: Float, dY: Float): Boolean {
            setupTranslation(dX, dY)
            matrix.postTranslate(distanceX, distanceY)
            getCumulativeTranslation()
            return true
        }
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {

            return true
        }
        override fun onLongPress(event: MotionEvent) {
            // Get the x and y coordinates of the long press
            Log.d("gesHH", "onDown: action is ${event.action}")
            if(!isZoomOn){
               val x = event.x
               val y = event.y

               // Show buttons at this location
               showLongPressPopUp(x,y)
           }

        }
    }

     fun startTranslate(){
        savedMatrix.set(matrix)
        matrix.postTranslate(150f, 0f)
        getCumulativeTranslation()
         // Finalize matrix updates
         matrix.invert(matrixInverse)
         savedMatrix.set(matrix)
         invalidate()
    }
    init {
        mScaleGestureDetector = ScaleGestureDetector(context, mScaleGestureListener)
        mGestureDetector = GestureDetector(context, mGestureListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        val values = FloatArray(9)
        matrix.getValues(values)
        canvas.save()
        canvas.translate(values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y])
        canvas.scale(values[Matrix.MSCALE_X], values[Matrix.MSCALE_Y])
        super.dispatchDraw(canvas)
        canvas.restore()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    private fun checkScaleBounds(): Boolean {
        val values = FloatArray(9)
        matrix.getValues(values)
        val sx = values[Matrix.MSCALE_X] * scale
        val sy = values[Matrix.MSCALE_Y] * scale
        return sx in (MIN_ZOOM + 1e-5f)..MAX_ZOOM && sy in (MIN_ZOOM + 1e-5f)..MAX_ZOOM
    }

    private fun setupTranslation(dX: Float, dY: Float) {
        distanceX = -dX
        distanceY = -dY
        contentSize?.let {
            val values = FloatArray(9)
            matrix.getValues(values)
            val totX = values[Matrix.MTRANS_X] + distanceX
            val totY = values[Matrix.MTRANS_Y] + distanceY
            val sx = values[Matrix.MSCALE_X]
            val viewableRect = Rect()
            getDrawingRect(viewableRect)
            val offscreenWidth = it.width() - (viewableRect.right - viewableRect.left)
            val offscreenHeight = it.height() - (viewableRect.bottom - viewableRect.top)
            val maxDx = (it.width() - it.width() / sx) * sx
            val maxDy = (it.height() - it.height() / sx) * sx

            if (totX > 0 && distanceX > 0) distanceX = 0f
            if (totY > 0 && distanceY > 0) distanceY = 0f
            if (totX * -1 > offscreenWidth + maxDx && distanceX < 0) distanceX = 0f
            if (totY * -1 > offscreenHeight + maxDy && distanceY < 0) distanceY = 0f
        }
    }

    fun setContentSize(width: Float, height: Float) {
        contentSize = RectF(0f, 0f, width, height)
    }
    fun getCumulativeTranslation(){
        // Get the matrix values (translation and scaling)
        val values = FloatArray(9)
        matrix.getValues(values)

        // Total translation
        val totalTranslationX = values[Matrix.MTRANS_X]
        val totalTranslationY = values[Matrix.MTRANS_Y]

        // Scaling factors
        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        canvasView.setTrans(totalTranslationX,totalTranslationY,scaleX,scaleY)

    }

    fun clOnTouchEvent(event: MotionEvent){

    }
    fun setIsZoomOn(isZoomOn:Boolean){
        this.isZoomOn=isZoomOn
        Log.d("mytag", "setIsZoomOn: ${isZoomOn}")
    }
    fun saveCanvasPath():Boolean{

       return canvasView.savePaths()

    }


}
