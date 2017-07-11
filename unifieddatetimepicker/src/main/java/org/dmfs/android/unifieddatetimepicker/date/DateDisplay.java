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
import android.text.format.DateUtils;
import android.util.AttributeSet;

import org.dmfs.android.unifieddatetimepicker.R;
import org.dmfs.rfc5545.DateTime;

import java.util.Formatter;
import java.util.TimeZone;


/**
 * Dialog allowing users to select a date.
 */
public class DateDisplay extends AbstractDisplay
{
    private final static TimeZone TIME_ZONE_UTC = TimeZone.getTimeZone("UTC");

    private final static int DATE_FORMAT_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_ALL
            | DateUtils.FORMAT_SHOW_WEEKDAY;


    public DateDisplay(Context context)
    {
        super(context);
    }


    public DateDisplay(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
    }


    public DateDisplay(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);
    }


    @Override
    protected EditorComponent displayState()
    {
        return EditorComponent.MONTH_AND_DAY;
    }


    @Override
    protected int getViewResource()
    {
        return R.layout.udtp_month_and_day_display;
    }


    @Override
    protected String getText()
    {
        DateTime dateTime = dateTime();

        // watch out: the timestamp returned by floating events will be in UTC
        TimeZone timeZone = dateTime.isFloating() ? TIME_ZONE_UTC : dateTime.getTimeZone();

        Formatter formatter = new Formatter();

        return DateUtils.formatDateRange(getContext(), formatter, dateTime.getTimestamp(), dateTime.getTimestamp(), DATE_FORMAT_FLAGS, timeZone.getID())
                .toString();
    }


    @Override
    protected boolean fieldsChanged(DateTime oldValue, DateTime newValue)
    {
        return oldValue.getMonth() != newValue.getMonth() || oldValue.getDayOfMonth() != newValue.getDayOfMonth();
    }

}
