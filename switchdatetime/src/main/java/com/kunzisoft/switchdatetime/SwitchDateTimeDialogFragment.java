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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.kunzisoft.switchdatetime.date.OnYearSelectedListener;
import com.kunzisoft.switchdatetime.date.Utils;
import com.kunzisoft.switchdatetime.date.widget.ListPickerYearView;
import com.kunzisoft.switchdatetime.time.RadialPickerLayout;
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

    private static final String STATE_DATETIME = "STATE_DATETIME";
    private Calendar dateTimeCalendar = Calendar.getInstance();

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

    private int year = dateTimeCalendar.get(Calendar.YEAR);
    private int month = dateTimeCalendar.get(Calendar.MONTH);
    private int day = dateTimeCalendar.get(Calendar.DAY_OF_MONTH);
    private int hour = dateTimeCalendar.get(Calendar.HOUR_OF_DAY);
    private int minute = dateTimeCalendar.get(Calendar.MINUTE);

    private SimpleDateFormat dayAndMonthSimpleDate;
    private SimpleDateFormat yearSimpleDate;

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

        if (savedInstanceState != null) {
            // Restore value from saved state
            dateTimeCalendar.setTime(new Date(savedInstanceState.getLong(STATE_DATETIME)));
        } else {
            // Init with values set
            dateTimeCalendar.set(year, month, day, hour, minute);
        }

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

        //TODO block listener for bug button (fast)
        // Switch date to time and reverse
        switcher = (ViewAnimator) dateTimeLayout.findViewById(R.id.dateSwitcher);
        ImageButton buttonSwitch = (ImageButton) dateTimeLayout.findViewById(R.id.button_switch);
        buttonSwitch.setBackgroundColor(Color.TRANSPARENT);
        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.animLabelElement(view);
                switcher.showNext();
            }
        });

        // Values header hour minutes
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
        SwitchTimePicker timePicker = new SwitchTimePicker(getContext(), new SwitchTimePicker.OnTimeSelectedListener() {
            @Override
            public void onTimeSelected(RadialPickerLayout view, int hourOfDayTime, int minuteTime) {
                hour = hourOfDayTime;
                minute = minuteTime;
            }
        });
        timePicker.setHourOfDay(hour);
        timePicker.setHourOfDay(minute);
        timePicker.onCreateView(dateTimeLayout, savedInstanceState);
        timePicker.setOnClickTimeListener(onTimeClickListener);

        // Construct DatePicker
        MaterialCalendarView materialCalendarView = (MaterialCalendarView) dateTimeLayout.findViewById(com.kunzisoft.switchdatetime.R.id.datePicker);
        materialCalendarView.setDateSelected(dateTimeCalendar, true);
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay calendarDay, boolean selected) {
                year = calendarDay.getYear();
                month = calendarDay.getMonth();
                day = calendarDay.getDay();
                yearHeaderValues.setText(String.valueOf(year));
                Date currentDate = calendarDay.getDate();
                monthAndDayHeaderValues.setText(dayAndMonthSimpleDate.format(currentDate));
            }
        });

        // Construct YearPicker
        ListPickerYearView listPickerYearView = (ListPickerYearView) dateTimeLayout.findViewById(R.id.yearPicker);
        listPickerYearView.setDatePickerListener(new OnYearSelectedListener() {
            @Override
            public void onYearSelected(View view, int yearPicker) {
                year = yearPicker;
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
        dateTimeCalendar.set(Calendar.DAY_OF_WEEK, day);
        dateTimeCalendar.set(Calendar.HOUR, hour);
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

    public void setHour(int hour) {
        this.hour = hour;
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

