package com.kunzisoft.switchdatetime.date;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.format.DateUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fourmob.datetimepicker.R;
import com.fourmob.datetimepicker.Utils;
import com.fourmob.datetimepicker.date.AccessibleDateAnimator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;


/**
 * Thanks to FlavienLaurent and Writtmeyer
 */
public class SwitchDatePicker implements View.OnClickListener, DatePickerController {

    private Context mContext;

    private static final String KEY_SELECTED_YEAR = "year";
    private static final String KEY_SELECTED_MONTH = "month";
    private static final String KEY_SELECTED_DAY = "day";
    private static final String KEY_VIBRATE = "vibrate";

    // https://code.google.com/p/android/issues/detail?id=13050
    private static final int MAX_YEAR = 2037;
    private static final int MIN_YEAR = 1902;

    private static final int UNINITIALIZED = -1;
    private static final int MONTH_AND_DAY_VIEW = 0;
    private static final int YEAR_VIEW = 1;

    public static final int ANIMATION_DELAY = 500;
    public static final String KEY_WEEK_START = "week_start";
    public static final String KEY_YEAR_START = "year_start";
    public static final String KEY_YEAR_END = "year_end";
    public static final String KEY_CURRENT_VIEW = "current_view";
    public static final String KEY_LIST_POSITION = "list_position";
    public static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";

    private static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd", Locale.getDefault());
    private static SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.getDefault());
    private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();

    private final Calendar mCalendar = Calendar.getInstance();
    private HashSet<OnDateChangedListener> mListeners = new HashSet<OnDateChangedListener>();
    private OnDateSetListener mCallBack;

    private AccessibleDateAnimator mAnimator;
    private boolean mDelayAnimation = true;
    private long mLastVibrate;
    private int mCurrentView = UNINITIALIZED;

    private int mWeekStart = mCalendar.getFirstDayOfWeek();
    private int mMaxYear = MAX_YEAR;
    private int mMinYear = MIN_YEAR;

    private String mDayPickerDescription;
    private String mYearPickerDescription;
    private String mSelectDay;
    private String mSelectYear;

    private TextView mDayOfWeekView;
    private DayPickerView mDayPickerView;
    private LinearLayout mMonthAndDayView;
    private TextView mSelectedDayTextView;
    private TextView mSelectedMonthTextView;
    private Vibrator mVibrator;
    private YearPickerView mYearPickerView;
    private TextView mYearView;

    private boolean mVibrate = true;
    private boolean mCloseOnSingleTapDay;

    private void adjustDayInMonthIfNeeded(int month, int year) {
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = Utils.getDaysInMonth(month, year);
        if (day > daysInMonth) {
            mCalendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
        }
    }

    /**
     * TODO constructor SwitchDatePicker
     * @param context
     * @param onDateSetListener
     * @param year
     * @param month
     * @param day
     * @param vibrate
     */
    public SwitchDatePicker(Context context, OnDateSetListener onDateSetListener, int year, int month, int day, boolean vibrate) {
        mContext = context;
        if (year > MAX_YEAR)
            throw new IllegalArgumentException("year end must < " + MAX_YEAR);
        if (year < MIN_YEAR)
            throw new IllegalArgumentException("year end must > " + MIN_YEAR);
        mCallBack = onDateSetListener;
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        mVibrate = vibrate;
    }


    public void setVibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    private void setCurrentView(int currentView) {
        setCurrentView(currentView, false);
    }

    private void setCurrentView(int currentView, boolean forceRefresh) {
        long timeInMillis = mCalendar.getTimeInMillis();
        switch (currentView) {
            case MONTH_AND_DAY_VIEW:
                ObjectAnimator monthDayAnim = Utils.getPulseAnimator(mMonthAndDayView, 0.9F, 1.05F);
                if (mDelayAnimation) {
                    monthDayAnim.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mDayPickerView.onDateChanged();
                if (mCurrentView != currentView || forceRefresh) {
                    mMonthAndDayView.setSelected(true);
                    mYearView.setSelected(false);
                    mAnimator.setDisplayedChild(MONTH_AND_DAY_VIEW);
                    mCurrentView = currentView;
                }
                monthDayAnim.start();
                String monthDayDesc = DateUtils.formatDateTime(mContext, timeInMillis, DateUtils.FORMAT_SHOW_DATE);
                mAnimator.setContentDescription(mDayPickerDescription + ": " + monthDayDesc);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay);
                break;
            case YEAR_VIEW:
                ObjectAnimator yearAnim = Utils.getPulseAnimator(mYearView, 0.85F, 1.1F);
                if (mDelayAnimation) {
                    yearAnim.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mYearPickerView.onDateChanged();
                if (mCurrentView != currentView  || forceRefresh) {
                    mMonthAndDayView.setSelected(false);
                    mYearView.setSelected(true);
                    mAnimator.setDisplayedChild(YEAR_VIEW);
                    mCurrentView = currentView;
                }
                yearAnim.start();
                String dayDesc = YEAR_FORMAT.format(timeInMillis);
                mAnimator.setContentDescription(mYearPickerDescription + ": " + dayDesc);
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear);
                break;
        }
    }

    private void updateDisplay(boolean announce) {
        /*if (mDayOfWeekView != null) {
            mDayOfWeekView.setText(mCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
                    Locale.getDefault()).toUpperCase(Locale.getDefault()));
        }
        mSelectedMonthTextView.setText(mCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
                Locale.getDefault()).toUpperCase(Locale.getDefault()));*/

        if (this.mDayOfWeekView != null){
            this.mCalendar.setFirstDayOfWeek(mWeekStart);
            this.mDayOfWeekView.setText(mDateFormatSymbols.getWeekdays()[this.mCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault()));
        }

        this.mSelectedMonthTextView.setText(mDateFormatSymbols.getMonths()[this.mCalendar.get(Calendar.MONTH)].toUpperCase(Locale.getDefault()));

        mSelectedDayTextView.setText(DAY_FORMAT.format(mCalendar.getTime()));
        mYearView.setText(YEAR_FORMAT.format(mCalendar.getTime()));

        // Accessibility.
        long millis = mCalendar.getTimeInMillis();
        mAnimator.setDateMillis(millis);
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
        String monthAndDayText = DateUtils.formatDateTime(mContext, millis, flags);
        mMonthAndDayView.setContentDescription(monthAndDayText);

        if (announce) {
            flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
            String fullDateText = DateUtils.formatDateTime(mContext, millis, flags);
            Utils.tryAccessibilityAnnounce(mAnimator, fullDateText);
        }
    }

    private void updatePickers() {
        Iterator<OnDateChangedListener> iterator = mListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().onDateChanged();
        }
    }

    public int getFirstDayOfWeek() {
        return mWeekStart;
    }

    public int getMaxYear() {
        return mMaxYear;
    }

    public int getMinYear() {
        return mMinYear;
    }

    public SimpleMonthAdapter.CalendarDay getSelectedDay() {
        return new SimpleMonthAdapter.CalendarDay(mCalendar);
    }

    public void onClick(View view) {
        tryVibrate();
        if (view.getId() == R.id.date_picker_year)
            setCurrentView(YEAR_VIEW);
        else if (view.getId() == R.id.date_picker_month_and_day)
            setCurrentView(MONTH_AND_DAY_VIEW);
    }

    /**
     * TODO
     * */
    public View onCreateView(View view, Bundle savedInstanceState) {

        mDayOfWeekView = ((TextView) view.findViewById(R.id.date_picker_header));
        mMonthAndDayView = ((LinearLayout) view.findViewById(R.id.date_picker_month_and_day));
        mMonthAndDayView.setOnClickListener(this);
        mSelectedMonthTextView = ((TextView) view.findViewById(R.id.date_picker_month));
        mSelectedDayTextView = ((TextView) view.findViewById(R.id.date_picker_day));
        mYearView = ((TextView) view.findViewById(R.id.date_picker_year));
        mYearView.setOnClickListener(this);

        int listPosition = -1;
        int currentView = MONTH_AND_DAY_VIEW;
        int listPositionOffset = 0;
        if (savedInstanceState != null) {
            mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
            mMinYear = savedInstanceState.getInt(KEY_YEAR_START);
            mMaxYear = savedInstanceState.getInt(KEY_YEAR_END);
            currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
            listPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
            listPositionOffset = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET);
        }

        mDayPickerView = new DayPickerView(mContext, this);
        mYearPickerView = new YearPickerView(mContext, this);

        Resources resources = mContext.getResources();
        mDayPickerDescription = resources.getString(R.string.day_picker_description);
        mSelectDay = resources.getString(R.string.select_day);
        mYearPickerDescription = resources.getString(R.string.year_picker_description);
        mSelectYear = resources.getString(R.string.select_year);

        mAnimator = ((AccessibleDateAnimator) view.findViewById(R.id.animator));
        mAnimator.addView(mDayPickerView);
        mAnimator.addView(mYearPickerView);
        mAnimator.setDateMillis(mCalendar.getTimeInMillis());

        AlphaAnimation inAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        inAlphaAnimation.setDuration(300L);
        mAnimator.setInAnimation(inAlphaAnimation);

        AlphaAnimation outAlphaAnimation = new AlphaAnimation(1.0F, 0.0F);
        outAlphaAnimation.setDuration(300L);
        mAnimator.setOutAnimation(outAlphaAnimation);

        updateDisplay(false);
        setCurrentView(currentView, true);

        if (listPosition != -1) {
            if (currentView == MONTH_AND_DAY_VIEW) {
                mDayPickerView.postSetSelection(listPosition);
            }
            if (currentView == YEAR_VIEW) {
                mYearPickerView.postSetSelectionFromTop(listPosition, listPositionOffset);
            }
        }
        return view;
    }


    public void onDayOfMonthSelected(int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        updatePickers();
        updateDisplay(true);

        if(mCloseOnSingleTapDay) {
            //onDoneButtonClick();
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        bundle.putInt(KEY_SELECTED_YEAR, mCalendar.get(Calendar.YEAR));
        bundle.putInt(KEY_SELECTED_MONTH, mCalendar.get(Calendar.MONTH));
        bundle.putInt(KEY_SELECTED_DAY, mCalendar.get(Calendar.DAY_OF_MONTH));
        bundle.putInt(KEY_WEEK_START, mWeekStart);
        bundle.putInt(KEY_YEAR_START, mMinYear);
        bundle.putInt(KEY_YEAR_END, mMaxYear);
        bundle.putInt(KEY_CURRENT_VIEW, mCurrentView);

        int listPosition = -1;
        if (mCurrentView == 0) {
            listPosition = mDayPickerView.getMostVisiblePosition();
        } if (mCurrentView == 1) {
            listPosition = mYearPickerView.getFirstVisiblePosition();
            bundle.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
        }
        bundle.putInt(KEY_LIST_POSITION, listPosition);
        bundle.putBoolean(KEY_VIBRATE, mVibrate);
    }

    public void onYearSelected(int year) {
        adjustDayInMonthIfNeeded(mCalendar.get(Calendar.MONTH), year);
        mCalendar.set(Calendar.YEAR, year);
        updatePickers();
        setCurrentView(MONTH_AND_DAY_VIEW);
        updateDisplay(true);
    }

    public void registerOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        mListeners.add(onDateChangedListener);
    }

    public void setFirstDayOfWeek(int startOfWeek) {
        if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and " +
                    "Calendar.SATURDAY");
        }
        mWeekStart = startOfWeek;
        if (mDayPickerView != null) {
            mDayPickerView.onChange();
        }
    }

    public void setOnDateSetListener(OnDateSetListener onDateSetListener) {
        mCallBack = onDateSetListener;
    }

    public void setYearRange(int minYear, int maxYear) {
        if (maxYear < minYear)
            throw new IllegalArgumentException("Year end must be larger than year start");
        if (maxYear > MAX_YEAR)
            throw new IllegalArgumentException("max year end must < " + MAX_YEAR);
        if (minYear < MIN_YEAR)
            throw new IllegalArgumentException("min year end must > " + MIN_YEAR);
        mMinYear = minYear;
        mMaxYear = maxYear;
        if (mDayPickerView != null)
            mDayPickerView.onChange();
    }

    public void tryVibrate() {
        if (mVibrator != null && mVibrate) {
            long timeInMillis = SystemClock.uptimeMillis();
            if (timeInMillis - mLastVibrate >= 125L) {
                mVibrator.vibrate(5L);
                mLastVibrate = timeInMillis;
            }
        }
    }

    public void setCloseOnSingleTapDay(boolean closeOnSingleTapDay) {
        mCloseOnSingleTapDay = closeOnSingleTapDay;
    }

    interface OnDateChangedListener {
        void onDateChanged();
    }

    public interface OnDateSetListener {
        void onDateSet(SwitchDatePicker switchDatePicker, int year, int month, int day);
    }
}