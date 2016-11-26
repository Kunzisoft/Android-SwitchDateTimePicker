package com.kunzisoft.switchdatetime.date.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.kunzisoft.switchdatetime.R;
import com.kunzisoft.switchdatetime.date.OnYearSelectedListener;

import java.util.ArrayList;

/**
 * ListView for select one year, year selected is highlight <br />
 * To get the year, assign an OnYearSelectedListener
 * @see com.kunzisoft.switchdatetime.date.OnYearSelectedListener#onYearSelected(View, int)
 * @author JJamet
 */
public class ListPickerYearView extends ListView implements AdapterView.OnItemClickListener {

    private int minYear = 1902;
    private int maxYear = 2037;
    private int currentYear;

    private YearPickerAdapter mAdapter;
    private int mChildSize;
    private OnYearSelectedListener yearChangeListener;
    private int mViewSize;

    public ListPickerYearView(Context context) {
        this(context, null, 0);
    }

    public ListPickerYearView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListPickerYearView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ListPickerYearView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if(attrs != null) {
            TypedArray yearTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ListPickerYearView);
            setMinYear(yearTypedArray.getInt(R.styleable.ListPickerYearView_minYear, minYear));
            setMaxYear(yearTypedArray.getInt(R.styleable.ListPickerYearView_maxYear, minYear));
            currentYear = yearTypedArray.getInt(R.styleable.ListPickerYearView_defaultYear, 2000);
            yearTypedArray.recycle();
        }

        setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        Resources resources = context.getResources();
        mViewSize = resources.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height);
        mChildSize = resources.getDimensionPixelOffset(R.dimen.year_label_height);

        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(mChildSize / 3);

        ArrayList<Integer> years = new ArrayList<>();
        for (int year = minYear; year <= maxYear; year++) {
            years.add(year);
        }

        mAdapter = new YearPickerAdapter(getContext(), years, currentYear);
        setAdapter(mAdapter);

        setOnItemClickListener(this);
        setSelector(new StateListDrawable());
        setDividerHeight(0);

        refreshAndCenter();
    }

    /**
     * Refresh list and center on the selected year
     */
    public void refreshAndCenter() {
        mAdapter.notifyDataSetChanged();
        centerListOn(currentYear - minYear -1);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Vibrate
        //mController.tryVibrate();
        TextView clickedView = (TextView) view.findViewById(R.id.year_textView);
        if (clickedView != null) {
            currentYear = getYearFromTextView(clickedView);
            if(yearChangeListener != null) {
                yearChangeListener.onYearSelected(clickedView, getYearFromTextView(clickedView));
            }
            mAdapter.setSelectedYear(currentYear);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Center list on the selected year
     * @param position of year in the list
     */
    public void centerListOn(int position) {
        centerListOnWithTop(position, mViewSize / 2 - mChildSize / 2);
    }

    /**
     * Center list on the selected year and add y at the top
     * @param position of year in the list
     * @param y pixels from top
     */
    public void centerListOnWithTop(final int position, final int y) {
        post(new Runnable() {
            public void run() {
                setSelectionFromTop(position, y);
                requestLayout();
            }
        });
    }

    /**
     * Get year integer from TextView
     * @param view of text
     * @return
     */
    private static int getYearFromTextView(TextView view) {
        if(view == null)
            return 0;
        return Integer.valueOf(view.getText().toString());
    }

    /**
     * Attach listener for select year
     * @param onYearSelectedListener listener
     */
    public void setDatePickerListener(OnYearSelectedListener onYearSelectedListener) {
        this.yearChangeListener = onYearSelectedListener;
    }

    /**
     * Get current minYear
     * @return
     */
    public int getMinYear() {
        return minYear;
    }

    /**
     * Set minimum year of list
     * @param minYear minimum year
     */
    public void setMinYear(int minYear) {
        this.minYear = minYear;
    }

    /**
     * Get maximum year of list
     * @return
     */
    public int getMaxYear() {
        return maxYear;
    }

    /**
     * Set maximum year of list
     * @param maxYear
     */
    public void setMaxYear(int maxYear) {
        this.maxYear = maxYear;
    }

    /**
     * Get current year select
     * @return
     */
    public int getYearSelected() {
        return currentYear;
    }

    /**
     * Assign current year and refresh list
     * @param year
     */
    public void assignCurrentYear(int year) {
        currentYear = year;
        if(mAdapter != null)
            mAdapter.setSelectedYear(currentYear);
        refreshAndCenter();
    }
}