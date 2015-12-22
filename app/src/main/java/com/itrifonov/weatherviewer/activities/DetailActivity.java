package com.itrifonov.weatherviewer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.fragments.ForecastDetailFragment;

public class DetailActivity extends AppCompatActivity {

    public static String ARG_IGNORE_LANDSCAPE = "ARG_IGNORE_LANDSCAPE";
    private long timestamp = -1;
    private ForecastDetailFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Boolean ignoreLandscape = getIntent().getExtras().getBoolean(ARG_IGNORE_LANDSCAPE, false);
        if (!ignoreLandscape && isTabletLandscapeMode())
            finish();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        detailFragment = (ForecastDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.forecast_detail);

        if (savedInstanceState == null) {
            timestamp = getIntent().getExtras().getLong(getString(R.string.current_timestamp_key), -1);
        } else {
            timestamp = savedInstanceState.getLong(getString(R.string.current_timestamp_key), -1);
        }
        if (detailFragment != null) {
            detailFragment.update(timestamp);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        timestamp = intent.getExtras().getLong(getString(R.string.current_timestamp_key), -1);
        if (detailFragment != null) {
            detailFragment.update(timestamp);
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
}
