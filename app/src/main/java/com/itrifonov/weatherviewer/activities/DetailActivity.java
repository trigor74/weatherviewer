package com.itrifonov.weatherviewer.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.fragments.ForecastDetailFragment;

public class DetailActivity extends AppCompatActivity {

    public static String ARG_INDEX = "ARG_INDEX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (isTabletLandscapeMode())
//            finish();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ForecastDetailFragment detailFragment = (ForecastDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.forecast_detail);

        if (savedInstanceState == null) {
            int index = getIntent().getExtras().getInt(ARG_INDEX, -1);
            if (detailFragment != null) {
                detailFragment.update(index);
            }
        }
    }

    private boolean isTabletLandscapeMode() {
        return getResources().getBoolean(R.bool.is_tablet_landscape);
    }
}
