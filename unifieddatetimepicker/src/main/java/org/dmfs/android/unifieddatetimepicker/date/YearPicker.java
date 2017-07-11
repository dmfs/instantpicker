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

package org.dmfs.android.unifieddatetimepicker.date;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.dmfs.android.unifieddatetimepicker.DateTimePicker;
import org.dmfs.android.unifieddatetimepicker.OnDateTimeSetListener;
import org.dmfs.android.unifieddatetimepicker.PickerContext;
import org.dmfs.android.unifieddatetimepicker.R;
import org.dmfs.rfc5545.DateTime;

import java.util.ArrayList;
import java.util.List;


/**
 * Displays a selectable list of years.
 */
public class YearPicker extends ListView implements OnItemClickListener, DateTimePicker
{
    private static final String TAG = "YearPicker";

    private Paint mDividerPaint = new Paint();
    private YearAdapter mAdapter;
    private int mViewSize;
    private int mChildSize;
    private TextView mSelectedView;
    private DateTime mDateTime;

    private OnDateTimeSetListener mOnDateTimeSetListener;
    private OnPickerStateChangeListener mOnPickerStateChangeListener;

    private EditorComponent mCurrentEditorComponent;

    private PickerContext mPickerContext;


    /**
     * @param context
     */
    public YearPicker(Context context)
    {
        super(context);
        ViewGroup.LayoutParams frame = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(frame);
        Resources res = context.getResources();
        mViewSize = res.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height);
        mChildSize = res.getDimensionPixelOffset(R.dimen.year_label_height);
        setOnItemClickListener(this);
        setSelector(new StateListDrawable());
        setDividerHeight(0);
    }


    @Override
    public void setPickerContext(PickerContext pickerContext)
    {
        mPickerContext = pickerContext;
        init(getContext());
    }


    private void init(Context context)
    {
        ArrayList<String> years = new ArrayList<String>();
        for (int year = mPickerContext.minDateTime().getYear(); year <= mPickerContext.maxDateTime().getYear(); year++)
        {
            years.add(String.format("%d", year));
        }
        mAdapter = new YearAdapter(context, R.layout.year_label_text_view, years);
        setAdapter(mAdapter);

        mDividerPaint.setColor(0);
        mDividerPaint.setAlpha(128);
        mDividerPaint.setStrokeWidth(1f);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        TextView clickedView = (TextView) view;
        if (clickedView != null)
        {
            if (clickedView != mSelectedView)
            {
                if (mSelectedView != null)
                {
                    mSelectedView.setTextAppearance(getContext(), R.style.udtp_year_label);
                    mSelectedView.requestLayout();
                }
                clickedView.setTextAppearance(getContext(), R.style.udtp_year_label_focused);
                clickedView.requestLayout();
                mSelectedView = clickedView;
            }

            // TODO: this should be handled by DateTime
            int newYear = position + mPickerContext.minDateTime().getYear();
            int month = mDateTime.getMonth();
            int day = mDateTime.getDayOfMonth();

            int maxDay = mDateTime.getCalendarMetrics().getDaysPerPackedMonth(newYear, month);
            day = Math.min(day, maxDay);

            if (mDateTime.isAllDay())
            {
                mDateTime = new DateTime(mDateTime.getCalendarMetrics(), newYear, month, day);
            }
            else
            {
                mDateTime = new DateTime(mDateTime.getCalendarMetrics(), mDateTime.getTimeZone(), newYear, month, day, mDateTime.getHours(),
                        mDateTime.getMinutes(), mDateTime.getSeconds());
            }

            if (mOnDateTimeSetListener != null)
            {
                mOnDateTimeSetListener.onDateTimeSet(mDateTime);
            }

            mCurrentEditorComponent = EditorComponent.MONTH_AND_DAY;
            if (mOnPickerStateChangeListener != null)
            {
                mOnPickerStateChangeListener.onPickerStateChange(mCurrentEditorComponent);
            }

            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void setDateTime(DateTime dateTime)
    {
        mDateTime = dateTime;
        if (mAdapter != null)
        {
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void updateState(EditorComponent editorComponent)
    {
        postSetSelectionCentered(mDateTime.getYear() - mPickerContext.minDateTime().getYear());
        mCurrentEditorComponent = editorComponent;
    }


    @Override
    public void setOnDateTimeSetListener(OnDateTimeSetListener onDateTimeSetListener)
    {
        mOnDateTimeSetListener = onDateTimeSetListener;
    }


    @Override
    public void setOnPickerStateChangeListener(OnPickerStateChangeListener onPickerStateChangeListener)
    {
        mOnPickerStateChangeListener = onPickerStateChangeListener;
    }


    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        canvas.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1, mDividerPaint);
    }


    private class YearAdapter extends ArrayAdapter<String>
    {

        public YearAdapter(Context context, int resource, List<String> objects)
        {
            super(context, resource, objects);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            TextView v = (TextView) super.getView(position, convertView, parent);
            boolean selected = mDateTime.getYear() == position + mPickerContext.minDateTime().getYear();
            v.setTextAppearance(getContext(), selected ? R.style.udtp_year_label_focused : R.style.udtp_year_label);
            if (selected)
            {
                mSelectedView = v;
            }
            return v;
        }

    }


    public void postSetSelectionCentered(final int position)
    {
        postSetSelectionFromTop(position, mViewSize / 3 - mChildSize / 2);
    }


    private void postSetSelectionFromTop(final int position, final int offset)
    {
        post(new Runnable()
        {
            @Override
            public void run()
            {
                setSelectionFromTop(position, offset);
                requestLayout();
            }
        });
    }


    public int getFirstPositionOffset()
    {
        final View firstChild = getChildAt(0);
        if (firstChild == null)
        {
            return 0;
        }
        return firstChild.getTop();
    }


    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event)
    {
        super.onInitializeAccessibilityEvent(event);
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED)
        {
            event.setFromIndex(0);
            event.setToIndex(0);
        }
    }
}
