package org.dmfs.android.unifieddatetimepicker.events;

import android.content.Context;

import org.dmfs.rfc5545.Weekday;


/**
 * A factory that returns {@link WeekendProvider}s with a specific weekend day.
 */
public class WeekendProviderFactory implements EventProviderFactory
{
    private final static long serialVersionUID = 0L;

    /**
     * A specific WeekendProviderFactory that returns a {@link WeekendProvider} for {@link Weekday#SU}.
     */
    public final static WeekendProviderFactory SUNDAY_WEEKEND_PROVIDER_FACTORY = new WeekendProviderFactory(Weekday.SU);

    private final Weekday mWeekEndDay;


    public WeekendProviderFactory(Weekday weekendDay)
    {
        mWeekEndDay = weekendDay;
    }


    @Override
    public EventsProvider create(Context context)
    {
        return new WeekendProvider(mWeekEndDay);
    }
}
