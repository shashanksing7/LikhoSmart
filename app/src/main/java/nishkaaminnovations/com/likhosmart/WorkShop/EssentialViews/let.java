package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatEditText;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.util.Stack;

import nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews.SaveClasses.les;

public class let extends AppCompatEditText {

    // Constants
    private static final int TOUCH_TOLERANCE = 50; // Increased tolerance for touch
    private static final int DRAG_AREA_MARGIN = 35; // Margin to avoid rectangle and circles

    /*
     * Variables for resizing,rotating and dragging functionality
     */
    private boolean isResizing = false; // Flag to indicate if resizing is in progress
    private boolean isDragging = false; // Flag to indicate if dragging is in progress
    private boolean isRotating = false;  //Flag to indicate if rotating is in progress

    /*
     * Variables for custom drawing
     */
    private Paint rectanglePaint; // Paint for the outer rectangle
    private Paint touchPointPaint; // Paint for touch points (circles)
    private boolean showCircles = false; // Flag to toggle visibility of touch points

    /*
     * Other variables
     */
    private onChildViewClickListener onChildClickListener; // Listener for child view clicks
    private boolean isSecondTouch = false; // Flag for handling secondary touches
    private vu utility; // Helper utility for resizing and dragging logic
    /*
    Parent layout.
     */
    private cl layout;

    /*
    Variables to check if the user is deleting.
     */
    private boolean isDeleting = false; // Track if the user is deleting text
    /*
    Default text size for the Edit text.
     */
    private static final int DEFAULT_SIZE = 10;
    /*
    Object of the   HtmlTagHandler class.
     */
    private hth hth;

    private Boolean isLocked=false;
    /*
    Variables for redo and undo
     */
    // Stacks for undo and redo history
    private final Stack<Editable> undoStack = new Stack<>();
    private final Stack<Editable> redoStack = new Stack<>();
    private boolean isUndoOrRedo = false;

    /*
        Variables to represent the link text and url
         */
    private String linkName="noName";
    private  String linkUrl="noURL";

    public let(Context context) {
        super(context);
    }

    public let(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public let(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        // Custom logic for enabling/disabling the view
        if (!enabled) {
            isSecondTouch = false;
            utility = null;
            hth = null;
            if(!getText().toString().isEmpty()){
                updateHelperInstance();
                saveLikhoEditText();
            }
        } else {
            isSecondTouch = false;
            utility = new vu(this);
            hth = new hth();
        }
    }

    void init(cl layout, onChildViewClickListener onChildViewClickListener) {
        this.onChildClickListener=onChildViewClickListener;
        /*
        Initialising the helper instance.
         */
        setTextSize(10f);

        les =new les();
        // Set default text size in SP
        setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_SIZE);
        utility = new vu(this);
        this.layout = layout;
        // Set transparent background to remove the black outline
        setBackgroundColor(Color.TRANSPARENT);

        // Initialize paint for the rectangle
        rectanglePaint = new Paint();
        rectanglePaint.setColor(Color.BLUE); // Rectangle color
        rectanglePaint.setStyle(Paint.Style.STROKE);
        rectanglePaint.setStrokeWidth(4); // Rectangle thickness
        PathEffect dashEffect = new DashPathEffect(new float[]{10, 10}, 0);
        rectanglePaint.setPathEffect(dashEffect); // Dashed effect

        // Initialize paint for the touch points
        touchPointPaint = new Paint();
        touchPointPaint.setColor(Color.BLUE); // Touch point color
        touchPointPaint.setStyle(Paint.Style.STROKE);
        touchPointPaint.setStrokeWidth(2); // Stroke width for touch points
        // Set padding and minimum dimensions for the EditText
        setPadding(35, 35, 35, 35);
        setMinimumWidth(150);
        this.post(new Runnable() {
            @Override
            public void run() {
                // Add text watcher for dynamic resizing
                addTextChangedListener(new TextWatcher() {
                    private int start = 0;
                    private int count = 0;
                    private boolean isSpacePressed = false;
                    private Editable beforeChange;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        isDeleting = after < count;
                        if (!isUndoOrRedo) {
                            beforeChange = getText().toString() != null ? new SpannableStringBuilder(getText()) : null;
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        resize();
                        this.start = start;
                        this.count = count;
                        isSpacePressed = count > 0 && s != null && s.charAt(start) == ' ';
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Log.d("texttag", "count =  "+count);
                        if (!isDeleting && count > 0 && s != null && !isSpacePressed&&!isUndoOrRedo) {
                            Log.d("texttag", "afterTextChanged: called = "+count);
                            applyTextStyles(s, start, count);
                        }
                        isSpacePressed = false;
                        if (!isUndoOrRedo && beforeChange != null) {
                            // Push the old state to the undo stack

                            undoStack.push(beforeChange);
                            // Clear the redo stack because a new change invalidates the redo history
                            redoStack.clear();
                        }
                    }
                });
            }
        });
        // Toggle circle visibility based on focus
        setOnFocusChangeListener((v, hasFocus) -> {
            showCircles = hasFocus;
            invalidate();
        });
        fileName = (fileName != null && !fileName.isEmpty()) ? fileName : generateUniqueName();
    }
    private void resize() {
        int textMargin = 50;
        // Get the Layout object of the EditText, which contains information about the text layout
        Layout layout = getLayout();
        if (layout == null) {
            return; // Exit if layout is not initialized yet
        }

        int totalHeight = 0;

        // Loop through each line of the text
        for (int i = 0; i < layout.getLineCount(); i++) {
            // Get the start and end index of the line
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);

            // Get the portion of text in the current line
            CharSequence lineText = getText().subSequence(lineStart, lineEnd);

            // Determine the relative font size for the current line
            float maxRelativeSize = 1.0f; // Default multiplier
            if (lineText instanceof Spanned) {
                Spanned spannedText = (Spanned) lineText;

                // Find all RelativeSizeSpans applied to this line
                RelativeSizeSpan[] spans = spannedText.getSpans(0, lineText.length(), RelativeSizeSpan.class);
                for (RelativeSizeSpan span : spans) {
                    maxRelativeSize = Math.max(maxRelativeSize, span.getSizeChange());
                }
            }

            // Calculate the line height considering the maximum relative size span
            int lineHeight = (int) (getLineHeight() * maxRelativeSize);
            totalHeight += lineHeight;
        }

        // Add padding to the calculated height
        totalHeight += getPaddingTop() + getPaddingBottom();

        // Adjust the height of the EditText
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = totalHeight + textMargin; // Set the new height
        setLayoutParams(params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(onChildClickListener.isDrawingOn()){
            return;
        }
        // Draw outer rectangle if focused
        if (isFocused() || isSecondTouch) {
            drawOuterRectangle(canvas);
        }

        // Draw touch points if they are visible
        if (showCircles || isSecondTouch) {
            drawTouchPoints(canvas);
        }
    }

    private void drawOuterRectangle(Canvas canvas) {
        float inset = 35; // Margin
        RectF outerRect = new RectF(inset, inset, getWidth() - inset, getHeight() - inset);
        canvas.drawRect(outerRect, rectanglePaint);
    }

    Bitmap bitmap;

    private void drawTouchPoints(Canvas canvas) {
        float radius = 20; // Radius of touch points
        float inset = 35; // Margin matching the rectangle

        // Draw circles at the corners
        canvas.drawCircle(inset, inset, radius, touchPointPaint); // Top-left
        canvas.drawCircle(getWidth() - inset, inset, radius + 10, touchPointPaint); // Top-right
        canvas.drawCircle(inset, getHeight() - inset, radius, touchPointPaint); // Bottom-left
        canvas.drawCircle(getWidth() - inset, getHeight() - inset, radius, touchPointPaint); // Bottom-right
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (onChildClickListener != null) {
            if(onChildClickListener.isDrawingOn()){
                return true ;
            }
            onChildClickListener.onViewClicked(this, cl.ViewType.TEXT_VIEW,isLocked,true,linkUrl,linkName,0f,0f);
        }
        if(isLocked){
            return true;
        }

        if (!isSecondTouch) {
            isSecondTouch = true;
            return true;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                utility.setResizeCorner(utility.getResizeCorner(event));

                if (utility.getResizeCorner() == 1) {
                    utility.setLastTouchX(event.getRawX());
                    utility.setLastTouchY(event.getRawY());
                    utility.setCentreX((getX() + getWidth()) / 2);
                    utility.setCentreY((getY() + getHeight()) / 2);
                    isRotating = true;
                } else if (utility.getResizeCorner() != -1) {
                    utility.setInitialHeight(getHeight());
                    utility.setInitialWidth(getWidth());
                    utility.setInitialX(event.getRawX());
                    utility.setInitialY(event.getRawY());
                    isResizing = true;
                    showCircles = true;
                    invalidate();
                } else if (utility.isDraggableArea(event, DRAG_AREA_MARGIN)) {
                    utility.setDragOffsetX(event.getRawX() - getX());
                    utility.setDragOffsetY(event.getRawY() - getY());
                    isDragging = true;
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isResizing) {
                    utility.handleResize(event);
                    resize();
                    return true;
                }
                if (isDragging) {
                    utility.handleDrag(event);
                    invalidate();

                }
                if (isRotating) {
                    utility.handleRotation(event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(isDragging|| isRotating){
                    updateHelperInstance();
                }
                isResizing = false;
                isDragging = false;
                isRotating = false;
                invalidate();
                break;
        }

        return super.onTouchEvent(event);
    }

    /*
    Method to Check and Apply text Styles.
     */
    private void applyTextStyles(Editable editableText, int start, int count) {

        int end = start + count;

        // Ensure indices are within valid range
        if (start < 0 || end < 0 || start >= editableText.length() || end > editableText.length()) {
            return;
        }

        // Apply styles only to the newly added text segment
        if (layout.getIsBoldEnabled()) {
            editableText.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (layout.getIsItalicEnabled()) {
            editableText.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (layout.getIsUnderLineEnabled()) {
            editableText.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (layout.getIsStrikeThroughEnabled()) {
            editableText.setSpan(new StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (true) {
            editableText.setSpan(new TypefaceSpan(layout.getFontFace()), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (true) {
            String colorString = layout.getFontColor(); // Returns something like "#FF0000"
            int color = Color.parseColor(colorString);
            editableText.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (layout.getIsURLEnabled()) {
            editableText.setSpan(new URLSpan(layout.getLinkText()), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (true) {
            editableText.setSpan(new RelativeSizeSpan(layout.getFontSize()), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (layout.getIsAlignmentEnabled()) {
            editableText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        resize();
    }

    /*
Method to save the editable object as an html
 */
    private String saveUsingHtml() {
        return Html.toHtml(getText(), Html.FROM_HTML_MODE_COMPACT);
    }

    /*
    Method to extract th editable form html.
     */
   public  void getTextFromHTML(String html) {

        setText(Html.fromHtml(prepareHTMLForTagHandling(html), Html.FROM_HTML_MODE_COMPACT, null, hth));
    }

    /*
   Method to replace the tag with the custom tag for fontSize.
    */
    public String prepareHTMLForTagHandling(String htmlSource) {
        if (htmlSource == null || htmlSource.isEmpty()) {
            return null;
        }
        return htmlSource
                .replace("<span", "<fontsizetag")
                .replace("</span>", "</fontsizetag>");
    }

    public Boolean getLocked() {
        return isLocked;
    }

    public void setLocked(Boolean locked) {
        isLocked = locked;
        Log.d("mytag", "setLocked: "+locked);
        invalidate();
    }
    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    /*
    Redo and undo Method
     */
    // Undo method
    public void undo() {
        Log.d("undotag", "undo: start");
        if (!undoStack.isEmpty()) {
            Log.d("undotag", "undo: not empty");
            Editable currentText = getText();
            if (currentText != null) {
                redoStack.push(new SpannableStringBuilder(currentText)); // Save current state to redo stack
            }
            Editable previousText = undoStack.pop(); // Retrieve the last state
            isUndoOrRedo = true; // Set flag to prevent infinite loop
            setText(previousText);
            setSelection(previousText.length()); // Move cursor to end of text
            isUndoOrRedo = false;
        }
    }

    // Redo method
    public void redo() {
        if (!redoStack.isEmpty()) {
            Editable currentText = getText();
            if (currentText != null) {
                undoStack.push(new SpannableStringBuilder(currentText)); // Save current state to undo stack
            }
            Editable nextText = redoStack.pop(); // Retrieve the last undone state
            isUndoOrRedo = true; // Set flag to prevent infinite loop
            setText("");
            append(nextText);
            setSelection(nextText.length()); // Move cursor to end of text
            isUndoOrRedo = false;
        }
    }

    // Clear history (optional utility method)
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }


    /*
    Variables that will be used to save the edittext .
     */
    private String typeName="likhoEdit";
    private File editTextFile;
    /*
    Helper Instance.
     */
    private les les;
    /*
    Method to save the edittext with page specific name .
     */
    private String fileName="";
    void saveLikhoEditText(){
        File editTextFile = new File(
                        onChildClickListener.getDocumenLocation() + File.separator +
                        typeName + File.separator +
                        fileName
        );

        /*
        Creating String Json of the helper instance.
         */
        Gson gson = new Gson();
        String jsonString=gson.toJson(les);
        try (FileWriter writer = new FileWriter(editTextFile)) {
            writer.write(jsonString); // Write the JSON data
        }
        catch (Exception e){
            Log.d("savingError", "saveLikhoEditText: Exception is "+e.toString());
        }
    }

    /*
    Method to update the helper instance.
     */
    private void updateHelperInstance(){
        les.setX(getX());
        les.setY(getY());
        les.setRotationalAngle(getRotation());
        les.setFileName(fileName);
        les.setText(saveUsingHtml());
        les.setLinkUrl(linkUrl);
        les.setLinkName(linkName);
    }
    /**
     * Method to generate a name based on the current time and current page number.
     */
    public String generateUniqueName() {
        // Get the current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();
        // Combine the current time with the page number
        String uniqueName = onChildClickListener.getPageNumber()+currentTimeMillis + ".json";
        return uniqueName;
    }
    String getFileName(){
        return fileName;
    }
    void delete(){
        File editTextFile = new File(
                        onChildClickListener.getDocumenLocation() + File.separator +
                        typeName + File.separator +
                        fileName
        );
        editTextFile.delete();
    }

  public  void setFileName(String name){
        fileName=name;
   }
}