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
import com.itrifonov.weatherviewer.models.Settings;
import com.itrifonov.weatherviewer.services.WeatherUpdateService;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity
        implements ForecastListFragment.OnListItemSelectedListener {

    private int mPosition;
    private static final String STATE_POSITION = "STATE_POSITION";
    private Realm realm;
    private RealmResults<ForecastListItem> realmResults;
    private RealmChangeListener callback = new RealmChangeListener() {
        @Override
        public void onChange() {
            ProgressBar progress = (ProgressBar) findViewById(R.id.progress_missing_forecast);
            if (progress != null)
                progress.setVisibility(TextView.INVISIBLE);
            RealmResults<ForecastListItem> realmResults = realm.allObjects(ForecastListItem.class);
            if (realmResults.size() != 0)
                if (mPosition == -1)
                    mPosition = 0;
            TextView textMissingForecast = (TextView) findViewById(R.id.text_view_missing_forecast);
            if (textMissingForecast != null)
                textMissingForecast.setVisibility(TextView.INVISIBLE);
            if (isTabletLandscapeMode() && (mPosition >= 0)) {
                ForecastDetailFragment detailFragment = (ForecastDetailFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.forecast_detail);
                if ((findViewById(R.id.forecast_detail) != null) && (detailFragment != null)) {
                    detailFragment.update(mPosition);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realm = Realm.getDefaultInstance();
        realmResults = realm.where(ForecastListItem.class).findAllAsync();
        realmResults.addChangeListener(callback);

        setDefaultSettings();

        Settings settings = realm.where(Settings.class).findFirst();
        if (settings != null) {
            if (settings.getStartUpdateService()) {
                startService(new Intent(this, WeatherUpdateService.class));
            } else {
                stopService(new Intent(this, WeatherUpdateService.class));
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_weather_sunny);
        }

        mPosition = -1;
        TextView textMissingForecast = (TextView) findViewById(R.id.text_view_missing_forecast);
        ProgressBar progress = (ProgressBar) findViewById(R.id.progress_missing_forecast);
        RealmResults<ForecastListItem> realmResults = realm.allObjects(ForecastListItem.class);
        if (realmResults.size() != 0) {
            mPosition = 0;
            if (textMissingForecast != null)
                textMissingForecast.setVisibility(TextView.INVISIBLE);
            if (progress != null)
                progress.setVisibility(TextView.INVISIBLE);
        } else {
            if (textMissingForecast != null)
                textMissingForecast.setVisibility(TextView.VISIBLE);
            if (progress != null)
                progress.setVisibility(TextView.VISIBLE);
        }

        ForecastDetailFragment detailFragment = (ForecastDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.forecast_detail);

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(STATE_POSITION, -1);
            if (isTabletLandscapeMode()) {
                if ((findViewById(R.id.forecast_detail) != null) && (detailFragment != null) && (mPosition != -1)) {
                    detailFragment.update(mPosition);
                }
            } else {
                if (mPosition != -1) {
                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra(DetailActivity.ARG_INDEX, mPosition);
                    startActivity(intent);
                }
            }
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
    public void onListItemSelected(int position) {
        mPosition = position;
        if (findViewById(R.id.forecast_detail) != null) {
            ForecastDetailFragment detailFragment = (ForecastDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.forecast_detail);
            if (detailFragment != null) {
                detailFragment.update(position);
            }
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.ARG_INDEX, position);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_POSITION, mPosition);
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
    protected void onDestroy() {
        super.onDestroy();
        realmResults.removeChangeListener(callback);
        realm.close();
    }
}
