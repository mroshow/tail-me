/* Author: Dan
 * Summary: This activity is used to view the users location on
 * GoogleMaps. When a location change is detected, the view will move to center
 * on the new location.
 */
package com.danielflannery.tailme;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class Map extends FragmentActivity implements LocationListener {

    private LocationManager locationManager;
    private GoogleMap googlemap;
    private Location location; 
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain a reference to the location service.
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); 
        // Getting a reference to the DefaultSharedPreferences class
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this); 
        initMap();  //Build the map

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check to see if GPS is disabled and alert the user if it is
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            // Building an alert dialog to display location settings.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("GPS is disabled, would you like to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(settingsIntent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        
        // Obtain parameters from sharedPreferences
        long logging_interval = Long.parseLong(sharedPrefs.getString("logging_interval", "5000"));
        float accuracy = Float.parseFloat(sharedPrefs.getString("accuracy", "1"));
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, logging_interval,
                accuracy, this);
        if (sharedPrefs.getBoolean("network_provider_state", false) == false) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, logging_interval,
                    accuracy, this);
        }

    }
    
    private void initMap()
    {
        // Setting up the googlemap
        SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(
                R.id.map);
        googlemap = mf.getMap();
        googlemap.setMyLocationEnabled(true);
        googlemap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        try {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 8));
        } catch (Exception e) {
            LatLng templl = new LatLng(53.330873,-13.007812);
            googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(templl, 1));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);    //Remove the locationListener
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude()); // Obtain the latitude and longitude from the location object
        googlemap.animateCamera(CameraUpdateFactory.newLatLng(ll)); // Move the camera to the new coordinates.
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
