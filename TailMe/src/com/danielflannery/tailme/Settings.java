/* Author: Dan
 * Summary: This activity is used to display the settings of the application. 
 * Any changes made here will be saved to sharedPreferences. 
 * Extending PreferenceActivity is just a convenience class that derives from 
 * ListActivity and allows to bootstrap the preferences UI from an XML file.
 */

package com.danielflannery.tailme;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the preferences xml file
        addPreferencesFromResource(R.xml.preferences);
    }
}
