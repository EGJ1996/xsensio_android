package com.xsensio.nfcsensorcomm.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.xsensio.nfcsensorcomm.R;

/**
 * Activity to display the application settings
 */
public class SettingsActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
