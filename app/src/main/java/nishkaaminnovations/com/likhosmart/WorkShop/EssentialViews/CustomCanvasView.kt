package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.PathMeasure
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/*
This class will represent the canvas for our workshop.
 */
class CustomCanvasView@JvmOverloads constructor(context: Context, attr: AttributeSet?=null, defStyleAttr:Int=0) : View(context,attr,defStyleAttr) {
    /*
    This will represent the Laser object.
     */
    private lateinit var laserObject:Paint
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
    Variable to hold the path that hs been selected by the lasso tool
     */
    private var intersectedPaths: List<likhoPath> = mutableListOf()
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
    private var isLaserOn:Boolean=true
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
    The init block to initialize the paint object and other variables.
     */
    init {
        /*
        initializing tree.
         */
        quadtree= QuadTree(RectF(0f, 0f, width.toFloat(), height.toFloat()))
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
        for(path in drawnPaths){
            canvas.drawPath(path.path,createPaintObject(path.properties))
        }
        /*
        Draw the rectangle around the selected or intersected paths.
         */
        if(hasIntersected){
            drawBoundingRectangle(canvas)
        }
        /*
        Draw the laser if the selected.
         */
        if(drawLaser){
            canvas.drawCircle(laserX,laserY, 25f, outerGlowPaint!!)
            canvas.drawCircle(laserX,laserY, 5f, innerCirclePaint!!)
        }
    }

    /*
    Methods to draw the path on the canvas.
     */
    private fun touchStart(x: Float, y: Float) {

        /*
        Setting the current path and it's properties
         */
        mPath = Path()
        val lp = likhoPath(mPath,
            PaintProperties(color,strokeWidth, strokeStyle, alpha, pathEffect, shader, cap, join, xfermode)
        )
        /*
        Checking if eraser is on and adding the paths to respective lists.
         */
        if(!isEraserOn&&!isLassoSelected){
            drawnPaths.add(lp)
        }
        else{
            if(isEraserOn){
                eraserPaths.add(lp)
            }
            if(isLassoSelected){
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

        /*
        Sending the paths for intersection check if lasso is enabled and user is done drawing it
         */
        if (isLassoSelected) {
            intersectedPaths=checkForIntersections() // Insert after drawing the complete path
            if(intersectedPaths.size>0){
                hasIntersected=true
            }

        }
    }
    /*
    method to set the paint object according to the
     */
    // Method to initialize the Paint object with the above variables
    fun createPaintObject(paintProperties: PaintProperties): Paint {
        val paint = Paint().apply {
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
        return paint.apply {setAntiAlias(true)
        setDither(true)}
    }

    /**
     * onTouch method.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event!!.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                laserX = x
                laserY = y
                /*
                Checking if user has turned on the laser or not and handling the user touch
                accordingly.
                 */
                if(!isLaserOn){
                    /*
                Checking if the user has selected any thing from lasso or not.
                 */
                    if(hasIntersected){
                        /*checking is user click is inside the selected rectangle bound.
                         */
                        if(isPointInsideRect(overallBounds,x,y)){
                            isDragging=true
                            prevX=x
                            prevY=y
                        }
                        else{
                            /*
                            Resetting the lasso related variable.
                             */
                            isDragging=false
                            hasIntersected=false
                            lassoPath=null

                        }
                    }
                    else{
                        /*
                        Calling the path drawing method.
                         */
                        touchStart(x,y)
                    }
                }
                else{
                    drawLaser=true
                }
            }
            MotionEvent.ACTION_MOVE -> {

                if(!drawLaser){
                    if(isDragging){
                        offsetIntersectingPaths(x,y)
                    }
                    else{
                        touchMove(x,y)
                    }
                }
                laserX = x
                laserY = y
            }

            MotionEvent.ACTION_UP ->{

                if(drawLaser){
                    !drawLaser
                }
                else{
                    touchUp();
                }
            }
        }
        invalidate() // Redraw the canvas
        return true
    }

    /**
     * Methods to check if the paths intersects or not.
    */
    fun checkForIntersections(): MutableList<likhoPath> {
        Log.d("mytag", "checkForIntersections: start")

        val intersectingPaths: MutableList<likhoPath> = ArrayList()

        // Ensure there are at least two paths to compare
        if (drawnPaths.size < 2) {
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
     * Method to check if the user touch lies inside the selected bounding rectangle or not
     */

    private fun drawBoundingRectangle(canvas: Canvas) {
        val paths: List<likhoPath> =intersectedPaths
        if (paths == null || paths.isEmpty()) return

        // Initialize variables to hold the overall bounding box
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        // Iterate through all the paths to find the bounding box that contains all of them
        for (i in paths.indices) {
            val fp = paths[i]
            val bounds = RectF()
            fp.path.computeBounds(bounds, true)

            // Update the overall bounding box
            minX = min(minX.toDouble(), bounds.left.toDouble()).toFloat()
            minY = min(minY.toDouble(), bounds.top.toDouble()).toFloat()
            maxX = max(maxX.toDouble(), bounds.right.toDouble()).toFloat()
            maxY = max(maxY.toDouble(), bounds.bottom.toDouble()).toFloat()
        }
        // Draw the bounding rectangle
        overallBounds = RectF(minX, minY, maxX, maxY)

        // Set paint properties for the rectangle
        var mPaint=Paint().apply {
            setColor(Color.BLUE)
            setStyle(Paint.Style.STROKE)
            setStrokeWidth(5f)
        }
            canvas.drawRect(overallBounds!!,mPaint )
        invalidate()
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
            path.path.offset(dx, dy)
        }

        // Offset the overall bounding rectangle
        overallBounds!!.offset(dx, dy)

        // Update the previous touch position
        prevX = x
        prevY = y
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

    // Setter methods
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

}