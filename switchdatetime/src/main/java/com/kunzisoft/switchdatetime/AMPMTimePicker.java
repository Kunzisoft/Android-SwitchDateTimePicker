package com.kunzisoft.switchdatetime;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TimePicker;

/**
 * A class for solve listener with AM-PM click <br />
 * Thanks to Velval for init code
 * @author J-Jamet
 * @version 1.0
 */
public class AMPMTimePicker extends TimePicker {

    private static final String TAG = "AMPMTimePicker";
    private OnTimeChangedListener onTimeChangedListener;

    public AMPMTimePicker(Context context) {
        super(context);
    }

    public AMPMTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AMPMTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AMPMTimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Stop ScrollView from getting involved once you interact with the View
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            ViewParent p = getParent();
            if (p != null)
                p.requestDisallowInterceptTouchEvent(true);
        }
        return false;
    }

    @Override
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        super.setOnTimeChangedListener(onTimeChangedListener);
        this.onTimeChangedListener = onTimeChangedListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    @SuppressWarnings("deprecation")
    private void init() {
        try {
            ViewGroup amPmView;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                // LinearLayout (LOLLIPOP)
                // GridLayout (M-LANDSCAPE)
                // LinearLayout (M-PORTRAIT)
                ViewGroup v1 = (ViewGroup) getChildAt(0);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

                    // FrameLayout (LOLLIPOP-LANDSCAPE)
                    // FrameLayout - id:time_header (LOLLIPOP-PORTRAIT)
                    ViewGroup v2 = (ViewGroup) v1.getChildAt(0);

                    // FrameLayout - id:TimeHeader (LOLLIPOP-LANDSCAPE)
                    // LinearLayout (LOLLIPOP-PORTRAIT)
                    ViewGroup v3 = (ViewGroup) v2.getChildAt(0);

                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        ViewGroup v4 = (ViewGroup) v3.getChildAt(0); // LinearLayout (LOLLIPOP)
                        amPmView = (ViewGroup) v4.getChildAt(3); // LinearLayout - id:ampm_layout (LOLLIPOP)
                    } else { // PORTRAIT
                        amPmView = (ViewGroup) v3.getChildAt(3); // LinearLayout - id:ampm_layout (LOLLIPOP)
                    }
                } else { // M and after
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        ViewGroup v2 = (ViewGroup) v1.getChildAt(1); // RelativeLayout (M)
                        amPmView = (ViewGroup) v2.getChildAt(1); // LinearLayout - id:ampm_layout (M)
                    } else {
                        ViewGroup v2 = (ViewGroup) v1.getChildAt(0); // RelativeLayout - id:time_header (M)
                        amPmView = (ViewGroup) v2.getChildAt(3); // LinearLayout - id:ampm_layout (M)
                    }
                }

                View am = amPmView.getChildAt(0); // AppCompatCheckedTextView - id:am_label
                View pm = amPmView.getChildAt(1); // AppCompatCheckedTextView - id:pm_label

                View.OnTouchListener listener = new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int hour;
                        int minute;
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            hour = getCurrentHour();
                            minute = getCurrentMinute();
                        } else {
                            hour = getHour();
                            minute = getMinute();
                        }
                        hour = (hour >= 12) ? hour - 12 : hour + 12;
                        onTimeChangedListener.onTimeChanged(AMPMTimePicker.this, hour, minute);
                        return false;
                    }
                };
                am.setOnTouchListener(listener);
                pm.setOnTouchListener(listener);
            }
        } catch (Exception e) {
            Log.e(TAG, "TimePicker is not defined for this Android version : " + e.getMessage());
        }
    }
}