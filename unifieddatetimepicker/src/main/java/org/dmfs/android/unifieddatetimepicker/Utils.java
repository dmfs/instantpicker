/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dmfs.android.unifieddatetimepicker;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;


/**
 * Utility helper functions for time and date pickers.
 */
public class Utils
{

    public static final int PULSE_ANIMATOR_DURATION = 544;
    public static final int SHRINK_ANIMATOR_DURATION = 250;

    // Alpha level for time picker selection.
    public static final int SELECTED_ALPHA = 51;
    public static final int SELECTED_ALPHA_THEME_DARK = 102;
    // Alpha level for fully opaque.
    public static final int FULL_ALPHA = 255;

    static final String SHARED_PREFS_NAME = "com.android.calendar_preferences";


    public static boolean isJellybeanOrLater()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }


    /**
     * Try to speak the specified text, for accessibility. Only available on JB or later.
     *
     * @param text
     *         Text to announce.
     */
    @SuppressLint("NewApi")
    public static void tryAccessibilityAnnounce(View view, CharSequence text)
    {
        if (isJellybeanOrLater() && view != null && text != null)
        {
            view.announceForAccessibility(text);
        }
    }


    /**
     * Render an animator to pulsate a view in place.
     *
     * @param labelToAnimate
     *         the view to pulsate.
     * @param factor
     *
     * @return The animator object. Use .start() to begin.
     */
    public static ObjectAnimator getPulseAnimator(View labelToAnimate, float decreaseRatio, float increaseRatio, float factor)
    {
        Keyframe k0 = Keyframe.ofFloat(0f, 1f * factor);
        Keyframe k1 = Keyframe.ofFloat(0.275f, decreaseRatio * factor);
        Keyframe k2 = Keyframe.ofFloat(0.69f, increaseRatio * factor);
        Keyframe k3 = Keyframe.ofFloat(1f, 1f * factor);

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe("scaleX", k0, k1, k2, k3);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe("scaleY", k0, k1, k2, k3);
        ObjectAnimator pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX, scaleY);
        pulseAnimator.setDuration(PULSE_ANIMATOR_DURATION);

        return pulseAnimator;
    }


    /**
     * Render an animator to pulsate a view in place.
     *
     * @param hide
     * @param labelToAnimate
     *         the view to pulsate.
     * @param setAlpha
     * @param targetAlpha
     *
     * @return The animator object. Use .start() to begin.
     */
    public static ObjectAnimator getShrinkAnimator(View labelToAnimate, float decreaseRatio, boolean setAlpha, Float targetAlpha)
    {
        Keyframe k0 = Keyframe.ofFloat(0f, 1f);
        Keyframe k1 = Keyframe.ofFloat(1f, decreaseRatio);

        labelToAnimate.setPivotY(labelToAnimate.getHeight() * 0.75f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe("scaleX", k0, k1);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe("scaleY", k0, k1);
        ObjectAnimator animator;
        if (setAlpha)
        {
            PropertyValuesHolder alpha = PropertyValuesHolder.ofKeyframe("alpha", Keyframe.ofFloat(0, labelToAnimate.getAlpha()),
                    targetAlpha != null ? Keyframe.ofFloat(1f, targetAlpha) : k1);
            animator = ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX, scaleY, alpha);
        }
        else
        {
            animator = ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX, scaleY);
        }
        animator.setDuration(SHRINK_ANIMATOR_DURATION);

        return animator;
    }


    /**
     * Render an animator to pulsate a view in place.
     *
     * @param labelToAnimate
     *         the view to pulsate.
     * @param setAlpha
     * @param targetAlpha
     *
     * @return The animator object. Use .start() to begin.
     */
    public static ObjectAnimator getGrowAnimator(View labelToAnimate, float decreaseRatio, boolean setAlpha, Float targetAlpha)
    {
        Keyframe k0 = Keyframe.ofFloat(0, decreaseRatio);
        // Keyframe k2 = Keyframe.ofFloat(0.69f, 1.1f);
        Keyframe k1 = Keyframe.ofFloat(1f, 1f);

        labelToAnimate.setPivotY(labelToAnimate.getHeight() * 0.75f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe("scaleX", k0, k1);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe("scaleY", k0, k1);
        ObjectAnimator animator;
        if (setAlpha)
        {
            PropertyValuesHolder alpha = PropertyValuesHolder.ofKeyframe("alpha", Keyframe.ofFloat(0, labelToAnimate.getAlpha()),
                    targetAlpha != null ? Keyframe.ofFloat(1f, targetAlpha) : k1);
            animator = ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX, scaleY, alpha);
        }
        else
        {
            animator = ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX, scaleY);
        }
        animator.setDuration(SHRINK_ANIMATOR_DURATION);

        return animator;
    }


    public static ObjectAnimator swipeAnimator(View labelToAnimate, int translation)
    {
        PropertyValuesHolder translateX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0, translation);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, translateX);
        animator.setDuration(400);

        return animator;
    }


    public static int fetchPrimaryColor(Context context)
    {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimary });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
}
