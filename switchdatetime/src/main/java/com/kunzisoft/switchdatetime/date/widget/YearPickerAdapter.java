package com.kunzisoft.switchdatetime.date.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kunzisoft.switchdatetime.R;

import java.util.List;

/**
 * Adapter for manage elements of ListPickerYearView
 * @author JJamet
 */
class YearPickerAdapter extends BaseAdapter {

    private static final int LIST_ITEM_TYPE_STANDARD = 0;
    private static final int LIST_ITEM_TYPE_INDICATOR = 1;
    private static final int LIST_ITEM_TYPE_COUNT = 2;

    private List<Integer> listYears;
    private int selectedYear;

    private LayoutInflater layoutInflater;

    /**
     * Initialize adapter with list of years and element selected
     * @param context of adapter for layout
     * @param listYears list of years
     * @param selectedYear default year selected
     */
    YearPickerAdapter(Context context, List<Integer> listYears, int selectedYear) {
        this.listYears = listYears;
        this.selectedYear = selectedYear;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listYears.size();
    }

    @Override
    public Object getItem(int i) {
        return listYears.get(i);
    }

    @Override
    public long getItemId(int i) {
        return listYears.get(i);
    }

    @Override
    public int getItemViewType(int position) {
        if(listYears.get(position) == selectedYear)
            return LIST_ITEM_TYPE_INDICATOR;
        else
            return LIST_ITEM_TYPE_STANDARD;
    }

    @Override
    public int getViewTypeCount() {
        return LIST_ITEM_TYPE_COUNT;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        int type = getItemViewType(position);
        TextIndicatorViewHolder holder;
        if (convertView == null) {
            holder = new TextIndicatorViewHolder();
            switch(type) {
                case LIST_ITEM_TYPE_STANDARD:
                    convertView = layoutInflater.inflate(R.layout.year_text, parent, false);
                    break;
                case LIST_ITEM_TYPE_INDICATOR:
                    convertView = layoutInflater.inflate(R.layout.year_text_indicator, parent, false);
                    break;
            }
            assert convertView != null;
            convertView.setTag(holder);
        } else {
            holder = (TextIndicatorViewHolder) convertView.getTag();
        }

        holder.textView = (TextView) convertView.findViewById(R.id.year_textView);
        holder.textView.setText(String.valueOf(listYears.get(position)));
        return convertView;
    }

    /**
     * Get the list of years
     * @return years
     */
    public List<Integer> getListYears() {
        return listYears;
    }

    /**
     * Assign tle list of years, replace current list
     * @param listYears
     */
    public void setListYears(List<Integer> listYears) {
        this.listYears = listYears;
    }

    /**
     * Get the current selected year
     * @return
     */
    public int getSelectedYear() {
        return selectedYear;
    }

    /**
     * Assign the current selected year
     * @param selectedYear
     */
    public void setSelectedYear(int selectedYear) {
        this.selectedYear = selectedYear;
    }

    /**
     * Holder for TextIndicatorView
     */
    private class TextIndicatorViewHolder {
        /**
         * View of year
         */
        TextView textView;
    }
}
