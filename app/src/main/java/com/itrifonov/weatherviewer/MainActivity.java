package com.itrifonov.weatherviewer;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements ForecastListFragment.OnListItemSelectedListener {

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

        if (savedInstanceState != null) {
            // TODO: 18.11.2015 restore state
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
                // TODO: 18.11.2015 Add logic
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
        if (findViewById(R.id.forecast_detail) != null) {
            ForecastDetailFragment detailFragment = (ForecastDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.forecast_detail);
            if (detailFragment != null) {
                detailFragment.update(position);
            }
        } else {
            // TODO: 18.11.15 start new activity with detail info
            Intent intent = new Intent(this, DetailActivity.class);
            // TODO: 18.11.15 replace "ARG_INDEX" with constant name from class
            intent.putExtra(DetailActivity.ARG_INDEX, position);
            startActivity(intent);
        }
    }
}
