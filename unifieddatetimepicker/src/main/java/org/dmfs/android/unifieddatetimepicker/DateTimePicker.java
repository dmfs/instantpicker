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

package org.dmfs.android.unifieddatetimepicker;

import org.dmfs.rfc5545.DateTime;


/**
 * Created by marten on 11.12.15.
 */
public interface DateTimePicker
{
    public enum EditorComponent
    {
        HOURS, MINUTES, YEAR, MONTH_AND_DAY;
    }

    void setDateTime(DateTime dateTime);

    void setPickerContext(PickerContext pickerContext);

    void updateState(EditorComponent editorComponent);

    void setOnDateTimeSetListener(OnDateTimeSetListener onDateTimeSetListener);

    void setOnPickerStateChangeListener(OnPickerStateChangeListener onPickerStateChangeListener);

    public interface OnPickerStateChangeListener
    {
        void onPickerStateChange(EditorComponent editorComponent);
    }
}
