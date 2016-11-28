package com.kunzisoft.switchdatetime;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

public class Utils {

    private static final int ANIMATION_DELAY = 0;
    private static final int PULSE_ANIMATOR_DURATION = 544;

    public static void animLabelElement(View view) {
        ObjectAnimator monthDayAnim = getPulseAnimator(view, 0.9F, 1.05F);
        monthDayAnim.setStartDelay(ANIMATION_DELAY);
        monthDayAnim.start();
    }

    public static ObjectAnimator getPulseAnimator(View labelToAnimate, float decreaseRatio, float increaseRatio) {
        Keyframe k0 = Keyframe.ofFloat(0f, 1f);
        Keyframe k1 = Keyframe.ofFloat(0.275f, decreaseRatio);
        Keyframe k2 = Keyframe.ofFloat(0.69f, increaseRatio);
        Keyframe k3 = Keyframe.ofFloat(1f, 1f);

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe("scaleX", k0, k1, k2, k3);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe("scaleY", k0, k1, k2, k3);
        ObjectAnimator pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX, scaleY);
        pulseAnimator.setDuration(PULSE_ANIMATOR_DURATION);

        return pulseAnimator;
    }

    public static boolean isTouchExplorationEnabled(AccessibilityManager accessibilityManager) {
        if (Build.VERSION.SDK_INT >= 14) {
            return accessibilityManager.isTouchExplorationEnabled();
        } else {
            return false;
        }
    }

    /**
     * Try to speak the specified text, for accessibility. Only available on JB or later.
     * @param text Text to announce.
     */
    @SuppressLint("NewApi")
    public static void tryAccessibilityAnnounce(View view, CharSequence text) {
        if (Build.VERSION.SDK_INT >= 16 && view != null && text != null) {
            view.announceForAccessibility(text);
        }
    }
}
