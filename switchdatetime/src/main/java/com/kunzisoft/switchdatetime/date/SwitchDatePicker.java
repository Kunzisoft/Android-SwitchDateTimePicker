package com.kunzisoft.switchdatetime.date;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.fourmob.datetimepicker.R;
import com.fourmob.datetimepicker.Utils;
import com.nineoldandroids.animation.ObjectAnimator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;


/**
 * Thanks to FlavienLaurent and Writtmeyer
 */
public class SwitchDatePicker implements View.OnClickListener, DatePickerListener {

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
    private HashSet<OnDateChangedListener> mListeners = new HashSet<>();
    private OnDateSetListener mCallBack;
    private View.OnClickListener onMonthAndDayClickListener;
    private View.OnClickListener onYearClickListener;

    private boolean mDelayAnimation = true;
    private long mLastVibrate;
    private int mCurrentView = UNINITIALIZED;

    private int mWeekStart = mCalendar.getFirstDayOfWeek();
    private int mMaxYear = MAX_YEAR;
    private int mMinYear = MIN_YEAR;

    private TextView mMonthAndDayView;
    private Vibrator mVibrator;
    private YearPickerView mYearPickerView;
    private TextView mYearView;

    private SimpleDateFormat mSimpleDateFormatMonthDay;

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
    public SwitchDatePicker(Context context, OnDateSetListener onDateSetListener, int year, int month, int day,
                            SimpleDateFormat simpleDateFormat, boolean vibrate) {
        mContext = context;
        if (year > MAX_YEAR)
            throw new IllegalArgumentException("year end must < " + MAX_YEAR);
        if (year < MIN_YEAR)
            throw new IllegalArgumentException("year end must > " + MIN_YEAR);
        mCallBack = onDateSetListener;
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        mSimpleDateFormatMonthDay = simpleDateFormat;
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
                if (mCurrentView != currentView || forceRefresh) {
                    mMonthAndDayView.setSelected(true);
                    mYearView.setSelected(false);
                    mCurrentView = currentView;
                }
                monthDayAnim.start();
                String monthDayDesc = DateUtils.formatDateTime(mContext, timeInMillis, DateUtils.FORMAT_SHOW_DATE);
                break;
            case YEAR_VIEW:
                ObjectAnimator yearAnim = Utils.getPulseAnimator(mYearView, 0.85F, 1.1F);
                if (mDelayAnimation) {
                    yearAnim.setStartDelay(ANIMATION_DELAY);
                    mDelayAnimation = false;
                }
                mYearPickerView.refreshAndCenter();
                if (mCurrentView != currentView  || forceRefresh) {
                    mMonthAndDayView.setSelected(false);
                    mYearView.setSelected(true);
                    mCurrentView = currentView;
                }
                yearAnim.start();
                String dayDesc = YEAR_FORMAT.format(timeInMillis);

                break;
        }
    }

    private void updateDisplay(boolean announce) {

        mMonthAndDayView.setText(mSimpleDateFormatMonthDay.format(mCalendar.getTime()));
        mYearView.setText(YEAR_FORMAT.format(mCalendar.getTime()));

        // Accessibility.
        long millis = mCalendar.getTimeInMillis();
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
        String monthAndDayText = DateUtils.formatDateTime(mContext, millis, flags);
        mMonthAndDayView.setContentDescription(monthAndDayText);

        if (announce) {
            flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
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

    @Override
    public int getYear() {
        // TODO Year
        return 1990;
    }

    public void onClick(View view) {
        tryVibrate();
        if (view.getId() == R.id.date_picker_year) {
            setCurrentView(YEAR_VIEW);
            if(onYearClickListener != null) {
                onYearClickListener.onClick(view);
            }
        }
        else if (view.getId() == R.id.date_picker_month_and_day) {
            setCurrentView(MONTH_AND_DAY_VIEW);
            if(onMonthAndDayClickListener != null) {
                onMonthAndDayClickListener.onClick(view);
            }
        }
    }

    /**
     * TODO
     * */
    public View onCreateView(View view, Bundle savedInstanceState) {

        mMonthAndDayView = ((TextView) view.findViewById(R.id.date_picker_month_and_day));
        mMonthAndDayView.setOnClickListener(this);
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

        mYearPickerView = new YearPickerView(mContext);
        mYearPickerView.setDatePickerListener(this);

        MaterialCalendarView materialCalendarView = (MaterialCalendarView) view.findViewById(com.kunzisoft.switchdatetime.R.id.datePicker);
        final TextView dateText = (TextView) view.findViewById(com.kunzisoft.switchdatetime.R.id.date_picker_month_and_day);

        // TODO inject first date
        /*
        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.WEDNESDAY)
                .setMinimumDate(CalendarDay.from(2014, 4, 3))
                .setMaximumDate(CalendarDay.from(2018, 5, 12))
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit();
                */
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                dateText.setText(mSimpleDateFormatMonthDay.format(date.getDate()));
                mCallBack.onDateSet(date.getYear(), date.getMonth(), date.getDay());
            }
        });

        updateDisplay(false);
        setCurrentView(currentView, true);

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

    @Override
    public void onYearChange(int year) {
        adjustDayInMonthIfNeeded(mCalendar.get(Calendar.MONTH), year);
        mCalendar.set(Calendar.YEAR, year);
        updatePickers();
        setCurrentView(MONTH_AND_DAY_VIEW);
        updateDisplay(true);
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
            // TODO remove
        } if (mCurrentView == 1) {
            listPosition = mYearPickerView.getFirstVisiblePosition();
            bundle.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset());
        }
        bundle.putInt(KEY_LIST_POSITION, listPosition);
        bundle.putBoolean(KEY_VIBRATE, mVibrate);
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
    }

    public void setOnMonthAndDayClickListener(View.OnClickListener onMonthAndDayClickListener) {
        this.onMonthAndDayClickListener = onMonthAndDayClickListener;
    }

    public void setOnYearlickListener(View.OnClickListener onYearlickListener) {
        this.onYearClickListener = onYearlickListener;
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
        void onDateSet(int year, int month, int day);
    }
}