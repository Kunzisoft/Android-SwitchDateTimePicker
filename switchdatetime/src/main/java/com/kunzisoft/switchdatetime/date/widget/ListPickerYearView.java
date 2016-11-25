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

public class ListPickerYearView extends ListView implements AdapterView.OnItemClickListener {

    private static final String TAG = "YearPickerView";

    private int minYear = 1902;
    private int maxYear = 2037;
    private int currentYear;

    private YearPickerAdapter mAdapter;
    private int mChildSize;
    private OnYearSelectedListener yearChangeListener;
    private TextCircularIndicatorView mSelectedView;
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
            TypedArray yearTypedArray = getContext().obtainStyledAttributes(attrs, com.kunzisoft.switchdatetime.R.styleable.ListPickerYearView);
            setMinYear(yearTypedArray.getInt(com.kunzisoft.switchdatetime.R.styleable.ListPickerYearView_minYear, minYear));
            setMaxYear(yearTypedArray.getInt(com.kunzisoft.switchdatetime.R.styleable.ListPickerYearView_maxYear, minYear));
            currentYear = yearTypedArray.getInt(com.kunzisoft.switchdatetime.R.styleable.ListPickerYearView_defaultYear, 2000);
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

    public int getFirstPositionOffset() {
        final View firstChild = getChildAt(0);
        if (firstChild == null) {
            return 0;
        }
        return firstChild.getTop();
    }

    public void refreshAndCenter() {
        mAdapter.notifyDataSetChanged();
        postSetSelectionCentered(currentYear - minYear -1);
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

    private static int getYearFromTextView(TextView view) {
        if(view == null)
            return 0;
        return Integer.valueOf(view.getText().toString());
    }

    public void setDatePickerListener(OnYearSelectedListener onYearSelectedListener) {
        this.yearChangeListener = onYearSelectedListener;
    }

    //TODO modify setter
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

    public void assignCurrentYear(int year) {
        currentYear = year;
        if(mAdapter != null)
            mAdapter.setSelectedYear(currentYear);
        refreshAndCenter();
    }
}