package com.kunzisoft.switchdatetime.date;

import android.view.View;
import com.nineoldandroids.animation.ObjectAnimator;


/**
 * Created by joker on 24/11/16.
 */

public class Utils {

    public static final int MAX_YEAR = 2037;
    public static final int MIN_YEAR = 1902;

    public static final int ANIMATION_DELAY = 0;

    public static void animLabelElement(View view) {
        ObjectAnimator monthDayAnim = com.fourmob.datetimepicker.Utils.getPulseAnimator(view, 0.9F, 1.05F);
        monthDayAnim.setStartDelay(ANIMATION_DELAY);
        monthDayAnim.start();
    }
}
