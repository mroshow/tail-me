
package com.danielflannery.tailme;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class Map extends FragmentActivity implements LocationListener {

    private LocationManager locationManager;
    private GoogleMap googlemap;    //Create the map
    private Location location;
    private String provider = LocationManager.GPS_PROVIDER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //something
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
                            enableLocationSettings();
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 3000, 20, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(
                getApplicationContext(),
                "Location change detected! Lat: " + location.getLatitude() + "Lang: "
                        + location.getLongitude() + " Altidude: " + location.getAltitude(),
                Toast.LENGTH_SHORT).show();
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        googlemap.animateCamera(CameraUpdateFactory.newLatLng(ll));
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getApplicationContext(), "Gps Disabled",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private void initMap()
    {
        SupportMapFragment mf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(
                R.id.map);
        googlemap = mf.getMap();
        googlemap.setMyLocationEnabled(true);
        googlemap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        try {
            location = locationManager.getLastKnownLocation(provider);
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 8));
        } catch (Exception e) {
            LatLng templl = new LatLng(53.330873,-13.007812);
            googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(templl, 1));
        }
    }

    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

}
