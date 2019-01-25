/*
 * Copyright (C) 2016 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.android.instantpicker.date;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.dmfs.android.instantpicker.DateTimePicker;
import org.dmfs.android.instantpicker.OnDateTimeSetListener;
import org.dmfs.android.instantpicker.PickerContext;
import org.dmfs.android.instantpicker.Utils;
import org.dmfs.rfc5545.DateTime;


/**
 * An abstract class that provides a simple date display component.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class AbstractDisplay extends FrameLayout implements OnClickListener, DateTimePicker, ViewTreeObserver.OnGlobalLayoutListener
{
    private final static float ALPHA_INACTIVE = 0.75f;

    /**
     * The current
     */
    private EditorComponent mCurrentEditorComponent = null;

    /**
     * The DateTime that's currently displayed.
     */
    private DateTime mDateTime;

    /**
     * The display TextView.
     */
    private TextView mTextView;

    private OnPickerStateChangeListener mOnPickerStateChangeListener;


    public AbstractDisplay(Context context)
    {
        super(context);
        init(context, null, 0);
    }


    public AbstractDisplay(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        init(context, attributeSet, 0);
    }


    public AbstractDisplay(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);
        init(context, attributeSet, defStyle);
    }


    private void init(Context context, AttributeSet attributeSet, int defStyle)
    {
        View root = LayoutInflater.from(context).inflate(getViewResource(), this, true);

        mTextView = (TextView) root.findViewById(android.R.id.title);
        mTextView.setOnClickListener(this);
        mTextView.setAlpha(ALPHA_INACTIVE);

        updateDisplay();

        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }


    protected abstract EditorComponent displayState();

    protected abstract int getViewResource();

    protected abstract String getText();


    @Override
    public void updateState(EditorComponent editorComponent)
    {
        EditorComponent displayEditorComponent = displayState();
        if (editorComponent == mCurrentEditorComponent || (editorComponent != displayEditorComponent && mCurrentEditorComponent != displayEditorComponent))
        {
            // the editor component did not change or this display is not affected
            return;
        }

        if (mCurrentEditorComponent == null && editorComponent == displayEditorComponent)
        {
            // make sure the initial selection is without animation
            mTextView.setScaleX(1f);
            mTextView.setScaleY(1f);
            mTextView.setAlpha(1f);
        }
        else if (editorComponent == displayEditorComponent)
        {
            Utils.getGrowAnimator(mTextView, 0.75f, true, null).start();
        }
        else
        {
            Utils.getShrinkAnimator(mTextView, 0.75f, true, null).start();
        }
        mCurrentEditorComponent = editorComponent;
    }


    @Override
    public void setDateTime(DateTime dateTime)
    {
        if (dateTime != null && dateTime.equals(mDateTime) || dateTime == null && mDateTime == null)
        {
            // nothing to do, datetime didn't change
            return;
        }

        if (mDateTime != null && dateTime != null && fieldsChanged(mDateTime, dateTime) && mCurrentEditorComponent != displayState())
        {
            ObjectAnimator pulseAnimator = Utils.getPulseAnimator(mTextView, 0.85f, 1.1f, 0.75f);
            pulseAnimator.start();
        }
        mDateTime = dateTime;
        updateDisplay();
    }


    @Override
    public void setOnDateTimeSetListener(OnDateTimeSetListener onDateTimeSetListener)
    {
        // we don't support setting anything, so the listener would never be called
    }


    @Override
    public void setOnPickerStateChangeListener(OnPickerStateChangeListener onPickerStateChangeListener)
    {
        mOnPickerStateChangeListener = onPickerStateChangeListener;
    }


    private void updateDisplay()
    {
        mTextView.setText(mDateTime != null ? getText() : "");
    }


    @Override
    public void onClick(View v)
    {
        if (v.getId() == android.R.id.title)
        {
            if (mOnPickerStateChangeListener != null)
            {
                mOnPickerStateChangeListener.onPickerStateChange(displayState());
            }
        }
    }


    @Override
    public void setPickerContext(PickerContext pickerContext)
    {
        // ignore for now
        // TODO: implement this as soon as a subclass needs the PickerContext
    }


    /**
     * Returns the {@link DateTime} this display is currently showing.
     *
     * @return A {@link DateTime}, may be {@code null} if none has been set yet.
     */
    protected DateTime dateTime()
    {
        return mDateTime;
    }


    @Override
    public void onGlobalLayout()
    {
        mTextView.setPivotY(mTextView.getHeight() * 0.75f);
    }


    protected abstract boolean fieldsChanged(DateTime oldValue, DateTime newValue);
}
