package com.itrifonov.weatherviewer;

import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
//        if (actionBar != null) {
//            actionBar.setHomeAsUpIndicator(R.drawable.menu);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }

        if (savedInstanceState == null) {
            // TODO: 18.11.2015 load settings
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            ForecastListFragment listFragment = new ForecastListFragment();
            transaction.add(R.id.layout_forecast_content, listFragment);
            transaction.commit();
        } else {
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
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                } else {
                    finish();
                }
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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onListItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
//        if (findViewById(R.id.layout_forecast_detail) != null) {
//            // TODO: 18.11.2015 find fragment with details and update data
//        } else {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        ForecastDetailFragment detailFragment = new ForecastDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ForecastDetailFragment.ARG_INDEX, position);
        detailFragment.setArguments(args);
        transaction.replace(R.id.layout_forecast_content, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
//        }
    }
}
