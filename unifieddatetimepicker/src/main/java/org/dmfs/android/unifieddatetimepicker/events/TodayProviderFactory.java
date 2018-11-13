/*
 * Copyright 2018 dmfs GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dmfs.android.unifieddatetimepicker.events;

import android.content.Context;


/**
 * A factory that returns {@link WeekendProvider}s with a specific weekend day.
 */
public class TodayProviderFactory implements EventProviderFactory
{
    private final static long serialVersionUID = 0L;

    public final static TodayProviderFactory TODAY_PROVIDER_FACTORY = new TodayProviderFactory();


    @Override
    public EventsProvider create(Context context)
    {
        return new TodayProvider();
    }
}
