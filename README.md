AWARE: Google Activity Recognition
=========================================

[![Release](https://jitpack.io/v/denzilferreira/com.aware.plugin.google.activity_recognition.svg)](https://jitpack.io/#denzilferreira/com.aware.plugin.google.activity_recognition)

This plugin uses Google Location API's to capture users mode of transportation: still, walking, running, biking, in vehicle (car, bus).

# Settings
Parameters adjustable on the dashboard and client: 
- **status_plugin_google_activity_recognition**: (boolean) activate/deactivate plugin
- **frequency_plugin_google_activity_recognition**: (integer) How frequently to detect user's activity (in seconds)

# Broadcasts
**ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION**
Broadcasted when we have a new activity inference, with the following extras:
- **activity**: (int) the current [DetectedActivity](https://developers.google.com/android/reference/com/google/android/gms/location/DetectedActivity)
- **confidence**: (int) how confident is the prediction (0-100%)

# Providers
##  Google Activity Recognition Data
> content://com.aware.plugin.google.activity_recognition.provider.gar/plugin_google_activity_recognition

Field | Type | Description
----- | ---- | -----------
_id | INTEGER | primary key auto-incremented
timestamp | REAL | unix timestamp in milliseconds of sample
device_id | TEXT | AWARE device ID
activity_name | TEXT | human-readable activity name: unknown, tilting, on_foot, in_vehicle, on_bicycle, running, walking
activity_type	| INTEGER | one of [DetectedActivity](https://developers.google.com/android/reference/com/google/android/gms/location/DetectedActivity)
confidence | INTEGER |	prediction accuracy (0-100%)
activities | TEXT | JSON array with other potential activities, e.g., [{'activity':'walking','confidence':90},...]

