package com.kunzisoft.switchdatetime.date;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fourmob.datetimepicker.R;
import com.fourmob.datetimepicker.date.TextViewWithCircularIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class YearPickerView extends ListView implements AdapterView.OnItemClickListener {

    private int minYear = 1902;
    private int maxYear = 2037;
    private int currentYear = 2000;

    private YearAdapter mAdapter;
    private int mChildSize;
    private DatePickerListener yearChangeListener;
    private TextViewWithCircularIndicator mSelectedView;
    private int mViewSize;

    public YearPickerView(Context context) {
        this(context, null, 0);
    }

    public YearPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YearPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public YearPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private static int getYearFromTextView(TextView view) {
        return Integer.valueOf(view.getText().toString());
    }

    private void init(Context context, AttributeSet attrs) {
        if(attrs != null) {
            //TODO minYear maxYear
        }

        // TODO select year in listener

        setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        Resources resources = context.getResources();
        mViewSize = resources.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height);
        mChildSize = resources.getDimensionPixelOffset(R.dimen.year_label_height);

        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(mChildSize / 3);

        ArrayList<String> years = new ArrayList<String>();
        for (int year = minYear; year <= maxYear; year++) {
            years.add(String.format(Locale.getDefault(), "%d", year));
        }
        mAdapter = new YearAdapter(context, R.layout.year_label_text_view, years);
        setAdapter(mAdapter);

        setOnItemClickListener(this);
        setSelector(new StateListDrawable());
        setDividerHeight(0);

        refreshAndCenter();
    }

    public int getFirstPositionOffset() {
        final View firstChild = getChildAt(0);
        if (firstChild == null) {
            return 0;
        }
        return firstChild.getTop();
    }

    public void refreshAndCenter() {
        mAdapter.notifyDataSetChanged();
        if(yearChangeListener == null)
            postSetSelectionCentered((maxYear - minYear) /2);
        else
            postSetSelectionCentered(yearChangeListener.getYear() - minYear);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Vibrate
        //mController.tryVibrate();
        TextViewWithCircularIndicator clickedView = (TextViewWithCircularIndicator) view;
        if (clickedView != null) {
            if (clickedView != mSelectedView) {
                if (mSelectedView != null) {
                    mSelectedView.drawIndicator(false);
                    mSelectedView.requestLayout();
                }
                clickedView.drawIndicator(true);
                clickedView.requestLayout();
                mSelectedView = clickedView;
            }
            if(yearChangeListener != null) {
                yearChangeListener.onYearChange(getYearFromTextView(clickedView));
            }
            refreshAndCenter();
        }
    }

    public void postSetSelectionCentered(int position) {
        postSetSelectionFromTop(position, mViewSize / 2 - mChildSize / 2);
    }

    public void postSetSelectionFromTop(final int position, final int y) {
        post(new Runnable() {
            public void run() {
                setSelectionFromTop(position, y);
                requestLayout();
            }
        });
    }

    public void setDatePickerListener(DatePickerListener datePickerListener) {
        this.yearChangeListener = datePickerListener;
    }

    public int getMinYear() {
        return minYear;
    }

    public void setMinYear(int minYear) {
        this.minYear = minYear;
    }

    public int getMaxYear() {
        return maxYear;
    }

    public void setMaxYear(int maxYear) {
        this.maxYear = maxYear;
    }

    public int getYear() {
        return currentYear;
    }

    public void setYear(int year) {
        this.currentYear = year;
    }

    /**
     * Adapter for year view
     */
    private class YearAdapter extends ArrayAdapter<String> {

        public YearAdapter(Context context, int resource, List<String> years) {
            super(context, resource, years);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextViewWithCircularIndicator v = (TextViewWithCircularIndicator) super.getView(position, convertView, parent);
            v.requestLayout();
            int year = getYearFromTextView(v);
            boolean selected = currentYear == year;
            v.drawIndicator(selected);
            if (selected) {
                mSelectedView = v;
            }
            return v;
        }
    }
}