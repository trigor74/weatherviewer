package com.itrifonov.weatherviewer.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Switch;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.models.Settings;

import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsActivity extends AppCompatActivity {

    private Realm realm;
    private EditText apiKey;
    private EditText city;
    private Switch delOldSwitch;

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
        delOldSwitch = (Switch) findViewById(R.id.switch_delete_old_data);
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
        realm.commitTransaction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
