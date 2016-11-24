package com.kunzisoft.switchdatetime.date;

/**
 * Created by joker on 17/11/16.
 */

public interface DatePickerListener {
    int getFirstDayOfWeek();

    int getYear();

    void onDayOfMonthSelected(int year, int month, int day);

    void onYearChange(int year);

    void registerOnDateChangedListener(SwitchDatePicker.OnDateChangedListener onDateChangedListener);

    void tryVibrate();
}