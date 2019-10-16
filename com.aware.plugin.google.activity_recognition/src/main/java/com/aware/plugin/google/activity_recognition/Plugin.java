
package com.aware.plugin.google.activity_recognition;

import android.accounts.Account;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SyncRequest;
import android.os.Bundle;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

public class Plugin extends Aware_Plugin implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static String ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION = "ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION";
    public static String EXTRA_ACTIVITY = "activity";
    public static String EXTRA_CONFIDENCE = "confidence";

    private static GoogleApiClient gARClient;
    private static PendingIntent gARPending;

    public static int current_activity = -1;
    public static int current_confidence = -1;

    @Override
    public void onCreate() {
        super.onCreate();

        AUTHORITY = Google_AR_Provider.getAuthority(this);

        TAG = "AWARE::Google Activity Recognition";

        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                Intent context = new Intent(ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION);
                context.putExtra(EXTRA_ACTIVITY, current_activity);
                context.putExtra(EXTRA_CONFIDENCE, current_confidence);
                sendBroadcast(context);
            }
        };

        if (!is_google_services_available()) {
            if (DEBUG)
                Log.e(TAG, "Google Services is not available on this device.");
        } else {
            gARClient = new GoogleApiClient.Builder(this)
                    .addApiIfAvailable(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            Intent gARIntent = new Intent(getApplicationContext(), com.aware.plugin.google.activity_recognition.Algorithm.class);
            gARPending = PendingIntent.getService(getApplicationContext(), 0, gARIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    /**
     * Allow callback to other applications when data is stored in provider
     */
    private static AWARESensorObserver awareSensor;

    public static void setSensorObserver(AWARESensorObserver observer) {
        awareSensor = observer;
    }

    public static AWARESensorObserver getSensorObserver() {
        return awareSensor;
    }

    /**
     * Callbacks when activities are detected
     */
    public interface AWARESensorObserver {
        void onActivityChanged(ContentValues data);

        void isRunning(int confidence);

        void isWalking(int confidence);

        void isStill(int confidence);

        void isBycicle(int confidence);

        void isVehicle(int confidence);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (PERMISSIONS_OK) {
            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            if (Aware.getSetting(getApplicationContext(), Settings.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION).length() == 0) {
                Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, true);
            } else {
                if (Aware.getSetting(getApplicationContext(), Settings.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION).equalsIgnoreCase("false")) {
                    Aware.stopPlugin(getApplicationContext(), getPackageName());
                    return START_STICKY;
                }
            }

            if (Aware.getSetting(this, Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION).length() == 0) {
                Aware.setSetting(this, Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, 60);
            }
            if (gARClient != null && !gARClient.isConnected()) gARClient.connect();

            if (Aware.isStudy(this)) {
                Account aware_account = Aware.getAWAREAccount(getApplicationContext());
                String authority = Google_AR_Provider.getAuthority(getApplicationContext());
                long frequency = Long.parseLong(Aware.getSetting(this, Aware_Preferences.FREQUENCY_WEBSERVICE)) * 60;

                ContentResolver.setIsSyncable(aware_account, authority, 1);
                ContentResolver.setSyncAutomatically(aware_account, authority, true);
                SyncRequest request = new SyncRequest.Builder()
                        .syncPeriodic(frequency, frequency / 3)
                        .setSyncAdapter(aware_account, authority)
                        .setExtras(new Bundle()).build();
                ContentResolver.requestSync(request);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ContentResolver.setSyncAutomatically(Aware.getAWAREAccount(this), Google_AR_Provider.getAuthority(this), false);
        ContentResolver.removePeriodicSync(
                Aware.getAWAREAccount(this),
                Google_AR_Provider.getAuthority(this),
                Bundle.EMPTY
        );

        Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, false);

        //we might get here if phone doesn't support Google Services
        if (gARClient != null && gARClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(gARClient, gARPending);
            gARClient.disconnect();
        }
    }

    private boolean is_google_services_available() {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int result = googleApi.isGooglePlayServicesAvailable(this);
        return (result == ConnectionResult.SUCCESS);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connection_result) {
        if (DEBUG)
            Log.w(TAG, "Error connecting to Google's activity recognition services, will try again in 5 minutes");
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (DEBUG)
            Log.i(TAG, "Connected to Google's Activity Recognition API");

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(gARClient, Long.valueOf(Aware.getSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION)) * 1000, gARPending);
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (DEBUG)
            Log.w(TAG, "Error connecting to Google's activity recognition services, will try again in 5 minutes");

    }
}
