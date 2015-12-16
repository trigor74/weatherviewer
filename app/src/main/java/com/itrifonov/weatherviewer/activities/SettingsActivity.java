package com.itrifonov.weatherviewer.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.models.Settings;
import com.itrifonov.weatherviewer.services.ServiceHelper;

import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsActivity extends AppCompatActivity {

    private Realm realm;
    private EditText apiKey;
    private EditText city;
    private SwitchCompat delOldSwitch;
    private SwitchCompat startUpdateService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        realm = Realm.getDefaultInstance();
        apiKey = (EditText) findViewById(R.id.edit_text_api_key);
        city = (EditText) findViewById(R.id.edit_text_city_name);
        delOldSwitch = (SwitchCompat) findViewById(R.id.switch_delete_old_data);
        startUpdateService = (SwitchCompat) findViewById(R.id.switch_start_update_service);
        startUpdateService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ServiceHelper.getInstance(getApplicationContext()).startNotificationService();
                } else {
                    ServiceHelper.getInstance(getApplicationContext()).stopNotificationService();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RealmResults<Settings> realmResults = realm.allObjects(Settings.class);
        if (realmResults.size() != 0) {
            Settings settings = realmResults.first();
            if (apiKey != null) {
                apiKey.setText(settings.getApiKey());
            }
            if (city != null) {
                city.setText(settings.getCity());
            }
            if (delOldSwitch != null) {
                delOldSwitch.setChecked(settings.getDeleteOldData());
            }
            if (startUpdateService != null) {
                startUpdateService.setChecked(settings.getStartUpdateService());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        realm.beginTransaction();
        Settings settings = realm.where(Settings.class).findFirst();
        if (apiKey != null) {
            settings.setApiKey(apiKey.getText().toString());
        }
        if (city != null) {
            settings.setCity(city.getText().toString());
        }
        if (delOldSwitch != null) {
            settings.setDeleteOldData(delOldSwitch.isChecked());
        }
        if (startUpdateService != null) {
            settings.setStartUpdateService(startUpdateService.isChecked());
        }
        realm.commitTransaction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
