package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.PathMeasure
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses.LikhoPathDeserializer
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses.LikhoPathSerializer
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses.PaintPropertiesDeserializer
import java.io.File
import java.io.FileWriter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/*
This class will represent the canvas for our workshop.
 */
class CustomCanvasView@JvmOverloads constructor(context: Context, attr: AttributeSet?=null, defStyleAttr:Int=0) : View(context,attr,defStyleAttr) {
    /*
    This will represent the paint object.
     */
    private lateinit var normalPaint:Paint
   /**
   Objects that will be used to set the property of the paint object.
    **/
   private var color: Int = Color.BLACK          // Path color
    private var strokeWidth: Float = 5f       // Stroke width
    private var strokeStyle: Paint.Style = Paint.Style.STROKE // Fill or Stroke style
    private var alpha: Int = 255                   // Transparency (0-255)
    private var pathEffect: PathEffect? = null     // Dashed or patterned lines
    private var shader: Shader? = null             // Gradient or pattern
    private var cap: Paint.Cap = Paint.Cap.ROUND   // Line end style
    private var join: Paint.Join = Paint.Join.ROUND// Line join style
    private var xfermode: PorterDuffXfermode? = null // Blending mode or erase
    /*
    lists to hold the paths that have been drawn on the canvas by the user.
     */
    private val drawnPaths: MutableList<likhoPath> = mutableListOf()
    /*
    Variable to hold the path that has been selected by the lasso tool
     */
    private var intersectedPaths = mutableListOf<likhoPath>()
    /*
    Variable to hold the path that has been copied by the user
     */
    private var copiedPaths = mutableListOf<likhoPath>()
    /*
    Variables to represent if lasso tool has been selected by user or not.
     */
    private var isLassoSelected:Boolean=false
    /*
    variable to represent the path selected during user tap.
     */
    private  var selectedPath:likhoPath?=null
    /*
    Variable to hold the eraser paths .
     */
    private val eraserPaths: MutableList<likhoPath> = mutableListOf()
    /*
    Variable to denote if erases is on or not.
     */
    private  var isEraserOn:Boolean=false;
    /*
    Variable to represent the currently drawn path.
     */
    private lateinit var mPath:Path
    /*
    variables to draw the on
     */
    private var mX: Float = 0f
    private var mY:Float = 0f
    /*
    Variable to represent the tolerance between pints to be considered.
     */
    private val TOUCH_TOLERANCE: Float = 4f
    /*
    Variable to represent the .
     */
    private lateinit var quadtree:QuadTree;
    /*
    Variable to represent if the laser has been selected or not.
     */
    private var isLaserOn:Boolean=false
    /*
    Variable to represent if the laser should be drawn or not.
     */
    private  var drawLaser:Boolean=false;
    /*
    Variable to represent the x and y of the laser.
     */
    private var laserX:Float=0f
    private var laserY:Float=0f
    /*
    APint objects for the laser
     */
    private var innerCirclePaint: Paint? = null
    private var outerGlowPaint: Paint? = null
    /*
    This variable represents the overall bounding rectangle of the selected paths.
     */
    var overallBounds: RectF? = null
    /*
    This variable is used to represent if the lasso has intersected any path or not.
     */
    private var hasIntersected=false;
    /*
    Variable to represent the lasso path .
     */
    private  var lassoPath:likhoPath?=null
    /*
    variable will represent if the usr is attemting to dragging or not.
     */
    private  var isDragging:Boolean=false
    /*
    Variables for offsetting the selected paths.
     */
    private var prevX: Float = 0f
    private var prevY:Float = 0f
    /*
    This variable will represent if the app is in read mode.
     */
    private var isReadingModeOn:Boolean=true
    /*
    Varibale to represent the bounding rectangle paint .
     */
    private lateinit var rectPaint:Paint;

    /*
    Purpose: An interface for handling click events on the view.
    Usage: This is used to notify when the view is clicked (e.g., when resizing or rotating is completed). The listener provides a method (onViewClicked) that is invoked when the view is clicked.
    */
    private lateinit var onChildClickListener: onChildViewClickListener
    /*
    This variable will store the current path tapped by user.
     */
    private   var currentPath:likhoPath?=null
    /*
    Lasso path paint.
     */
    private  lateinit var lassoPaint:Paint
    /*
     Variable to represent the selected pen type
    */
    private var selectedPenType:PenType=PenType.NormalPen

    /*
    This variable will represent if the user is offsetting or not
     */
    private var  isOffSetting:Boolean=false
    /*
    These variables represent the stack for undo and redo actions.
     */
    // Declare the combined undo and redo stacks
    private var undoStack: MutableList<UndoAction> = mutableListOf()
    private var redoStack: MutableList<UndoAction> = mutableListOf()
    /*
    This variable will be used to make sure that .
     */
    private var storUndoOnce:Boolean=false
    /*
    This varibale will be used to represent if the selected paths are being dragged once again
     */
    private var isFirstCall = true
    /*
Variables that will be used to save the edittext .
*/
    private val typeName = "likhoCanvas"
    private val DocumentName = "Trial"
    private var pathFile: File? = null
    private var pageNumber: String="Page1"
    /*
    The init block to initialize the paint object and other variables.
     */
    init {
        /*
        Initialising the lasso paint.
         */
        lassoPaint= Paint().apply {
            color = Color.RED  // Change to desired color
            style = Paint.Style.STROKE
            strokeWidth = 8f  // Thick stroke for the lasso path
            pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)  // Dashed effect
            isAntiAlias = true
            isDither = true
        }
        /*
        initializing tree.
         */
        quadtree= QuadTree(RectF(0f, 0f, width.toFloat(), height.toFloat()))
        /*
        initialising the normal paint
         */
        normalPaint=Paint()
        normalPaint.isAntiAlias=true

        /*
        initialising the rect paint variable.
         */
        // Set paint properties for the rectangle
        rectPaint=Paint().apply {
            setColor(Color.BLUE)
            setStyle(Paint.Style.STROKE)
            setStrokeWidth(5f)
        }
        /**
         * Initialising the laser's paint object.
         */
        // Create paint for the inner circle with gradient (Red to Yellow)
        innerCirclePaint = Paint()
        innerCirclePaint!!.style = Paint.Style.FILL
        innerCirclePaint!!.isAntiAlias = true
        // Radial Gradient from Red to Yellow for the inner circle
        val gradient = RadialGradient(
            0f, 0f, 100f,
            intArrayOf(Color.RED, Color.YELLOW), null, Shader.TileMode.CLAMP
        )
        innerCirclePaint!!.setShader(gradient)

        // Create paint for the outer glow circle (Red with blur)
        outerGlowPaint = Paint()
        outerGlowPaint!!.setStyle(Paint.Style.FILL)
        outerGlowPaint!!.setAntiAlias(true)
        outerGlowPaint!!.setColor(Color.RED) // Set to red color for the outer glow
        outerGlowPaint!!.setMaskFilter(BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL))
    }
    /*
    The onDraw method.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        /*
        iterating on the path list and drawing them according to their specification.
         */
        for(path in drawnPaths) {
            canvas.drawPath(path.path, createPaintObject(path.properties))
        }
        for(path in eraserPaths){

            canvas.drawPath(path.path,createPaintObject(path.properties))
        }
        // Draw the lasso path if it's being drawn
        if (isLassoSelected && lassoPath!=null) {
            canvas.drawPath(lassoPath!!.path, lassoPaint)
        }
        /*
        Draw the rectangle around the selected or intersected paths.
         */
        if(hasIntersected){
            canvas.drawRect(overallBounds!!,rectPaint)
        }
        /*
        Draw the laser if the selected.
         */
        if(drawLaser){
            canvas.drawCircle(laserX,laserY, 25f, outerGlowPaint!!)
            canvas.drawCircle(laserX,laserY, 5f, innerCirclePaint!!)
        }

    }

    private lateinit var lp:likhoPath

    /*
    Methods to draw the path on the canvas.
     */
    private fun touchStart(x: Float, y: Float) {

        /*
        Setting the current path and it's properties
         */
        mPath = Path()
         lp = likhoPath(mPath,
            PaintProperties(color,strokeWidth, strokeStyle, alpha, pathEffect, shader, cap, join, xfermode),false,"noURL","noURL",0f,0f,0f,0f,"",generateUniqueName()!!
        )
        /*
        Checking if eraser is on and adding the paths to respective lists.
         */
        if(!isEraserOn&&!isLassoSelected){
            drawnPaths.add(lp)
            Log.d("mytag", "touchStart: normal")
        }
        else{
            if(isEraserOn){
                eraserPaths.add(lp)
                Log.d("mytag", "touchStart: eraser")
            }
            if(isLassoSelected){
                Log.d("mytag", "touchStart: lasso")
                lassoPath=lp
            }
        }
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
    }
    private fun touchMove(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }
    private fun touchUp() {
        mPath.lineTo(mX, mY)
        addDrawAction(lp)
        isFirstCall=true
        /*
        Sending the paths for intersection check if lasso is enabled and user is done drawing it
         */
        if (isLassoSelected&&lassoPath!=null) {
            intersectedPaths=checkForIntersections() // Insert after drawing the complete path
            lassoPath=null
            if(intersectedPaths.size>0){
                overallBounds=calculateBoundingBox()
                hasIntersected=true
                handleLassoModeTap()
                invalidate()
            }

        }

        CoroutineScope(Dispatchers.IO).launch {
            lp.xml=convertPathToSVG(lp.path)
            initialiseFile(lp.pathName)
            saveLikhoEditText(lp)
        }
    }
    /*
    method to set the paint object according to the
     */
    // Method to initialize the Paint object with the above variables
    fun createPaintObject(paintProperties: PaintProperties): Paint {
        normalPaint.apply {
            color = paintProperties.color
            strokeWidth =  paintProperties.strokeWidth
            style =  paintProperties.strokeStyle
            alpha = paintProperties.alpha
            pathEffect =  paintProperties.pathEffect
            shader =  paintProperties.shader
            strokeCap =  paintProperties.cap
            strokeJoin =  paintProperties.join
            xfermode =  paintProperties.xfermode
        }
        return normalPaint.apply {setAntiAlias(true)
        setDither(true)}
    }
    /**
     * onTouch method.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) {
            // If the view is disabled, do not handle touch events
            return false
        }

        val x = event!!.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                laserX = x
                laserY = y

                // Handle laser tool if enabled
                if (isLaserOn) {
                    drawLaser = true
                    invalidate()
                    return true
                }

                // Handle reading mode and check if user taps on a path
                if (isReadingModeOn && !hasIntersected) {
                    intersectedPaths = checkForPointIntersections(x, y)
                    if (intersectedPaths.isNotEmpty()) {
                        hasIntersected = true
                        overallBounds = calculateBoundingBox()
                            currentPath=intersectedPaths.get(0)
                            // Get the screen position below the RectF or overallBounds
                            val xPosition = overallBounds!!.left // Align to the left of the rectangle
                            val yPosition = overallBounds!!.bottom // Position just below the rectangle

                            // Optional offset if you need to add spacing below
                            val offset = 10 // Adjust this based on your UI needs
                            val adjustedYPosition = yPosition + offset

                            // Pass these positions to your onChildClickListener
                            onChildClickListener?.onViewClicked(
                                this,
                                CustomLayout.ViewType.CANVAS,
                                false,
                                true,
                                currentPath!!.linkUrl,
                                currentPath!!.linkName,
                                adjustedYPosition-1000f,  // Y position below the rectangle
                                xPosition-100f           // X position aligned with left or right of the rectangle
                            )

                        invalidate()
                        return true
                    }
                }
                // Handle dragging if a selection exists
                if (hasIntersected) {
                    if (isPointInsideRect(overallBounds, x, y)) {
                        isDragging = true
                        storUndoOnce=true
                        prevX = x
                        prevY = y
                    } else {
                        // Reset selection when tapping outside
                        resetSelection()
                    }
                    invalidate()
                    return true
                }

                // Handle starting a new path if not dragging or selecting
                if (!isEraserOn && !isLassoSelected) {
                    touchStart(x, y)
                } else if (isEraserOn) {
                    touchStart(x, y)
                } else if (isLassoSelected) {
                    touchStart(x, y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (drawLaser) {
                    laserX = x
                    laserY = y
                    invalidate()
                    return true
                }
                if (isDragging) {
                    // Offset the intersecting paths for dragging
                    offsetIntersectingPaths(x, y)
                    prevX = x
                    prevY = y
                } else {
                    // Handle regular drawing or lasso movement
                    touchMove(x, y)
                }
            }

            MotionEvent.ACTION_UP -> {
                if (drawLaser) {
                    drawLaser = false
                } else if (isDragging) {
                    isDragging = false
                    storUndoOnce=false
                    addOffsetAction(intersectedPaths)
                } else {
                    // Complete the drawing or lasso path
                    touchUp()
                }
            }
        }
        invalidate() // Redraw the canvas for all actions
        return true
    }
    /**
     * Methods to check if the paths intersects or not.
    */
    fun checkForIntersections(): MutableList<likhoPath> {
        Log.d("mytag", "checkForIntersections: start")
        Log.d("mytag", "checkForIntersections: in intersection drawnPaths size = "+drawnPaths.size)
        val intersectingPaths: MutableList<likhoPath> = ArrayList()
        // Ensure there are at least two paths to compare
        if (drawnPaths.size <1) {
            Log.d("mytag", "checkForIntersections: Not enough paths to check for intersections.")
            return intersectingPaths // Return empty list
        }
        // Take the last path in the list
        val lastPath:likhoPath = lassoPath!!
        val lastPathMeasure = PathMeasure(lastPath.path, false)
        val lastPathLength = lastPathMeasure.length
        val step = 10f // Step size for points along the path

        // Cache points for the last path
        val lastPathPoints: MutableList<FloatArray> = ArrayList()
        for (i in 0..(lastPathLength / step).toInt()) {
            val point = FloatArray(2)
            lastPathMeasure.getPosTan(i * step, point, null)
            lastPathPoints.add(point)
        }

        // Iterate over the remaining paths in the list
        for (k in 0 until drawnPaths.size) {
            val currentPath: Path =drawnPaths.get(k).path
            val currentPathMeasure = PathMeasure(currentPath, false)
            val currentPathLength = currentPathMeasure.length

            // Cache points for the current path
            val currentPathPoints: MutableList<FloatArray> = ArrayList()
            for (j in 0..(currentPathLength / step).toInt()) {
                val point = FloatArray(2)
                currentPathMeasure.getPosTan(j * step, point, null)
                currentPathPoints.add(point)
            }

            // Loop through segments of the last path
            var hasIntersected = false
            for (i in 0 until lastPathPoints.size - 1) {
                val startLast = lastPathPoints[i]
                val endLast = lastPathPoints[i + 1]
                val lastBBox = createBoundingBox(startLast, endLast)
                // Loop through segments of the current path
                for (j in 0 until currentPathPoints.size - 1) {
                    val startCurrent = currentPathPoints[j]
                    val endCurrent = currentPathPoints[j + 1]

                    val currentBBox = createBoundingBox(startCurrent, endCurrent)

                    // Check bounding box intersection first
                    if (RectF.intersects(lastBBox, currentBBox)) {
                        // Check if segments actually intersect
                        if (doSegmentsIntersect(startLast, endLast, startCurrent, endCurrent)) {
                            Log.d(
                                "mytag",
                                "checkForIntersections: found intersection with path at index $k"
                            )
                            hasIntersected = true
                            break // No need to check more segments of the current path
                        }
                    }
                }

                if (hasIntersected) {
                    break // Exit the loop for segments of the last path
                }
            }
            if (hasIntersected) {
                intersectingPaths.add(drawnPaths.get(k))
            }
        }
        Log.d(
            "mytag",
            "checkForIntersections: total intersecting paths = " + intersectingPaths.size
        )
        return intersectingPaths
    }

    /*
     Helper method to create a bounding box for a line segment
     */
    private fun createBoundingBox(start: FloatArray, end: FloatArray): RectF {
        val left = min(start[0].toDouble(), end[0].toDouble()).toFloat()
        val right = max(start[0].toDouble(), end[0].toDouble()).toFloat()
        val top = min(start[1].toDouble(), end[1].toDouble()).toFloat()
        val bottom = max(start[1].toDouble(), end[1].toDouble()).toFloat()
        return RectF(left, top, right, bottom)
    }
    private fun doSegmentsIntersect(
        p1: FloatArray,
        p2: FloatArray,
        q1: FloatArray,
        q2: FloatArray
    ): Boolean {
        return if (orientation(p1, p2, q1) != orientation(p1, p2, q2) &&
            orientation(q1, q2, p1) != orientation(q1, q2, p2)
        ) {
            true
        } else {
            checkIfOverlapping(p1, p2, q1, q2)
        }
    }

    // Utility method to check the orientation of three points
    private fun orientation(p: FloatArray, q: FloatArray, r: FloatArray): Int {
        val `val` = (q[1] - p[1]) * (r[0] - q[0]) - (q[0] - p[0]) * (r[1] - q[1])
        //        Log.d("mytag", "orientation: value = " +val);
        if (`val` == 0f) return 0 // Collinear

        return if ((`val` > 0)) 1 else 2 // Clockwise or counterclockwise
    }

    private fun checkIfOverlapping(
        p: FloatArray,
        q: FloatArray,
        r: FloatArray,
        s: FloatArray
    ): Boolean {
        // Validate input

        require(!(p.size != 2 || q.size != 2 || r.size != 2 || s.size != 2)) { "Each point must have exactly 2 coordinates." }

        // Calculate midpoints
        val midpoint1 = floatArrayOf((p[0] + q[0]) / 2, (p[1] + q[1]) / 2)
        val midpoint2 = floatArrayOf((r[0] + s[0]) / 2, (r[1] + s[1]) / 2)

        // Calculate distance between midpoints
        val dx = midpoint2[0] - midpoint1[0]
        val dy = midpoint2[1] - midpoint1[1]
        val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        if (distance < (strokeWidth) + 1) {
            return true
        }
        return false
    }
    /**
     * Method to check if the user tapped point intersects with any path or not
     */
    fun checkForPointIntersections(x: Float, y: Float): MutableList<likhoPath> {
        Log.d("mytag", "checkForPointIntersections: start")

        val intersectingPaths: MutableList<likhoPath> = ArrayList()

        // Ensure there are paths to compare
        if (drawnPaths.isEmpty()) {
            Log.d("mytag", "checkForPointIntersections: No paths to check.")
            return intersectingPaths // Return empty list
        }

        // Iterate over the paths in the list
        for (k in 0 until drawnPaths.size) {
            if(drawnPaths[k].isLocked){
                continue
            }
            val currentPath: Path = drawnPaths[k].path

            val currentPathMeasure = PathMeasure(currentPath, false)
            val currentPathLength = currentPathMeasure.length

            // Iterate over segments of the current path
            for (j in 0 until (currentPathLength / 10f).toInt()) {
                val point = FloatArray(2)
                currentPathMeasure.getPosTan(j * 10f, point, null)

                // Check if the point (x, y) is close to the current point
                val distance = Math.sqrt(
                    Math.pow((point[0] - x).toDouble(), 2.0) + Math.pow((point[1] - y).toDouble(), 2.0)
                )

                if (distance < 10) { // A threshold to determine if the point is close to the path segment
                    Log.d(
                        "mytag",
                        "checkForPointIntersections: found intersection with path at index $k"
                    )
                    intersectingPaths.add(drawnPaths[k])
                    break // No need to check more segments of the current path
                }
            }
        }

        Log.d(
            "mytag",
            "checkForPointIntersections: total intersecting paths = " + intersectingPaths.size
        )
        return intersectingPaths
    }
    /**
     * Method to check if the user touch lies inside the selected bounding rectangle or not
     */

    private fun calculateBoundingBox(): RectF? {
        val paths: List<likhoPath> = intersectedPaths
        if (paths.isEmpty()) return null  // Return null if there are no paths

        // Initialize variables to hold the overall bounding box
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        // Iterate through all the paths to find the bounding box that contains all of them
        for (fp in paths) {
            val bounds = RectF()
            fp.path.computeBounds(bounds, true)

            // Update the overall bounding box
            minX = min(minX.toDouble(), bounds.left.toDouble()).toFloat()
            minY = min(minY.toDouble(), bounds.top.toDouble()).toFloat()
            maxX = max(maxX.toDouble(), bounds.right.toDouble()).toFloat()
            maxY = max(maxY.toDouble(), bounds.bottom.toDouble()).toFloat()
        }

        // Return the calculated bounding box as a RectF
        return RectF(minX, minY, maxX, maxY)
    }

    fun isPointInsideRect(rectF: RectF?, x: Float, y: Float): Boolean {
        if (rectF == null) {
            return false
        }
        return rectF.contains(x, y)
    }

    /**
     * Method to offset the selected path.
     */
    private fun offsetIntersectingPaths(x: Float, y: Float) {
        // Calculate the difference in x and y coordinates
        val dx: Float = x - prevX
        val dy: Float = y - prevY


        // Offset the intersecting paths
        for (path in intersectedPaths) {
            if(isFirstCall){
                path.xOffSet=0f
                path.yOffSet=0f
            }
            path.path.offset(dx, dy)
            path.xOffSet+=dx
            path.yOffSet+=dy
            CoroutineScope(Dispatchers.IO).launch {
                saveLikhoEditText(path)
            }
        }
        // Offset the overall bounding rectangle
        overallBounds!!.offset(dx, dy)

        // Update the previous touch position
        prevX = x
        prevY = y
        isFirstCall=false
    }

    /*
    setter method for all the variables
     */
    // Getter methods
    fun getColor(): Int = color
    fun getStrokeWidth(): Float = strokeWidth
    fun getStrokeStyle(): Paint.Style = strokeStyle
    fun getPaintAlpha(): Int = alpha
    fun getPathEffect(): PathEffect? = pathEffect
    fun getShader(): Shader? = shader
    fun getCap(): Paint.Cap = cap
    fun getJoin(): Paint.Join = join
    fun getXfermode(): PorterDuffXfermode? = xfermode
    fun getisReadingModeOn(): Boolean =isReadingModeOn



    // Setter methods

    fun setReadingMode(isOn: Boolean) {
        isReadingModeOn = isOn
    }
    fun setColor(newColor: Int) {
        color = newColor
    }

    fun setStrokeWidth(newWidth: Float) {
        strokeWidth = newWidth
    }

    fun setStrokeStyle(newStyle: Paint.Style) {
        strokeStyle = newStyle
    }

    fun setAlpha(newAlpha: Int) {
        alpha = newAlpha
    }

    fun setPathEffect(newEffect: PathEffect?) {
        pathEffect = newEffect
    }

    fun setShader(newShader: Shader?) {
        shader = newShader
    }

    fun setCap(newCap: Paint.Cap) {
        cap = newCap
    }

    fun setJoin(newJoin: Paint.Join) {
        join = newJoin
    }

    fun setXfermode(newXfermode: PorterDuffXfermode?) {
        xfermode = newXfermode
    }
    /*
    Setter and getter for laser.
     */
    fun isLaserOn(): Boolean {
        return isLaserOn
    }
    fun setLaserOn(isOn: Boolean) {
        isLaserOn = isOn
    }

    /*
Setter and getter for laser.
 */
    fun isEraserOn(): Boolean {
        Log.d("mytag", "setEraserOn get  true: "+isEraserOn)
        return isEraserOn
    }
    fun setEraserOn(isOn: Boolean) {
        Log.d("mytag", "setEraserOn true: "+isOn)
        isEraserOn = isOn
    }
    /*
    Method to copy the selected paths
     */
    fun copySelectedPaths(){
        copiedPaths.clear()
        for (originalLikhoPath in intersectedPaths) {
            // Copy and offset path
            val newPath = Path().apply {
                addPath(originalLikhoPath.path)
                offset(100f, 100f)
            }

            // Copy properties
            val paintProperties = PaintProperties(
                color = originalLikhoPath.properties.color,
                strokeWidth = originalLikhoPath.properties.strokeWidth,
                strokeStyle = originalLikhoPath.properties.strokeStyle,
                alpha = originalLikhoPath.properties.alpha,
                pathEffect = originalLikhoPath.properties.pathEffect,
                shader = originalLikhoPath.properties.shader,
                cap = originalLikhoPath.properties.cap,
                join = originalLikhoPath.properties.join,
                xfermode = originalLikhoPath.properties.xfermode
            )

            // Create a new likhoPath
            val isLocked = originalLikhoPath.isLocked
            val linkURL=originalLikhoPath.linkUrl
            val linkName=originalLikhoPath.linkName
            copiedPaths.add(likhoPath(newPath, paintProperties, isLocked,linkURL,linkName,originalLikhoPath.xOffSet,originalLikhoPath.yOffSet,originalLikhoPath.prevXOffSet,originalLikhoPath.prevYOffSet,originalLikhoPath.xml,originalLikhoPath.pathName))
            Log.d("mytag", "pasteCopiedPaths: copied size = "+copiedPaths.size)
        }

    }
    /*
   Method to paste the paths
     */
    fun pasteCopiedPaths(){
        Log.d("mytag", "pasteCopiedPaths: copied size = "+copiedPaths.size)
        if(copiedPaths.size>0){
            Log.d("mytag", "pasteCopiedPaths: ")
            drawnPaths.addAll(copiedPaths)
            invalidate()
        }
    }
    fun deleteSelectedPaths(){
        // Remove paths from drawnPaths that are in intersectedPaths
        drawnPaths.removeAll { drawnPath ->
            intersectedPaths.any { intersectedPath ->
                // Compare if the paths are the same
                drawnPath.path == intersectedPath.path
            }
        }
        intersectedPaths.clear()
        isDragging=false
        hasIntersected=false
        overallBounds=null

        invalidate()
    }
    /*
    Method to lock the paths
     */
    fun lockSelectedPaths(){
        // Use forEach to iterate over intersectedPaths and change isLocked
        intersectedPaths.forEach { path ->
            path.isLocked =!path.isLocked // or false, depending on your requirement
        }
    }
    // Setter for the interface listener
    fun setOnChildClickListener(listener: onChildViewClickListener) {
        onChildClickListener = listener
    }

    /*
    Hepler method to reset the variables.
     */
    private fun resetSelection() {
        hasIntersected = false
        intersectedPaths.clear()
        overallBounds = null
        lassoPath = null
        copiedPaths.clear()
        onChildClickListener?.onViewClicked(this, CustomLayout.ViewType.NONE, false,false,"noURL","",0f,0f)
        invalidate()
        currentPath=null
    }
    /*
    Method to set the linkURL and linkNAME
     */
    fun setPathLink(linkURL:String,linkName:String){
        if(currentPath!=null){
            currentPath!!.linkUrl=linkURL
            currentPath!!.linkName=linkName
        }
    }

    /*
    Method to set is lasso selected .
     */
    fun setIsLassoSelected(isLassoSelected:Boolean){
        this.isLassoSelected=isLassoSelected
        Log.d("mytag", "setIsLassoSelected: "+isLassoSelected)
    }
    /*
    Method to and set the pen Type.
     */
    fun getPenType():PenType{
        return selectedPenType
    }
    fun setPenType(type:PenType){
        selectedPenType=type
    }
    /*
enum class to represent the pen type selected
 */
    enum class PenType{
        NormalPen,
        DashedPen,
        LaserPen,
        LassoPen,
        EraserPen,
        HighLighterPen;
    }
    /*
    Method to hande the lasso intersection.
     */
    private fun handleLassoModeTap(){

                // Pass these positions to your onChildClickListener
                onChildClickListener?.onViewClicked(
                    this,
                    CustomLayout.ViewType.CANVAS,
                    false,
                    true,
                    "noURL",
                    "",
                    0F,0F
                )
                invalidate()
    }
    /*
    Method for redo undo.
     */
    // Method for undo action
    fun undoAction() {
        if (undoStack.isNotEmpty()) {
            val lastAction = undoStack.removeLast()
            when (lastAction) {
                is UndoAction.DrawAction -> {
                    if (drawnPaths.isNotEmpty()) {
                        drawnPaths.removeAt(drawnPaths.size - 1)
                    }
                }
                is UndoAction.OffsetAction -> {
                    undoRedoOffSet(lastAction, reverse = true) // Reverse offset for undo
//                    drawnPaths.clear()
                }
            }
            redoStack.add(lastAction)
            invalidate()
        } else {
        }
    }

    fun redoAction() {
        if (redoStack.isNotEmpty()) {
            val lastAction = redoStack.removeLast()
            when (lastAction) {
                is UndoAction.DrawAction -> {
                    drawnPaths.add(lastAction.path)
                }
                is UndoAction.OffsetAction -> {
//                    drawnPaths.clear()
                    undoRedoOffSet(lastAction, reverse = false) // Apply offset for redo

                }
            }
            undoStack.add(lastAction)
            invalidate()
        } else {
        }
    }

    fun undoRedoOffSet(userAction: UndoAction.OffsetAction, reverse: Boolean) {
        userAction.paths.forEach { path ->
            val xOffset = if (reverse) -path.xOffSet else path.xOffSet
            val yOffset = if (reverse) -path.yOffSet else path.yOffSet
            path.path.offset(xOffset, yOffset)
        }
    }


    // Method to add a draw action to the stack
    fun addDrawAction(path: likhoPath) {

        val pathss=Path().apply {
            addPath(path.path)
        }
//        undoStack.add(UndoAction.DrawAction(likhoPath(pathss,(path.properties),path.isLocked,path.linkUrl,path.linkName,path.xOffSet,path.yOffSet,path.prevXOffSet,path.prevYOffSet)))
        undoStack.add(UndoAction.DrawAction(path))
        redoStack.clear() // Clear redo stack after a new action
    }
    // Method to add an offset action to the stack
    fun addOffsetAction(intersectedPaths: MutableList<likhoPath>) {
        val copiedPaths = intersectedPaths.map { originalPath ->
            val copiedPath = Path().apply { addPath(originalPath.path) }
            likhoPath(
                copiedPath,
                originalPath.properties,
                originalPath.isLocked,
                originalPath.linkUrl,
                originalPath.linkName,
                originalPath.xOffSet,
                originalPath.yOffSet,
                originalPath.prevYOffSet,
                originalPath.prevYOffSet,
                originalPath.xml,
                originalPath.pathName
            )
        }
        undoStack.add(UndoAction.OffsetAction(intersectedPaths))
        redoStack.clear()
    }
    /*
    Sealed class to represent the action of user drawing or offsetting
     */
    sealed class UndoAction {
        data class DrawAction(val path: likhoPath) : UndoAction()
        data class OffsetAction(val paths: List<likhoPath>) : UndoAction() // Store multiple paths
    }

    /*
    Method r to undo and redo drawing
     */
    private var undoDrawing:MutableList<likhoPath> = mutableListOf()
    private fun  undoDrawing(){
        /*
        Removing and adding the path form drawn paths and adding it to the undo stack
         */
    }

    /*
Method to initialise the file  object.
 */
    private fun initialiseFile(currentFileName:String) {
        val appFolder = File(context.filesDir, "Notes")
        if(appFolder!=null){
        }
        val documentFolder = File(appFolder, DocumentName)
        if (!documentFolder.exists()) {
            documentFolder.mkdir()
        }
        val typeFolder = File(documentFolder, typeName)
        if (!typeFolder.exists()) {
            typeFolder.mkdir()
        }
        pathFile = File(typeFolder,currentFileName)
        if (!pathFile!!.exists()) {
            try {
                pathFile!!.createNewFile()
            } catch (e: Exception) {
                Log.d("fileCreatingError", "initialiseFile: error creating editextfile")
            }
        }

    }
    /*
    Method to save the edittext with page specific name .
     */
    fun saveLikhoEditText(currentPath:likhoPath) {
        val currentFolder:File=File(context.filesDir,"Notes" + File.separator + DocumentName + File.separator + typeName)

        /*
        Creating String Json of the helper instance.
         */
        val gson = GsonBuilder()
            .registerTypeAdapter(PaintProperties::class.java, PaintPropertiesSerializer())
            .registerTypeAdapter(PaintProperties::class.java, PaintPropertiesDeserializer())
            .registerTypeAdapter(likhoPath::class.java, LikhoPathSerializer())
            .registerTypeAdapter(likhoPath::class.java, LikhoPathDeserializer())
            .create()

        val jsonString = gson.toJson(currentPath)
        val deSerialized = gson.fromJson(jsonString, likhoPath::class.java)
        try {
            FileWriter(pathFile).use { writer ->
                writer.write(jsonString) // Write the JSON data
            }
        } catch (e: Exception) {
            Log.d("savingError", "saveLikhoEditText: Exception is $e")
        }
    }
    /*
    Method to generate a name on the basis of the crrent time and current page no
     */
    fun generateUniqueName(): String? {
        // Get the current time in milliseconds
        val currentTimeMillis = System.currentTimeMillis()
        // Combine the current time with the page number
        val uniqueName = pageNumber+currentTimeMillis+".json"
        return uniqueName.toString()
    }

    /**
     * Methods to convert path to svg and back.
     */
    private fun convertPathToSVG(path: Path): String {
        val svgPathData = StringBuilder()
        val pathMeasure = PathMeasure(path, false)
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        val numSteps = 200 // Increase the number of segments for smoother path

        val isClosed = isPathClosed(path) // Check if the path is closed

        // Start SVG path with move-to command
        svgPathData.append("M")
        pathMeasure.getPosTan(0f, pos, tan)
        svgPathData.append(String.format(Locale.US, "%.2f,%.2f", pos[0], pos[1]))

        // Approximate the path
        var lastX = pos[0]
        var lastY = pos[1]

        val length = pathMeasure.length
        var distance = length / numSteps

        while (distance <= length) {
            pathMeasure.getPosTan(distance, pos, tan)

            // Calculate the midpoint for smoother curves
            val midX = (lastX + pos[0]) / 2
            val midY = (lastY + pos[1]) / 2

            // Calculate control point to make transition smoother
            val controlX = lastX + (pos[0] - lastX) / 3
            val controlY = lastY + (pos[1] - lastY) / 3

            svgPathData.append(String.format(Locale.US, " Q%.2f,%.2f %.2f,%.2f", controlX, controlY, pos[0], pos[1]))

            lastX = pos[0]
            lastY = pos[1]

            distance += 4 // Increment the distance by 4 (adjust as needed for smoothness)
        }

        // If the path is closed, add the closing command
        if (isClosed) {
            svgPathData.append(" Z")
        }

        val svgHeader = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\">"
        val svgFooter = "</svg>"
        val width = width // Assuming you want to use the canvas size
        val height = height

        val finalSvg = String.format(Locale.US, svgHeader, width, height) +
                "<path d=\"${svgPathData}\" stroke=\"#000000\" fill=\"none\" stroke-width=\"$strokeWidth\"/>" +
                svgFooter
        return finalSvg
    }

    fun parseSVGPath(svgData: String): Path {
        // Extract only the path data from the SVG string
        val pathData = svgData.replace(".*<path d=\"".toRegex(), "").replace("\".*", "")

        val path = Path()
        var currentCommand = ' '
        var currentX = 0f
        var currentY = 0f
        var startX = 0f
        var startY = 0f

        val commands = pathData.split("(?=[MLHVCSQTAZ])".toRegex()) // Split commands and their parameters

        for (command in commands) {
            if (command.isEmpty()) continue

            currentCommand = command[0]
            val params = command.substring(1).trim().split("\\s+|,".toRegex())

            try {
                when (currentCommand) {
                    'M' -> { // Move To
                        currentX = params[0].toFloat()
                        currentY = params[1].toFloat()
                        path.moveTo(currentX, currentY)
                        startX = currentX
                        startY = currentY
                    }
                    'L' -> { // Line To
                        currentX = params[0].toFloat()
                        currentY = params[1].toFloat()
                        path.lineTo(currentX, currentY)
                    }
                    'H' -> { // Horizontal Line To
                        currentX = params[0].toFloat()
                        path.lineTo(currentX, currentY)
                    }
                    'V' -> { // Vertical Line To
                        currentY = params[0].toFloat()
                        path.lineTo(currentX, currentY)
                    }
                    'C' -> { // Cubic Bezier Curve
                        val controlX1 = params[0].toFloat()
                        val controlY1 = params[1].toFloat()
                        val controlX2 = params[2].toFloat()
                        val controlY2 = params[3].toFloat()
                        currentX = params[4].toFloat()
                        currentY = params[5].toFloat()
                        path.cubicTo(controlX1, controlY1, controlX2, controlY2, currentX, currentY)
                    }
                    'Q' -> { // Quadratic Bezier Curve
                        val controlX = params[0].toFloat()
                        val controlY = params[1].toFloat()
                        currentX = params[2].toFloat()
                        currentY = params[3].toFloat()
                        path.quadTo(controlX, controlY, currentX, currentY)
                    }
                    'A' -> { // Elliptical Arc
                        // Parsing arcs can be complex; basic implementation:
                    }
                    'Z' -> { // Close Path
                        path.close()
                    }
                    else -> {
                        Log.e("SVGParseError", "Unknown path command: $currentCommand")
                    }
                }
            } catch (e: NumberFormatException) {
                Log.e("SVGParseError", "Invalid number format in command: $command")
            } catch (e: ArrayIndexOutOfBoundsException) {
                Log.e("SVGParseError", "Missing parameters in command: $command")
            }
        }

        Log.d("mytag", "parseSVGPath: done")
        return path
    }
    private fun isPathClosed(path: Path): Boolean {
        val pathMeasure = PathMeasure(path, false)
        val posStart = FloatArray(2)
        val posEnd = FloatArray(2)
        pathMeasure.getPosTan(0f, posStart, null)
        pathMeasure.getPosTan(pathMeasure.length, posEnd, null)

        return abs((posStart[0] - posEnd[0]).toDouble()) < 1e-5 && abs((posStart[1] - posEnd[1]).toDouble()) < 1e-5
    }

    /*
    Method to add retrieved path.
     */
    fun addStoredPath(path:likhoPath){
        Log.d("mytag", "addStoredPath: drawnPats ssize = ${drawnPaths.size}")
        drawnPaths.add(path)
        Log.d("mytag", "addStoredPath: drawnPats ssize = ${drawnPaths.size}")
        invalidate()
    }

}


