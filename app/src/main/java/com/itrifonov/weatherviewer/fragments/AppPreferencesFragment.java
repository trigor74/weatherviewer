package com.itrifonov.weatherviewer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.services.ServiceHelper;

public class AppPreferencesFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        findPreference(getString(R.string.settings_appid_key))
                .setSummary(preferences.getString(getString(R.string.settings_appid_key),
                        getString(R.string.settings_appid_default)));

        findPreference(getString(R.string.settings_city_key))
                .setSummary(preferences.getString(getString(R.string.settings_city_key),
                        getString(R.string.settings_city_default)));

        findPreference(getString(R.string.settings_units_key))
                .setSummary(preferences.getString(getString(R.string.settings_units_key),
                        getString(R.string.settings_units_default)));

        findPreference(getString(R.string.settings_language_key))
                .setSummary(preferences.getString(getString(R.string.settings_language_key),
                        getString(R.string.settings_language_default)));

        findPreference(getString(R.string.settings_notification_interval_key))
                .setSummary(preferences.getString(getString(R.string.settings_notification_interval_key),
                        getString(R.string.settings_notification_interval_default)));
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
        if (key.equals(getString(R.string.settings_appid_key))) {
            findPreference(key).setSummary(sharedPreferences.getString(key, getString(R.string.settings_appid_default)));
        }
        if (key.equals(getString(R.string.settings_city_key))) {
            findPreference(key).setSummary(sharedPreferences.getString(key, getString(R.string.settings_city_default)));
        }
        if (key.equals(getString(R.string.settings_units_key))) {
            findPreference(key).setSummary(sharedPreferences.getString(key, getString(R.string.settings_units_default)));
        }
        if (key.equals(getString(R.string.settings_language_key))) {
            findPreference(key).setSummary(sharedPreferences.getString(key, getString(R.string.settings_language_default)));
        }
        if (key.equals(getString(R.string.settings_notification_interval_key))) {
            findPreference(key).setSummary(sharedPreferences.getString(key, getString(R.string.settings_notification_interval_default)));

        }
        if (key.equals(getString(R.string.settings_show_notifications_key))
                || key.equals(getString(R.string.settings_notification_interval_key))) { // also if interval has been changed - start service for change timer
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