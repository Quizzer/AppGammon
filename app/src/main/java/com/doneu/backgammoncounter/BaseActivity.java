package com.doneu.backgammoncounter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import java.lang.reflect.Field;

    public abstract class BaseActivity extends AppCompatActivity {

        String theme;

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            theme = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.prefsThemesList), getString(R.string.prefsThemesList_default));
            try {
                Field resourceField = R.style.class.getDeclaredField(theme);
                int resourceId = resourceField.getInt(resourceField);
                setTheme(resourceId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            super.onCreate(savedInstanceState);
        }

        @Override
        protected void onResume() {
            super.onResume();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (!theme.equals(prefs.getString(getString(R.string.prefsThemesList), " none "))) {
                this.recreate();
            }
        }
    }