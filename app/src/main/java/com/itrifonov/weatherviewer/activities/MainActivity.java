package com.itrifonov.weatherviewer.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.itrifonov.weatherviewer.interfaces.IServiceHelperCallbackListener;
import com.itrifonov.weatherviewer.services.ServiceHelper;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity
        implements IOnListItemSelectedListener {

    private SharedPreferences preferences;
    private long timestamp = -1;
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

            if (hasData()) {
                if (timestamp == -1)
                    timestamp = 0;
                TextView textMissingForecast = (TextView) findViewById(R.id.text_view_missing_forecast);
                if (textMissingForecast != null)
                    textMissingForecast.setVisibility(TextView.INVISIBLE);
                if (isTabletLandscapeMode()) {
                    ForecastDetailFragment detailFragment = (ForecastDetailFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.forecast_detail);
                    if (detailFragment != null)
                        detailFragment.update(timestamp);
                }
            } else {
                String resultMessage = event.getString(ServiceHelper.PARAM_UPDATE_RESULT);
                if (resultMessage != null && !resultMessage.isEmpty()) {
                    TextView textMissingForecast = (TextView) findViewById(R.id.text_view_missing_forecast);
                    if (textMissingForecast != null) {
                        String text = getString(R.string.txt_no_data);
                        text = text.concat("\n".concat(resultMessage));
                        if (resultMessage.equals(getString(R.string.txt_no_internet_connection)))
                            text = text.concat("\n".concat(getString(R.string.txt_check_internet_connection)));
                        textMissingForecast.setText(text);
                    }
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realm = Realm.getDefaultInstance();

        preferences = getPreferences(Context.MODE_PRIVATE);

        ServiceHelper serviceHelper = ServiceHelper.getInstance(getApplicationContext());
        serviceHelper.addListener(callback);

        Boolean showNotifications = preferences.getBoolean(getString(R.string.settings_show_notifications_key),
                getResources().getBoolean(R.bool.settings_show_notifications_default));
        if (showNotifications) {
            serviceHelper.startNotificationService();
        } else {
            serviceHelper.stopNotificationService();
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
        if (hasData()) {
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

        timestamp = preferences.getLong(getString(R.string.current_timestamp_key), timestamp);

        if (savedInstanceState != null) {
            timestamp = savedInstanceState.getLong(getString(R.string.current_timestamp_key), timestamp);
        } else {
            if (getIntent().getExtras() != null)
                timestamp = getIntent().getExtras().getLong(getString(R.string.current_timestamp_key), timestamp);
        }

        if (hasData()
                && isTabletLandscapeMode()
                && (findViewById(R.id.forecast_detail) != null)
                && (detailFragment != null)) {
            detailFragment.update(timestamp);
        }
    }

    private Boolean hasData() {
        if (realm != null) {
            RealmResults<ForecastListItem> realmResults = realm.allObjects(ForecastListItem.class);
            return realmResults.size() != 0;
        } else {
            return false;
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
                Intent intent = new Intent(this, AppPreferencesActivity.class);
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
            intent.putExtra(getString(R.string.current_timestamp_key), timestamp);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(getString(R.string.current_timestamp_key), timestamp);
        super.onSaveInstanceState(outState);
    }

    private boolean isTabletLandscapeMode() {
        return getResources().getBoolean(R.bool.is_tablet_landscape);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ServiceHelper.getInstance(getApplicationContext()).addListener(callback);
    }

    @Override
    protected void onPause() {
        ServiceHelper.getInstance(getApplicationContext()).removeListener(callback);
        preferences.edit().putLong(getString(R.string.current_timestamp_key), timestamp).commit();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
