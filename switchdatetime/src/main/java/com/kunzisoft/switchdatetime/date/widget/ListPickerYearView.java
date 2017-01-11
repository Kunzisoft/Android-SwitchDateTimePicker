package com.kunzisoft.switchdatetime.date.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.kunzisoft.switchdatetime.R;
import com.kunzisoft.switchdatetime.date.OnYearSelectedListener;

import java.util.ArrayList;

/**
 * ListView for select one year, year selected is highlight <br />
 * To get the year, assign an OnYearSelectedListener
 * @see com.kunzisoft.switchdatetime.date.OnYearSelectedListener#onYearSelected(View, int)
 * @author JJamet
 */
public class ListPickerYearView extends RecyclerView implements YearPickerAdapter.OnClickYearListener{

    private final static String TAG = "ListPickerYearView";

    private int minYear = 1970;
    private int maxYear = 2100;
    private int currentYear;

    private YearPickerAdapter mAdapter;
    private OnYearSelectedListener yearChangeListener;
    private int mChildSize;
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

    private void init(Context context, AttributeSet attrs) {
        setLayoutManager(new LinearLayoutManager(context));

        if(attrs != null) {
            TypedArray yearTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ListPickerYearView);
                setMinYear(yearTypedArray.getInt(R.styleable.ListPickerYearView_minYear, minYear));
                setMaxYear(yearTypedArray.getInt(R.styleable.ListPickerYearView_maxYear, minYear));
            currentYear = yearTypedArray.getInt(R.styleable.ListPickerYearView_defaultYear, YearPickerAdapter.UNDEFINED);
            yearTypedArray.recycle();
        }

        setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        Resources resources = context.getResources();
        mViewSize = resources.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height);
        mChildSize = resources.getDimensionPixelOffset(R.dimen.year_label_height);

        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(mChildSize / 3);

        mAdapter = new YearPickerAdapter();
        setAdapter(mAdapter);

        mAdapter.setOnClickYearListener(this);

        refreshAndCenter();
    }

    /**
     * Assign years to adapter only if view is init
     */
    private void injectYearsInAdapter() {
        if(mAdapter != null) {
            ArrayList<Integer> years = new ArrayList<>();
            for (int year = minYear; year <= maxYear; year++) {
                years.add(year);
            }
            mAdapter.setListYears(years);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Refresh list and center on the selected year
     */
    public void refreshAndCenter() {
        mAdapter.notifyDataSetChanged();
        centerListOn(currentYear - minYear -1);
    }

    @Override
    public void onItemYearClick(View view, Integer year, int position) {
        int positionOldYear = mAdapter.getPositionSelectedYear();
        currentYear = year;

        // TODO Vibrate
        //mController.tryVibrate();
        if(yearChangeListener != null) {
            yearChangeListener.onYearSelected(view, year);
        }

        try {
            mAdapter.setSelectedYear(currentYear);
        } catch (YearPickerAdapter.SelectYearException e) {
            Log.e(TAG, e.getMessage());
        }
        mAdapter.notifyDataSetChanged();
        mAdapter.notifyItemChanged(positionOldYear);
        mAdapter.notifyItemChanged(position);
    }

    /**
     * Center list on the selected year
     * @param position of year in the list
     */
    public void centerListOn(final int position) {
        getLayoutManager().scrollToPosition(position);
        try {
            getLayoutManager().scrollVerticallyBy(mViewSize / 2 - mChildSize / 2, null, null);
        } catch(Exception e){
            Log.w(TAG, "Can't scroll more");
        }
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
        injectYearsInAdapter();
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
        injectYearsInAdapter();
    }

    /**
     * Get current year selected
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
        if(mAdapter != null) {
            try {
                mAdapter.setSelectedYear(currentYear);
            } catch (YearPickerAdapter.SelectYearException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        refreshAndCenter();
    }
}