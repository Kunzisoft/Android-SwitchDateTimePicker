package com.kunzisoft.switchdatetime.date;

/**
 * Created by joker on 17/11/16.
 */

interface DatePickerController {
    int getFirstDayOfWeek();

    int getMaxYear();

    int getMinYear();

    SimpleMonthAdapter.CalendarDay getSelectedDay();

    void onDayOfMonthSelected(int year, int month, int day);

    void onYearSelected(int year);

    void registerOnDateChangedListener(SwitchDatePicker.OnDateChangedListener onDateChangedListener);

    void tryVibrate();
}