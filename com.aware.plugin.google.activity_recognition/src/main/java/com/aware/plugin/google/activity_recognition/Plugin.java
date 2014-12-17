/**
@author: denzil
 */
package com.aware.plugin.google.activity_recognition;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.aware.Aware;
import com.aware.plugin.google.activity_recognition.Google_AR_Provider.Google_Activity_Recognition_Data;
import com.aware.utils.Aware_Plugin;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

public class Plugin extends Aware_Plugin implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	public static String ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION = "ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION";
	public static String EXTRA_ACTIVITY = "activity";
	public static String EXTRA_CONFIDENCE = "confidence";

	private PendingIntent gARPending = null;
    private GoogleApiClient googleClient = null;

	public static int current_activity = -1;
	public static int current_confidence = -1;

	@Override
	public void onCreate() {
		super.onCreate();

		TAG = "AWARE::Google Activity Recognition";
		DEBUG = true;

		DATABASE_TABLES = Google_AR_Provider.DATABASE_TABLES;
		TABLES_FIELDS = Google_AR_Provider.TABLES_FIELDS;
		CONTEXT_URIS = new Uri[]{ Google_Activity_Recognition_Data.CONTENT_URI };

		CONTEXT_PRODUCER = new ContextProducer() {
			@Override
			public void onContext() {
				Intent context = new Intent(ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION);
				context.putExtra(EXTRA_ACTIVITY, current_activity);
				context.putExtra(EXTRA_CONFIDENCE, current_confidence);
				sendBroadcast(context);
			}
		};

		Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, true);
		if( Aware.getSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION).length() == 0 ) {
			Aware.setSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, 60);
		} else {
			Aware.setSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, Aware.getSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION));
		}
	
		Intent refresh = new Intent(Aware.ACTION_AWARE_REFRESH);
		sendBroadcast(refresh);
	
		if ( ! is_google_services_available() ) {
			Log.e(TAG,"Google Services activity recognition not available on this device.");
			stopSelf();
		} else {
            googleClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(ActivityRecognition.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

			Intent gARIntent = new Intent();
			gARIntent.setClassName(getPackageName(), getPackageName() + ".Algorithm");
			gARPending = PendingIntent.getService(this, 0, gARIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			if( ! googleClient.isConnected() && ! googleClient.isConnecting() ) googleClient.connect();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if( ! googleClient.isConnected() && ! googleClient.isConnecting() ) googleClient.connect();
		if( googleClient.isConnected() ) {
            if( Aware.getSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION).length() == 0 ) {
                Aware.setSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, 60);
            }
			ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleClient, Long.valueOf(Aware.getSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION)) * 1000, gARPending);
		}
		
		if( Aware.getSetting(getApplicationContext(), Settings.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION).equals("false") ) {
			stopSelf();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

        Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, false);

		//we might get here if phone doesn't support Google Services
		if ( googleClient != null && googleClient.isConnected() ) {
			ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(googleClient, gARPending);
            googleClient.disconnect();
		}
	}

	private boolean is_google_services_available() {
		if ( ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) ) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connection_result) {
		if( connection_result.hasResolution() ) {
			if( ! googleClient.isConnected() || ! googleClient.isConnecting() ) googleClient.connect();
		} else {
			Log.w(TAG,"Error connecting to Google's activity recognition services, will try again in 5 minutes");
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleClient, Long.valueOf(Aware.getSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION)) * 1000, gARPending);
	}

    @Override
    public void onConnectionSuspended(int i) {
        if( ! googleClient.isConnected() || ! googleClient.isConnecting() ) googleClient.connect();
    }
}
