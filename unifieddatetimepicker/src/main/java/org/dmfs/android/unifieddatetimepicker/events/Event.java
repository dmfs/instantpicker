package org.dmfs.android.unifieddatetimepicker.events;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;


/**
 * Created by marten on 17.12.15.
 */
public interface Event
{
    /**
     * Returns the date and time of the event.
     *
     * @return
     */
    public DateTime start();

    /**
     * Returns the Duration of the event.
     *
     * @return
     */
    public Duration duration();

    /**
     * Returns the title of the event.
     *
     * @return
     */
    public String title();

}
