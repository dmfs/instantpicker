package org.dmfs.android.instantpicker.events;

import org.dmfs.rfc5545.DateTime;

import java.util.List;


/**
 * Created by marten on 17.12.15.
 */
public interface EventsProvider
{
    boolean hasEventsOn(DateTime day);

    void appendEventsTo(DateTime day, List<Event> events);

    void setOnLoadListener(OnLoadListener listener);

    public interface OnLoadListener
    {
        public void onLoad(EventsProvider provider);
    }
}
