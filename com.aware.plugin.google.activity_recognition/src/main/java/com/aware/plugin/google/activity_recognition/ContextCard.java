package com.aware.plugin.google.activity_recognition;

import java.util.Calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aware.utils.Converters;

/**
 * New Stream UI cards<br/>
 * Implement here what you see on your Plugin's UI.
 * @author denzilferreira
 */
public class ContextCard {
	
	public static View getContextCard(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mInflated = (View) inflater.inflate(R.layout.layout, null);
		
		TextView still = (TextView) mInflated.findViewById(R.id.time_still);
        TextView walking = (TextView) mInflated.findViewById(R.id.time_walking);
        TextView biking = (TextView) mInflated.findViewById(R.id.time_biking);
        TextView vehicle = (TextView) mInflated.findViewById(R.id.time_vehicle);
        
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        
        //Modify time to be at the begining of today
        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        
        //Get stats for today
        still.setText(Converters.readable_elapsed(Stats.getTimeStill(context.getContentResolver(), mCalendar.getTimeInMillis(), System.currentTimeMillis())));
        walking.setText(Converters.readable_elapsed(Stats.getTimeWalking(context.getContentResolver(), mCalendar.getTimeInMillis(), System.currentTimeMillis())));
        biking.setText(Converters.readable_elapsed(Stats.getTimeBiking(context.getContentResolver(), mCalendar.getTimeInMillis(), System.currentTimeMillis())));
        vehicle.setText(Converters.readable_elapsed(Stats.getTimeVehicle(context.getContentResolver(), mCalendar.getTimeInMillis(), System.currentTimeMillis())));
		return mInflated;
	}
}
