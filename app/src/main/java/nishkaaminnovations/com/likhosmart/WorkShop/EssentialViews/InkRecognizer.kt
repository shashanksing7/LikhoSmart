package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.RecognitionContext
import com.google.mlkit.vision.digitalink.RecognitionResult
import com.google.mlkit.vision.digitalink.WritingArea
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.WorkShop.WorkShop
import java.util.concurrent.ExecutionException
import kotlin.math.abs


/*
This class will be used to represent the ink to text builder.
 */
class InkRecognizer(context: Context, attr: AttributeSet?=null, defStyleAttr:Int=0): View(context,attr,defStyleAttr) {
    /*
    These variables will be used to represent the paints,and other boolean objects
     */
    private var mPaint: Paint? = null
    private var mX = 0f
    private var mY = 0f
    private lateinit var mPath: Path
    private var Paths: MutableList<Path>? = null
    private val TOUCH_TOLERANCE = 0f

    /*
    Ink Builder variables
     */
    private var inkBuilder: Ink.Builder?
    lateinit var strokeBuilder: Ink.Stroke.Builder

   private  lateinit var ink: Ink
   private  lateinit var modelIdentifier: DigitalInkRecognitionModelIdentifier
   private  lateinit var recognizer: DigitalInkRecognizer
   private  lateinit var rmodel: DigitalInkRecognitionModel
   private  lateinit var remoteModelManager: RemoteModelManager
   private var isDownloaded: Boolean = false
    private lateinit var recognitionContext:RecognitionContext
    private  var preContext:String=""
    private lateinit var resultListener:WorkShop.RecognitionResultListener
    /*
    Variables that will be used to offset the strokes in the stroke builder.
     */
    private var offsetX:Int=50
    /*
    Variable to set if the recognition is manual or Automatic.
     */
    private var isAutoMatic:Boolean=false


    /*
    initialising the objects and lists .
     */
    init {
        mPaint = Paint()
        mPaint!!.strokeWidth = 20f
        mPaint!!.color = Color.BLACK
        mPaint!!.isAntiAlias = true
        mPaint!!.isDither = true
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeJoin = Paint.Join.ROUND
        mPaint!!.strokeCap = Paint.Cap.ROUND
        Paths = ArrayList()
        /*
        Ink builder object.
         */
        inkBuilder = Ink.builder()
        /*
        Setting the DigitalInkRecognitionModelIdentifier.
         */
        try {
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US")!!
        } catch (e: MlKitException) {
            // language tag failed to parse, handle error.
        }
        if (modelIdentifier == null) {
            // no model was found, handle error.
        }
        /*
        Setting the model according to the modelIdentifier.
         */
        rmodel = DigitalInkRecognitionModel.builder(modelIdentifier).build()
        /*
        Setting the model manager.
         */
        remoteModelManager=RemoteModelManager.getInstance()
        // Use coroutine to download the model and initialize the recognizer
        CoroutineScope(Dispatchers.IO).launch {
            downLoadModel()
        }
        /*
        checking if the model has been downloaded and then initialising the recognizer
         */
        try {
        remoteModelManager.isModelDownloaded(rmodel)
            .addOnSuccessListener {
                result->
                if (result) {
                    Log.d("ModelCheck", "Model is downloaded.")
                    /*
                    Initialising the recognizer.
                     */
                    recognizer= DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(rmodel).build())
                } else {
                    Log.d("ModelCheck", "Model is not downloaded.")
                }

            }
        } catch (e: ExecutionException) {
            Log.e("ModelCheck", "Error checking model download status", e)
        } catch (e: InterruptedException) {
            Log.e("ModelCheck", "Error checking model download status", e)
        }
        /*
        Initialising the recognition context.
         */
        recognitionContext=RecognitionContext.builder()
            .setWritingArea(WritingArea(width.toFloat(),height.toFloat()))
            .setPreContext(preContext)
            .build()

    }

    /*
    OnDraw method.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        for(path in Paths!!){
            canvas.drawPath(path,mPaint!!)
        }
    }

    /*
    Method  to draw the paths and store the paths.
     */
    private fun touchStart(x: Float, y: Float) {
        mPath = Path()
        mPath.reset()
        mPath.moveTo(x, y)
        Paths?.add(mPath)
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = abs((x - mX).toDouble()).toFloat()
        val dy = abs((y - mY).toDouble()).toFloat()

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp() {
        mPath.lineTo(mX, mY)
        if(isAutoMatic){
            recognize(inkBuilder!!.build())
        }
        offsetPaths(false)
    }

    /*
    On touch method to make the path and ink form it.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        val action = event?.actionMasked
        val x = event!!.x
        val y = event!!.y
        var t = System.currentTimeMillis()

        /*
        Building the ink and paths.
         */
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                strokeBuilder = Ink.Stroke.builder()
                strokeBuilder.addPoint(Ink.Point.create(x-offsetX, y, t))
                touchStart(x,y)
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                strokeBuilder!!.addPoint(Ink.Point.create(x-offsetX, y, t))
                touchMove(x,y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                strokeBuilder.addPoint(Ink.Point.create(x-offsetX, y, t))
                inkBuilder!!.addStroke(strokeBuilder.build())
                touchUp()
                invalidate()
            }
            else -> {

            }
        }
        return  true;
    }
    /*
    Method to download a language model.
     */
    fun downLoadModel(){
        /*
        starting the download.
         */
        remoteModelManager.download(rmodel, DownloadConditions.Builder().build())
            .addOnSuccessListener {
                Log.d("mytag", "downLoadModel: Downloaded")
            }
            .addOnFailureListener{e: Exception ->

            Log.d("mytag", "downLoadModel: Not Downloaded, error is "+e.toString())
            }
    }

    private fun recognize(ink:Ink){
        /*
        Start recognition.
         */
        recognizer.recognize(ink,recognitionContext)
            .addOnSuccessListener { result: RecognitionResult ->
                // `result` contains the recognizer's answers as a RecognitionResult.
                Log.i("mytag", result.candidates[0].text)
                // Get the recognition result
                val resultText = result.candidates[0].text
                // Count the number of characters in preContext
                val preContextLength = preContext.length

                // Remove the first `preContextLength` characters from the result
                val updatedResult = if (resultText.length >= preContextLength) {
                    resultText.substring(preContextLength)
                } else {
                    resultText // Handle cases where resultText is shorter than preContext
                }
                resultListener.getRecognitionResult(updatedResult)
                preContext=result.candidates[0].text
            }
            .addOnFailureListener { e: Exception ->
                Log.e("mytag", "Error during recognition: $e")
            }
    }
    /*
    Method to offset the paths.
     */
    fun offsetPaths(userOffset:Boolean) {
        var path: Path
        for (i in (Paths!!.size - 1) downTo 0) {
            path= Paths!!.get(i)
            if(!userOffset){
                path.offset(-offsetX.toFloat(), 0f)
            }
            else{
                path.offset(-550f, 0f)
            }
            invalidate()
        }
    }
    fun setRecognitionResultListener(resultListener: WorkShop.RecognitionResultListener){
        this.resultListener=resultListener
    }
    /*
    Method to clear the inkBuilder.
     */
    fun clearInk(){
        inkBuilder=null
        inkBuilder=Ink.builder()
        preContext=""
        Paths!!.clear()
        invalidate()
    }
    /*
    Method to start recognition
     */
    fun startRecognition(){
        recognize(inkBuilder!!.build())
    }
    /*
    Method to set the isAutomatic variable.
     */
    fun setIsAutomatic(isisAutomatic:Boolean){
        this.isAutoMatic=isisAutomatic
    }
}