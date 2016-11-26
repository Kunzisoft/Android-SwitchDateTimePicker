package com.kunzisoft.switchdatetime.date;

import android.view.View;

/**
 * Listener for select year
 * @author JJamet
 */
public interface OnYearSelectedListener {
    /**
     * Call when year is selected
     * @param view of event
     * @param year selected
     */
    void onYearSelected(View view, int year);
}
