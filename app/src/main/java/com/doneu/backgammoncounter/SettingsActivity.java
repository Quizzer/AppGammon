package com.doneu.backgammoncounter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.doneu.backgammoncounter.utils.Constants;

import java.lang.reflect.Field;

/**
 * Created by doneu on 01.06.17.
 */

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences prefs;

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(BuildConfig.DEBUG) {
                Log.d(Constants.TAG, "onSharedPreferenceChanged: " + key);
            }

            if (key.equals(getString(R.string.prefsThemesList))) {
                Log.d(Constants.TAG, "         |-------> " + prefs.getString(getString(R.string.prefsThemesList), " not available?") );

                this.recreate();
/*
                final Intent intent = getIntent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
                startActivity(intent);
*/
            }
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        /* SettingsActivity cannot extend BaseActivity, so a separate themes (just like in BaseActivity) check is needed here: */
        String theme = prefs.getString(getString(R.string.prefsThemesList), getString(R.string.prefsThemesList_default));
        if(BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "onCreate: SettingsActivity //" + theme);
        }

        try {
            Field resourceField = R.style.class.getDeclaredField(theme);
            int resourceId = resourceField.getInt(resourceField);
            setTheme(resourceId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        prefs.registerOnSharedPreferenceChangeListener(this);

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

    }

    @Override
    protected void onDestroy() {

        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
}
