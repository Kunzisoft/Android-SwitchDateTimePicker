package com.kunzisoft.switchdatetime;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.kunzisoft.switchdatetime.date.OnYearSelectedListener;
import com.kunzisoft.switchdatetime.date.widget.ListPickerYearView;
import com.kunzisoft.switchdatetime.time.SwitchTimePicker;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A fragment that displays a dialog window with Date and Time who can be selected by switch button
 * @author J-Jamet
 */
public class SwitchDateTimeDialogFragment extends DialogFragment {

    private static final String TAG = "SwitchDateTimeDialogFrg";

    private static final String STATE_DATETIME = "STATE_DATETIME";
    private Calendar dateTimeCalendar = Calendar.getInstance();
    private Calendar minimumDateTime = new GregorianCalendar(1970, 1, 1);
    private Calendar maximumDateTime = new GregorianCalendar(2200, 1, 1);

    private static final String TAG_LABEL = "LABEL";
    private static final String TAG_POSITIVE_BUTTON = "POSITIVE_BUTTON";
    private static final String TAG_NEGATIVE_BUTTON = "NEGATIVE_BUTTON";

    private String mLabel;
    private String mPositiveButton;
    private String mNegativeButton;
    private OnButtonClickListener mListener;

    private final static int UNDEFINED_TIME_VALUE = -1;
    private int year = UNDEFINED_TIME_VALUE;
    private int month = UNDEFINED_TIME_VALUE;
    private int day = UNDEFINED_TIME_VALUE;
    private int hourOfDay = UNDEFINED_TIME_VALUE;
    private int minute = UNDEFINED_TIME_VALUE;

    private boolean is24HoursMode = false;
    private int startAtPosition = 0;

    private SimpleDateFormat dayAndMonthSimpleDate;
    private SimpleDateFormat yearSimpleDate;

    private ViewAnimator viewSwitcher;
    private SwitchTimePicker timePicker;
    private MaterialCalendarView materialCalendarView;
    private ListPickerYearView listPickerYearView;

    private TextView monthAndDayHeaderValues;
    private TextView yearHeaderValues;

    private boolean blockAnimationIn;
    private boolean blockAnimationOut;

    /**
     * Create a new instance of SwitchDateTimeDialogFragment
     * @param label Title of dialog
     * @param positiveButton Text for positive button
     * @param negativeButton Text for negative button
     * @return DialogFragment
     */
    public static SwitchDateTimeDialogFragment newInstance(String label, String positiveButton, String negativeButton) {
        SwitchDateTimeDialogFragment switchDateTimeDialogFragment = new SwitchDateTimeDialogFragment();
        // Add arguments
        Bundle args = new Bundle();
        args.putString(TAG_LABEL, label);
        args.putString(TAG_POSITIVE_BUTTON, positiveButton);
        args.putString(TAG_NEGATIVE_BUTTON, negativeButton);
        switchDateTimeDialogFragment.setArguments(args);

        return switchDateTimeDialogFragment;
    }

    /**
     * Set listener for actions
     * @param onButtonClickListener Listener for click
     */
    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.mListener = onButtonClickListener;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current datetime
        savedInstanceState.putLong(STATE_DATETIME, dateTimeCalendar.getTimeInMillis());
        timePicker.onSaveInstanceState(savedInstanceState);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        if(getArguments() != null) {
            mLabel = getArguments().getString(TAG_LABEL);
            mPositiveButton = getArguments().getString(TAG_POSITIVE_BUTTON);
            mNegativeButton = getArguments().getString(TAG_NEGATIVE_BUTTON);
        }

        if (savedInstanceState != null) {
            // Restore value from saved state
            dateTimeCalendar.setTime(new Date(savedInstanceState.getLong(STATE_DATETIME)));
        }

        // Init values with current time if setDefault is not used
        if(year == UNDEFINED_TIME_VALUE)
            year = dateTimeCalendar.get(Calendar.YEAR);
        if(month == UNDEFINED_TIME_VALUE)
            month = dateTimeCalendar.get(Calendar.MONTH);
        if(day == UNDEFINED_TIME_VALUE)
            day = dateTimeCalendar.get(Calendar.DAY_OF_MONTH);
        if(hourOfDay == UNDEFINED_TIME_VALUE)
            hourOfDay = dateTimeCalendar.get(Calendar.HOUR_OF_DAY);
        if(minute == UNDEFINED_TIME_VALUE)
            minute = dateTimeCalendar.get(Calendar.MINUTE);
        assignAllValuesToCalendar();

        // Throw exception if default select date isn't between minimumDateTime and maximumDateTime
        if(dateTimeCalendar.before(minimumDateTime) || dateTimeCalendar.after(maximumDateTime))
            throw new RuntimeException("Default date " + dateTimeCalendar.getTime() + " must be between "
                    + minimumDateTime.getTime() + " and " + maximumDateTime.getTime());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        getActivity().getTheme().applyStyle(R.style.Theme_SwitchDateTime, false);
        //getActivity().setTheme(R.style.Theme_SwitchDateTime);
        View dateTimeLayout = inflater.inflate(R.layout.dialog_switch_datetime_picker,
                (ViewGroup) getActivity().findViewById(R.id.datetime_picker));

        // Set label
        TextView labelView = (TextView) dateTimeLayout.findViewById(R.id.label);
        if(mLabel != null)
            labelView.setText(mLabel);
        else
            labelView.setText(getString(R.string.label_datetime_dialog));

        // Lock animation for fast clicks
        blockAnimationIn = false;
        blockAnimationOut = false;
        viewSwitcher = (ViewAnimator) dateTimeLayout.findViewById(R.id.dateSwitcher);
        viewSwitcher.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                blockAnimationIn = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                blockAnimationIn = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        viewSwitcher.getOutAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                blockAnimationOut = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                blockAnimationOut = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        viewSwitcher.setDisplayedChild(startAtPosition);

        // Button for switch between Hours/Minutes, Calendar and YearList
        ImageButton buttonSwitch = (ImageButton) dateTimeLayout.findViewById(R.id.button_switch);
        buttonSwitch.setBackgroundColor(Color.TRANSPARENT);
        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.animLabelElement(view);
                if(!(blockAnimationIn && blockAnimationOut))
                    viewSwitcher.showNext();
            }
        });

        // Values header hourOfDay minutes
        View timeHeaderValues = dateTimeLayout.findViewById(R.id.time_header_values);
        View.OnClickListener onTimeClickListener =
                new OnClickHeaderElementListener(HeaderViewsPosition.VIEW_HOURS_AND_MINUTES.getPosition());
        timeHeaderValues.setOnClickListener(onTimeClickListener);
        // Values header month day
        monthAndDayHeaderValues = (TextView) dateTimeLayout.findViewById(R.id.date_picker_month_and_day);
        View.OnClickListener onMonthAndDayClickListener =
                new OnClickHeaderElementListener(HeaderViewsPosition.VIEW_MONTH_AND_DAY.getPosition());
        monthAndDayHeaderValues.setOnClickListener(onMonthAndDayClickListener);
        // Values header year
        yearHeaderValues = (TextView) dateTimeLayout.findViewById(R.id.date_picker_year);
        View.OnClickListener onYearClickListener =
                new OnClickHeaderElementListener(HeaderViewsPosition.VIEW_YEAR.getPosition());
        yearHeaderValues.setOnClickListener(onYearClickListener);

        // Init simple date format if null
        if(dayAndMonthSimpleDate == null)
            dayAndMonthSimpleDate = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        if(yearSimpleDate == null)
            yearSimpleDate = new SimpleDateFormat("yyyy", Locale.getDefault());

        // Init headers
        yearHeaderValues.setText(String.valueOf(year));
        monthAndDayHeaderValues.setText(dayAndMonthSimpleDate.format(dateTimeCalendar.getTime()));

        // Construct TimePicker
        SwitchTimePicker.OnTimeSelectedListener onTimeSelectedListener = new SwitchTimePicker.OnTimeSelectedListener() {
            @Override
            public void onTimeSelected(int hourOfDayTime, int minuteTime) {
                hourOfDay = hourOfDayTime;
                minute = minuteTime;
                dateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDayTime);
                dateTimeCalendar.set(Calendar.MINUTE, minuteTime);
            }
        };
        // Init time with saved elements
        if(savedInstanceState == null)
            timePicker = new SwitchTimePicker(getContext(), onTimeSelectedListener);
        else
            timePicker = new SwitchTimePicker(getContext(), onTimeSelectedListener, savedInstanceState);
        timePicker.setIs24HourMode(is24HoursMode);
        timePicker.setHourOfDay(hourOfDay);
        timePicker.setMinute(minute);
        timePicker.onCreateView(dateTimeLayout, savedInstanceState);
        timePicker.setOnClickTimeListener(onTimeClickListener);

        // Construct DatePicker
        materialCalendarView = (MaterialCalendarView) dateTimeLayout.findViewById(com.kunzisoft.switchdatetime.R.id.datePicker);
        materialCalendarView.state().edit()
                .setMinimumDate(CalendarDay.from(minimumDateTime))
                .setMaximumDate(CalendarDay.from(maximumDateTime))
                .commit();
        materialCalendarView.setCurrentDate(dateTimeCalendar.getTime());
        materialCalendarView.setDateSelected(dateTimeCalendar, true);
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay calendarDay, boolean selected) {
                Date currentDate = calendarDay.getDate();
                year = calendarDay.getYear();
                month = calendarDay.getMonth();
                day = calendarDay.getDay();

                dateTimeCalendar.set(Calendar.YEAR, year);
                dateTimeCalendar.set(Calendar.MONTH, month);
                dateTimeCalendar.set(Calendar.DAY_OF_MONTH, day);
                listPickerYearView.assignCurrentYear(year);

                yearHeaderValues.setText(String.valueOf(year));
                monthAndDayHeaderValues.setText(dayAndMonthSimpleDate.format(currentDate));
            }
        });
        materialCalendarView.invalidate();

        // Construct YearPicker
        listPickerYearView = (ListPickerYearView) dateTimeLayout.findViewById(R.id.yearPicker);
        listPickerYearView.setMinYear(minimumDateTime.get(Calendar.YEAR));
        listPickerYearView.setMaxYear(maximumDateTime.get(Calendar.YEAR));
        listPickerYearView.assignCurrentYear(year);
        listPickerYearView.setDatePickerListener(new OnYearSelectedListener() {
            @Override
            public void onYearSelected(View view, int yearPicker) {
                year = yearPicker;

                dateTimeCalendar.set(Calendar.YEAR, year);
                yearHeaderValues.setText(String.valueOf(year));

                // Unfortunately, we have lags here and thread isn't a solution :/
                materialCalendarView.setCurrentDate(dateTimeCalendar.getTime());
                materialCalendarView.setDateSelected(dateTimeCalendar, true);
                // For resolve bug of switch year
                materialCalendarView.goToNext();
                materialCalendarView.goToPrevious();
            }
        });

        // Assign buttons
        AlertDialog.Builder db = new AlertDialog.Builder(getContext());
        db.setView(dateTimeLayout);
        if(mPositiveButton == null)
            mPositiveButton = getString(R.string.positive_button_datetime_picker);
        db.setPositiveButton(mPositiveButton, new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(mListener !=null) {
                            assignAllValuesToCalendar();
                            mListener.onPositiveButtonClick(dateTimeCalendar.getTime());
                        }
                    }
                });
        if(mNegativeButton == null)
            mNegativeButton = getString(R.string.negative_button_datetime_picker);
        db.setNegativeButton(mNegativeButton, new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Close dialog
                        if(mListener !=null) {
                            assignAllValuesToCalendar();
                            mListener.onNegativeButtonClick(dateTimeCalendar.getTime());
                        }
                    }
                });

        return db.create();
    }

    /**
     * Assign each value of time in calendar
     */
    private void assignAllValuesToCalendar() {
        dateTimeCalendar.set(Calendar.YEAR, year);
        dateTimeCalendar.set(Calendar.MONTH, month);
        dateTimeCalendar.set(Calendar.DAY_OF_MONTH, day);
        dateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateTimeCalendar.set(Calendar.MINUTE, minute);
        dateTimeCalendar.set(Calendar.SECOND, 0);
    }

    /**
     * Define "Time" as the first view to show
     */
    public void startAtTimeView() {
        startAtPosition = 0;
    }

    /**
     * Define "Calendar" as the first view to show
     */
    public void startAtCalendarView() {
        startAtPosition = 1;
    }

    /**
     * Define "Year" as the first view to show
     */
    public void startAtYearView() {
        startAtPosition = 2;
    }

    /**
     * Assign default year at start
     * @param year
     */
    public void setDefaultYear(int year) {
        this.year = year;
    }

    @Deprecated
    /**
     * @deprecated Does not change after launch
     * {@link #setDefaultYear(int)}
     */
    public void setYear(int year) {
        setDefaultYear(year);
    }

    /**
     * Assign default month at start (ex: Calendar.DECEMBER)
     * @see Calendar
     * @param month
     */
    public void setDefaultMonth(int month) {
        this.month = month;
    }

    @Deprecated
    /**
     * @deprecated Does not change after launch
     * {@link #setDefaultMonth(int)}
     */
    public void setMonth(int month) {
        setDefaultMonth(month);
    }

    /**
     * Assign default day at start
     * @param day
     */
    public void setDefaultDay(int day) {
        this.day = day;
    }

    @Deprecated
    /**
     * @deprecated Does not change after launch
     * {@link #setDefaultDay(int)}
     */
    public void setDay(int day) {
        setDefaultDay(day);
    }

    /**
     * Assign default hour of day (in 24 hours) at start
     * @param hourOfDay
     */
    public void setDefaultHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    @Deprecated
    /**
     * @deprecated Does not change after launch and 24 hours format
     * {@link #setDefaultHourOfDay(int)}
     */
    public void setHour(int hour) {
        setDefaultHourOfDay(hour);
    }

    /**
     * Assign default minute at start
     * @param minute
     */
    public void setDefaultMinute(int minute) {
        this.minute = minute;
    }

    @Deprecated
    /**
     * @deprecated Does not change after launch
     * {@link #setDefaultMinute(int)}
     */
    public void setMinute(int minute) {
        setDefaultMinute(minute);
    }

    /**
     * Get current year
     * @return
     */
    public int getYear() {
        return year;
    }

    /**
     * Get current month as Calendar.MONTH
     * @see Calendar
     * @return
     */
    public int getMonth() {
        return month;
    }

    /**
     * Get current day
     * @return
     */
    public int getDay() {
        return day;
    }

    /**
     * Get current hour of day (hour in 24 hours)
     * @return
     */
    public int getHourOfDay() {
        return hourOfDay;
    }

    /**
     * Get current minute
     * @return
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Assign default DateTime at start
     * @param date
     */
    public void setDefaultDateTime(Date date) {
        this.dateTimeCalendar.setTime(date);
    }

    /**
     * Assign minimum DateTime who can be selected
     * @param date
     */
    public void setMinimumDateTime(Date date) {
        this.minimumDateTime.setTime(date);
    }

    /**
     * Assign maximum DateTime who can be selected
     * @param date
     */
    public void setMaximumDateTime(Date date) {
        this.maximumDateTime.setTime(date);
    }

    /**
     * Get minimum DateTime who can be selected
     * @return
     */
    public Date getMinimumDateTime() {
        return minimumDateTime.getTime();
    }

    /**
     * Get maximum DateTime who can be selected
     * @return
     */
    public Date getMaximumDateTime() {
        return maximumDateTime.getTime();
    }

    /**
     * Return default SimpleDateFormat for Month and Day
     * @return
     */
    public SimpleDateFormat getSimpleDateMonthAndDayFormat() {
        return dayAndMonthSimpleDate;
    }

    /**
     * Assign a SimpleDateFormat like "d MMM" to show formatted DateTime
     * @param simpleDateFormat
     */
    public void setSimpleDateMonthAndDayFormat(SimpleDateFormat simpleDateFormat) throws SimpleDateMonthAndDayFormatException{
        Pattern patternMonthAndDay = Pattern.compile("(M|w|W|D|d|F|E|u|\\s)*");
        Matcher matcherMonthAndDay = patternMonthAndDay.matcher(simpleDateFormat.toPattern());
        if(!matcherMonthAndDay.matches()) {
            throw new SimpleDateMonthAndDayFormatException(simpleDateFormat.toPattern() + "isn't allowed for " + patternMonthAndDay.pattern());
        }
        this.dayAndMonthSimpleDate = simpleDateFormat;
    }

    /**
     * Define if time miust be in 24 hours mode or in 12 hours, must be applied before "show"
     * @param is24HoursMode
     */
    public void set24HoursMode(boolean is24HoursMode) {
        this.is24HoursMode = is24HoursMode;
    }

    /**
     * Class exception if SimpleDateFormat contains something else that "d" or/and "M"
     */
    public class SimpleDateMonthAndDayFormatException extends Exception {
        SimpleDateMonthAndDayFormatException(String message) {
            super(message);
        }
    }

    /**
     * Callback class for assign action on positive and negative button
     */
    public interface OnButtonClickListener {
        void onPositiveButtonClick(Date date);
        void onNegativeButtonClick(Date date);
    }

    /**
     * Enumeration of header views
     */
    public enum HeaderViewsPosition {
        VIEW_HOURS_AND_MINUTES(0), VIEW_MONTH_AND_DAY(1), VIEW_YEAR(2);

        private int positionSwitch;

        HeaderViewsPosition(int position) {
            this.positionSwitch = position;
        }

        public int getPosition() {
            return positionSwitch;
        }
    }

    /**
     * Listener for click on Header element
     */
    public class OnClickHeaderElementListener implements View.OnClickListener{
        private int positionView;

        OnClickHeaderElementListener(int positionView) {
            this.positionView = positionView;
        }

        @Override
        public void onClick(View view) {
            Utils.animLabelElement(view);
            if(viewSwitcher.getDisplayedChild() != positionView)
                viewSwitcher.setDisplayedChild(positionView);
        }
    }
}

