package org.dmfs.android.unifieddatetimepicker.events;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.Weekday;

import java.util.List;


/**
 * Created by marten on 17.12.15.
 */
public class WeekendProvider implements EventsProvider
{
    private final static Duration ONE_DAY_DURATION = new Duration(1, 1, 0);
    private final Weekday mWeekendDay;


    public WeekendProvider(Weekday weekendDay)
    {
        mWeekendDay = weekendDay;
    }


    @Override
    public boolean hasEventsOn(DateTime day)
    {
        return day.getDayOfWeek() == mWeekendDay.ordinal();
    }


    @Override
    public void appendEventsTo(DateTime day, List<Event> events)
    {
        if (day.getDayOfWeek() != mWeekendDay.ordinal())
        {
            return;
        }

        events.add(new SimpleEvent(day.toAllDay(), ONE_DAY_DURATION));
    }


    @Override
    public void setOnLoadListener(OnLoadListener listener)
    {
        // this never gets loaded, so no need to store the listener
    }
}
