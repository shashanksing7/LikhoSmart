package nishkaaminnovations.com.likhosmart.WorkShop.MusicPlayer

import AudioPlayer
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.cl
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses.las
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.onChildViewClickListener
import nishkaaminnovations.com.likhosmart.databinding.WaveseekerandplaypauseBinding
import java.io.File
import java.io.FileWriter


class la @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    uri: Uri
) : ConstraintLayout(context, attrs, defStyleAttr),setWaveComplete,AudioPlayer.playerPreparedListener
     {

    private val binding: WaveseekerandplaypauseBinding =
        WaveseekerandplaypauseBinding.inflate(LayoutInflater.from(context), this, true)
    /*
    This variable will be used to represent the uri of the audio.
     */
    private lateinit var audiouri: Uri

    /*
    Purpose: An interface for handling click events on the view.
     Usage: This is used to notify when the view is clicked (e.g., when resizing or rotating is completed). The listener provides a method (onViewClicked) that is invoked when the view is clicked.
    */
    private lateinit var onChildClickListener: onChildViewClickListener

    private var isPlaying = false
    private var playPauseListener: ((Boolean) -> Unit)? = null
    // Job for updating progress
    private var updateJob: Job? = null
    // Variables for drag and rotation
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var rotationAngle = 0f
    private var centerX = 0f
    private var centerY = 0f
    private val rectMargin:Float=30f
    private var rotatePaint: Paint? = null
    private val rotateRadius = 26
    private var bitmap: Bitmap? = null
    private var isLocked:Boolean=false
    /*
    Variables to represent the link text and url
     */
    private var linkName = "noName"
    private var linkUrl = "noURL"

         /*
 Variables that will be used to save the edittext .
  */
         private val typeName = "likhoAudio"
  /*
  This is used to represent the helper class.
   */
         private lateinit var las:las
    /*
    Variables for the redo and undo.
     */
    private val undoStack: MutableList<TransformationState> = mutableListOf()
    private val redoStack: MutableList<TransformationState> = mutableListOf()
    // Keep track of the current state
    private var currentState: TransformationState? = null
    private var fileName:String= ""
    init {

        // Initialize rotatePaint for drawing the rotation handle (a circle).
        rotatePaint = Paint().apply {
            color = Color.BLACK // Set the color of the rotation handle to black.
            style = Paint.Style.FILL_AND_STROKE // Set the style to fill the circle and also outline it.
            strokeWidth = 5f // Set the stroke width to 5px.
        }
        las=las()
        setupListeners()
       CoroutineScope(Dispatchers.IO).launch {
           binding.waveformSeekBar.setSampleFrom(uri.toString())
       }
        binding.waveformSeekBar.onProgressChanged = object : SeekBarOnProgressChanged {
            override fun onProgressChanged(
                waveformSeekBar: WaveformSeekBar,
                progress: Float,
                fromUser: Boolean
            ) {
                val duration = AudioPlayer.getDuration() // Total duration in milliseconds
                // Calculate the position based on progress (as percentage of duration)
                val position = ((progress / 100) * duration).toInt()

                Log.d("mytag", "onProgressChanged: progress = $progress, position = $position ms")

                if (fromUser) { // Only seek if the user initiated the change
                    AudioPlayer.seekTo(position)
                }
            }
        }
        AudioPlayer.setOnComplete(this)

        AudioPlayer.stopAudio()
        updateButtonState()
        startProgressUpdates()
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rotate);
        setWillNotDraw(false)
        post { invalidate() }
    }
    private fun setupListeners() {
        AudioPlayer.setPlayerPreparedListener(this)
        binding.playPauseButton.setOnClickListener {

            if (onChildClickListener != null&&!(onChildClickListener.isDrawingOn())) {
                binding.root.isEnabled=true
//                onChildClickListener?.onViewClicked(this,CustomLayout.ViewType.AUDIO,isLocked,true,linkUrl,linkName,0f,0f)
            }
            else{
                return@setOnClickListener
            }
            if (isPlaying) {
                // Pause the audio and stop progress updates

                AudioPlayer.pauseAudio()
                stopProgressUpdates()
            } else {
                // Resume the audio and restart progress updates
                CoroutineScope(Dispatchers.IO).launch {
                    AudioPlayer.playAudio(context,audiouri,null)
                }
                AudioPlayer.resumeAudio()
                startProgressUpdates()
            }
            isPlaying = !isPlaying
            updateButtonState()
        }
    }
    private fun updateButtonState() {
        binding.playPauseButton.setImageResource(
            if (!isPlaying) nishkaaminnovations.com.likhosmart.R.drawable.play else nishkaaminnovations.com.likhosmart.R.drawable.pause
        )
        Log.d("mytag", ": playing = "+AudioPlayer.isPlaying())
    }
    fun setOnPlayPauseListener(listener: (Boolean) -> Unit) {
        playPauseListener = listener
    }
    fun setWaveformSamples(samples: IntArray) {
        binding.waveformSeekBar.setSampleFrom(samples)
    }
    // Function to start updating the waveform progress
    private fun startProgressUpdates() {
        if (updateJob?.isActive == true) return // Avoid duplicate jobs

        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isPlaying) {
                val progress = withContext(Dispatchers.IO) {
                    val currentPosition = AudioPlayer.getCurrentPosition()
                    val duration = AudioPlayer.getDuration()
                    if (duration > 0) (currentPosition.toFloat() / duration) * 100 else 0F
                }

                binding.waveformSeekBar.progress = progress
                delay(500) // Update every 500ms
            }
        }
    }

    // Function to stop updating the waveform progress
    private fun stopProgressUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopProgressUpdates() // Clean up when view is detached
    }

    /*
    The ondraw method
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if(isLocked||onChildClickListener.isDrawingOn()){
            return
        }
        // Check if the view is enabled before proceeding
        if (!isEnabled) {
            return
        }
//        drawCircle(canvas)
    }

     private var firstLoad:Boolean=true
    // Drag and rotation handling
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // If there's a click listener and the view is disabled, trigger the onViewClicked method

        if (onChildClickListener != null) {
            if (onChildClickListener.isDrawingOn()||firstLoad==true) {
                firstLoad=false
                return true
            }
            onChildClickListener?.onViewClicked(this,cl.ViewType.AUDIO,isLocked,true,linkUrl,linkName,0f,0f)
            Log.d("imageuri", "onTouchEvent: image calling")
        }
        if(isLocked){
            return true
        }

        if(!isEnabled){
            return false
        }
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Record initial touch point and calculate drag offset
                lastTouchX = event.rawX
                lastTouchY = event.rawY
                dragOffsetX = event.rawX - x
                dragOffsetY = event.rawY - y
                calculateCenter()
            }
            MotionEvent.ACTION_MOVE -> {
                // Determine if drag or rotation should occur
                if (isNearRotationHandle(event)) {
                    captureState()
                    handleRotation(event)
                } else {
                    captureState()
                    handleDrag(event)

                }
            }
        }
        return true
    }

    private fun handleDrag(event: MotionEvent) {
        val newX = event.rawX - dragOffsetX
        val newY = event.rawY - dragOffsetY

        // Ensure view stays within parent boundaries
        val parent = parent as ViewGroup
        x = newX.coerceIn(0f, (parent.width - width).toFloat())
        y = newY.coerceIn(0f, (parent.height - height).toFloat())
    }

    private fun handleRotation(event: MotionEvent) {
        val newTouchX = event.rawX
        val newTouchY = event.rawY

        val angle = Math.toDegrees(
            Math.atan2((newTouchY - centerY).toDouble(), (newTouchX - centerX).toDouble()) -
                    Math.atan2((lastTouchY - centerY).toDouble(), (lastTouchX - centerX).toDouble())
        ).toFloat()

        rotationAngle += angle
        rotation = rotationAngle

        lastTouchX = newTouchX
        lastTouchY = newTouchY
    }

    private fun isNearRotationHandle(event: MotionEvent): Boolean {
        val handleMargin = 100 // Customize based on the handle size
        return event.x > width - handleMargin && event.y < handleMargin
    }

    private fun calculateCenter() {
        centerX = x + width / 2f
        centerY = y + height / 2f
    }
    /*
    Method to draw the circles on the canvas
     */
    private fun drawCircle(canvas: Canvas) {
        // Draw circles at the four corners for resizing handles (top-left, top-right, bottom-left, bottom-right).
        // These circles are drawn with the resizerPaint object for style and color.

            canvas.drawBitmap(bitmap!!,(width - rectMargin).toFloat(), rectMargin.toFloat(),rotatePaint )

    }
    fun setOnChildClickListener(listener: onChildViewClickListener) {
        this.onChildClickListener = listener
        fileName =
            if ((fileName != null && !fileName.isEmpty())) fileName else generateUniqueName()
    }

    override fun isWaveComplete(progress: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            if(progress){
                binding.playPauseButton.setImageResource(
                    nishkaaminnovations.com.likhosmart.R.drawable.play
                )
            }
        }
    }
    /*
    Setter and getter methods for the audio uri.
     */
    fun getAudioUri():Uri{
        return audiouri
    }
    fun setAudioUri(uri:Uri){
        audiouri=uri
        Log.d("audioURI", "setAudioUri: audio uri set in wave ${uri}")
    }
    fun getLocked(): Boolean {
        return isLocked
    }

    fun setLocked(locked: Boolean) {
        isLocked = locked
        invalidate()
    }

    fun getLinkName(): String {
        return linkName
    }

    fun setLinkName(linkName: String) {
        this.linkName = linkName
    }

    fun getLinkUrl(): String {
        return linkUrl
    }

    fun setLinkUrl(linkUrl: String) {
        this.linkUrl = linkUrl
    }

    /*
    Methods for redo and undo of the x,y rotation
     */
    /*
Method for the redo and undo.
 */
    fun undo() {
        Log.d("redotag", "undo: ")
        if (undoStack.isNotEmpty()) {
            // Push the current state to redoStack before undoing
            currentState?.let {
                redoStack.add(it)
                Log.d("redotag", "undo:adding to redo")
            }

            // Pop the last state from undoStack
            val lastState = undoStack.removeAt(undoStack.size - 1)
            currentState = lastState

            // Apply the state
            applyTransformation(lastState)
        }
    }

    fun redo() {
        Log.d("redotag", "redo: ")
        if (redoStack.isNotEmpty()) {
            // Push the current state to undoStack before redoing

            currentState?.let {
                undoStack.add(it)
                Log.d("redotag", "redo:adding to undo")
            }

            // Pop the next state from redoStack
            val nextState = redoStack.removeAt(redoStack.size - 1)
            currentState = nextState

            // Apply the state
            applyTransformation(nextState)
        }
    }

    private fun applyTransformation(state: TransformationState) {
        // Apply width, height, rotation, and position to the image view

        val params: ViewGroup.LayoutParams = layoutParams
        params.width = state.width.toInt()
        params.height = state.height.toInt()
        layoutParams=params
        rotationAngle = state.rotationAngle
        rotation = state.rotationAngle
        x = state.xPosition
        y = state.yPosition

        invalidate() // Redraw the view

        // Update the current state
        currentState = state
    }


    /*
    Method to capture the current state of the view.
     */
    private fun captureState() {
        val state = TransformationState(
            width = width.toFloat(),
            height = height.toFloat(),
            rotationAngle = rotationAngle,
            xPosition = x,
            yPosition = y
        )
        undoStack.add(state)
        redoStack.clear() // Clear the redo stack whenever a new transformation is made
    }


    /*
    Data class to represent the state of the view.
     */
    data class TransformationState(
        val width: Float,
        val height: Float,
        val rotationAngle: Float,
        val xPosition: Float,
        val yPosition: Float
    )

    override fun playerPrepared(isPrepared: Boolean) {
             isPlaying=true
             updateButtonState()
         }

         /*
         Method to save the edittext with page specific name .
          */
         fun saveLikhoEditText() {
             val audioFile = File(
                  onChildClickListener.getDocumenLocation() + File.separator + typeName + File.separator + fileName
             )
             Log.d("audioURI", "saveLikhoEditText: path = ${audioFile.path} ")

             /*
             Creating String Json of the helper instance.
              */
             val gson = Gson()
             val jsonString = gson.toJson(las)
             try {
                 FileWriter(audioFile).use { writer ->
                     writer.write(jsonString) // Write the JSON data
                 }
             } catch (e: Exception) {
                 Log.d("savingError", "saveLikhoEditText: Exception is $e")
             }
         }

         /*
         Method to update the helper instance.
          */
         private fun updateHelperInstance() {
             las!!.setX(x)
             las!!.setY(y)
             las!!. setRotationalAngle(rotationAngle)
             las!!.setFileName(fileName)
             las!!.setUri(audiouri.toString())
             las!!.setLinkUrl(linkUrl)
             las!!.setLinkName(linkName)
             Log.d("myURI", "updateHelperInstance: audio uri${audiouri}")
         }
         /**
          * Method to generate a name based on the current time and current page number.
          */
         fun generateUniqueName(): String {
             // Get the current time in milliseconds
             val currentTimeMillis = System.currentTimeMillis()
             // Combine the current time with the page number
             val uniqueName = onChildClickListener.getPageNumber()+currentTimeMillis + ".json"
             return uniqueName
         }

         override fun setEnabled(enabled: Boolean) {
             super.setEnabled(enabled)
             if (!enabled) {
                 updateHelperInstance()
                 saveLikhoEditText()
                 binding.waveformSeekBar.isEnabled=false
             } else {
                 binding.playPauseButton.isEnabled=true
                 binding.waveformSeekBar.isEnabled=true
             }
         }

         fun setFileName(name: String) {
             fileName = name
         }

         /*
         Method to delete the audio file
          */
       fun delete(){
             try{
                 val audioFile = File(
                     onChildClickListener.getDocumenLocation() + File.separator + typeName + File.separator +fileName
                 )
                 audioFile.delete()
             }
             catch(e:Exception){
                 Log.d("mytag", "deleteAudio: ${e.toString()}")
             }
         }
         fun getFileName():String {
             return fileName
         }

     }
