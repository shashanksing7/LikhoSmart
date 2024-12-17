package nishkaaminnovations.com.likhosmart.WorkShop

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView
import com.github.squti.androidwaverecorder.WaveRecorder
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nishkaaminnovations.com.likhosmart.DataBase.docModel
import nishkaaminnovations.com.likhosmart.R
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.CustomCanvasView
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.CustomLayout
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.InkRecognizer
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.PaintProperties
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.PaintPropertiesSerializer
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses.LikhoPathDeserializer
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses.LikhoPathSerializer
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses.PaintPropertiesDeserializer
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses.likhoAudioSave
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses.likhoEditSave
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses.likhoImageSave
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.childSelectedListener
import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.likhoPath
import nishkaaminnovations.com.likhosmart.WorkShop.MusicPlayer.WaveformWithPlayPause
import nishkaaminnovations.com.likhosmart.WorkShop.Viewpager.PageAdapter
import nishkaaminnovations.com.likhosmart.WorkShop.Viewpager.PageFragment
import nishkaaminnovations.com.likhosmart.databinding.FragmentWorkShopBinding
import top.defaults.colorpicker.ColorPickerView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


/*
This class will work as the workshop for the document editing.
 */
class WorkShop : Fragment(){
    /*
    Variable to represent the Adapter of the view pager.
     */
    private lateinit var pageAdapter:PageAdapter;
    /*
    Variable to represent the current layout.
     */
    private lateinit var currentPageLayout:CustomLayout
    /*
    The binding variable for thi class.
     */
    private lateinit var binding: FragmentWorkShopBinding
    /*
    Array to hold the Main buttons.
     */
    private lateinit var mainButtonArray: Array<AppCompatImageButton>
    private var firstTime=true
    /*
Array to hold the Dialog buttons.
 */
    private lateinit var dialogButtonArray: Array<AppCompatImageButton>
    /*
    Audio pick request code.
     */
    private val AUDIO_REQUEST_CODE:Int=200
    /*
    Variables for AudioRecording.
     */
    private var isRecording = false
    private var isPaused = false
    private var recordingTimeMillis: Long = 0
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    /**
     * Launcher
     */
    private var audiPickerLauncher: ActivityResultLauncher<Intent>?=null
    private  var documentScannerLauncher:ActivityResultLauncher<IntentSenderRequest>?=null
    /*
    enum class for knowing the recording status
     */
    enum class RecordingState {
        NOT_STARTED, RECORDING, PAUSED
    }
    /*
    These variables will be used for the Document Scnner.
     */
    private var scannerOptions: GmsDocumentScannerOptions.Builder?=null
    private var documentScanner:GmsDocumentScanner?=null
    /*
    Variable to represent the bottom sheet dialog
     */
    private  lateinit var bottomSheetDialog:BottomSheetDialog
    /*
    Variables that will represent the pen color and the stroke width
     */
    private var penColor: Int =Color.BLACK
    private var penWidth:Float=5f
    /*
    Variable for the colroPicker dialog
     */
    private  lateinit var ColorPickerDialog:Dialog
    /*
    Variable to represent the selected color button.
     */
    private lateinit var  selectedColorButton:AppCompatImageButton
    /*
    This variable is used to represent if the recognition mode is on or not
     */
    private var isRecognitionOn:Boolean=false
    /*
    This variable is used to Represent the currently selected child
     */
    private  var currentSelectedChild:View?=null
    /*
    This variable will be used to represent if there is child currently selected or not
     */
    private var isAnyChildSelected:Boolean=false
    /*
    This variable will be used to represent the type of the currently selected view.
     */
    private var selectedChildType:CustomLayout.ViewType=CustomLayout.ViewType.NONE
    /*
    Variable to represent if the child is locked or not.
     */
    private var isChildLocked:Boolean=false
    /*
    This Variable will eb used to represent if the delete button should be on  or not.
     */
    private var isDeleteAble:Boolean=true
    /*
    Variables for Link pop up.
     */
    private var popupWindow: PopupWindow? = null
    private var etLinkName: EditText? = null
    private var etLinkUrl: EditText? = null
    private var btnSaveLink: Button? = null
    private lateinit var popupView:View
    /*
     Declare the dialog at the class level
     */
    private lateinit var loadingDialog: Dialog
    /*
    Variables to decide if the dialogbox should disappear.
     */
   var loadedCount:Int=0
    /*
    Variable to represent the currently loaded document
     */
    // Access the arguments using Safe Args
    private val args by navArgs<WorkShopArgs>()
    private lateinit var docModel:docModel
    /*
    Variable to represen the current fragment or page
     */
    private lateinit var   currentFragment:PageFragment
    /**
     * Variables that will represent the Current page on the viewPager2
     */
    private var  pageNumber:String="Page";
    private var  documentName:String="Trial"
    private var typeLikhoEditText:String="likhoEdit"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentWorkShopBinding.inflate(inflater,container,false)
        /*
        Initializing the Document scanner builder.
         */
        buildOptions()
        /*
        setting the document scanner.
         */
        documentScanner=GmsDocumentScanning.getClient(scannerOptions!!.build())
        /*
        registering for the activity result launcher for the Document scanner.
         */
        documentScannerLauncher=registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult(),this::handleResult)
        /*
        registering activity for the audio picker
         */
         audiPickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
                result->
            if(result.resultCode==Activity.RESULT_OK){
                val data: Intent? = result.data
                val uri:Uri?=data?.data
                Log.d("myaudio", "showAudioPickerDialog: path = "+uri)
                addMusic(uri!!)
            }
        }
        /*
        Initialising the adapter.
         */
        pageAdapter=PageAdapter(requireActivity() as AppCompatActivity)
        /*
        Getting the currenlty oaded document model
         */
        docModel=args.document
        /**
         * Method to add the frgaments to the viewPager2 adapter.
         */
        createDocumentList(docModel)
        documentName=docModel.name

        /*
        Setting the adapter.
         */
        binding.LikhoPager.adapter=pageAdapter
        /*
        Hiding the belwo button.
         */
        binding.bottomButtonsLayout.visibility=View.GONE
        /*
        Hiding the textEditing Button.
         */
        binding.TextEditingLayout.visibility=View.GONE
        /*
        Adding the main buttons to an array.
         */
        mainButtonArray= arrayOf(binding.textButton,binding.drawButton,binding.audioButton,binding.imageButton,binding.shapeButton,binding.lockButton)
        /*
        initialising the buttons.
         */
        binding.LikhoPager.isEnabled=false
        addLinkToViewPopUp()
        initializeMainButton()
        initialiseTextEditingButtons()
        initialiseChildEditingButton()
        initialiseDrawingButton()
        (binding.root)
        showColorPickerDialog(binding.root)
        showLoadingDialog(binding.root)

        /**
         * Ink recogniser
         */
        /*
        Initialising the inke Recogniser layout
         */
        initialiseInkRecogniserLayout()
        /***
        Getting the current fragments layout file.
         */
        binding.LikhoPager.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentFragment = pageAdapter.createFragment(position)
               currentPageLayout=currentFragment.layout
                if (currentFragment != null) {
                    currentPageLayout = currentFragment.layout
                    pageNumber="Page"+(currentFragment.pageNumber).toString()
                    currentPageLayout.setChildSelectedListener(object :childSelectedListener{
                        override fun getSelectedChild(
                            currentChildType:CustomLayout.ViewType,
                            isChildSelected: Boolean
                            ,isLocked:Boolean
                        ) {
                            selectedChildType=currentChildType
                            isAnyChildSelected=isChildSelected
                            Log.d("mytag", "getSelectedChild:isAnyChildSelected= "+isAnyChildSelected)
                            isChildLocked=isLocked
                            if(isAnyChildSelected){
                                Log.d("mytag", "getSelectedChild: "+currentChildType)
                                binding.bottomButtonsLayout.visibility=View.VISIBLE
                                binding.mainButtonsLayout.visibility=View.GONE
                                if(currentChildType == CustomLayout.ViewType.TEXT_VIEW){
                                    binding.TextEditingLayout.visibility=View.VISIBLE
                                }
                                if(currentChildType==CustomLayout.ViewType.CANVAS){
                                    binding.lockText.visibility=View.GONE
                                    binding.childLockButton.visibility=View.GONE
                                    modifyDrawingLayoutParam(153)

                                }
                                else if(currentChildType==CustomLayout.ViewType.IMAGE_VIEW){
                                    binding.drawingButtonsLayout.visibility=View.GONE
                                    binding.bottomButtonsLayout.visibility=View.VISIBLE
                                    binding.mainButtonsLayout.visibility=View.GONE
                                }
                                if(isLocked){
                                    binding.lockText.text="Unlock"
                                }
                                else{
                                    binding.lockText.text="Lock"
                                }
                                /*
                                Enabling buttons.
                                */
                                binding.copyButton.isEnabled=true
                                binding.lockButton.isEnabled=true
                                binding.deleteButton.isEnabled=true
                                isDeleteAble=true

                            }
                            else{
                                Log.d("mytag", "getSelectedChild:no chils selected ")
                                binding.bottomButtonsLayout.visibility=View.GONE
                                binding.mainButtonsLayout.visibility=View.VISIBLE
                                binding.TextEditingLayout.visibility=View.GONE
                                binding.lockText.visibility=View.VISIBLE
                                binding.childLockButton.visibility=View.VISIBLE
                                modifyDrawingLayoutParam(0)
                                isDeleteAble=false
                            }
                        }

                    })
                }
                if(!currentFragment.pageLoaded){
                    Log.d("beforeCoroutine", "onPageSelected: calling before coroutines")
                    loadingDialog.show()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Run all tasks concurrently and wait for their completion
                            coroutineScope {
                                launch { readLikhoEditText() }
                                launch { readLikhoImage() }
                                launch { readLikhoAudio() }
                                launch { readLikhoCanvas() }
                            }
                        } finally {
                            withContext(Dispatchers.Main) {
                                loadingDialog.dismiss()
                            }
                        }
                    }
                    currentFragment.pageLoaded=true
                    loadedCount=0

                }
            }

        })

        /*
        initialising the view pager.
         */

        return binding.root
    }

    /*
    Method to add the pages according  to the documents
     */
    private fun createDocumentList(docModel: docModel) {
        for (i in 0 until docModel.noOfPages) {
            // Example logic: Print page number
            pageAdapter.addFragment(PageFragment(i,false))
        }
    }

    /*
    Method to add waveMusic view.
     */
    private fun addMusic(uri:Uri){
       val audioUri=Uri.parse(copyAudioToLocalStorage(uri))

        Log.d("audioUri", "addMusic: musicUri = ${audioUri}")
        val view=WaveformWithPlayPause(requireContext(),null,0,audioUri!!)
        view.setAudioUri(audioUri)
        currentPageLayout.addMusic(view)
        currentPageLayout.setSelectedViewType(CustomLayout.ViewType.AUDIO)
    }
    /*
    Method to change the colors of the buttons
     */
    private fun changeState(button: AppCompatImageButton){
        for (i in mainButtonArray){
            if(i==button){
                if(button.isSelected){
                    button.isSelected=false
                }else{
                    button.isSelected=true
                }

            }
            else{
                i.isSelected=false
            }
        }

    }
    /*
    Method to initialize the Main buttons
     */
    private fun initializeMainButton() {
        /*
        initializing text button.
         */
        binding.textButton.setOnClickListener {
            changeState(binding.textButton)
            currentPageLayout.firstTouched = false
            currentPageLayout.setSelectedViewType(CustomLayout.ViewType.TEXT_VIEW)
            binding.TextEditingLayout.visibility = View.VISIBLE

        }
        /*
        initializing draw button button.
         */
        binding.drawButton.setOnClickListener {
            changeState(binding.drawButton)
            currentPageLayout.setSelectedViewType(CustomLayout.ViewType.CANVAS)
            currentPageLayout.setIsDrawingOn(true)
            modifyPenInitialState()
            binding.mainButtonsLayout.visibility = View.GONE
            binding.drawingButtonsLayout.visibility = View.VISIBLE

        }
        /*
        initializing shape button.
         */
        binding.shapeButton.setOnClickListener {
            changeState(binding.shapeButton)
        }
        /*
        initializing audio button.
         */
        binding.audioButton.setOnClickListener {
            changeState(binding.audioButton)
            showAudioPickerDialog(it)

        }
        /*
        initializing image button.
         */
        binding.imageButton.setOnClickListener {
            changeState(binding.imageButton)
            currentPageLayout.firstTouched = false
            currentPageLayout.setSelectedViewType(CustomLayout.ViewType.IMAGE_VIEW)
            documentScanner!!.getStartScanIntent(requireActivity())
                .addOnSuccessListener { intentSender ->
                    documentScannerLauncher!!.launch(
                        IntentSenderRequest.Builder(intentSender).build()
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("ScannerError", "Failed to start scanner", e)
                }
        }
        /*
        initializing lock button.
         */
        binding.lockButton.setOnClickListener {
            changeState(binding.lockButton)
            val isUnlocked = binding.LikhoPager.isUserInputEnabled
            binding.LikhoPager.isUserInputEnabled = !isUnlocked
            binding.mainLockText.text = if (isUnlocked) "UnLock" else "Lock"
        }

    }
        /*
    Method to initialize the text editing button.
     */
    private fun initialiseTextEditingButtons(){

        /*
        Initialising recogniser button.
         */
        binding.recogniserHelp.setOnClickListener{

            modifySelection(it as AppCompatImageButton)
            isRecognitionOn=!isRecognitionOn
            modifyLayoutVisibility()
        }
        /*
        initialising font type button.
         */
        binding.fontButton.setOnClickListener{

            modifySelection(it as AppCompatImageButton)
        }
        /*
         initialising color type button.
         */
        binding.colorButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
        }
        /*
         initialising font size button.
         */
        binding.fontSizeButton.setOnClickListener{
            modifySelection(it as TextView)
        }
        /*
         initialising bold button.
         */
        binding.boldButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
            currentPageLayout.setIsBoldEnabled(!currentPageLayout.getIsBoldEnabled())
        }
        /*
         initialising italic button.
         */
        binding.italicButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
            currentPageLayout.setIsItalicEnabled(!currentPageLayout.getIsItalicEnabled())
        }
        /*
         initialising underline button.
         */
        binding.underlineButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
            currentPageLayout.setIsUnderLineEnabled(!currentPageLayout.getIsUnderLineEnabled())
        }
        /*
         initialising strike through button.
         */
        binding.strikeThroughButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
            currentPageLayout.setIsStrikeThroughEnabled(!currentPageLayout.getIsStrikeThroughEnabled())
        }
        /*
         initialising hyper link button.
         */
        binding.hyperlinkButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)

        }
        /*
         initialising alignment button.
         */
        binding.alingmentButton.setOnClickListener{
            modifySelection(it as AppCompatImageButton)
            currentPageLayout.setIsAlignmentEnabled(!currentPageLayout.getIsAlignmentEnabled())
        }
    }
    /*
    Method to modify the selection of buttons in text editing layout.
     */
    private  fun modifySelection(button:View){
        if(button.isSelected){
            button.isSelected=false
        }
        else{
            button.isSelected=true
        }
    }

    /*
    Function to toggle visibility.
     */
    fun toggleVisibility(view: View, onVisible: () -> Unit = {}, onHidden: () -> Unit = {}) {
        if (view.visibility == View.VISIBLE) {
            view.visibility = View.GONE
            onHidden() // Action to perform when the view is hidden
        } else {
            view.visibility = View.VISIBLE
            onVisible() // Action to perform when the view is visible
        }
    }

    /*
    Method to show the audio picker dialog to the user.
     */
    private fun showAudioPickerDialog(view: View) {
        // Create the dialog object with custom style
        val dialog = Dialog(view.context, R.style.CustomDialog)
        /*
        ActivityResultLauncher
         */
        // Inflate the custom layout for the dialog
        val inflater = LayoutInflater.from(view.context)
        val dialogView = inflater.inflate(R.layout.audiooptions, null)
        val audioRecord:AppCompatImageButton=dialogView.findViewById(R.id.audioRecord)
        val audioPick:AppCompatImageButton=dialogView.findViewById(R.id.audioPick)

        audioPick.setOnClickListener{
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        intent.setType("audio/*")
            audiPickerLauncher?.launch(intent)
            dialog.dismiss()
        }
        audioRecord.setOnClickListener{
            val outputDirectory = File(requireContext()!!.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "MyAudioClips")
//            val outputDirectory = File(requireContext().filesDir, "MyAudioClips")
            dialog.dismiss()
            showAudioRecordingDialog(outputDirectory,it)
        }
        // Set content view and style for the dialog
        dialog.setContentView(dialogView)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(view.context, R.drawable.dialogbg))

        // Make the dialog cancellable
        dialog.setCancelable(true)

        // Show the dialog
        dialog.show()
    }
    /*
    Method to record the audio.
     */
    @SuppressLint("MissingInflatedId")
    private fun showAudioRecordingDialog(file: File, view: View) {
        // Current recording state
        var recordingState = RecordingState.NOT_STARTED
        // Create the dialog object with custom style
        val dialog = Dialog(view.context, R.style.CustomDialog)
        // Inflate the custom layout for the dialog
        val inflater = LayoutInflater.from(view.context)
        val dialogView = inflater.inflate(R.layout.audiorecorderlayout, null)
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(view.context, R.drawable.dialogbg))

        // Initializing the buttons and textview of the dialog.
        val pauseResumeButton: AppCompatImageButton = dialogView.findViewById(R.id.pauseResumeButton)
        val cancelButton: AppCompatImageButton = dialogView.findViewById(R.id.cancelButton)
        val saveButton: AppCompatImageButton = dialogView.findViewById(R.id.saveButton)

        val currentClipFile = File(file, "clip_${System.currentTimeMillis()}.mp3")
        val recordingTimeText: TextView = dialogView.findViewById(R.id.recordingTimeText)
        val filePath: String = currentClipFile.absolutePath
        val waveRecorder = WaveRecorder(filePath)
        var trial=1
        /*
        Handling the clicks.
         */
        pauseResumeButton.setOnClickListener {
            when (recordingState) {
                RecordingState.NOT_STARTED -> {
                    Log.d("mytag", "showAudioRecordingDialog: starting")
                    // Start recording
                    waveRecorder.startRecording()
                    // Start the recording timer
                    startRecordingTimer(recordingTimeText)
                    // Change the image to pause
                    pauseResumeButton.setImageResource(R.drawable.pause_button_svgrepo_com) // Replace with your pause icon
                    recordingState = RecordingState.RECORDING
                }
                RecordingState.RECORDING -> {
                    // Pause recording
                    waveRecorder.pauseRecording()
                    Log.d("mytag", "showAudioRecordingDialog: pausing")
                    // Pause the timer
                    pauseRecordingTimer()
                    // Change the image to resume
                    pauseResumeButton.setImageResource(R.drawable.play_button_svgrepo_com) // Replace with your play icon
                    recordingState = RecordingState.PAUSED
                }
                RecordingState.PAUSED -> {
                    // Resume recording
                    waveRecorder.resumeRecording()
                    // Resume the timer
                    resumeRecordingTimer(recordingTimeText)
                    // Change the image to pause
                    Log.d("mytag", "showAudioRecordingDialog: resuming")
                    pauseResumeButton.setImageResource(R.drawable.pause_button_svgrepo_com) // Replace with your pause icon
                    recordingState = RecordingState.RECORDING
                }
            }
        }

        saveButton.setOnClickListener {
            // Save the recorded file
            waveRecorder.stopRecording()
            addMusic(Uri.fromFile(currentClipFile))
            // Reset state after saving
            recordingState = RecordingState.NOT_STARTED
            dialog.dismiss()

        }
        cancelButton.setOnClickListener {
            // Stop recording
            waveRecorder.stopRecording()
            currentClipFile.delete()
            recordingState = RecordingState.NOT_STARTED // Reset state
            // Reset timer
            recordingTimeMillis = 0
            recordingTimeText.text = "00:00" // Reset TextView
            timer?.cancel()
            isTimerRunning = false
            dialog.dismiss()
        }

        dialog.setContentView(dialogView)
        dialog.setCancelable(false)
        dialog.show()
    }
    /*
    Utility methods.
     */
    private fun startRecordingTimer(recordingTimeText: TextView) {
        if (!isTimerRunning) {
            timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    recordingTimeMillis++
                    updateRecordingTime(recordingTimeText)
                }

                override fun onFinish() {}
            }
            timer?.start()
            isTimerRunning = true
        }
    }

    private fun pauseRecordingTimer() {
        timer?.cancel()
        isTimerRunning = false
    }

    private fun resumeRecordingTimer(recordingTimeText: TextView) {
        startRecordingTimer(recordingTimeText)  // Restart the timer from where it left off
    }


    private fun updateRecordingTime(recordingTimeText: TextView) {
        val minutes = (recordingTimeMillis / 60).toInt()
        val seconds = (recordingTimeMillis % 60).toInt()
        recordingTimeText.text = String.format("%02d:%02d", minutes, seconds)
    }

    /*
    method to build the document scanner options
     */
    private fun buildOptions(){
        scannerOptions=GmsDocumentScannerOptions.Builder()
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setGalleryImportAllowed(true)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
    }
    /*
    Method to handle the result of the document scanner.
     */
    private  fun handleResult(activityResult: ActivityResult){
        /*
        Getting the result code and result of the scanning.
         */
        val resultCode = activityResult.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        /*
        checking if the scanning was correct.
         */
        if (resultCode == Activity.RESULT_OK && result != null) {

            if (!result.pages!!.isEmpty()) {
                /*
                Add the image t the image view
                 */
                val imageUri=result!!.getPages()!!.get(0).getImageUri()
                Log.d("mytag", "handleResult: uri = "+imageUri)
                currentPageLayout.addImageView(imageUri)
                saveImageToLocalStorage(requireContext().contentResolver, imageUri)
            }


        } else if (resultCode == Activity.RESULT_CANCELED) {
            /*
            Show toast
             */
            Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Scan Error", Toast.LENGTH_SHORT).show();
        }

    }

    /*
    Method to show the dialogBox for the drawing operations
     */
    private fun initialiseDrawingButton() {

        dialogButtonArray= arrayOf(
            binding.normalPen,binding.eraser,binding.highlighter,binding.laserPen,binding.dashedPen,binding.lasso
        )
        /**
         * initializing the buttons lisFdratener
         */
       binding.normalPen.setOnClickListener{
            changeDrawingButtonsState(it as AppCompatImageButton)
            currentPageLayout.setBrushToNormal(penWidth,penColor)
           currentPageLayout.setPenType(CustomCanvasView.PenType.NormalPen)
           currentPageLayout.setIsLassoSelected(false)

        }
        binding.eraser.setOnClickListener{
            changeDrawingButtonsState(it as AppCompatImageButton)
            currentPageLayout.setBrushToErase(penWidth)
            currentPageLayout.setPenType(CustomCanvasView.PenType.EraserPen)
            currentPageLayout.setIsLassoSelected(false)
        }
        binding.highlighter.setOnClickListener{
            changeDrawingButtonsState(it as AppCompatImageButton)
            currentPageLayout.setBrushToHighlighter(penWidth,penColor)
            currentPageLayout.setPenType(CustomCanvasView.PenType.HighLighterPen)
            currentPageLayout.setIsLassoSelected(false)
        }
        binding.laserPen.setOnClickListener{
            changeDrawingButtonsState(it as AppCompatImageButton)
            currentPageLayout.setBrushToLaser()
            currentPageLayout.setPenType(CustomCanvasView.PenType.LaserPen)
            currentPageLayout.setIsLassoSelected(false)
        }
        binding.dashedPen.setOnClickListener{
            changeDrawingButtonsState(it as AppCompatImageButton)
            currentPageLayout.setBrushToDashed(penWidth,penColor)
            currentPageLayout.setPenType(CustomCanvasView.PenType.DashedPen)
            currentPageLayout.setIsLassoSelected(false)
        }
        binding.chooseColor.setOnClickListener{
            ColorPickerDialog.show()
        }
        binding.lasso.setOnClickListener{
            changeDrawingButtonsState(it as AppCompatImageButton)
            currentPageLayout.setIsLassoSelected(true)
            currentPageLayout.setPenType(CustomCanvasView.PenType.LassoPen)
        }
        binding.drawingDoneButton.setOnClickListener{
            currentPageLayout.setIsDrawingOn(false)
            binding.drawingButtonsLayout.visibility=View.GONE
            binding.mainButtonsLayout.visibility=View.VISIBLE
            currentPageLayout.setSelectedViewType(CustomLayout.ViewType.NONE)
        }
        binding.brushStroke.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Code to execute before text is changed (optional)
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Code to execute when text is changing
                // You can handle real-time text changes here
            }
            override fun afterTextChanged(s: Editable?) {
                // Code to execute after the text has changed (optional)
                // Check if the text is not null or empty
                s?.let {
                    val text = it.toString()  // Get the text as a String

                    // Convert the text to an integer and assign it to penWidth
                    penWidth = text.toFloatOrNull() ?: 0f  // Default to 0 if conversion fails
                }
            }
        })
    }
    /*
    Method to change the sate of the buttons
     */
    private fun changeDrawingButtonsState(button: AppCompatImageButton){
        for (i in dialogButtonArray){
            if(i==button){
                if(button.isSelected){
                  break
                }else{
                    button.isSelected=true
                }

            }
            else{
                i.isSelected=false
            }
        }

    }
    /*
    Method to show the color pciker diaolog box
     */
    private  fun showColorPickerDialog(view:View){
        /*
        Creating the dialog object.
         */
        ColorPickerDialog=Dialog(view.context,R.style.CustomDialog)
        /*
        Inflating the dialog boc view.
         */
        val inflater = LayoutInflater.from(view.context)
        val dialogView = inflater.inflate(R.layout.colorpickerdialog, null)

        /*
        Initialising the view and button
         */
        val colorpicker=dialogView.findViewById<ColorPickerView>(R.id.colorpicker)
        val saveButton=dialogView.findViewById<AppCompatButton>(R.id.saveButton)
        /*
        Setting the color of global variable and the selected paint image button
         */
        var tempColor:Int=Color.BLACK
        colorpicker.subscribe{ color,fromUser,shouldPropagate->
            tempColor=color
        }
        saveButton.setOnClickListener{
            penColor=tempColor
            currentPageLayout.setNormalPaintColor(tempColor)
            binding.choosenColor.imageTintList= ColorStateList.valueOf(tempColor)
            ColorPickerDialog.dismiss()
        }
        /*
        Setting the view in dialog
         */
        ColorPickerDialog.setContentView(dialogView)
        ColorPickerDialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        ColorPickerDialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(view.context, R.drawable.dialogbg))
        /*
        Making dialog cancelable
         */
        ColorPickerDialog.setCancelable(true)
        ColorPickerDialog.setOnDismissListener{
            colorpicker.unsubscribe{color,fromUser,shouldPropagate->
            }
        }
    }
    /*
    Method to initialise the ink recogniser layout.
     */
    private  fun initialiseInkRecogniserLayout(){
        /*
        Hiding and disabling the recognizer button
         */

        /*
        Initialising the INk recogniser view.
         */
        val inkRecognizer=InkRecognizer(requireContext())
        inkRecognizer.setRecognitionResultListener(object:RecognitionResultListener{
            override fun getRecognitionResult(result: String) {
                Log.i("mytag", result)
                /*
                getting the current selected edittext of the current layout.
                 */
                if (currentPageLayout.getCurrentEditText()!=null){
                    val editText=  currentPageLayout.getCurrentEditText()!!
                    editText.append(result)
                }

            }

        })
        /*
        adding dynamic view to the container.
         */
        binding.inkrecognizerContainer.addView(inkRecognizer)
        // Assuming inkRecognizer is a View or has a layout that you can modify
        val layoutParams = inkRecognizer.layoutParams

        // Set the height to 300 and width to match_parent
        layoutParams.height = 450
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

        // Apply the updated layoutParams
        inkRecognizer.layoutParams = layoutParams
        /*
        Initialising the toggle switch.
         */
        binding.manualOrAuto.setOnToggledListener(object :OnToggledListener{
            override fun onSwitched(toggleableView: ToggleableView?, isOn: Boolean) {
                /*
                Checking if the toggle is on or off.
                 */
                if(isOn){
                    inkRecognizer.setIsAutomatic(true)
                    binding.readButton.isEnabled=false
                }
                else{
                    inkRecognizer.setIsAutomatic(false)
                    binding.readButton.isEnabled=true
                }
            }

        })
        /*
        Initialising the read button
         */
        binding.readButton.setOnClickListener{
            inkRecognizer.startRecognition()
        }
        /*
        Initialising the read button
         */
        binding.clearRecognizer.setOnClickListener{
            inkRecognizer.clearInk()
        }
        /*
        Initialising the read button
         */
        binding.offsetPaths.setOnClickListener{
            inkRecognizer.offsetPaths(true)
        }

    }
    /*
    Method to hide/show,modify the bottom margin.
     */
    private fun modifyLayoutVisibility(){
        if(isRecognitionOn){
            /*
            Setting the bottom margin
             */
            val layoutParams = binding.TextEditingLayout.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = 595
            binding.TextEditingLayout.layoutParams = layoutParams
            binding.mainButtonsLayout.visibility=View.GONE
            binding.inkrecognizerContainer.visibility=View.VISIBLE
        }
        else{
            /*
            Setting the bottom margin
             */
            val layoutParams = binding.TextEditingLayout.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin =150
            binding.TextEditingLayout.layoutParams = layoutParams
            binding.mainButtonsLayout.visibility=View.VISIBLE
            binding.inkrecognizerContainer.visibility=View.GONE
        }
    }
    interface RecognitionResultListener{
        fun getRecognitionResult(result:String)
    }
    /*
    Method to initialise the child editing button.
     */
    fun initialiseChildEditingButton(){
        binding.pasteButton.isEnabled=false
        binding.undoButton.setOnClickListener{
            currentPageLayout.undoCanvas()
        }
        binding.redoButton.setOnClickListener{
            currentPageLayout.redoCanvas()
        }
        binding.childredoButton.setOnClickListener{
            currentPageLayout.remainingViewRedo(selectedChildType)
        }
        binding.childundoButton.setOnClickListener{
            Log.d("undotag", "undo:child")
            currentPageLayout.remainingViewUndo(selectedChildType)
        }
        binding.copyButton.setOnClickListener{
            /*
            Disabling buttons
             */

            when(selectedChildType){
                CustomLayout.ViewType.TEXT_VIEW->{
                    currentPageLayout.firstTouched=false
                    currentPageLayout.copyChild(selectedChildType)
                }
                CustomLayout.ViewType.IMAGE_VIEW->{
                    currentPageLayout.firstTouched=false
                    currentPageLayout.copyChild(selectedChildType)
                }
                CustomLayout.ViewType.AUDIO->{
                    currentPageLayout.copyChild(selectedChildType)
                }
                CustomLayout.ViewType.CANVAS->{
                    currentPageLayout.copyChild(selectedChildType)
                }
                else->{

                }

            }
            binding.pasteButton.isEnabled=true
            Toast.makeText(requireActivity(),"Copied",Toast.LENGTH_SHORT).show()
        }
        /*
        paste the copied view
         */
        binding.pasteButton.setOnClickListener{
            currentPageLayout.pasteChild()
            binding.pasteButton.isEnabled=false

            Toast.makeText(requireActivity(),"Pasted",Toast.LENGTH_SHORT).show()
        }
        /*
        delete the view
         */
        binding.deleteButton.setOnClickListener{
            if(isDeleteAble){
                if (isAnyChildSelected){
                    currentPageLayout.deleteView()
                    it.isEnabled=false
                    binding.TextEditingLayout.visibility=View.GONE
                    binding.bottomButtonsLayout.visibility=View.GONE
                    binding.mainButtonsLayout.visibility=View.VISIBLE
                    isAnyChildSelected=false
                }
            }
        }
        /*
        initialising the lock and unlock button.
         */
        binding.childLockButton.setOnClickListener {

            // Toggle the state first
            isChildLocked = !isChildLocked

            // Update the UI based on the new state
            if (isChildLocked) {
                binding.lockText.text = "Unlock"

            } else {
                binding.lockText.text = "Lock"

            }

            // Apply the lock/unlock operation
            currentPageLayout.lockUnlockChild(isChildLocked)
        }

        binding.belowlinkButton.setOnClickListener{
            /*
             Show add link pop up
              */
            popupWindow!!.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        }

    }
    /*
    Method to save image to local storage.
     */
    fun saveImageToLocalStorage(contentResolver: ContentResolver, imageUri: Uri) {
        try {
            // Create a unique filename
            val fileName = "image_${System.currentTimeMillis()}.jpg"

            // Check if Android version is Q or above
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/myimages")
                }

                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    contentResolver.openOutputStream(it).use { outputStream ->
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                        if (outputStream != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        }
                    }
                }
            } else {
                // For older Android versions, use direct file path
                val myImagesDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "myimages")
                if (!myImagesDir.exists()) myImagesDir.mkdirs()

                val file = File(myImagesDir, fileName)
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                val outputStream: OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            }

            Log.d("SaveImage", "Image saved successfully to $fileName")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SaveImage", "Error saving image: ${e.message}")
        }
    }

    /*
    Method for setting up pop up for adding link to th child.
     */

    private fun addLinkToViewPopUp() {
        // Inflate the popup layout
        val inflater = LayoutInflater.from(requireContext())
         popupView = inflater.inflate(R.layout.setlinkpopuplayoit, null)

        // Create a PopupWindow
         popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true // Focusable
        )

        // Find views in the popup
        val linkNameEditText: EditText = popupView.findViewById(R.id.et_link_name)
        val linkUrlEditText: EditText = popupView.findViewById(R.id.et_link_url)
        val saveButton: Button = popupView.findViewById(R.id.btn_save_link)

        popupWindow!!.elevation = 15f
        // Set up the Save button click listener
        saveButton.setOnClickListener {
            val linkName = linkNameEditText.text.toString().trim()
            var linkUrl = linkUrlEditText.text.toString().trim()

            // Check if the link name is valid (not empty)
            if (linkName.isEmpty()) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the link URL is empty
            if (linkUrl.isEmpty()) {
                Toast.makeText(requireContext(), "URL cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ensure the URL starts with a valid scheme (http/https)
            if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
                linkUrl = "https://$linkUrl"
            }

            // Validate the updated URL
            if (!Patterns.WEB_URL.matcher(linkUrl).matches()) {
                Toast.makeText(requireContext(), "Enter a valid URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If both are valid, handle saving the link
            currentPageLayout.setLinkInChild(linkName, linkUrl, selectedChildType)
            Toast.makeText(requireContext(), "Link saved successfully", Toast.LENGTH_SHORT).show()
            popupWindow!!.dismiss()
        }

    }
    /*
    Method to modify the initial state of the
     */
    private fun modifyPenInitialState(){
        when(currentPageLayout.getPenType()){
            CustomCanvasView.PenType.NormalPen->{
                changeDrawingButtonsState(binding.normalPen)
            }
            CustomCanvasView.PenType.DashedPen->{
                changeDrawingButtonsState(binding.dashedPen)
            }
            CustomCanvasView.PenType.LaserPen->{
                changeDrawingButtonsState(binding.laserPen)
            }
            CustomCanvasView.PenType.LassoPen->{
                changeDrawingButtonsState(binding.lasso)
            }
            CustomCanvasView.PenType.EraserPen->{
                changeDrawingButtonsState(binding.eraser)
            }
            CustomCanvasView.PenType.HighLighterPen->{
                changeDrawingButtonsState(binding.highlighter)
            }
            else->{

            }
        }
    }
    /*
    method to modify the param layout of drawing button layout.
     */
    private fun modifyDrawingLayoutParam(height: Int) {
        val params = binding.drawingButtonsLayout.layoutParams

        // Check if params are an instance of MarginLayoutParams
        if (params is ViewGroup.MarginLayoutParams) {
            params.bottomMargin = height // Set the bottom margin
            binding.drawingButtonsLayout.layoutParams = params // Apply changes
        } else {
            // Log or handle cases where layoutParams is not MarginLayoutParams
            Log.e("modifyDrawingLayoutParam", "LayoutParams is not MarginLayoutParams")
        }
    }

    /**
     *
     * Method to read the saved data form the file.
     */
    private suspend fun readLikhoEditText(){
        /*
        Variable to represent editText  file location based on the document name and pageNumber
         */
        val editFolder:File=File(requireContext().filesDir,"Notes" + File.separator + documentName + File.separator + typeLikhoEditText)
        Log.d("beforeCoroutine", "readLikhoEditText: absolute path = ${editFolder}")
        /*
        Iterating over the folder to retrieve all the files .
         */
        val filteredFiles = editFolder.listFiles()?.filter {
            it.isFile && it.name.endsWith(".json") // Regex to check for a number in the filename
        }
        /*
        Now Going Through Each File in the list and reading it's content.
         */
        val gson = Gson()
        if (filteredFiles != null && filteredFiles.isNotEmpty()) {
            for (file in filteredFiles) {
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        readForEditText(file,gson)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d("JSONReader", "Failed to read file: ${file.name}")
                }
            }
        } else {
            Log.d("JSONReader", "No JSON files found in the folder.")
        }
//        incrementLoadedCount()
    }
    /*
    Helper method to read for edittext.
     */
    private  suspend fun readForEditText(file: File,gson: Gson){
        // Read each JSON file
        val content = readFileContent(file)
        // Optionally, parse JSON (Gson example)
        val helperInstance = gson.fromJson(content, likhoEditSave::class.java)
        withContext(Dispatchers.Main){
            val editText=currentPageLayout.addTextView(100f,100f)
            editText.getTextFromHTML(helperInstance.text)
        }
    }

    /*
    Method to read the saved image .
     */
    /*
Variables that will be used to save the edittext .
*/
    private val typeName = "likhoImage"
    private suspend fun readLikhoImage(){
        /*
       Variable to represent editText  file location based on the document name and pageNumber
        */
        val imageFolder:File=File(requireContext().filesDir,"Notes" + File.separator + documentName + File.separator + typeName)
        /*
        Iterating over the folder to retrieve all the files .
         */
        val filteredFiles = imageFolder.listFiles()?.filter {
            it.isFile && it.name.endsWith(".json") // Regex to check for a number in the filename
        }
        /*
        Now Going Through Each File in the list and reading it's content.
         */
        val gson = Gson()
        if (filteredFiles != null && filteredFiles.isNotEmpty()) {
            for (file in filteredFiles) {
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        readForImage(file,gson)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d("JSONReader", "Failed to read file: ${file.name}")
                }
            }
        } else {
            Log.d("JSONReader", "No JSON files found in the folder.")
        }
//        incrementLoadedCount()
    }

    /*
    Helper method to read fro image
     */
    private suspend fun readForImage(file: File,gson: Gson){
        // Read each JSON file
        val content = readFileContent(file)
        // Optionally, parse JSON (Gson example)

        val helperInstance = gson.fromJson(content, likhoImageSave::class.java)
        withContext(Dispatchers.Main){
            currentPageLayout.addImageView( Uri.parse(helperInstance.getUri()))

        }
    }

    /*
Method to read the saved image .
 */
    /*
Variables that will be used to save the edittext .
*/
    private val typeAudioName = "likhoAudio"
    private suspend fun readLikhoAudio(){
        /*
       Variable to represent editText  file location based on the document name and pageNumber
        */
        val audioFolder:File=File(requireContext().filesDir,"Notes" + File.separator + documentName + File.separator + typeAudioName)
        /*
        Iterating over the folder to retrieve all the files .
         */
        val filteredFiles = audioFolder.listFiles()?.filter {
            it.isFile && it.name.endsWith(".json") // Regex to check for a number in the filename
        }
        /*
        Now Going Through Each File in the list and reading it's content.
         */
        val gson = Gson()
        if (filteredFiles != null && filteredFiles.isNotEmpty()) {
            for (file in filteredFiles) {
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        readForAudio(file,gson)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d("JSONReader", "Failed to read file: ${file.name}")
                }
            }
        } else {
            Log.d("JSONReader", "No JSON files found in the folder.")
        }
//        incrementLoadedCount()
    }

    /*
    Helper Method to read for audio Picker.
     */
    private  suspend fun readForAudio(file: File,gson:Gson){
        // Read each JSON file
        val content = readFileContent(file)
        // Optionally, parse JSON (Gson example)

        val audioHelperInstance = gson.fromJson(content, likhoAudioSave::class.java)
        val view=WaveformWithPlayPause(requireContext(),null,0,Uri.parse(audioHelperInstance.getUri())!!)
        withContext(Dispatchers.Main){
            view.setAudioUri(Uri.parse(audioHelperInstance.getUri()))
            currentPageLayout.addMusic(view)
        }
    }

    /*
Method to read and add the paths to canvas.
 */
    private val typeCanvasName = "likhoCanvas"
    private suspend fun readLikhoCanvas(){
        /*
       Variable to represent editText  file location based on the document name and pageNumber
        */
        val canvasFolder:File=File(requireContext().filesDir,"Notes" + File.separator + documentName + File.separator + typeCanvasName)
        Log.d("canvaspath", "readLikhoAudio: absolute path = ${canvasFolder.absolutePath}")
        /*
        Iterating over the folder to retrieve all the files .
         */
        val filteredFiles = canvasFolder.listFiles()?.filter {
            it.isFile && it.name.endsWith(".json") // Regex to check for a number in the filename
        }
        Log.d("canvaspath", "readLikhoAudio: after filter")
        /*
        Now Going Through Each File in the list and reading it's content.
         */
        val gson = GsonBuilder()
            .registerTypeAdapter(PaintProperties::class.java, PaintPropertiesSerializer())
            .registerTypeAdapter(PaintProperties::class.java, PaintPropertiesDeserializer())
            .registerTypeAdapter(likhoPath::class.java, LikhoPathSerializer())
            .registerTypeAdapter(likhoPath::class.java, LikhoPathDeserializer())
            .create()
        val canvasView=currentPageLayout.getCanvasView()
        if (filteredFiles != null && filteredFiles.isNotEmpty()) {
            for (file in filteredFiles) {
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        readForCanvas(file,canvasView,gson)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d("JSONReader", "Failed to read file: ${file.name}")
                }
            }
        } else {
            Log.d("JSONReader", "No JSON files found in the folder.")
        }
//        incrementLoadedCount()
    }
    /*
    Helper method to read canvas data.
     */
    private suspend fun readForCanvas(file:File,canvasView: CustomCanvasView,gson:Gson){
        // Read each JSON file
        val content = readFileContent(file)
        // Optionally, parse JSON (Gson example)

        val pathHelperInstance = gson.fromJson(content, likhoPath::class.java)
        /*
       Adding path from the xml to likhoPath.
        */
        pathHelperInstance.path=canvasView.parseSVGPath(pathHelperInstance.xml)
        withContext(Dispatchers.Main){
            canvasView.addStoredPath(pathHelperInstance)
        }
    }
    /*
    Helper method to read the file form the storage
     */
    private fun readFileContent(file: File): String {
        return file.bufferedReader().use { reader ->
            reader.readText().trim()
        }
    }
    /*
    Method to save the Audio inside the internal directory.
     */
    private fun copyAudioToLocalStorage(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val fileName = "likhoAudio_${System.currentTimeMillis()}.mp3" // Example: Unique name with timestamp
        val outputFile = File(requireContext().filesDir,"Notes" + File.separator + documentName + File.separator + typeAudioName+File.separator+fileName)
        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        return outputFile.absolutePath // Save this path instead of the URI
    }
    /*
    Method to initialise the Dialog box
     */
    private fun showLoadingDialog(view:View) {
        // Create a custom ProgressBar layout
        loadingDialog = Dialog(view.context, R.style.CustomDialog)
        // Inflate the custom layout for the dialog
        val inflater = LayoutInflater.from(view.context)
        val dialogView = inflater.inflate(R.layout.dialog_loading, null)
        // Set content view for the dialog
        loadingDialog.setContentView(dialogView)
        // Set dialog window size and background drawable
        loadingDialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        loadingDialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(view.context, R.drawable.popup_background))
        // Make the dialog cancellable
        loadingDialog.setCancelable(false)
    }
    private suspend fun incrementLoadedCount() {
        withContext(Dispatchers.Main) {
            loadedCount++
            if (loadedCount == 4) {
                loadingDialog.dismiss()
            }
        }
    }

}