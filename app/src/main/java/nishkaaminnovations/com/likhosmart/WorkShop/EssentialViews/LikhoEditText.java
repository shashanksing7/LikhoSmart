package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews;

import android.content.Context;
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
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.appcompat.widget.AppCompatEditText;

class LikhoEditText extends AppCompatEditText {

    // Constants
    private static final int TOUCH_TOLERANCE = 50; // Increased tolerance for touch
    private static final int DRAG_AREA_MARGIN = 35; // Margin to avoid rectangle and circles

    /*
     * Variables for resizing,rotating and dragging functionality
     */
    private boolean isResizing = false; // Flag to indicate if resizing is in progress
    private boolean isDragging = false; // Flag to indicate if dragging is in progress
    private  boolean isRotating=false;  //Flag to indicate if rotating is in progress



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
    private ViewUtility utility; // Helper utility for resizing and dragging logic
    /*
    Parent layout.
     */
    private CustomLayout layout;

    /*
    Variables to check if the user is deleting.
     */
    private boolean isDeleting = false; // Track if the user is deleting text
    /*
    Default text size for the Edit text.
     */
    private static final int DEFAULT_SIZE=10;
    /*
    Object of the   HtmlTagHandler class.
     */
    private HtmlTagHandler htmlTagHandler;

    public LikhoEditText(Context context) {
        super(context);
        init();
    }

    public LikhoEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LikhoEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        // Custom logic for enabling/disabling the view
        if (!enabled) {
            isSecondTouch = false;
            utility = new ViewUtility(this);
            htmlTagHandler= new HtmlTagHandler();

        } else {
            isSecondTouch = false;
            utility=null;
            htmlTagHandler=null;
        }
    }

    private void init() {
        // Set default text size in SP
        setTextSize(TypedValue.COMPLEX_UNIT_SP,DEFAULT_SIZE);
        utility = new ViewUtility(this);
        layout =(CustomLayout) getParent();
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

        // Add text watcher for dynamic resizing
        this.post(() -> addTextChangedListener(new TextWatcher() {
            private int start=0;
            private int count=0;
            private boolean isSpacePressed = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isDeleting = after < count;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                this.start = start;
                this.count = count;
                isSpacePressed = count > 0 && s != null && s.charAt(start) == ' ';
                resize();

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isDeleting && count > 0 && s != null && !isSpacePressed) {
//                    applyTextStyles(s, start, count);
                }
                isSpacePressed = false;
            }
        }));

        // Toggle circle visibility based on focus
        setOnFocusChangeListener((v, hasFocus) -> {
            showCircles = hasFocus;
            invalidate();
        });
    }

    private void resize() {
        // Adjust height dynamically based on text content
        int lineHeight = getLineHeight();
        int lines = getLineCount();
        int height = lineHeight * lines + getPaddingTop() + getPaddingBottom();

        // Update the view's height
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = height;
        setLayoutParams(params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

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

    private void drawTouchPoints(Canvas canvas) {
        float radius = 20; // Radius of touch points
        float inset = 35; // Margin matching the rectangle

        // Draw circles at the corners
        canvas.drawCircle(inset, inset, radius, touchPointPaint); // Top-left
        canvas.drawCircle(getWidth() - inset, inset, radius+10, touchPointPaint); // Top-right
        canvas.drawCircle(inset, getHeight() - inset, radius, touchPointPaint); // Bottom-left
        canvas.drawCircle(getWidth() - inset, getHeight() - inset, radius, touchPointPaint); // Bottom-right
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (onChildClickListener != null && !isEnabled()) {
            onChildClickListener.onViewClicked(this);
        }

        if (!isSecondTouch) {
            isSecondTouch = true;
            return true;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                utility.setResizeCorner(utility.getResizeCorner(event));

                if(utility.getResizeCorner()==1){
                    utility.setLastTouchX(event.getRawX());
                    utility.setLastTouchY(event.getRawY());
                    utility.setCentreX((getX()+getWidth())/2);
                    utility.setCentreY((getY()+getHeight())/2);
                    isRotating=true;
                }
                 else if (utility.getResizeCorner()!=-1) {
                    utility.setInitialHeight(getHeight());
                    utility.setInitialWidth(getWidth());
                    utility.setInitialX(event.getRawX());
                    isResizing = true;
                    showCircles = true;
                    invalidate();
                }
                else if (utility.isDraggableArea(event, DRAG_AREA_MARGIN)) {
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
                    invalidate();
                    return true;
                }
                if (isDragging) {
                    utility.handleDrag(event);
                    invalidate();

                }
                if(isRotating){
                    utility.handleRotation(event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isResizing = false;
                isDragging = false;
                isRotating=false;
                invalidate();
                break;
        }

        return super.onTouchEvent(event);
    }
    /*
    Method to Check and Apply text Styles.
     */
    private void applyTextStyles(int start, int count) {
        Editable editableText = getText();
        int end = start + count;

        // Ensure indices are within valid range
        if (start < 0 || end < 0 || start >= editableText.length() || end > editableText.length()) {
            return;
        }

        // Apply styles only to the newly added text segment
        if (layout.isBoldEnabledPublic()) {
            editableText.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (layout.isItalicEnabledPublic()) {
            editableText.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (layout.isUnderLineEnabledPublic()) {
            editableText.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (layout.isStrikeThroughEnabledPublic()) {
            editableText.setSpan(new StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (true) {
            editableText.setSpan(new TypefaceSpan(layout.getFontFacePublic()), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (true) {
            String colorString = layout.getFontColorPublic(); // Returns something like "#FF0000"
            int color = Color.parseColor(colorString);
            editableText.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (layout.isURLEnabledPublic()) {
            editableText.setSpan(new URLSpan(layout.getLinkTextPublic()), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (true) {
            editableText.setSpan(new RelativeSizeSpan(layout.getFontSizePublic()), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (layout.isAlignmentEnabledPublic()) {
            editableText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
        /*
    Method to save the editable object as an html
     */
    private void  saveUsingHtml(){
           String html = Html.toHtml (getText(),Html.FROM_HTML_MODE_COMPACT);
    }
    /*
    Method to extract th editable form html.
     */
    private void  getTextFromHTML(){

        String html="to be initilaized";
        setText(Html.fromHtml(prepareHTMLForTagHandling(html),Html.FROM_HTML_MODE_COMPACT,null,htmlTagHandler));

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
    // Setter for child click listener
    public void setOnChildClickListener(onChildViewClickListener listener) {
        this.onChildClickListener = listener;
    }
}