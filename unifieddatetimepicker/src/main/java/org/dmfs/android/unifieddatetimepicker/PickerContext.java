package org.dmfs.android.unifieddatetimepicker;

import org.dmfs.android.unifieddatetimepicker.events.Event;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Weekday;

import java.util.List;
import java.util.TimeZone;


/**
 * Created by marten on 13.12.15.
 */
public interface PickerContext
{
    DateTime minDateTime();

    DateTime maxDateTime();

    Weekday firstDayOfWeek();

    boolean use24Hours();

    boolean hasEvents(DateTime date);

    List<Event> events(DateTime date);

    TimeZone defaultTimeZone();
}
