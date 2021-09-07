package com.kunzisoft.switchdatetime;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewAnimator;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

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
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A fragment that displays a dialog window with Date and Time who can be selected by switch button
 *
 * @author J-Jamet
 */
public class SwitchDateTimeDialogFragment extends DialogFragment {

    private static final String TAG = "SwitchDateTimeDialogFrg";

    private static final String STATE_DATETIME = "STATE_DATETIME";
    private static final String STATE_CURRENT_POSITION = "STATE_CURRENT_POSITION";

    private static final int UNDEFINED_POSITION = -1;
    private Calendar dateTimeCalendar = Calendar.getInstance();
    private Calendar minimumDateTime = new GregorianCalendar(1970, 1, 1);
    private Calendar maximumDateTime = new GregorianCalendar(2200, 1, 1);
    private TimeZone timeZone = TimeZone.getDefault();

    private static final String TAG_LABEL = "LABEL";
    private static final String TAG_POSITIVE_BUTTON = "POSITIVE_BUTTON";
    private static final String TAG_NEGATIVE_BUTTON = "NEGATIVE_BUTTON";
    private static final String TAG_NEUTRAL_BUTTON = "NEUTRAL_BUTTON";
    private static final String TAG_DEFAULT_LOCALE = "DEFAULT_LOCALE";

    private static final String DEFAULT_LOCALE = "en";


    private String mLabel;
    private String mPositiveButton;
    private String mDefaultLocale;
    private String mNegativeButton;
    private String mNeutralButton;
    private OnButtonClickListener mListener;

    private boolean is24HoursMode = false;
    private boolean highlightAMPMSelection = false;
    private int startAtPosition = UNDEFINED_POSITION;
    private int currentPosition = 0;
    private int alertStyleId;

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
     *
     * @param label          Title of dialog
     * @param positiveButton Text for positive button
     * @param negativeButton Text for negative button
     * @return DialogFragment
     */
    public static SwitchDateTimeDialogFragment newInstance(String label, String positiveButton, String negativeButton) {
        return newInstance(label, positiveButton, negativeButton, null, DEFAULT_LOCALE);
    }

    /**
     * Create a new instance of SwitchDateTimeDialogFragment
     *
     * @param label          Title of dialog
     * @param positiveButton Text for positive button
     * @param negativeButton Text for negative button
     * @param defaultLocale Text for default locale
     * @return DialogFragment
     */
    public static SwitchDateTimeDialogFragment newInstance(String label, String positiveButton, String negativeButton, String neutralButton, String defaultLocale) {
        SwitchDateTimeDialogFragment switchDateTimeDialogFragment = new SwitchDateTimeDialogFragment();
        // Add arguments
        Bundle args = new Bundle();
        args.putString(TAG_LABEL, label);
        args.putString(TAG_POSITIVE_BUTTON, positiveButton);
        args.putString(TAG_NEGATIVE_BUTTON, negativeButton);
        args.putString(TAG_DEFAULT_LOCALE, defaultLocale);
        if (neutralButton != null) {
            args.putString(TAG_NEUTRAL_BUTTON, neutralButton);
        }
        switchDateTimeDialogFragment.setArguments(args);

        return switchDateTimeDialogFragment;
    }

    /**
     * Set listener for actions
     *
     * @param onButtonClickListener Listener for click
     */
    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.mListener = onButtonClickListener;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current datetime and position
        savedInstanceState.putLong(STATE_DATETIME, dateTimeCalendar.getTimeInMillis());
        savedInstanceState.putInt(STATE_CURRENT_POSITION, currentPosition);
        timePicker.onSaveInstanceState(savedInstanceState);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public @NonNull
    Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        assert getActivity() != null;
        assert getContext() != null;

        dateTimeCalendar.setTimeZone(timeZone);

        if (getArguments() != null) {
            mLabel = getArguments().getString(TAG_LABEL);
            mPositiveButton = getArguments().getString(TAG_POSITIVE_BUTTON);
            mNegativeButton = getArguments().getString(TAG_NEGATIVE_BUTTON);
            mNeutralButton = getArguments().getString(TAG_NEUTRAL_BUTTON);
            mDefaultLocale=getArguments().getString(TAG_DEFAULT_LOCALE);
        }
        setDefaultLocale(mDefaultLocale);

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(STATE_CURRENT_POSITION);
            dateTimeCalendar.setTime(new Date(savedInstanceState.getLong(STATE_DATETIME)));
        }

        // Throw exception if default select date isn't between minimumDateTime and maximumDateTime
        if (dateTimeCalendar.before(minimumDateTime) || dateTimeCalendar.after(maximumDateTime))
            throw new RuntimeException("Default date " + dateTimeCalendar.getTime() + " must be between "
                    + minimumDateTime.getTime() + " and " + maximumDateTime.getTime());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        getActivity().getTheme().applyStyle(R.style.Theme_SwitchDateTime, false);
        View dateTimeLayout = inflater.inflate(R.layout.dialog_switch_datetime_picker,
                (ViewGroup) getActivity().findViewById(R.id.datetime_picker));

        // Set label
        TextView labelView = dateTimeLayout.findViewById(R.id.label);
        if (mLabel != null)
            labelView.setText(mLabel);
        else
            labelView.setText(getString(R.string.label_datetime_dialog));

        // Lock animation for fast clicks
        blockAnimationIn = false;
        blockAnimationOut = false;
        viewSwitcher = dateTimeLayout.findViewById(R.id.dateSwitcher);
        viewSwitcher.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                blockAnimationIn = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                blockAnimationIn = false;
                currentPosition = viewSwitcher.getDisplayedChild();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
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
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // Defined the start position
        if (startAtPosition != UNDEFINED_POSITION)
            currentPosition = startAtPosition;
        viewSwitcher.setDisplayedChild(currentPosition);

        // Button for switch between Hours/Minutes, Calendar and YearList
        ImageButton buttonSwitch = dateTimeLayout.findViewById(R.id.button_switch);
        buttonSwitch.setBackgroundColor(Color.TRANSPARENT);
        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.animLabelElement(view);
                if (!(blockAnimationIn && blockAnimationOut))
                    viewSwitcher.showNext();
            }
        });

        // Values header hourOfDay minutes
        View timeHeaderValues = dateTimeLayout.findViewById(R.id.time_header_values);
        View.OnClickListener onTimeClickListener =
                new OnClickHeaderElementListener(HeaderViewsPosition.VIEW_HOURS_AND_MINUTES.getPosition());
        timeHeaderValues.setOnClickListener(onTimeClickListener);
        // Values header month day
        monthAndDayHeaderValues = dateTimeLayout.findViewById(R.id.date_picker_month_and_day);
        View.OnClickListener onMonthAndDayClickListener =
                new OnClickHeaderElementListener(HeaderViewsPosition.VIEW_MONTH_AND_DAY.getPosition());
        monthAndDayHeaderValues.setOnClickListener(onMonthAndDayClickListener);
        // Values header year
        yearHeaderValues = dateTimeLayout.findViewById(R.id.date_picker_year);
        View.OnClickListener onYearClickListener =
                new OnClickHeaderElementListener(HeaderViewsPosition.VIEW_YEAR.getPosition());
        yearHeaderValues.setOnClickListener(onYearClickListener);

        // Init simple date format if null
        if (dayAndMonthSimpleDate == null)
            dayAndMonthSimpleDate = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        if (yearSimpleDate == null)
            yearSimpleDate = new SimpleDateFormat("yyyy", Locale.getDefault());

        dayAndMonthSimpleDate.setTimeZone(timeZone);
        yearSimpleDate.setTimeZone(timeZone);

        // Init headers
        yearHeaderValues.setText(yearSimpleDate.format(dateTimeCalendar.getTime()));
        monthAndDayHeaderValues.setText(dayAndMonthSimpleDate.format(dateTimeCalendar.getTime()));

        // Construct TimePicker
        SwitchTimePicker.OnTimeSelectedListener onTimeSelectedListener = new SwitchTimePicker.OnTimeSelectedListener() {
            @Override
            public void onTimeSelected(int hourOfDayTime, int minuteTime) {
                dateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDayTime);
                dateTimeCalendar.set(Calendar.MINUTE, minuteTime);
            }
        };
        // Init time with saved elements
        timePicker = new SwitchTimePicker(getContext(), onTimeSelectedListener, savedInstanceState);
        timePicker.setIs24HourMode(is24HoursMode);
        timePicker.setHighlightAMPMSelection(highlightAMPMSelection);
        timePicker.setHourOfDay(dateTimeCalendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(dateTimeCalendar.get(Calendar.MINUTE));
        timePicker.onCreateView(dateTimeLayout, savedInstanceState);
        timePicker.setOnClickTimeListener(onTimeClickListener);

        // Construct DatePicker
        materialCalendarView = dateTimeLayout.findViewById(com.kunzisoft.switchdatetime.R.id.datePicker);
        materialCalendarView.state().edit()
                .setMinimumDate(CalendarDay.from(minimumDateTime))
                .setMaximumDate(CalendarDay.from(maximumDateTime))
                .commit();
        materialCalendarView.setCurrentDate(dateTimeCalendar);
        materialCalendarView.setDateSelected(dateTimeCalendar, true);
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay calendarDay, boolean selected) {
                dateTimeCalendar.set(Calendar.YEAR, calendarDay.getYear());
                dateTimeCalendar.set(Calendar.MONTH, calendarDay.getMonth());
                dateTimeCalendar.set(Calendar.DAY_OF_MONTH, calendarDay.getDay());
                listPickerYearView.assignCurrentYear(calendarDay.getYear());
                yearHeaderValues.setText(yearSimpleDate.format(dateTimeCalendar.getTime()));
                monthAndDayHeaderValues.setText(dayAndMonthSimpleDate.format(dateTimeCalendar.getTime()));
                timePicker.clickHour();
            }
        });
        materialCalendarView.invalidate();

        // Construct YearPicker
        listPickerYearView = dateTimeLayout.findViewById(R.id.yearPicker);
        listPickerYearView.setMinYear(minimumDateTime.get(Calendar.YEAR));
        listPickerYearView.setMaxYear(maximumDateTime.get(Calendar.YEAR));
        listPickerYearView.assignCurrentYear(dateTimeCalendar.get(Calendar.YEAR));
        listPickerYearView.setDatePickerListener(new OnYearSelectedListener() {
            @Override
            public void onYearSelected(View view, int yearPicker) {
                dateTimeCalendar.set(Calendar.YEAR, yearPicker);
                yearHeaderValues.setText(yearSimpleDate.format(dateTimeCalendar.getTime()));

                // Unfortunately, we have lags here and thread isn't a solution :/
                materialCalendarView.setCurrentDate(dateTimeCalendar.getTime().getTime());
                materialCalendarView.setDateSelected(dateTimeCalendar, true);
                // For resolve bug of switch year
                materialCalendarView.goToNext();
                materialCalendarView.goToPrevious();
            }
        });

        // Assign buttons
        AlertDialog.Builder db;
        if (alertStyleId != 0) {
            db = new AlertDialog.Builder(getContext(), alertStyleId);
        } else {
            db = new AlertDialog.Builder(getContext());
        }
        db.setView(dateTimeLayout);
        if (mPositiveButton == null)
            mPositiveButton = getString(android.R.string.ok);
        db.setPositiveButton(mPositiveButton, new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null) {
                            mListener.onPositiveButtonClick(dateTimeCalendar.getTime());
                        }
                    }
                });
        if (mNegativeButton == null)
            mNegativeButton = getString(android.R.string.cancel);
        db.setNegativeButton(mNegativeButton, new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Close dialog
                        if (mListener != null) {
                            mListener.onNegativeButtonClick(dateTimeCalendar.getTime());
                        }
                    }
                });
        if (mNeutralButton != null) {
            db.setNeutralButton(mNeutralButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mListener != null) {
                        if (mListener instanceof OnButtonWithNeutralClickListener)
                            ((OnButtonWithNeutralClickListener) mListener).onNeutralButtonClick(dateTimeCalendar.getTime());
                    }
                }
            });
        }
        return db.create();
    }

    private void setDefaultLocale(String mDefaultLocale) {
        Locale locale = new Locale(mDefaultLocale);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config,getResources().getDisplayMetrics());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        startAtPosition = UNDEFINED_POSITION;
    }

    /**
     * Define "Time" as the first view to show
     */
    public void startAtTimeView() {
        startAtPosition = HeaderViewsPosition.VIEW_HOURS_AND_MINUTES.getPosition();
    }

    /**
     * Define "Calendar" as the first view to show
     */
    public void startAtCalendarView() {
        startAtPosition = HeaderViewsPosition.VIEW_MONTH_AND_DAY.getPosition();
    }

    /**
     * Define "Year" as the first view to show
     */
    public void startAtYearView() {
        startAtPosition = HeaderViewsPosition.VIEW_YEAR.getPosition();
    }

    /**
     * Assign default year at start
     */
    public void setDefaultYear(int year) {
        this.dateTimeCalendar.set(Calendar.YEAR, year);
    }

    /**
     * @deprecated Does not change after launch
     * {@link #setDefaultYear(int)}
     */
    @Deprecated
    public void setYear(int year) {
        setDefaultYear(year);
    }

    /**
     * Assign default month at start (ex: Calendar.DECEMBER)
     *
     * @see Calendar
     */
    public void setDefaultMonth(int month) {
        this.dateTimeCalendar.set(Calendar.MONTH, month);
    }

    /**
     * @deprecated Does not change after launch
     * {@link #setDefaultMonth(int)}
     */
    @Deprecated
    public void setMonth(int month) {
        setDefaultMonth(month);
    }

    /**
     * Assign default day at start
     */
    public void setDefaultDay(int day) {
        this.dateTimeCalendar.set(Calendar.DAY_OF_MONTH, day);
    }

    /**
     * @deprecated Does not change after launch
     * {@link #setDefaultDay(int)}
     */
    @Deprecated
    public void setDay(int day) {
        setDefaultDay(day);
    }

    /**
     * Assign default hour of day (in 24 hours) at start
     */
    public void setDefaultHourOfDay(int hourOfDay) {
        this.dateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    }

    /**
     * @deprecated Does not change after launch and 24 hours format
     * {@link #setDefaultHourOfDay(int)}
     */
    @Deprecated
    public void setHour(int hour) {
        setDefaultHourOfDay(hour);
    }

    /**
     * Assign default minute at start
     */
    public void setDefaultMinute(int minute) {
        this.dateTimeCalendar.set(Calendar.MINUTE, minute);
    }

    /**
     * @deprecated Does not change after launch
     * {@link #setDefaultMinute(int)}
     */
    @Deprecated
    public void setMinute(int minute) {
        setDefaultMinute(minute);
    }

    /**
     * Get current year
     */
    public int getYear() {
        return this.dateTimeCalendar.get(Calendar.YEAR);
    }

    /**
     * Get current month as Calendar.MONTH
     *
     * @see Calendar
     */
    public int getMonth() {
        return this.dateTimeCalendar.get(Calendar.MONTH);
    }

    /**
     * Get current day
     */
    public int getDay() {
        return this.dateTimeCalendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get current hour of day (hour in 24 hours)
     */
    public int getHourOfDay() {
        return this.dateTimeCalendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Get current minute
     */
    public int getMinute() {
        return this.dateTimeCalendar.get(Calendar.MINUTE);
    }

    /**
     * Assign default DateTime at start
     */
    public void setDefaultDateTime(Date date) {
        this.dateTimeCalendar.setTime(date);
    }

    /**
     * Assign minimum DateTime who can be selected
     */
    public void setMinimumDateTime(Date date) {
        this.minimumDateTime.setTime(date);
    }

    /**
     * Assign maximum DateTime who can be selected
     */
    public void setMaximumDateTime(Date date) {
        this.maximumDateTime.setTime(date);
    }

    /**
     * Get minimum DateTime who can be selected
     */
    public Date getMinimumDateTime() {
        return minimumDateTime.getTime();
    }

    /**
     * Get maximum DateTime who can be selected
     */
    public Date getMaximumDateTime() {
        return maximumDateTime.getTime();
    }

    /**
     * Return default SimpleDateFormat for Month and Day
     */
    public SimpleDateFormat getSimpleDateMonthAndDayFormat() {
        return dayAndMonthSimpleDate;
    }

    /**
     * Assign a SimpleDateFormat like "d MMM" to show formatted DateTime
     *
     * @param simpleDateFormat Format to show month and day
     */
    public void setSimpleDateMonthAndDayFormat(SimpleDateFormat simpleDateFormat) throws SimpleDateMonthAndDayFormatException {
        Pattern patternMonthAndDay = Pattern.compile("(M|w|W|D|d|F|E|u|\\.|\\s)*");
        Matcher matcherMonthAndDay = patternMonthAndDay.matcher(simpleDateFormat.toPattern());
        if (!matcherMonthAndDay.matches()) {
            throw new SimpleDateMonthAndDayFormatException(simpleDateFormat.toPattern() + "isn't allowed for " + patternMonthAndDay.pattern());
        }
        this.dayAndMonthSimpleDate = simpleDateFormat;
    }

    /**
     * Define if time must be in 24 hours mode or in 12 hours, must be applied before "show"
     */
    public void set24HoursMode(boolean is24HoursMode) {
        this.is24HoursMode = is24HoursMode;
    }

    /**
     * Highlight AM or PM selected, by default AM or PM selected is not highlight. Only works if 24 hours mode is activated
     *
     * @param highlightAMPMSelection true to visually highlight selected item
     */
    public void setHighlightAMPMSelection(boolean highlightAMPMSelection) {
        this.highlightAMPMSelection = highlightAMPMSelection;
    }

    /**
     * Set timezone different from default
     */
    public void setTimeZone(TimeZone timeZone) {
        if (timeZone != null) {
            this.timeZone = timeZone;
        }
    }

    /**
     * Define if the AlertDialog must be styled, must be applied before "show"
     */
    public void setAlertStyle(@StyleRes int styleId) {
        this.alertStyleId = styleId;
    }

    /**
     * Class exception if SimpleDateFormat contains something else that "d" or/and "M"
     */
    public static class SimpleDateMonthAndDayFormatException extends Exception {
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
     * Callback class for assign action on positive, negative and neutral button
     */
    public interface OnButtonWithNeutralClickListener extends OnButtonClickListener {
        void onNeutralButtonClick(Date date);
    }

    /**
     * Enumeration of header views
     */
    public enum HeaderViewsPosition {
        VIEW_HOURS_AND_MINUTES(0), VIEW_MONTH_AND_DAY(1), VIEW_YEAR(2);

        private final int positionSwitch;

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
    public class OnClickHeaderElementListener implements View.OnClickListener {
        private final int positionView;

        OnClickHeaderElementListener(int positionView) {
            this.positionView = positionView;
        }

        @Override
        public void onClick(View view) {
            Utils.animLabelElement(view);
            if (viewSwitcher.getDisplayedChild() != positionView)
                viewSwitcher.setDisplayedChild(positionView);

            startAtPosition = positionView;
        }
    }
}

