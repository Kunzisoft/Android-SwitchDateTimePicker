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
import com.kunzisoft.switchdatetime.date.Utils;
import com.kunzisoft.switchdatetime.date.widget.ListPickerYearView;
import com.kunzisoft.switchdatetime.time.SwitchTimePicker;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A fragment that displays a dialog window with Date and Time who can be selected by switch button
 * @author J-Jamet
 * @version 1.0
 */
public class SwitchDateTimeDialogFragment extends DialogFragment {

    private static final String TAG = "SwitchDateTimeDialogFrg";

    private static final String STATE_DATETIME = "STATE_DATETIME";
    private Calendar dateTimeCalendar;

    private static final String TAG_LABEL = "LABEL";
    private static final String TAG_POSITIVE_BUTTON = "POSITIVE_BUTTON";
    private static final String TAG_NEGATIVE_BUTTON = "NEGATIVE_BUTTON";

    private String mLabel;
    private String mPositiveButton;
    private String mNegativeButton;
    private OnButtonClickListener mListener;

    private View dateTimeLayout;
    private ViewGroup viewGroup;
    private ViewAnimator switcher;
    private boolean lockAnimation = false;

    private int year;
    private int month;
    private int day;
    private int hourOfDay;
    private int minute;

    private SimpleDateFormat dayAndMonthSimpleDate;
    private SimpleDateFormat yearSimpleDate;

    private SwitchTimePicker timePicker;
    private MaterialCalendarView materialCalendarView;
    private ListPickerYearView listPickerYearView;

    private boolean blockAnimationIn;
    private boolean blockAnimationOut;

    /**
     * Create a new instance of SwitchDateTimeDialogFragment
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        if(getArguments() != null) {
            mLabel = getArguments().getString(TAG_LABEL);
            mPositiveButton = getArguments().getString(TAG_POSITIVE_BUTTON);
            mNegativeButton = getArguments().getString(TAG_NEGATIVE_BUTTON);
        }

        dateTimeCalendar = Calendar.getInstance();

        if (savedInstanceState != null) {
            // Restore value from saved state
            dateTimeCalendar.setTime(new Date(savedInstanceState.getLong(STATE_DATETIME)));
        }

        year = dateTimeCalendar.get(Calendar.YEAR);
        month = dateTimeCalendar.get(Calendar.MONTH);
        day = dateTimeCalendar.get(Calendar.DAY_OF_MONTH);
        hourOfDay = dateTimeCalendar.get(Calendar.HOUR_OF_DAY);
        minute = dateTimeCalendar.get(Calendar.MINUTE);


        LayoutInflater inflater = LayoutInflater.from(getActivity());
        dateTimeLayout = inflater.inflate(R.layout.dialog_switch_datetime_picker,
                (ViewGroup) getActivity().findViewById(R.id.datetime_picker));

        // ViewGroup add
        viewGroup = (ViewGroup) dateTimeLayout.findViewById(R.id.section_add);

        // Set label
        TextView labelView = (TextView) dateTimeLayout.findViewById(R.id.label);
        if(mLabel != null)
            labelView.setText(mLabel);
        else
            labelView.setText(getString(R.string.label_datetime_dialog));

        blockAnimationIn = false;
        blockAnimationOut = false;
        // Switch date to time and reverse
        switcher = (ViewAnimator) dateTimeLayout.findViewById(R.id.dateSwitcher);
        switcher.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
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
        switcher.getOutAnimation().setAnimationListener(new Animation.AnimationListener() {
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

        ImageButton buttonSwitch = (ImageButton) dateTimeLayout.findViewById(R.id.button_switch);
        buttonSwitch.setBackgroundColor(Color.TRANSPARENT);
        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.animLabelElement(view);
                if(!(blockAnimationIn && blockAnimationOut))
                    switcher.showNext();
            }
        });

        // Values header hourOfDay minutes
        final View timeHeaderValues = dateTimeLayout.findViewById(R.id.time_header_values);
        View.OnClickListener onTimeClickListener =
                new OnClickHeaderElementListener(HeaderViewsPosition.VIEW_HOURS_AND_MINUTES.getPosition());
        timeHeaderValues.setOnClickListener(onTimeClickListener);
        // Values header month day
        final TextView monthAndDayHeaderValues = (TextView) dateTimeLayout.findViewById(R.id.date_picker_month_and_day);
        View.OnClickListener onMonthAndDayClickListener =
                new OnClickHeaderElementListener(HeaderViewsPosition.VIEW_MONTH_AND_DAY.getPosition());
        monthAndDayHeaderValues.setOnClickListener(onMonthAndDayClickListener);
        // Values header year
        final TextView yearHeaderValues = (TextView) dateTimeLayout.findViewById(R.id.date_picker_year);
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

                dateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                dateTimeCalendar.set(Calendar.MINUTE, minute);
            }
        };
        // Init time with saved elements
        if(savedInstanceState == null)
            timePicker = new SwitchTimePicker(getContext(), onTimeSelectedListener);
        else
            timePicker = new SwitchTimePicker(getContext(), onTimeSelectedListener, savedInstanceState);
            timePicker = new SwitchTimePicker(getContext(), onTimeSelectedListener, savedInstanceState);
        timePicker.setHourOfDay(hourOfDay);
        timePicker.setMinute(minute);
        timePicker.onCreateView(dateTimeLayout, savedInstanceState);
        timePicker.setOnClickTimeListener(onTimeClickListener);

        // Construct DatePicker
        materialCalendarView = (MaterialCalendarView) dateTimeLayout.findViewById(com.kunzisoft.switchdatetime.R.id.datePicker);
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

        // Construct YearPicker
        listPickerYearView = (ListPickerYearView) dateTimeLayout.findViewById(R.id.yearPicker);
        listPickerYearView.setDatePickerListener(new OnYearSelectedListener() {
            @Override
            public void onYearSelected(View view, int yearPicker) {
                year = yearPicker;

                dateTimeCalendar.set(Calendar.YEAR, year);

                materialCalendarView.setCurrentDate(dateTimeCalendar.getTime());
                materialCalendarView.setDateSelected(dateTimeCalendar, true);
                // For resolve bug of switch year
                materialCalendarView.goToNext();
                materialCalendarView.goToPrevious();

                yearHeaderValues.setText(String.valueOf(year));
            }
        });

        // Assign buttons
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
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

    //TODO resolve bug
    private void assignAllValuesToCalendar() {
        dateTimeCalendar.set(Calendar.YEAR, year);
        dateTimeCalendar.set(Calendar.MONTH, month);
        dateTimeCalendar.set(Calendar.DAY_OF_MONTH, day);
        dateTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateTimeCalendar.set(Calendar.MINUTE, minute);
        dateTimeCalendar.set(Calendar.SECOND, 0);
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    /**
     * TODO
     * @return
     */
    public SimpleDateFormat getSimpleDateMonthAndDayFormat() {
        return dayAndMonthSimpleDate;
    }

    /**
     * Assign a SimpleDateFormat like "d MMM" to show formatted DateTime
     * @param simpleDateFormat
     */
    public void setSimpleDateMonthAndDayFormat(SimpleDateFormat simpleDateFormat) {
        // TODO REGEX for dd MM
        this.dayAndMonthSimpleDate = simpleDateFormat;
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
            if(switcher.getDisplayedChild() != positionView)
                switcher.setDisplayedChild(positionView);
        }
    }
}

