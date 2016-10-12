package com.kunzisoft.switchdatetime;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewSwitcher;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A fragment that displays a dialog window with Date and Time who can be selected by switch button
 * @author J-Jamet
 * @version 1.0
 */
public class SwitchDateTimeDialogFragment extends DialogFragment {

    private static final String STATE_DATETIME = "STATE_DATETIME";
    private Calendar dateTime = Calendar.getInstance();

    private static final String TAG_LABEL = "LABEL";
    private static final String TAG_POSITIVE_BUTTON = "POSITIVE_BUTTON";
    private static final String TAG_NEGATIVE_BUTTON = "NEGATIVE_BUTTON";

    private String mLabel;
    private String mPositiveButton;
    private String mNegativeButton;
    private OnButtonClickListener mListener;

    private ViewGroup viewGroup;
    private ViewSwitcher switcher;

    private int year = dateTime.get(Calendar.YEAR);
    private int month = dateTime.get(Calendar.MONTH);
    private int day = dateTime.get(Calendar.DAY_OF_MONTH);
    private int hour = dateTime.get(Calendar.HOUR_OF_DAY);
    private int minute = dateTime.get(Calendar.MINUTE);

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
        savedInstanceState.putLong(STATE_DATETIME, dateTime.getTimeInMillis());

        super.onSaveInstanceState(savedInstanceState);
    }

    @NonNull
    @Override
    @SuppressWarnings("deprecation")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        if(getArguments() != null) {
            mLabel = getArguments().getString(TAG_LABEL);
            mPositiveButton = getArguments().getString(TAG_POSITIVE_BUTTON);
            mNegativeButton = getArguments().getString(TAG_NEGATIVE_BUTTON);
        }

        if (savedInstanceState != null) {
            // Restore value from saved state
            dateTime.setTime(new Date(savedInstanceState.getLong(STATE_DATETIME)));
        } else {
            // Init with values set
            dateTime.set(year, month, day, hour, minute);
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dateTimeLayout = inflater.inflate(R.layout.dialog_switch_datetime_picker,
                (ViewGroup) getActivity().findViewById(R.id.datetime_picker));

        // ViewGroup add
        viewGroup = (ViewGroup) dateTimeLayout.findViewById(R.id.section_add);

        // Set label
        TextView labelView = (TextView) dateTimeLayout.findViewById(R.id.label);
        if(mLabel != null)
            labelView.setText(mLabel);
        else
            labelView.setText(getString(R.string.label_datetime_dialog));
        final TextView dateText = (TextView) dateTimeLayout.findViewById(R.id.value);

        // Assign buttons
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        db.setView(dateTimeLayout);
        if(mPositiveButton == null)
            mPositiveButton = getString(R.string.positive_button_datetime_picker);
        db.setPositiveButton(mPositiveButton, new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(mListener !=null)
                            mListener.onPositiveButtonClick(dateTime.getTime());
                    }
                });
        if(mNegativeButton == null)
            mNegativeButton = getString(R.string.negative_button_datetime_picker);
        db.setNegativeButton(mNegativeButton, new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Close dialog
                        if(mListener !=null)
                            mListener.onNegativeButtonClick(dateTime.getTime());
                    }
                });

        // Show datetime from locale format
        //TODO DateFormat
        dateText.setText(DateFormat.getDateTimeInstance().format(dateTime.getTime()));
        AlertDialog dialog = db.show();

        // Switch date to time and reverse
        switcher = (ViewSwitcher) dialog.findViewById(R.id.dateSwitcher);
        ImageButton buttonSwitch = (ImageButton) dialog.findViewById(R.id.button_switch);
        buttonSwitch.setBackgroundColor(Color.TRANSPARENT);
        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(switcher.getDisplayedChild() == 0) {
                    switcher.showNext();
                    ((ImageButton) view).setImageResource(R.drawable.ic_clock_32dp);
                }
                else if (switcher.getDisplayedChild() == 1) {
                    switcher.showPrevious();
                    ((ImageButton) view).setImageResource(R.drawable.ic_calendar_32dp);
                }
            }
        });

        // Construct TimePicker
        TimePicker timePicker = (TimePicker) dialog.findViewById(R.id.timePicker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(hour);
        } else {
            timePicker.setCurrentHour(hour);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setMinute(minute);
        } else {
            timePicker.setCurrentMinute(minute);
        }
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                dateTime.set(Calendar.HOUR_OF_DAY, i);
                dateTime.set(Calendar.MINUTE, i1);
                dateText.setText(DateFormat.getDateTimeInstance().format(dateTime.getTime()));
            }
        });

        // Construct DatePicker
        DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);
        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                dateTime.set(Calendar.YEAR, i);
                dateTime.set(Calendar.MONTH, i1);
                dateTime.set(Calendar.DAY_OF_MONTH, i2);
                dateText.setText(DateFormat.getDateTimeInstance().format(dateTime.getTime()));
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                switcher.setMinimumWidth(viewGroup.getWidth() + switcher.getWidth());
            }
        });

        return dialog;
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
     * Callback class for assign action on positive and negative button
     */
    public interface OnButtonClickListener {
        void onPositiveButtonClick(Date date);
        void onNegativeButtonClick(Date date);
    }
}

