package com.itrifonov.weatherviewer;

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

import com.itrifonov.weatherviewer.weatherapi.WeatherForecastData;

public class MainActivity extends AppCompatActivity
        implements ForecastListFragment.OnListItemSelectedListener,
        WeatherForecastData.OnWeatherForecastUpdatedListener {

    private int mPosition;
    private static final String STATE_POSITION = "STATE_POSITION";
    private WeatherForecastData mWeatherForecast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_weather_sunny);
        }

        mPosition = -1;
        mWeatherForecast = WeatherForecastData.getInstance(this);
        TextView textView = (TextView) findViewById(R.id.text_view_missing_forecast);
        ProgressBar progress = (ProgressBar) findViewById(R.id.progress_missing_forecast);
        if (mWeatherForecast.getWeatherForecastList() != null) {
            mPosition = 0;
            if (textView != null)
                textView.setVisibility(TextView.INVISIBLE);
            if (progress != null)
                progress.setVisibility(TextView.INVISIBLE);
        } else {
            if (textView != null)
                textView.setVisibility(TextView.VISIBLE);
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
                TextView textView = (TextView) findViewById(R.id.text_view_missing_forecast);
                ProgressBar progress = (ProgressBar) findViewById(R.id.progress_missing_forecast);
                if ((textView != null) && (textView.getVisibility() == View.VISIBLE) && (progress != null))
                    progress.setVisibility(TextView.VISIBLE);
                ((ForecastListFragment) getSupportFragmentManager().findFragmentById(R.id.forecast_list))
                        .updateWeatherForecast();
                return true;
            case R.id.action_search:
                // TODO: 18.11.2015 Add logic
                return true;
            case R.id.action_settings:
                // TODO: 18.11.2015 Add logic
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

    @Override
    public void onWeatherForecastUpdated() {
        ProgressBar progress = (ProgressBar) findViewById(R.id.progress_missing_forecast);
        if (progress != null)
            progress.setVisibility(TextView.INVISIBLE);
        if ((mPosition == -1) && (mWeatherForecast.getWeatherForecastList() != null))
            mPosition = 0;
        if (mWeatherForecast.getWeatherForecastList() != null) {
            TextView textView = (TextView) findViewById(R.id.text_view_missing_forecast);
            if (textView != null)
                textView.setVisibility(TextView.INVISIBLE);
        }
        if (isTabletLandscapeMode() && (mPosition >= 0)) {
            ForecastDetailFragment detailFragment = (ForecastDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.forecast_detail);
            if ((findViewById(R.id.forecast_detail) != null) && (detailFragment != null)) {
                detailFragment.update(mPosition);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mWeatherForecast != null)
            mWeatherForecast.removeOnWeatherForecastUpdatedListner(this);
        super.onDestroy();
    }
}
