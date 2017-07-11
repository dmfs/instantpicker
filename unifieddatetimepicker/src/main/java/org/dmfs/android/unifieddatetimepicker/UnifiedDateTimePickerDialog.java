package org.dmfs.android.unifieddatetimepicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ViewAnimator;

import org.dmfs.android.unifieddatetimepicker.date.AccessibleDateAnimator;
import org.dmfs.android.unifieddatetimepicker.date.DateDisplay;
import org.dmfs.android.unifieddatetimepicker.date.SimpleDatePicker;
import org.dmfs.android.unifieddatetimepicker.date.YearDisplay;
import org.dmfs.android.unifieddatetimepicker.date.YearPicker;
import org.dmfs.android.unifieddatetimepicker.events.Event;
import org.dmfs.android.unifieddatetimepicker.events.EventProviderFactory;
import org.dmfs.android.unifieddatetimepicker.events.EventsProvider;
import org.dmfs.android.unifieddatetimepicker.time.RadialTimePicker;
import org.dmfs.android.unifieddatetimepicker.time.TimeDisplay;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Weekday;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;


/**
 * Created by marten on 12.12.15.
 */
public final class UnifiedDateTimePickerDialog extends DialogFragment
        implements DateTimePicker.OnPickerStateChangeListener, OnDateTimeSetListener, View.OnClickListener, PickerContext
{
    private static final int ANIMATION_DURATION = 300;

    private final static String ARG_EVENT_PROVIDER_FACTORIES = "event_provider_factories";
    private final static String ARG_INITIAL_TIMESTAMP = "initial_timestamp";
    private final static String ARG_INITIAL_TIMEZONE = "initial_timezone";
    private final static String ARG_INITIAL_ALLDAY = "initial_allday";
    private final static String ARG_INITIAL_PICKER = "initial_picker";
    private final static String ARG_ALLOW_ALLDAY = "allow_allday";
    private final static String ARG_USE_24_HOURS = "use_24_hours";
    private final static String ARG_DEFAULT_TIMEZONE = "default_timezone";
    private final static String ARG_FIRST_DAY_OF_WEEK = "first_day_of_week";
    private final static String ARG_CURRENT_PICKER = "current_picker";

    private boolean mAllowAllDay;
    private boolean mUse24Hours;
    private Weekday mFirstDayOfWeek;
    private TimeZone mDefaultTimeZone;
    private DateTime mMinDate = new DateTime(1900, 0, 1);
    private DateTime mMaxDate = new DateTime(2100, 0, 1);
    private DateTimePicker.EditorComponent mInitialEditor = DateTimePicker.EditorComponent.MONTH_AND_DAY;

    private View mDialogView;
    private ViewAnimator mAnimator;
    private DateTimePicker mYearPicker;
    private DateTimePicker mTimePicker;
    private DateTimePicker mDatePicker;

    private DateTime mCurrentDateTime;

    private List<DateTimePicker> mDateTimePickers = new ArrayList<DateTimePicker>(8);
    private EventsProvider[] mEventProviders;

    private DateTimePicker.EditorComponent mEditorComponent;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        init(getArguments());

        if (savedInstanceState != null)
        {
            if (savedInstanceState.getBoolean(ARG_INITIAL_ALLDAY, false))
            {
                mCurrentDateTime = new DateTime(savedInstanceState.getLong(ARG_INITIAL_TIMESTAMP)).toAllDay();
            }
            else
            {
                mCurrentDateTime = new DateTime((TimeZone) savedInstanceState.getSerializable(ARG_INITIAL_TIMEZONE),
                        savedInstanceState.getLong(ARG_INITIAL_TIMESTAMP));
            }

            mEditorComponent = (DateTimePicker.EditorComponent) savedInstanceState.getSerializable(ARG_CURRENT_PICKER);
        }
    }


    private void init(Bundle args)
    {
        if (args.getBoolean(ARG_INITIAL_ALLDAY, false))
        {
            mCurrentDateTime = new DateTime(args.getLong(ARG_INITIAL_TIMESTAMP)).toAllDay();
        }
        else
        {
            mCurrentDateTime = new DateTime((TimeZone) args.getSerializable(ARG_INITIAL_TIMEZONE), args.getLong(ARG_INITIAL_TIMESTAMP));
        }

        mAllowAllDay = args.getBoolean(ARG_ALLOW_ALLDAY, true);
        mUse24Hours = args.getBoolean(ARG_USE_24_HOURS, DateFormat.is24HourFormat(getContext()));
        if (args.containsKey(ARG_FIRST_DAY_OF_WEEK))
        {
            mFirstDayOfWeek = (Weekday) args.getSerializable(ARG_FIRST_DAY_OF_WEEK);
        }
        else
        {
            mFirstDayOfWeek = Weekday.values()[Calendar.getInstance().getFirstDayOfWeek() - 1];
        }
        if (args.containsKey(ARG_INITIAL_PICKER))
        {
            mInitialEditor = (DateTimePicker.EditorComponent) args.getSerializable(ARG_INITIAL_PICKER);
        }
        if (args.containsKey(ARG_DEFAULT_TIMEZONE))
        {
            mDefaultTimeZone = (TimeZone) args.getSerializable(ARG_DEFAULT_TIMEZONE);
        }

        if (args.containsKey(ARG_EVENT_PROVIDER_FACTORIES))
        {
            Context context = getContext();
            EventProviderFactory[] factories = (EventProviderFactory[]) args.getSerializable(ARG_EVENT_PROVIDER_FACTORIES);

            mEventProviders = new EventsProvider[factories.length];
            for (int i = 0, count = factories.length; i < count; ++i)
            {
                mEventProviders[i] = factories[i].create(context);
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mDialogView = inflater.inflate(R.layout.udtp_dialog, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        Context context = getContext();

        initPicker((YearDisplay) mDialogView.findViewById(R.id.year_display));
        initPicker((DateDisplay) mDialogView.findViewById(R.id.date_display));
        initPicker((TimeDisplay) mDialogView.findViewById(R.id.time_display));

        mYearPicker = initPicker(new YearPicker(context));
        mDatePicker = initPicker(new SimpleDatePicker(context, this));
        mTimePicker = initPicker(new RadialTimePicker(context));

        mAnimator = (AccessibleDateAnimator) mDialogView.findViewById(R.id.animator);
        mAnimator.addView((View) mTimePicker);
        mAnimator.addView((View) mDatePicker);
        mAnimator.addView((View) mYearPicker);

        // set initial picker before we set up animations
        onPickerStateChange(mEditorComponent == null ? mInitialEditor : mEditorComponent);

        Animation inAnimation = new AlphaAnimation(0.0f, 1.0f);
        inAnimation.setDuration(ANIMATION_DURATION);
        mAnimator.setInAnimation(inAnimation);

        Animation outAnimation = new AlphaAnimation(1.0f, 0.0f);
        outAnimation.setDuration(ANIMATION_DURATION);
        mAnimator.setOutAnimation(outAnimation);

        mDialogView.findViewById(R.id.cancel).setOnClickListener(this);
        mDialogView.findViewById(R.id.ok).setOnClickListener(this);

        return mDialogView;
    }


    private DateTimePicker initPicker(DateTimePicker picker)
    {
        picker.setPickerContext(this);
        picker.setDateTime(mCurrentDateTime);
        picker.setOnPickerStateChangeListener(this);
        picker.setOnDateTimeSetListener(this);
        mDateTimePickers.add(picker);
        return picker;
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(ARG_INITIAL_TIMESTAMP, mCurrentDateTime.getTimestamp());
        outState.putBoolean(ARG_INITIAL_ALLDAY, mCurrentDateTime.isAllDay());
        outState.putSerializable(ARG_INITIAL_TIMEZONE, mCurrentDateTime.getTimeZone());
        outState.putSerializable(ARG_CURRENT_PICKER, mEditorComponent);
    }


    private int fetchPrimaryColor()
    {
        TypedValue typedValue = new TypedValue();

        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimary });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }


    public UnifiedDateTimePickerDialog setDateTime(DateTime dateTime)
    {
        mCurrentDateTime = dateTime;
        for (DateTimePicker picker : mDateTimePickers)
        {
            picker.setDateTime(dateTime);
        }
        return this;
    }


    @Override
    public void onPickerStateChange(DateTimePicker.EditorComponent editorComponent)
    {
        mEditorComponent = editorComponent;
        switch (editorComponent)
        {
            case MINUTES:
            case HOURS:
                setDisplayedPicker(0);
                break;
            case MONTH_AND_DAY:
                setDisplayedPicker(1);
                break;
            case YEAR:
                setDisplayedPicker(2);
                break;
        }
        for (DateTimePicker picker : mDateTimePickers)
        {
            picker.updateState(editorComponent);
        }
    }


    private void setDisplayedPicker(int picker)
    {
        if (mAnimator.getDisplayedChild() != picker)
        {
            mAnimator.setDisplayedChild(picker);
        }
    }


    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        if (id == R.id.ok)
        {
            // find a listener and call it
            OnDateTimeSetListener listener = null;
            if (getParentFragment() instanceof OnDateTimeSetListener)
            {
                listener = (OnDateTimeSetListener) getParentFragment();
            }
            else if (getActivity() instanceof OnDateTimeSetListener)
            {
                listener = (OnDateTimeSetListener) getActivity();
            }
            if (listener != null)
            {
                listener.onDateTimeSet(mCurrentDateTime);
            }

            dismiss();
        }
        else if (id == R.id.cancel)
        {
            dismiss();
        }
    }


    @Override
    public void onDateTimeSet(DateTime dateTime)
    {
        setDateTime(dateTime);
    }


    @Override
    public DateTime minDateTime()
    {
        return mMinDate;
    }


    @Override
    public DateTime maxDateTime()
    {
        return mMaxDate;
    }


    @Override
    public Weekday firstDayOfWeek()
    {
        return mFirstDayOfWeek;
    }


    @Override
    public boolean use24Hours()
    {
        return mUse24Hours;
    }


    @Override
    public boolean hasEvents(DateTime date)
    {
        if (mEventProviders == null || mEventProviders.length == 0)
        {
            return false;
        }
        for (EventsProvider provider : mEventProviders)
        {
            if (provider.hasEventsOn(date))
            {
                return true;
            }
        }
        return false;
    }


    @Override
    public List<Event> events(DateTime date)
    {
        if (!hasEvents(date))
        {
            return Collections.emptyList();
        }
        List<Event> events = new ArrayList<Event>(8);
        for (EventsProvider provider : mEventProviders)
        {
            provider.appendEventsTo(date, events);
        }

        return events;
    }


    @Override
    public TimeZone defaultTimeZone()
    {
        return mDefaultTimeZone;
    }


    public static class Builder
    {
        private final DateTime mInitialDate;
        private DateTimePicker.EditorComponent mInitialPicker = DateTimePicker.EditorComponent.MONTH_AND_DAY;
        private TimeZone mDefaultTimeZone = TimeZone.getDefault();
        private DateTime mMinDate;
        private DateTime mMaxDate;
        private Weekday mFirstDayOfWeek;
        private boolean mAllowAllDay;
        private Boolean mUse24Hours = null;
        private EventProviderFactory[] mEventProviderFactories;


        /**
         * Creates a new {@link UnifiedDateTimePickerDialog.Builder} starting with the given initial {@link DateTime}. If no initial date is given it will
         * default to {@link DateTime#today()}.
         *
         * @param initialDate
         */
        public Builder(@Nullable DateTime initialDate)
        {
            mInitialDate = initialDate == null ? DateTime.today() : initialDate;
        }


        /**
         * Set the default {@link TimeZone} to use when adding a time. Defaults to the local time zone if not set. Set to {@code null} to use floating dates.
         *
         * @param timeZone
         *         The default {@link TimeZone} or {@code null}.
         */
        public Builder setDefaultTimezone(@Nullable TimeZone timeZone)
        {
            mDefaultTimeZone = timeZone;
            return this;
        }


        /**
         * Set the initial picker.
         *
         * @param initialPicker
         *
         * @return
         */
        public Builder setInitialPicker(@NonNull DateTimePicker.EditorComponent initialPicker)
        {
            mInitialPicker = initialPicker;
            return this;
        }


        /**
         * Set the minimal date that the user can pick. Defaults to 1900-01-01.
         *
         * @param minDate
         */
        public Builder setMinDate(DateTime minDate)
        {
            mMinDate = minDate;
            return this;
        }


        /**
         * Set the maximal date that the user can pick. Defaults to 2100-01-01.
         *
         * @param maxDate
         */
        public Builder setMaxDate(DateTime maxDate)
        {
            mMaxDate = maxDate;
            return this;
        }


        /**
         * Set whether a time component must be present in the result or not.
         *
         * @param allowAllDay
         *         {@code true} to allow dates without a time component, {@code false} if all-day events are not allowed.
         */
        public Builder allowAllDay(boolean allowAllDay)
        {
            mAllowAllDay = allowAllDay;
            return this;
        }


        /**
         * Set one or multiple {@link EventProviderFactory}s to provide additional information about the days.
         *
         * @param factories
         */
        public Builder setEventProviderFactories(EventProviderFactory... factories)
        {
            mEventProviderFactories = factories;
            return this;
        }


        public Builder setFirstDayOfWeek(Weekday firstDayOfWeek)
        {
            mFirstDayOfWeek = firstDayOfWeek;
            return this;
        }


        public UnifiedDateTimePickerDialog build()
        {
            Bundle args = new Bundle();

            args.putLong(ARG_INITIAL_TIMESTAMP, mInitialDate.getTimestamp());
            args.putBoolean(ARG_INITIAL_ALLDAY, mInitialDate.isAllDay());
            args.putSerializable(ARG_INITIAL_TIMEZONE, mInitialDate.getTimeZone());

            args.putBoolean(ARG_ALLOW_ALLDAY, mAllowAllDay);
            if (mUse24Hours != null)
            {
                args.putBoolean(ARG_USE_24_HOURS, mUse24Hours);
            }
            if (mFirstDayOfWeek != null)
            {
                args.putSerializable(ARG_FIRST_DAY_OF_WEEK, mFirstDayOfWeek);
            }
            if (mInitialPicker != null)
            {
                args.putSerializable(ARG_INITIAL_PICKER, mInitialPicker);
            }

            if (mEventProviderFactories != null && mEventProviderFactories.length > 0)
            {
                args.putSerializable(ARG_EVENT_PROVIDER_FACTORIES, mEventProviderFactories);
            }

            // always add the default time zone, because null is a valid default value
            args.putSerializable(ARG_DEFAULT_TIMEZONE, mDefaultTimeZone);

            UnifiedDateTimePickerDialog result = new UnifiedDateTimePickerDialog();
            result.setArguments(args);
            return result;
        }
    }
}
