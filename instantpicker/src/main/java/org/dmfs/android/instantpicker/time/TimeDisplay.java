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

package org.dmfs.android.instantpicker.time;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dmfs.android.bolts.color.colors.AttributeColor;
import org.dmfs.android.instantpicker.DateTimePicker;
import org.dmfs.android.instantpicker.HapticFeedbackController;
import org.dmfs.android.instantpicker.OnDateTimeSetListener;
import org.dmfs.android.instantpicker.PickerContext;
import org.dmfs.android.instantpicker.R;
import org.dmfs.android.instantpicker.Utils;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;


/**
 * Dialog to set a time.
 */
public class TimeDisplay extends FrameLayout implements DateTimePicker, View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener
{
    private static final String TAG = "TimePicker";

    private static final String KEY_HOUR_OF_DAY = "hour_of_day";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_IS_24_HOUR_VIEW = "is_24_hour_view";
    private static final String KEY_CURRENT_ITEM_SHOWING = "current_item_showing";
    private static final String KEY_IN_KB_MODE = "in_kb_mode";
    private static final String KEY_TYPED_TIMES = "typed_times";
    private static final String KEY_DARK_THEME = "dark_theme";

    // private static final int HOUR_INDEX = 0;
    // private static final int MINUTE_INDEX = 1;
    // NOT a real index for the purpose of what's showing.
    public static final int AMPM_INDEX = 2;
    // Also NOT a real index, just used for keyboard mode.
    public static final int ENABLE_PICKER_INDEX = 3;
    public static final int AM = 0;
    public static final int PM = 1;

    // Delay before starting the highlight animation, in ms.
    private static final int PULSE_ANIMATOR_DELAY = 300;

    private OnDateTimeSetListener mCallback;

    private HapticFeedbackController mHapticFeedbackController;

    private TextView mHourView;
    private TextView mHourSpaceView;
    private TextView mMinuteView;
    private TextView mMinuteSpaceView;
    private TextView mAmPmTextView;
    private View mSeparator;
    private View mAmPmHitspace;
    private TextView mSetTimeButton;
    private View mToAllDay;

    private View mTimeDisplay;

    private int mSelectedColor;
    private int mUnselectedColor;
    private String mAmText;
    private String mPmText;

    private boolean mAllowAutoAdvance;
    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourMode;
    private boolean mThemeDark;

    // For hardware IME input.
    private char mPlaceholderText;
    private String mDoublePlaceholderText;
    private String mDeletedKeyFormat;
    private boolean mInKbMode = false;
    private ArrayList<Integer> mTypedTimes;
    private Node mLegalTimesTree;
    private int mAmKeyCode;
    private int mPmKeyCode;
    private boolean mIsAm = false;

    // Accessibility strings.
    private String mHourPickerDescription;
    private String mSelectHours;
    private String mMinutePickerDescription;
    private String mSelectMinutes;

    private OnPickerStateChangeListener mFieldClickListener;
    private DateTime mCurrentTime;

    private EditorComponent mCurrentEditorComponent;

    private PickerContext mPickerContext;


    public TimeDisplay(Context context)
    {
        super(context);
        init(context, null, 0);
    }


    public TimeDisplay(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        init(context, attributeSet, 0);
    }


    public TimeDisplay(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);
        init(context, attributeSet, defStyle);
    }


    private void init(Context context, AttributeSet attributeSet, int defStyle)
    {
        LayoutInflater.from(context).inflate(R.layout.udtp_time_display, this, true);

        setFocusable(true);
        setFocusableInTouchMode(true);

        float hsv[] = new float[3];

        Color.colorToHSV(new AttributeColor(context, android.R.attr.colorBackground).argb(), hsv);
        mThemeDark = hsv[2] * hsv[2] * (1 - hsv[1]) < 0.60;

        KeyboardListener keyboardListener = new KeyboardListener();

        mTimeDisplay = findViewById(R.id.time_display_content);
        setOnKeyListener(keyboardListener);
        Resources res = getResources();
        mHourPickerDescription = res.getString(R.string.hour_picker_description);
        mSelectHours = res.getString(R.string.select_hours);
        mMinutePickerDescription = res.getString(R.string.minute_picker_description);
        mSelectMinutes = res.getString(R.string.select_minutes);
        mSelectedColor = res.getColor(mThemeDark ? android.R.color.white : R.color.numbers_text_color);
        mUnselectedColor = (res.getColor(mThemeDark ? android.R.color.white : R.color.numbers_text_color) & 0x00ffffff) | 0x80000000;

        mSeparator = findViewById(R.id.separator);
        mSetTimeButton = (TextView) findViewById(R.id.set_time_label);
        mSetTimeButton.setOnClickListener(this);

        mHourView = (TextView) findViewById(R.id.hours);
        mHourView.setOnKeyListener(keyboardListener);
        mHourSpaceView = (TextView) findViewById(R.id.hour_space);
        mMinuteSpaceView = (TextView) findViewById(R.id.minutes_space);
        mMinuteView = (TextView) findViewById(R.id.minutes);
        mMinuteView.setOnKeyListener(keyboardListener);
        mAmPmTextView = (TextView) findViewById(R.id.ampm_label);
        mAmPmTextView.setOnKeyListener(keyboardListener);
        mAmPmHitspace = (TextView) findViewById(R.id.ampm_hitspace);
        mAmPmHitspace.setOnKeyListener(keyboardListener);
        String[] amPmTexts = new DateFormatSymbols().getAmPmStrings();
        mAmText = amPmTexts[0];
        mPmText = amPmTexts[1];
        mToAllDay = findViewById(R.id.to_all_day_button);
        mToAllDay.setOnClickListener(this);

        mHapticFeedbackController = new HapticFeedbackController(getContext());

        mHourView.setOnClickListener(this);
        mMinuteView.setOnClickListener(this);

        // Enable or disable the AM/PM view.
        if (mIs24HourMode)
        {
            mAmPmTextView.setVisibility(View.GONE);
            mAmPmHitspace.setVisibility(View.GONE);

            RelativeLayout.LayoutParams paramsSeparator = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            paramsSeparator.addRule(RelativeLayout.CENTER_IN_PARENT);
            TextView separatorView = (TextView) findViewById(R.id.separator);
            separatorView.setLayoutParams(paramsSeparator);
        }
        else
        {
            mAmPmTextView.setVisibility(View.VISIBLE);
            mAmPmHitspace.setVisibility(View.VISIBLE);
            updateAmPmDisplay(mInitialHourOfDay < 12);
            mAmPmTextView.setOnClickListener(this);
            mAmPmHitspace.setOnClickListener(this);
        }

        mAllowAutoAdvance = true;
        setHour(mInitialHourOfDay, true);
        setMinute(mInitialMinute);

        // Set up for keyboard mode.
        mDoublePlaceholderText = res.getString(R.string.time_placeholder);
        mDeletedKeyFormat = res.getString(R.string.deleted_key);
        mPlaceholderText = mDoublePlaceholderText.charAt(0);
        mAmKeyCode = mPmKeyCode = -1;
        generateLegalTimesTree();
        if (mInKbMode)
        {
            // mTypedTimes = savedInstanceState.getIntegerArrayList(KEY_TYPED_TIMES);
            tryStartingKbMode(-1);
            mHourView.invalidate();
        }
        else if (mTypedTimes == null)
        {
            mTypedTimes = new ArrayList<Integer>();
        }

        // Prepare some colors to use.
        int white = res.getColor(android.R.color.white);
        int timeDisplay = res.getColor(R.color.numbers_text_color);

        int darkGray = res.getColor(R.color.dark_gray);
        ColorStateList darkDoneTextColor = res.getColorStateList(R.color.done_text_color_dark);

        // Set the colors for each view based on the theme.
        ((TextView) findViewById(R.id.separator)).setTextColor(mThemeDark ? white : timeDisplay);
        // ((TextView) findViewById(R.id.ampm_label)).setTextColor(mThemeDark ? white : timeDisplay);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
    }


    @Override
    public void setDateTime(DateTime dateTime)
    {
        if (mCurrentTime != null && dateTime != null && !mCurrentTime.isAllDay() && !dateTime.isAllDay() && mCurrentTime.getHours() != dateTime.getHours()
                && mCurrentEditorComponent != EditorComponent.HOURS)
        {
            ObjectAnimator pulseAnimator = Utils.getPulseAnimator(mHourView, 0.85f, 1.1f, 1f);
            pulseAnimator.start();
        }

        mCurrentTime = dateTime;
        if (dateTime != null)
        {
            int hours = dateTime.getHours();
            mIsAm = hours < 12;
        }

        updateDisplay(dateTime == null);
    }


    @Override
    public void setPickerContext(PickerContext pickerContext)
    {
        mPickerContext = pickerContext;
        mIs24HourMode = pickerContext.use24Hours();
        generateLegalTimesTree();
        if (mIs24HourMode)
        {
            mAmPmTextView.setVisibility(View.GONE);
            mAmPmHitspace.setVisibility(View.GONE);

            RelativeLayout.LayoutParams paramsSeparator = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            paramsSeparator.addRule(RelativeLayout.CENTER_IN_PARENT);
            TextView separatorView = (TextView) findViewById(R.id.separator);
            separatorView.setLayoutParams(paramsSeparator);
        }
        else
        {
            mAmPmTextView.setVisibility(View.VISIBLE);
            mAmPmHitspace.setVisibility(View.VISIBLE);
            updateAmPmDisplay(mInitialHourOfDay < 12);
            mAmPmTextView.setOnClickListener(this);
            mAmPmHitspace.setOnClickListener(this);
        }
    }


    public void setOnDateTimeSetListener(OnDateTimeSetListener onDateTimeSetListener)
    {
        mCallback = onDateTimeSetListener;
    }


    @Override
    public void setOnPickerStateChangeListener(OnPickerStateChangeListener onPickerStateChangeListener)
    {
        mFieldClickListener = onPickerStateChangeListener;
    }


    @Override
    public void onClick(View v)
    {
        if (mFieldClickListener == null)
        {
            return;
        }

        int id = v.getId();
        EditorComponent editorComponent = null;
        if (id == mHourView.getId())
        {
            editorComponent = EditorComponent.HOURS;
        }
        else if (id == mMinuteView.getId())
        {
            editorComponent = EditorComponent.MINUTES;
        }
        else if (id == mSetTimeButton.getId())
        {
            editorComponent = EditorComponent.HOURS;
        }
        else if (id == mAmPmTextView.getId() || id == mAmPmHitspace.getId())
        {
            tryVibrate();
            mIsAm = !mIsAm;
            int hours = mCurrentTime.getHours();
            if (hours >= 12 && mIsAm)
            {
                hours -= 12;
            }
            else if (hours < 12 && !mIsAm)
            {
                hours += 12;
            }
            setDateTime(new DateTime(mCurrentTime.getCalendarMetrics(), mCurrentTime.getTimeZone(), mCurrentTime.getYear(), mCurrentTime.getMonth(),
                    mCurrentTime.getDayOfMonth(), hours, mCurrentTime.getMinutes(), mCurrentTime.getSeconds()));
            if (mCallback != null)
            {
                mCallback.onDateTimeSet(mCurrentTime);
            }
            // updateAmPmDisplay(mIsAm);
        }
        else if (id == R.id.to_all_day_button)
        {
            tryVibrate();
            editorComponent = EditorComponent.MONTH_AND_DAY;
            setDateTime(mCurrentTime.toAllDay());
            if (mCallback != null)
            {
                mCallback.onDateTimeSet(mCurrentTime);
            }
        }

        if (editorComponent != null)
        {
            mFieldClickListener.onPickerStateChange(editorComponent);
            updateState(editorComponent);
            tryVibrate();
            mCurrentEditorComponent = editorComponent;
        }
    }


    public void tryVibrate()
    {
        mHapticFeedbackController.tryVibrate();
    }


    private void updateAmPmDisplay(boolean isAm)
    {
        if (mIs24HourMode)
        {
            return;
        }

        mAmPmTextView.setText(isAm ? mAmText : mPmText);
        if (isAm)
        {
            // mAmPmTextView.setAlpha(1f);
            mAmPmHitspace.setContentDescription(mAmText);
        }
        else
        {
            // mAmPmTextView.setAlpha(1f);
            mAmPmHitspace.setContentDescription(mPmText);
        }
    }


    private void setHour(int value, boolean announce)
    {
        String format;
        if (mIs24HourMode)
        {
            format = "%02d";
        }
        else
        {
            format = "%d";
            value = value % 12;
            if (value == 0)
            {
                value = 12;
            }
        }

        CharSequence text = String.format(format, value);
        mHourView.setText(text);
        mHourSpaceView.setText(text);
    }


    private void setMinute(int value)
    {
        if (value == 60)
        {
            value = 0;
        }
        CharSequence text = String.format(Locale.getDefault(), "%02d", value);
        mMinuteView.setText(text);
        mMinuteSpaceView.setText(text);
    }


    @Override
    public void updateState(EditorComponent editorComponent)
    {
        if (editorComponent == mCurrentEditorComponent)
        {
            return;
        }

        View labelToAnimate = null;
        // View labelToAnimateIn = null;
        // View labelToAnimateOut = null;

        switch (editorComponent)
        {
            case HOURS:
                labelToAnimate = mHourView;
                break;
            case MINUTES:
                labelToAnimate = mMinuteView;
                break;
        }

        int hourColor = (editorComponent == EditorComponent.HOURS) ? mSelectedColor : mUnselectedColor;
        int minuteColor = (editorComponent == EditorComponent.MINUTES) ? mSelectedColor : mUnselectedColor;
        mHourView.setTextColor(hourColor);
        mMinuteView.setTextColor(minuteColor);

        if (labelToAnimate != null && mCurrentEditorComponent != editorComponent
                && (mCurrentEditorComponent == EditorComponent.HOURS || mCurrentEditorComponent == EditorComponent.MINUTES))
        {

            ObjectAnimator pulseAnimator = Utils.getPulseAnimator(labelToAnimate, 0.85f, 1.1f, 1f);

            pulseAnimator.start();
        }

        if ((editorComponent == EditorComponent.HOURS || editorComponent == EditorComponent.MINUTES)
                && (mCurrentEditorComponent != EditorComponent.HOURS && mCurrentEditorComponent != EditorComponent.MINUTES))
        {
            mTimeDisplay.setVisibility(View.VISIBLE);
            mToAllDay.setVisibility(View.VISIBLE);
            ObjectAnimator pulseAnimator = Utils.getGrowAnimator(mTimeDisplay, 0.75f, mCurrentTime.isAllDay(), 1f);
            mSeparator.setAlpha(1f);
            pulseAnimator.start();

            pulseAnimator = Utils.getGrowAnimator(mSetTimeButton, 0.75f, mCurrentTime.isAllDay(), 0f);
            pulseAnimator.start();
//            Utils.swipeAnimator(mSetTimeButton, -getWidth()/2).start();

            Utils.getGrowAnimator(mToAllDay, 0.75f, mCurrentTime.isAllDay(), 1f).start();
        }
        if (editorComponent != EditorComponent.HOURS && editorComponent != EditorComponent.MINUTES
                && (mCurrentEditorComponent == EditorComponent.HOURS || mCurrentEditorComponent == EditorComponent.MINUTES))
        {
            ObjectAnimator pulseAnimator = Utils.getShrinkAnimator(mTimeDisplay, 0.75f, mCurrentTime.isAllDay(), 0f);
            mSeparator.setAlpha(0.75f);
            pulseAnimator.start();

            pulseAnimator = Utils.getShrinkAnimator(mSetTimeButton, 0.75f, mCurrentTime.isAllDay(), null);
            pulseAnimator.start();
//            Utils.swipeAnimator(mSetTimeButton, getWidth()/2).start();

            Utils.getShrinkAnimator(mToAllDay, 0.75f, mCurrentTime.isAllDay(), 0f).start();
        }

        if ((editorComponent == EditorComponent.HOURS || editorComponent == EditorComponent.MINUTES) && mCurrentTime.isAllDay())
        {
            // we automatically add the current time + 1 hour
            DateTime now = DateTime.nowAndHere();
            setDateTime(new DateTime(mCurrentTime.getCalendarMetrics(), mPickerContext.defaultTimeZone(), mCurrentTime.getYear(), mCurrentTime.getMonth(),
                    mCurrentTime.getDayOfMonth(), now.getHours(), 0, 0).addDuration(new Duration(1, 0, 3600)));
            if (mCallback != null)
            {
                mCallback.onDateTimeSet(mCurrentTime);
            }
        }

        mCurrentEditorComponent = editorComponent;
    }


    /**
     * For keyboard mode, processes key events.
     *
     * @param keyCode
     *         the pressed key.
     *
     * @return true if the key was successfully processed, false otherwise.
     */
    private boolean processKeyUp(int keyCode)
    {
        if (keyCode == KeyEvent.KEYCODE_TAB)
        {
            if (mInKbMode)
            {
                if (isTypedTimeFullyLegal())
                {
                    finishKbMode(true);
                }
                return true;
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_ENTER)
        {
            if (mInKbMode)
            {
                if (!isTypedTimeFullyLegal())
                {
                    return true;
                }
                finishKbMode(false);
            }
            if (mCallback != null)
            {
                mCallback.onDateTimeSet(mCurrentTime);
            }
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_DEL)
        {
            if (mInKbMode)
            {
                if (!mTypedTimes.isEmpty())
                {
                    int deleted = deleteLastTypedKey();
                    String deletedKeyStr;
                    if (deleted == getAmOrPmKeyCode(AM))
                    {
                        deletedKeyStr = mAmText;
                    }
                    else if (deleted == getAmOrPmKeyCode(PM))
                    {
                        deletedKeyStr = mPmText;
                    }
                    else
                    {
                        deletedKeyStr = String.format("%d", getValFromKeyCode(deleted));
                    }
                    // Utils.tryAccessibilityAnnounce(mTimePicker, String.format(mDeletedKeyFormat, deletedKeyStr));
                    updateDisplay(true);
                }
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1 || keyCode == KeyEvent.KEYCODE_2 || keyCode == KeyEvent.KEYCODE_3
                || keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5 || keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_7
                || keyCode == KeyEvent.KEYCODE_8 || keyCode == KeyEvent.KEYCODE_9
                || (!mIs24HourMode && (keyCode == getAmOrPmKeyCode(AM) || keyCode == getAmOrPmKeyCode(PM))))
        {
            if (!mInKbMode)
            {
                mTypedTimes.clear();
                tryStartingKbMode(keyCode);
                return true;
            }
            // We're already in keyboard mode.
            if (addKeyIfLegal(keyCode))
            {
                updateDisplay(false);
            }
            return true;
        }
        return false;
    }


    /**
     * Try to start keyboard mode with the specified key, as long as the timepicker is not in the middle of a touch-event.
     *
     * @param keyCode
     *         The key to use as the first press. Keyboard mode will not be started if the key is not legal to start with. Or, pass in -1 to get into keyboard
     *         mode without a starting key.
     */
    private void tryStartingKbMode(int keyCode)
    {
        if (keyCode == -1 || addKeyIfLegal(keyCode))
        {
            mInKbMode = true;
            updateDisplay(false);
        }
    }


    private boolean addKeyIfLegal(int keyCode)
    {
        // If we're in 24hour mode, we'll need to check if the input is full. If in AM/PM mode,
        // we'll need to see if AM/PM have been typed.
        if ((mIs24HourMode && mTypedTimes.size() == 4) || (!mIs24HourMode && isTypedTimeFullyLegal()))
        {
            return false;
        }

        mTypedTimes.add(keyCode);
        if (!isTypedTimeLegalSoFar())
        {
            deleteLastTypedKey();
            return false;
        }

        int val = getValFromKeyCode(keyCode);
        // Utils.tryAccessibilityAnnounce(mTimePicker, String.format("%d", val));
        // Automatically fill in 0's if AM or PM was legally entered.
        if (isTypedTimeFullyLegal())
        {
            if (!mIs24HourMode && mTypedTimes.size() <= 3)
            {
                mTypedTimes.add(mTypedTimes.size() - 1, KeyEvent.KEYCODE_0);
                mTypedTimes.add(mTypedTimes.size() - 1, KeyEvent.KEYCODE_0);
            }
        }

        return true;
    }


    /**
     * Traverse the tree to see if the keys that have been typed so far are legal as is, or may become legal as more keys are typed (excluding backspace).
     */
    private boolean isTypedTimeLegalSoFar()
    {
        Node node = mLegalTimesTree;
        for (int keyCode : mTypedTimes)
        {
            node = node.canReach(keyCode);
            if (node == null)
            {
                return false;
            }
        }
        return true;
    }


    /**
     * Check if the time that has been typed so far is completely legal, as is.
     */
    private boolean isTypedTimeFullyLegal()
    {
        if (mIs24HourMode)
        {
            // For 24-hour mode, the time is legal if the hours and minutes are each legal. Note:
            // getEnteredTime() will ONLY call isTypedTimeFullyLegal() when NOT in 24hour mode.
            int[] values = getEnteredTime(null);
            return (values[0] >= 0 && values[1] >= 0 && values[1] < 60);
        }
        else
        {
            // For AM/PM mode, the time is legal if it contains an AM or PM, as those can only be
            // legally added at specific times based on the tree's algorithm.
            return (mTypedTimes.contains(getAmOrPmKeyCode(AM)) || mTypedTimes.contains(getAmOrPmKeyCode(PM)));
        }
    }


    private int deleteLastTypedKey()
    {
        int deleted = mTypedTimes.remove(mTypedTimes.size() - 1);
        if (!isTypedTimeFullyLegal())
        {
        }
        return deleted;
    }


    /**
     * Get out of keyboard mode. If there is nothing in typedTimes, revert to DateTimePicker's time.
     *
     * @param changeDisplays
     *         If true, update the displays with the relevant time.
     */
    private void finishKbMode(boolean updateDisplays)
    {
        mInKbMode = false;
        if (!mTypedTimes.isEmpty())
        {
            int values[] = getEnteredTime(null);
            if (!mIs24HourMode)
            {
                mIsAm = values[2] == AM;
                if (values[0] == 12)
                {
                    values[0] = 0;
                }

                if (!mIsAm)
                {
                    values[0] += 12;
                }
            }
            setDateTime(new DateTime(mCurrentTime.getCalendarMetrics(), mCurrentTime.getTimeZone(), mCurrentTime.getYear(), mCurrentTime.getMonth(),
                    mCurrentTime.getDayOfMonth(), values[0], values[1], 0));
            mTypedTimes.clear();
        }
        if (updateDisplays)
        {
            updateDisplay(false);
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
        if (mTypedTimes.isEmpty() && mCurrentTime.isAllDay())
        {
            // mSetTimeButton.setVisibility(View.VISIBLE);
            mTimeDisplay.setVisibility(View.INVISIBLE);
            mToAllDay.setVisibility(View.INVISIBLE);
            mSetTimeButton.setAlpha(0.75f);
            mTimeDisplay.setAlpha(0f);
            mToAllDay.setAlpha(0f);
        }
        else
        {
            // mSetTimeButton.setVisibility(View.INVISIBLE);
            mTimeDisplay.setVisibility(View.VISIBLE);
            mToAllDay.setVisibility(View.VISIBLE);
            mSetTimeButton.setAlpha(0f);
            mTimeDisplay.setAlpha(1f);
            mToAllDay.setAlpha(1f);
        }

        if (!allowEmptyDisplay && mTypedTimes.isEmpty())
        {
            int hour = mCurrentTime.getHours();
            int minute = mCurrentTime.getMinutes();
            setHour(hour, true);
            setMinute(minute);
            if (!mIs24HourMode)
            {
                updateAmPmDisplay(hour < 12);
            }
        }
        else
        {
            Boolean[] enteredZeros = { false, false };
            int[] values = getEnteredTime(enteredZeros);
            String hourFormat = enteredZeros[0] ? "%02d" : "%2d";
            String minuteFormat = (enteredZeros[1]) ? "%02d" : "%2d";
            String hourStr = (values[0] == -1) ? mDoublePlaceholderText : String.format(hourFormat, values[0]).replace(' ', mPlaceholderText);
            String minuteStr = (values[1] == -1) ? mDoublePlaceholderText : String.format(minuteFormat, values[1]).replace(' ', mPlaceholderText);
            mHourView.setText(hourStr);
            mHourSpaceView.setText(hourStr);
            mHourView.setTextColor(mUnselectedColor);
            mMinuteView.setText(minuteStr);
            mMinuteSpaceView.setText(minuteStr);
            mMinuteView.setTextColor(mUnselectedColor);
            if (!mIs24HourMode)
            {
                updateAmPmDisplay(values[2] == AM);
            }
        }
    }


    private static int getValFromKeyCode(int keyCode)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_0:
                return 0;
            case KeyEvent.KEYCODE_1:
                return 1;
            case KeyEvent.KEYCODE_2:
                return 2;
            case KeyEvent.KEYCODE_3:
                return 3;
            case KeyEvent.KEYCODE_4:
                return 4;
            case KeyEvent.KEYCODE_5:
                return 5;
            case KeyEvent.KEYCODE_6:
                return 6;
            case KeyEvent.KEYCODE_7:
                return 7;
            case KeyEvent.KEYCODE_8:
                return 8;
            case KeyEvent.KEYCODE_9:
                return 9;
            default:
                return -1;
        }
    }


    /**
     * Get the currently-entered time, as integer values of the hours and minutes typed.
     *
     * @param enteredZeros
     *         A size-2 boolean array, which the caller should initialize, and which may then be used for the caller to know whether zeros had been explicitly
     *         entered as either hours of minutes. This is helpful for deciding whether to show the dashes, or actual 0's.
     *
     * @return A size-3 int array. The first value will be the hours, the second value will be the minutes, and the third will be either TimePickerDialog.AM or
     * TimePickerDialog.PM.
     */
    private int[] getEnteredTime(Boolean[] enteredZeros)
    {
        int amOrPm = -1;
        int startIndex = 1;
        if (!mIs24HourMode && isTypedTimeFullyLegal())
        {
            int keyCode = mTypedTimes.get(mTypedTimes.size() - 1);
            if (keyCode == getAmOrPmKeyCode(AM))
            {
                amOrPm = AM;
            }
            else if (keyCode == getAmOrPmKeyCode(PM))
            {
                amOrPm = PM;
            }
            startIndex = 2;
        }
        int minute = -1;
        int hour = -1;
        for (int i = startIndex; i <= mTypedTimes.size(); i++)
        {
            int val = getValFromKeyCode(mTypedTimes.get(mTypedTimes.size() - i));
            if (i == startIndex)
            {
                minute = val;
            }
            else if (i == startIndex + 1)
            {
                minute += 10 * val;
                if (enteredZeros != null && val == 0)
                {
                    enteredZeros[1] = true;
                }
            }
            else if (i == startIndex + 2)
            {
                hour = val;
            }
            else if (i == startIndex + 3)
            {
                hour += 10 * val;
                if (enteredZeros != null && val == 0)
                {
                    enteredZeros[0] = true;
                }
            }
        }

        int[] ret = { hour, minute, amOrPm };
        return ret;
    }


    /**
     * Get the keycode value for AM and PM in the current language.
     */
    private int getAmOrPmKeyCode(int amOrPm)
    {
        // Cache the codes.
        if (mAmKeyCode == -1 || mPmKeyCode == -1)
        {
            // Find the first character in the AM/PM text that is unique.
            KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
            char amChar;
            char pmChar;
            for (int i = 0; i < Math.max(mAmText.length(), mPmText.length()); i++)
            {
                amChar = mAmText.toLowerCase(Locale.getDefault()).charAt(i);
                pmChar = mPmText.toLowerCase(Locale.getDefault()).charAt(i);
                if (amChar != pmChar)
                {
                    KeyEvent[] events = kcm.getEvents(new char[] { amChar, pmChar });
                    // There should be 4 events: a down and up for both AM and PM.
                    if (events != null && events.length == 4)
                    {
                        mAmKeyCode = events[0].getKeyCode();
                        mPmKeyCode = events[2].getKeyCode();
                    }
                    else
                    {
                        Log.e(TAG, "Unable to find keycodes for AM and PM.");
                    }
                    break;
                }
            }
        }
        if (amOrPm == AM)
        {
            return mAmKeyCode;
        }
        else if (amOrPm == PM)
        {
            return mPmKeyCode;
        }

        return -1;
    }


    /**
     * Create a tree for deciding what keys can legally be typed.
     */
    private void generateLegalTimesTree()
    {
        // Create a quick cache of numbers to their keycodes.
        int k0 = KeyEvent.KEYCODE_0;
        int k1 = KeyEvent.KEYCODE_1;
        int k2 = KeyEvent.KEYCODE_2;
        int k3 = KeyEvent.KEYCODE_3;
        int k4 = KeyEvent.KEYCODE_4;
        int k5 = KeyEvent.KEYCODE_5;
        int k6 = KeyEvent.KEYCODE_6;
        int k7 = KeyEvent.KEYCODE_7;
        int k8 = KeyEvent.KEYCODE_8;
        int k9 = KeyEvent.KEYCODE_9;

        // The root of the tree doesn't contain any numbers.
        mLegalTimesTree = new Node();
        if (mIs24HourMode)
        {
            // We'll be re-using these nodes, so we'll save them.
            Node minuteFirstDigit = new Node(k0, k1, k2, k3, k4, k5);
            Node minuteSecondDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            // The first digit must be followed by the second digit.
            minuteFirstDigit.addChild(minuteSecondDigit);

            // The first digit may be 0-1.
            Node firstDigit = new Node(k0, k1);
            mLegalTimesTree.addChild(firstDigit);

            // When the first digit is 0-1, the second digit may be 0-5.
            Node secondDigit = new Node(k0, k1, k2, k3, k4, k5);
            firstDigit.addChild(secondDigit);
            // We may now be followed by the first minute digit. E.g. 00:09, 15:58.
            secondDigit.addChild(minuteFirstDigit);

            // When the first digit is 0-1, and the second digit is 0-5, the third digit may be 6-9.
            Node thirdDigit = new Node(k6, k7, k8, k9);
            // The time must now be finished. E.g. 0:55, 1:08.
            secondDigit.addChild(thirdDigit);

            // When the first digit is 0-1, the second digit may be 6-9.
            secondDigit = new Node(k6, k7, k8, k9);
            firstDigit.addChild(secondDigit);
            // We must now be followed by the first minute digit. E.g. 06:50, 18:20.
            secondDigit.addChild(minuteFirstDigit);

            // The first digit may be 2.
            firstDigit = new Node(k2);
            mLegalTimesTree.addChild(firstDigit);

            // When the first digit is 2, the second digit may be 0-3.
            secondDigit = new Node(k0, k1, k2, k3);
            firstDigit.addChild(secondDigit);
            // We must now be followed by the first minute digit. E.g. 20:50, 23:09.
            secondDigit.addChild(minuteFirstDigit);

            // When the first digit is 2, the second digit may be 4-5.
            secondDigit = new Node(k4, k5);
            firstDigit.addChild(secondDigit);
            // We must now be followd by the last minute digit. E.g. 2:40, 2:53.
            secondDigit.addChild(minuteSecondDigit);

            // The first digit may be 3-9.
            firstDigit = new Node(k3, k4, k5, k6, k7, k8, k9);
            mLegalTimesTree.addChild(firstDigit);
            // We must now be followed by the first minute digit. E.g. 3:57, 8:12.
            firstDigit.addChild(minuteFirstDigit);
        }
        else
        {
            // We'll need to use the AM/PM node a lot.
            // Set up AM and PM to respond to "a" and "p".
            Node ampm = new Node(getAmOrPmKeyCode(AM), getAmOrPmKeyCode(PM));

            // The first hour digit may be 1.
            Node firstDigit = new Node(k1);
            mLegalTimesTree.addChild(firstDigit);
            // We'll allow quick input of on-the-hour times. E.g. 1pm.
            firstDigit.addChild(ampm);

            // When the first digit is 1, the second digit may be 0-2.
            Node secondDigit = new Node(k0, k1, k2);
            firstDigit.addChild(secondDigit);
            // Also for quick input of on-the-hour times. E.g. 10pm, 12am.
            secondDigit.addChild(ampm);

            // When the first digit is 1, and the second digit is 0-2, the third digit may be 0-5.
            Node thirdDigit = new Node(k0, k1, k2, k3, k4, k5);
            secondDigit.addChild(thirdDigit);
            // The time may be finished now. E.g. 1:02pm, 1:25am.
            thirdDigit.addChild(ampm);

            // When the first digit is 1, the second digit is 0-2, and the third digit is 0-5,
            // the fourth digit may be 0-9.
            Node fourthDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            thirdDigit.addChild(fourthDigit);
            // The time must be finished now. E.g. 10:49am, 12:40pm.
            fourthDigit.addChild(ampm);

            // When the first digit is 1, and the second digit is 0-2, the third digit may be 6-9.
            thirdDigit = new Node(k6, k7, k8, k9);
            secondDigit.addChild(thirdDigit);
            // The time must be finished now. E.g. 1:08am, 1:26pm.
            thirdDigit.addChild(ampm);

            // When the first digit is 1, the second digit may be 3-5.
            secondDigit = new Node(k3, k4, k5);
            firstDigit.addChild(secondDigit);

            // When the first digit is 1, and the second digit is 3-5, the third digit may be 0-9.
            thirdDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            secondDigit.addChild(thirdDigit);
            // The time must be finished now. E.g. 1:39am, 1:50pm.
            thirdDigit.addChild(ampm);

            // The hour digit may be 2-9.
            firstDigit = new Node(k2, k3, k4, k5, k6, k7, k8, k9);
            mLegalTimesTree.addChild(firstDigit);
            // We'll allow quick input of on-the-hour-times. E.g. 2am, 5pm.
            firstDigit.addChild(ampm);

            // When the first digit is 2-9, the second digit may be 0-5.
            secondDigit = new Node(k0, k1, k2, k3, k4, k5);
            firstDigit.addChild(secondDigit);

            // When the first digit is 2-9, and the second digit is 0-5, the third digit may be 0-9.
            thirdDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
            secondDigit.addChild(thirdDigit);
            // The time must be finished now. E.g. 2:57am, 9:30pm.
            thirdDigit.addChild(ampm);
        }
    }


    /**
     * Simple node class to be used for traversal to check for legal times. mLegalKeys represents the keys that can be typed to get to the node. mChildren are
     * the children that can be reached from this node.
     */
    private class Node
    {
        private int[] mLegalKeys;
        private ArrayList<Node> mChildren;


        public Node(int... legalKeys)
        {
            mLegalKeys = legalKeys;
            mChildren = new ArrayList<Node>();
        }


        public void addChild(Node child)
        {
            mChildren.add(child);
        }


        public boolean containsKey(int key)
        {
            for (int i = 0; i < mLegalKeys.length; i++)
            {
                if (mLegalKeys[i] == key)
                {
                    return true;
                }
            }
            return false;
        }


        public Node canReach(int key)
        {
            if (mChildren == null)
            {
                return null;
            }
            for (Node child : mChildren)
            {
                if (child.containsKey(key))
                {
                    return child;
                }
            }
            return null;
        }
    }


    private class KeyboardListener implements OnKeyListener
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if (event.getAction() == KeyEvent.ACTION_UP)
            {
                return processKeyUp(keyCode);
            }
            return false;
        }
    }


    @Override
    public void onGlobalLayout()
    {
        mSetTimeButton.setPivotX(mSetTimeButton.getWidth() * 0.5f);
        mSetTimeButton.setPivotY(mSetTimeButton.getHeight() * 0.75f);

        mTimeDisplay.setPivotX(mTimeDisplay.getWidth() * 0.5f);
        mTimeDisplay.setPivotY(mTimeDisplay.getHeight() * 0.75f);

        mToAllDay.setPivotX(mToAllDay.getWidth() * 0.5f);
        mToAllDay.setPivotY(mToAllDay.getHeight() * 0.75f);

    }
}
