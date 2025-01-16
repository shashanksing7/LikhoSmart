package nishkaaminnovations.com.likhosmart.WorkShop.EssentialViews;

import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;

import androidx.annotation.NonNull;

import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class hth implements Html.TagHandler {
    private static final String TAG = "mytag";

    int start=0;
    int end =0;
    float sizeInPixels=0;
    @Override
    public void handleTag(final boolean opening, final String tag, Editable output, final XMLReader xmlReader) {
        if (opening) {
            if (tag.equalsIgnoreCase("fontsizetag")) {
                String sizeAttribute = getAttribute("style", xmlReader, "1"); // Default size is 1
                sizeInPixels = getFontValue(sizeAttribute);
                start = output.length();  // Record the start position
            }
        } else { // closing tag
            if (tag.equalsIgnoreCase("fontsizetag")) {
                end = output.length();  // Record the end position
                // Applying the span correctly using EXCLUSIVE_EXCLUSIVE to ensure it does not extend beyond the range
                output.setSpan(new RelativeSizeSpan(sizeInPixels), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static <T> T getLast(Editable text, Class<T> kind) {
        final T[] objs = text.getSpans(0, text.length(), kind);
        if(objs.length == 0) {
            return null;
        } else {
            for(int i = objs.length; i > 0; i--) {
                if(text.getSpanFlags(objs[i - 1]) == Spannable.SPAN_MARK_MARK) {
                    return objs[i - 1];
                }
            }
            return null;
        }
    }
    private static String getAttribute(@NonNull String attr, @NonNull XMLReader reader, String defaultAttr) {
        try {
            final Field elementField = reader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            final Object element = elementField.get(reader);
            final Field attsField = element.getClass().getDeclaredField("theAtts");
            attsField.setAccessible(true);
            final Object atts = attsField.get(element);
            final Field dataField = atts.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            final String[] data = (String[]) dataField.get(atts);
            final Field lengthField = atts.getClass().getDeclaredField("length");
            lengthField.setAccessible(true);
            final int len = (Integer) lengthField.get(atts);
            for(int i = 0; i < len; i++) {
                if(attr.equals(data[i * 5 + 1])) {
                    return data[i * 5 + 4];
                }
            }
        } catch(Exception e) {
        }
        return defaultAttr;
    }

    private static class A {
        String href;

        A(String href) {
            this.href = href;
        }
    }

    private float getFontValue(String inputText) {
        // Pattern to match font-size values like 16px, 1.5em, 100%, etc.
        Pattern fontSizePattern = Pattern.compile("font-size:\\s*([\\d.]+)(em|px|%)?");

        // Match the inputText against the regular expression
        Matcher matcher = fontSizePattern.matcher(inputText);

        // Default value if no font-size is found (as a float)
        float fontSize = 1.5f;

        // If a match is found, extract the numeric value
        if (matcher.find()) {
            // Extract the numeric part (group 1)
            fontSize = Float.parseFloat(matcher.group(1));
        }

        // Return the numeric font size value as a float (without the unit)
        return fontSize;
    }


}