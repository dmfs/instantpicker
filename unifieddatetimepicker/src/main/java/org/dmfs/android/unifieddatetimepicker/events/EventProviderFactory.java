package org.dmfs.android.unifieddatetimepicker.events;

import android.content.Context;

import java.io.Serializable;


/**
 * A Factory that creates specific {@link EventsProvider}s.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface EventProviderFactory extends Serializable
{
    public EventsProvider create(Context context);
}
