package com.itrifonov.weatherviewer.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.fragments.ForecastDetailFragment;
import com.itrifonov.weatherviewer.fragments.ForecastListFragment;
import com.itrifonov.weatherviewer.interfaces.IOnListItemSelectedListener;
import com.itrifonov.weatherviewer.services.interfaces.IServiceHelperCallbackListener;
import com.itrifonov.weatherviewer.models.Settings;
import com.itrifonov.weatherviewer.services.ServiceHelper;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity
        implements IOnListItemSelectedListener {

    private long timestamp = -1;
    private static final String STATE_TIMESTAMP = "STATE_TIMESTAMP";
    private Boolean hasData = false;
    private Realm realm;
    private IServiceHelperCallbackListener callback = new IServiceHelperCallbackListener() {
        @Override
        public void onServiceHelperCallback(Bundle event) {
            int eventId = event.getInt(ServiceHelper.SERVICE_HELPER_EVENT, -1);
            if (eventId != ServiceHelper.EVENT_UPDATE_STOPPED)
                return;

            ProgressBar progress = (ProgressBar) findViewById(R.id.progress_missing_forecast);
            if (progress != null)
                progress.setVisibility(TextView.INVISIBLE);
            if (hasData && timestamp == -1)
                timestamp = 0;
            TextView textMissingForecast = (TextView) findViewById(R.id.text_view_missing_forecast);
            if (textMissingForecast != null)
                textMissingForecast.setVisibility(TextView.INVISIBLE);
            if (isTabletLandscapeMode() && hasData) {
                ForecastDetailFragment detailFragment = (ForecastDetailFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.forecast_detail);
                if (detailFragment != null)
                    detailFragment.update(timestamp);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realm = Realm.getDefaultInstance();

        setDefaultSettings();

        RealmResults<ForecastListItem> realmResults = realm.allObjects(ForecastListItem.class);
        hasData = realmResults.size() != 0;

        ServiceHelper serviceHelper = ServiceHelper.getInstance(getApplicationContext());
        Settings settings = realm.where(Settings.class).findFirst();
        if (settings != null) {
            if (settings.getStartUpdateService()) {
                serviceHelper.startNotificationService();
            } else {
                serviceHelper.stopNotificationService();
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_weather_viewer);
        }

        TextView textMissingForecast = (TextView) findViewById(R.id.text_view_missing_forecast);
        ProgressBar progress = (ProgressBar) findViewById(R.id.progress_missing_forecast);
        if (hasData) {
            timestamp = 0;
            if (textMissingForecast != null)
                textMissingForecast.setVisibility(TextView.INVISIBLE);
            if (progress != null)
                progress.setVisibility(TextView.INVISIBLE);
        } else {
            timestamp = -1;
            if (textMissingForecast != null)
                textMissingForecast.setVisibility(TextView.VISIBLE);
            if (progress != null)
                progress.setVisibility(TextView.VISIBLE);
        }

        ForecastDetailFragment detailFragment = (ForecastDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.forecast_detail);

        if (savedInstanceState != null) {
            timestamp = savedInstanceState.getInt(STATE_TIMESTAMP, -1);
            if (!isTabletLandscapeMode() && hasData) {
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra(DetailActivity.ARG_TIMESTAMP, timestamp);
                startActivity(intent);
            }
        }
        if (hasData
                && isTabletLandscapeMode()
                && (findViewById(R.id.forecast_detail) != null)
                && (detailFragment != null)) {
            detailFragment.update(timestamp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                TextView textMissingForecast = (TextView) findViewById(R.id.text_view_missing_forecast);
                ProgressBar progress = (ProgressBar) findViewById(R.id.progress_missing_forecast);
                if ((textMissingForecast != null) && (textMissingForecast.getVisibility() == View.VISIBLE) && (progress != null))
                    progress.setVisibility(TextView.VISIBLE);
                ((ForecastListFragment) getSupportFragmentManager().findFragmentById(R.id.forecast_list))
                        .updateWeatherForecast();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemSelected(long ts) {
        timestamp = ts;
        if (findViewById(R.id.forecast_detail) != null) {
            ForecastDetailFragment detailFragment = (ForecastDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.forecast_detail);
            if (detailFragment != null) {
                detailFragment.update(timestamp);
            }
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.ARG_TIMESTAMP, timestamp);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(STATE_TIMESTAMP, timestamp);
        super.onSaveInstanceState(outState);
    }

    private boolean isTabletLandscapeMode() {
        return getResources().getBoolean(R.bool.is_tablet_landscape);
    }

    private void setDefaultSettings() {
        RealmResults<Settings> results = realm.allObjects(Settings.class);
        if (results.size() == 0) {
            realm.beginTransaction();
            Settings settings = realm.createObject(Settings.class);
            settings.setApiKey("28bfbe7a35614f03ddaaf3b091f2a414");
            settings.setCity("Cherkasy,UA");
            settings.setUnits("metric");
            settings.setLastUpdate(-1);
            settings.setDeleteOldData(true);
            settings.setStartUpdateService(true);
            realm.commitTransaction();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ServiceHelper.getInstance(getApplicationContext()).addListener(callback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ServiceHelper.getInstance(getApplicationContext()).removeListener(callback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
