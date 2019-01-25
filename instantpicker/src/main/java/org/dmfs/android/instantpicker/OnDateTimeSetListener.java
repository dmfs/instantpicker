package org.dmfs.android.instantpicker;

import org.dmfs.rfc5545.DateTime;


/**
 * The callback interface used to indicate the user is done filling in the time (they clicked on the 'Set' button).
 */
public interface OnDateTimeSetListener
{

    /**
     * @param dateTime
     *         The {@link DateTime} with the new time.
     */
    void onDateTimeSet(DateTime dateTime);
}
