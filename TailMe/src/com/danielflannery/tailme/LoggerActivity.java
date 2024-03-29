/* Author: Dan
 * Summary: This activity controls the launch of the logging service and
 * the saving of the recorded log.
 */

package com.danielflannery.tailme;

import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoggerActivity extends Activity {

    private String saveName = "";   // Desired saved log name
    private Intent serviceIntent;   // The logger service Intent
    private int switchCase = 0; // Keeping track of button state
    private boolean active = false; // Keeping track of whether the service is running or not
    private Bundle bundle = new Bundle();;  // Bundle to pass extras to the service
    private static final int NOTIFY_ME_ID = 1001;   // Notification message ID
    private NotificationManager notifyMgr;  // NotificationManager object
    private LocationManager locationManager;    // Location manager
    private SharedPreferences prefs;
    private Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logger);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        // Obtaining references to location and notification services
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        checkGPSStatus(); // Check if GPS is enabled on the device
        checkButtons(); // Create the buttons and add listeners

    }

    // Overriding the back key so the activity isint destroyed on press.
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {

        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (active == true) {
                moveTaskToBack(true);
                return true; // return
            }
            else {
                finish();
            }

        }

        return false;
    }

    // Create a dialogbox to allow the user to name their log
    private void showDialog() {
        AlertDialog.Builder alertdg = new AlertDialog.Builder(this);
        alertdg.setTitle("Name your saved log:");
        alertdg.setMessage("Choose a save name:");
        final EditText episode = new EditText(this);
        episode.setWidth(430);
        LinearLayout layout = new LinearLayout(this);
        layout.addView(episode);
        alertdg.setView(layout);
        alertdg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Obtaining entered filename and passing it to the service.
                saveName = episode.getText().toString();
                bundle.putString("1", saveName);
                serviceIntent.putExtras(bundle);
                serviceIntent.putExtra(android.content.Intent.EXTRA_TEXT, saveName);
                startService(serviceIntent.addFlags(RESULT_OK));
                stopService(serviceIntent);
                if(prefs.getBoolean("instructions_toggle", true) == true){
                    showInstructions();
                }
                
            }
        });

        alertdg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopService(serviceIntent.addFlags(RESULT_CANCELED));
            }
        });
        alertdg.show();

    }

    // Create the notification for the logging service
    public void triggerNotification(View v) {
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.large_icon);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                LoggerActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notify = new NotificationCompat.Builder(this)
                .setContentTitle("Touch to interact")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pIntent)
                .setLargeIcon(largeIcon).build();

        notify.flags |= Notification.FLAG_ONGOING_EVENT;
        // Set default vibration
        notify.defaults |= Notification.DEFAULT_VIBRATE;
        // Status remains when the user clicks it
        notify.flags |= Notification.FLAG_NO_CLEAR;
        // Send notification
        notifyMgr.notify(NOTIFY_ME_ID, notify);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_logger, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(getApplicationContext(),
                        com.danielflannery.tailme.Settings.class);
                startActivityForResult(intent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Check to see if GPS is disabled and alert the user if it is
    private void checkGPSStatus() { 
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            // Building an alert dialog to display location settings.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("GPS must be enabled to Track your location.")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(settingsIntent);
                        }
                    })
                    .setNegativeButton("Return", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    // A method to handle the button in the activity
    private void checkButtons() {
        final Button button1 = (Button) findViewById(R.id.toggle_button);
        final ProgressBar progbar1 = (ProgressBar) findViewById(R.id.progressBar1);
        final TextView tv = (TextView) findViewById(R.id.loggerText);
        tv.setTextSize(16);
        tv.setText("Logging service is currently not running.");

        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                serviceIntent = new Intent(v.getContext(), LoggerService.class);

                switch (switchCase) {
                    case 0:

                        startService(serviceIntent);
                        triggerNotification(v);
                        progbar1.setVisibility(View.VISIBLE);
                        tv.setText("Logging service started. Happy trails!");
                        button1.setText("Stop Tailing Me");
                        active = true;
                        switchCase = 1;
                        break;

                    case 1:
                        notifyMgr.cancel(NOTIFY_ME_ID);
                        active = false;
                        showDialog();
                        button1.setText("Start Tailing Me");
                        progbar1.setVisibility(View.INVISIBLE);
                        tv.setText("Logging service not runnning atm.");
                        switchCase = 0;
                        break;
                }
            }
 
        });
    }
    // Building an alert dialog to display Instructions
    private void showInstructions() {
        
        AlertDialog.Builder instructions = new AlertDialog.Builder(this);
        instructions.setTitle("Path saved sucessfully!")
        .setMessage("Now what? Head over to code.google.com/p/tail-me and download the Desktop App to view your paths.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton("Dont show again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putBoolean("instructions_toggle", false);
                        editor.commit();
                    }
                });
        AlertDialog alert = instructions.create();
        alert.show();
        
    } 
}
