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
 * limitations under the License
 */

package org.dmfs.android.unifieddatetimepicker.time;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.dmfs.android.unifieddatetimepicker.DateTimePicker;
import org.dmfs.android.unifieddatetimepicker.HapticFeedbackController;
import org.dmfs.android.unifieddatetimepicker.OnDateTimeSetListener;
import org.dmfs.android.unifieddatetimepicker.PickerContext;
import org.dmfs.android.unifieddatetimepicker.R;
import org.dmfs.android.unifieddatetimepicker.Utils;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.w3c.dom.Node;

import java.util.ArrayList;


/**
 * Dialog to set a time.
 */
public class RadialTimePicker extends FrameLayout implements RadialPickerLayout.OnValueSelectedListener, DateTimePicker
{
    private static final String TAG = "TimePickerDialog";

    public static final int HOUR_INDEX = 0;
    public static final int MINUTE_INDEX = 1;
    // NOT a real index for the purpose of what's showing.
    public static final int AMPM_INDEX = 2;
    // Also NOT a real index, just used for keyboard mode.
    public static final int ENABLE_PICKER_INDEX = 3;

    private OnDateTimeSetListener mCallback;

    private HapticFeedbackController mHapticFeedbackController;

    private RadialPickerLayout mTimePicker;

    private boolean mAllowAutoAdvance;
    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourMode = false;
    private boolean mThemeDark;

    // For hardware IME input.
    private String mDoublePlaceholderText;
    private boolean mInKbMode;
    private ArrayList<Integer> mTypedTimes;
    private Node mLegalTimesTree;

    // Accessibility strings.
    private String mHourPickerDescription;
    private String mSelectHours;
    private String mMinutePickerDescription;
    private String mSelectMinutes;

    private OnPickerStateChangeListener mOnPickerStateChangeListener;
    private DateTime mCurrentTime;

    private EditorComponent mCurrentEditorComponent;

    private PickerContext mPickerContext;


    public RadialTimePicker(Context context)
    {
        super(context);
        init(context);
    }


    @Override
    public void setOnPickerStateChangeListener(OnPickerStateChangeListener onPickerStateChangeListener)
    {
        mOnPickerStateChangeListener = onPickerStateChangeListener;
    }


    @Override
    public void updateState(EditorComponent editorComponent)
    {
        mTimePicker.setCurrentItemShowing(editorComponent == EditorComponent.HOURS ? HOUR_INDEX : MINUTE_INDEX,
                mCurrentEditorComponent == EditorComponent.HOURS || mCurrentEditorComponent == EditorComponent.MINUTES);
        mCurrentEditorComponent = editorComponent;
    }


    public RadialTimePicker(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        init(context);
    }


    public RadialTimePicker(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);
        init(context);
    }


    @Override
    public void setPickerContext(PickerContext pickerContext)
    {
        mPickerContext = pickerContext;
        mIs24HourMode = pickerContext.use24Hours();
        mTimePicker.initialize(getContext(), mHapticFeedbackController, mInitialHourOfDay, mInitialMinute, mPickerContext);
        mTimePicker.invalidate();
    }


    @Override
    public void setDateTime(DateTime dateTime)
    {
        mCurrentTime = dateTime;
        if (dateTime != null && !dateTime.isAllDay())
        {
            mTimePicker.setTime(dateTime.getHours(), dateTime.getMinutes());
        }
    }


    public void initialize(OnDateTimeSetListener callback, int hourOfDay, int minute, boolean is24HourMode)
    {
        mCallback = callback;

        mInitialHourOfDay = hourOfDay;
        mInitialMinute = minute;
        mIs24HourMode = is24HourMode;
        mInKbMode = false;
        mThemeDark = false;
    }


    public void setOnDateTimeSetListener(OnDateTimeSetListener onDateTimeSetListener)
    {
        mCallback = onDateTimeSetListener;
    }


    private class KeyboardListener implements OnKeyListener
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if (event.getAction() == KeyEvent.ACTION_UP)
            {
                // return processKeyUp(keyCode);
            }
            return false;
        }
    }


    public void init(Context context)
    {
        LayoutInflater.from(context).inflate(R.layout.time_picker_radial, this, true);

        KeyboardListener keyboardListener = new KeyboardListener();

        Resources res = getResources();
        mHourPickerDescription = res.getString(R.string.hour_picker_description);
        mSelectHours = res.getString(R.string.select_hours);
        mMinutePickerDescription = res.getString(R.string.minute_picker_description);
        mSelectMinutes = res.getString(R.string.select_minutes);
        mHapticFeedbackController = new HapticFeedbackController(getContext());

        mTimePicker = (RadialPickerLayout) findViewById(R.id.time_picker);
        mTimePicker.setOnValueSelectedListener(this);
        mTimePicker.setOnKeyListener(keyboardListener);

        int currentItemShowing = HOUR_INDEX;

        setCurrentItemShowing(currentItemShowing, false, true, true);

        mAllowAutoAdvance = true;

        // Set up for keyboard mode.
        mDoublePlaceholderText = res.getString(R.string.time_placeholder);

        if (mInKbMode)
        {
            // mTypedTimes = savedInstanceState.getIntegerArrayList(KEY_TYPED_TIMES);
        }
        else if (mTypedTimes == null)
        {
            mTypedTimes = new ArrayList<Integer>();
        }

        // Set the theme at the end so that the initialize()s above don't counteract the theme.
        mTimePicker.setTheme(getContext().getApplicationContext(), mThemeDark);
        // Prepare some colors to use.
        int circleBackground = res.getColor(R.color.circle_background);

        int lightGray = res.getColor(R.color.light_gray);

        mTimePicker.setBackgroundColor(mThemeDark ? lightGray : circleBackground);
    }


    public void tryVibrate()
    {
        mHapticFeedbackController.tryVibrate();
    }


    /**
     * Called by the picker for updating the header display.
     */
    @Override
    public void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance)
    {
        if (pickerIndex == HOUR_INDEX)
        {
            setTime(newValue, mCurrentTime.getMinutes());
            String announcement = String.format("%d", newValue);
            if (mAllowAutoAdvance && autoAdvance)
            {
                setCurrentItemShowing(MINUTE_INDEX, true, true, false);
                announcement += ". " + mSelectMinutes;
                mOnPickerStateChangeListener.onPickerStateChange(EditorComponent.MINUTES);
            }
            else
            {
                mTimePicker.setContentDescription(mHourPickerDescription + ": " + newValue);
            }
            Utils.tryAccessibilityAnnounce(mTimePicker, announcement);
        }
        else if (pickerIndex == MINUTE_INDEX)
        {
            // setMinute(newValue);
            mTimePicker.setContentDescription(mMinutePickerDescription + ": " + newValue);
            setTime(mCurrentTime.getHours(), newValue);

        }
        else if (pickerIndex == AMPM_INDEX)
        {
            setTime(mCurrentTime.getHours(), mCurrentTime.getMinutes());
        }
        else if (pickerIndex == ENABLE_PICKER_INDEX)
        {

        }
    }


    @Override
    public void onCycle(int pickerIndex, boolean forward)
    {
        // if (pickerIndex == MINUTE_INDEX)
        {
            mCurrentTime = mCurrentTime.addDuration(new Duration(forward ? 1 : -1, 0, pickerIndex == HOUR_INDEX ? 3600 * (mIs24HourMode ? 24 : 12) : 3600));
            if (mCallback != null)
            {
                mCallback.onDateTimeSet(mCurrentTime);
                mTimePicker.setTime(mCurrentTime.getHours(), mCurrentTime.getMinutes());
            }
        }
    }


    private void setTime(int hour, int minute)
    {
        mCurrentTime = new DateTime(mCurrentTime.getCalendarMetrics(), mCurrentTime.getTimeZone(), mCurrentTime.getYear(), mCurrentTime.getMonth(),
                mCurrentTime.getDayOfMonth(), hour, minute, mCurrentTime.getSeconds());
        if (mCallback != null)
        {
            mCallback.onDateTimeSet(mCurrentTime);
            mTimePicker.setTime(mCurrentTime.getHours(), mCurrentTime.getMinutes());
        }

    }


    // Show either Hours or Minutes.
    private void setCurrentItemShowing(int index, boolean animateCircle, boolean delayLabelAnimate, boolean announce)
    {
        mTimePicker.setCurrentItemShowing(index, animateCircle);

        TextView labelToAnimate;
        if (index == HOUR_INDEX)
        {
            int hours = mTimePicker.getHours();
            if (!mIs24HourMode)
            {
                hours = hours % 12;
            }
            mTimePicker.setContentDescription(mHourPickerDescription + ": " + hours);
            if (announce)
            {
                Utils.tryAccessibilityAnnounce(mTimePicker, mSelectHours);
            }
        }
        else
        {
            int minutes = mTimePicker.getMinutes();
            mTimePicker.setContentDescription(mMinutePickerDescription + ": " + minutes);
            if (announce)
            {
                Utils.tryAccessibilityAnnounce(mTimePicker, mSelectMinutes);
            }
        }
    }


    /**
     * Update the hours, minutes, and AM/PM displays with the typed times. If the typedTimes is empty, either show an empty display (filled with the placeholder
     * text), or update from the timepicker's values.
     *
     * @param allowEmptyDisplay
     *         if true, then if the typedTimes is empty, use the placeholder text. Otherwise, revert to the timepicker's values.
     */
    private void updateDisplay(boolean allowEmptyDisplay)
    {
        if (!allowEmptyDisplay && mTypedTimes.isEmpty())
        {
            setCurrentItemShowing(mTimePicker.getCurrentItemShowing(), true, true, true);
        }
    }

}
