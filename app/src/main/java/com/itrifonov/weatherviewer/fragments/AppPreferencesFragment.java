package com.itrifonov.weatherviewer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.services.ServiceHelper;

public class AppPreferencesFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_language_key))) {
            Preference langPref = findPreference(key);
            langPref.setSummary(sharedPreferences.getString(key, ""));
        } else {
            if (key.equals(getString(R.string.settings_show_notifications_key))) {
                Boolean showNotifications = sharedPreferences.getBoolean(
                        getString(R.string.settings_show_notifications_key),
                        getResources().getBoolean(R.bool.settings_show_notifications_default));
                if (showNotifications) {
                    ServiceHelper.getInstance(getActivity().getApplicationContext()).startNotificationService();
                } else {
                    ServiceHelper.getInstance(getActivity().getApplicationContext()).stopNotificationService();
                }
            }
        }
    }
}
