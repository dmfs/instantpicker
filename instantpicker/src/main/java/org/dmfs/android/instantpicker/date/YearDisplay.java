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

import android.content.Context;
import android.util.AttributeSet;

import org.dmfs.android.instantpicker.R;
import org.dmfs.rfc5545.DateTime;

import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * A display that shows the selected year.
 */
public class YearDisplay extends AbstractDisplay
{

    private static SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.getDefault());


    public YearDisplay(Context context)
    {
        super(context);
    }


    public YearDisplay(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
    }


    public YearDisplay(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);
    }


    @Override
    protected EditorComponent displayState()
    {
        return EditorComponent.YEAR;
    }


    @Override
    protected int getViewResource()
    {
        return R.layout.udtp_year_display;
    }


    @Override
    protected String getText()
    {
        return YEAR_FORMAT.getNumberFormat().format(dateTime().getYear());
    }


    @Override
    protected boolean fieldsChanged(DateTime oldValue, DateTime newValue)
    {
        return oldValue.getYear() != newValue.getYear();
    }
}
