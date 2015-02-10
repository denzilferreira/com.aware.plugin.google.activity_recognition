package com.aware.plugin.google.activity_recognition;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.aware.Aware;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	/**
	 * State of Google's Activity Recognition plugin
	 */
	public static final String STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION = "status_plugin_google_activity_recognition";
	
	/**
	 * Frequency of Google's Activity Recognition plugin in seconds<br/>
	 * By default = 60 seconds
	 */
	public static final String FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION = "frequency_plugin_google_activity_recognition";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		syncSettings();
	}
	
	private void syncSettings() {
		CheckBoxPreference check = (CheckBoxPreference) findPreference(STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION);
		check.setChecked(Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION).equals("true"));
		EditTextPreference frequency = (EditTextPreference) findPreference(FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION);
		frequency.setSummary(Aware.getSetting(getApplicationContext(), FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION) + " seconds");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		syncSettings();		
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference preference = findPreference(key);

		if( preference.getKey().equals(STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION) ) {
			boolean is_active = sharedPreferences.getBoolean(key, false);
			Aware.setSetting( getApplicationContext(), key, is_active);
            if( is_active ) {
                Aware.startPlugin(getApplicationContext(), getPackageName());
            } else {
                Aware.stopPlugin(getApplicationContext(), getPackageName());
            }
		}
		if( preference.getKey().equals(FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION)) {
			preference.setSummary( sharedPreferences.getString(key, "60") + " seconds" );
			Aware.setSetting( getApplicationContext(), key, sharedPreferences.getString(key, "60") );
            Aware.startPlugin(getApplicationContext(), getPackageName());
		}

	}	
}
