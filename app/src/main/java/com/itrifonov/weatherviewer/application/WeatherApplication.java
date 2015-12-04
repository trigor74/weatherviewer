package com.itrifonov.weatherviewer.application;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class WeatherApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}
