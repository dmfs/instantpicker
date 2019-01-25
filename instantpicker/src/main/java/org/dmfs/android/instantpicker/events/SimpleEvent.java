package org.dmfs.android.instantpicker.events;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;


/**
 * Created by marten on 17.12.15.
 */
public class SimpleEvent implements Event
{
    private final DateTime mDateTime;
    private final Duration mDuration;
    private final String mTitle;


    public SimpleEvent(DateTime dateTime, Duration duration)
    {
        this(dateTime, duration, null);
    }


    public SimpleEvent(DateTime dateTime, Duration duration, String title)
    {
        mDateTime = dateTime;
        mDuration = duration;
        mTitle = title;
    }


    @Override
    public DateTime start()
    {
        return mDateTime;
    }


    @Override
    public Duration duration()
    {
        return mDuration;
    }


    @Override
    public String title()
    {
        return mTitle;
    }
}
