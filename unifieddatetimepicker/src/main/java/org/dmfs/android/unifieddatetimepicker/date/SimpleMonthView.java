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
import android.graphics.Canvas;

import org.dmfs.rfc5545.DateTime;


public class SimpleMonthView extends MonthView
{

    public SimpleMonthView(Context context)
    {
        super(context);
    }


    @Override
    public void drawMonthDay(Canvas canvas, DateTime date, int x, int y, int startX, int stopX, int startY, int stopY)
    {
        if (mSelectedDay == date.getDayOfMonth())
        {
            canvas.drawCircle(x, y - (MINI_DAY_NUMBER_TEXT_SIZE / 3), DAY_SELECTED_CIRCLE_SIZE, mSelectedCirclePaint);
        }

        // If we have a mindate or maxdate, gray out the day number if it's outside the range.
        if (isOutOfRange(date))
        {
            mMonthNumPaint.setColor(mDisabledDayTextColor);
        }
        else if (mSelectedDay == date.getDayOfMonth())
        {
            mMonthNumPaint.setColor(mSelectedDayTextColor);
        }
        else if (mPickerContext.hasEvents(date))
        {
            mMonthNumPaint.setColor(mTodayNumberColor);
        }
        else if (mHasToday && mToday == date.getDayOfMonth())
        {
            mMonthNumPaint.setColor(mSelectedDayColor);
        }
        else
        {
            mMonthNumPaint.setColor(mDayTextColor);
        }
        canvas.drawText(String.format("%d", date.getDayOfMonth()), x, y, mMonthNumPaint);
    }
}
