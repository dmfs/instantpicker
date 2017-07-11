package org.dmfs.android.unifieddatetimepicker.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.dmfs.android.unifieddatetimepicker.OnDateTimeSetListener;
import org.dmfs.android.unifieddatetimepicker.UnifiedDateTimePickerDialog;
import org.dmfs.android.unifieddatetimepicker.events.WeekendProviderFactory;
import org.dmfs.rfc5545.DateTime;


public class DemoActivity extends AppCompatActivity implements OnDateTimeSetListener
{

	private DateTime mPickedDate;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);
	}


	public void pickDate(View v)
	{
		UnifiedDateTimePickerDialog picker = new UnifiedDateTimePickerDialog.Builder(mPickedDate == null ? DateTime.today() : mPickedDate)
			.setEventProviderFactories(WeekendProviderFactory.SUNDAY_WEEKEND_PROVIDER_FACTORY)//.setDefaultTimezone(TimeZone.getTimeZone("America/New_York"))
			.build();
		picker.show(getSupportFragmentManager(), "");
	}


	@Override
	public void onDateTimeSet(DateTime dateTime)
	{
		mPickedDate = dateTime;
		Toast.makeText(this, dateTime.isFloating() ? dateTime.toString() : dateTime.toString() + " @ " + dateTime.getTimeZone().getID(), Toast.LENGTH_LONG)
			.show();
	}
}
