/* Author: Dan
 * Summary: This is the main activity, It produces 2 buttons to launch a
 * map and logging activity
 */

package com.danielflannery.tailme;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View topLevelLayout = findViewById(R.id.top_layout);
        topLevelLayout.setVisibility(View.VISIBLE);

        // Starting the map activity from the map button
        final Button mapButton = (Button) findViewById(R.id.button1);
        mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Map Loading...", Toast.LENGTH_SHORT)
                        .show();
                Intent intent = new Intent(v.getContext(), Map.class);
                startActivityForResult(intent, 0);
            }
        });

        // Starting the logger activity the startTailingMe button
        final Button loggerButton = (Button) findViewById(R.id.log_button);
        loggerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LoggerActivity.class);
                startActivity(intent);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_about:
                Intent intent = new Intent(getApplicationContext(), About.class);
                startActivityForResult(intent, 0);
                return true;
            case R.id.menu_settings:
                Intent intent2 = new Intent(getApplicationContext(), Settings.class);
                startActivityForResult(intent2, 0);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
