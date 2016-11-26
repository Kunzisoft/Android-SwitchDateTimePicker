package com.kunzisoft.switchdatetime.date.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kunzisoft.switchdatetime.R;

/**
 * TextView with circular colored background
 * @author JJamet
 */
public class TextCircularIndicatorView extends TextView {

    private int mCircleColor = Color.BLUE;
    private Paint mCirclePaint = new Paint();

    public TextCircularIndicatorView(Context context) {
        this(context, null, 0);
    }

    public TextCircularIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextCircularIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TextCircularIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    /**
     * Initialize constructor
     * @param attrs
     */
    private void init(AttributeSet attrs) {

        if(attrs != null) {
            TypedArray circularIndicatorTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TextCircularIndicatorView);
            setCircleColor(circularIndicatorTypedArray.getColor(R.styleable.TextCircularIndicatorView_colorCircleIndicator, mCircleColor));
            circularIndicatorTypedArray.recycle();
        }

        mCirclePaint.setFakeBoldText(true);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setTextAlign(Paint.Align.CENTER);
        mCirclePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public CharSequence getContentDescription() {
        return getText();
    }

    @Override
    public void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2;
        canvas.drawCircle(width / 2, height / 2, radius, mCirclePaint);
        super.onDraw(canvas);
    }

    /**
     * Get color of background circle
     * @return
     */
    public int getCircleColor() {
        return mCircleColor;
    }

    /**
     * Set color of background circle
     * @param color
     */
    public void setCircleColor(@ColorInt int color) {
        this.mCircleColor = color;
    }

}