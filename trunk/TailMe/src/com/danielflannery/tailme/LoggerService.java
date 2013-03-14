/* Author: Dan
 * Summary: This service is ran from the LoggerActivity. It records the users
 * location until the service is ended via a button in the logger activity.
 */

package com.danielflannery.tailme;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class LoggerService extends Service implements LocationListener {
    static final String TAG = "LoggerService";
    private ArrayList<Location> coordinatesList;
    private Intent activityIntent;
    private String saveName;
    private SharedPreferences sharedPrefs;
    private LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        coordinatesList = new ArrayList<Location>();    // Arraylist to hold the location details
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this); // Getting a reference to the DefaultSharedPreferences class
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.d(TAG, "onCreate"); // Debugging
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        activityIntent = intent;
        // Obtain parameters from sharedPreferences
        long logging_interval = Long.parseLong(sharedPrefs.getString("logging_interval", "5000"));
        float accuracy = Float.parseFloat(sharedPrefs.getString("accuracy", "1"));

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, logging_interval,
                accuracy, this);
        if (sharedPrefs.getBoolean("network_provider_state", false) == false) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, logging_interval,
                    accuracy, this);
        }

        Log.d(TAG, "onStarted"); // Debugging
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (activityIntent.getFlags() == -1) {
            try {
                activityIntent.getExtras();
                Bundle bundle = activityIntent.getExtras();
                saveName = bundle.getString("1");   // Get the saveName passed in the bundle
                try {
                    saveCsv();  // Call the case csv method
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "File save failed: " + ex,
                            Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, saveName);
            } catch (Exception ee) {
            }
            Log.d(TAG, "onDestroyed with an OK"); // Debugging
        }
        else if (activityIntent.getFlags() == 0) {
            Log.d(TAG, "onDestroyed with a CANCEL"); // Debugging
        }
        locationManager.removeUpdates(this);
    }

    private void saveCsv() {

        // Check to see if folder exists
        File folder = new File(Environment.getExternalStorageDirectory() + "/TailMe");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        // Create the file 
        if (success) {
            try {
                File myFile = new File(Environment.getExternalStorageDirectory() + "/TailMe/",
                        saveName + ".csv");
                int x = 1;
                while (myFile.exists()) {
                    File tempFile = new File(
                            Environment.getExternalStorageDirectory() + "/TailMe/", saveName + "("
                                    + x + ")" + ".csv");
                    myFile.renameTo(tempFile);
                    x++;

                }
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

                Log.d(TAG, "writing to CSV!"); // Debugging
                for (int i = 0; i < coordinatesList.size(); i++)
                {

                    // Provider
                    myOutWriter.append(coordinatesList.get(i).getProvider());
                    myOutWriter.append(',');
                    // Latitude
                    Double tempLat = coordinatesList.get(i).getLatitude();
                    myOutWriter.append(tempLat.toString());
                    myOutWriter.append(',');
                    // Longtitude
                    Double tempLong = coordinatesList.get(i).getLongitude();
                    myOutWriter.append(tempLong.toString());
                    myOutWriter.append(',');
                    // Accuracy
                    Float tempAccuracy = coordinatesList.get(i).getAccuracy();
                    myOutWriter.append(tempAccuracy.toString());
                    myOutWriter.append(',');
                    // Speed
                    Float tempSpeed = coordinatesList.get(i).getSpeed();
                    myOutWriter.append(tempSpeed.toString());
                    myOutWriter.append(',');

                    myOutWriter.append('\n');

                }
                myOutWriter.close();
                fOut.close();
                Log.d(TAG, "CSV SAVED!"); // Debugging
                

            } catch (Exception e) {

            }
        } else {
            Toast.makeText(getApplicationContext(), "Folder creation failed.", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        // confirming that the location is different than the last location. ( not needed!! )
        if (location.equals(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
                || location.equals(locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER))) {
            Log.d(TAG, "Identical location.");
        }
        
        coordinatesList.add(location); // Add the location to the arraylist
        Log.d(TAG, "Location saved to arrayList from: " + location.getProvider());

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}
