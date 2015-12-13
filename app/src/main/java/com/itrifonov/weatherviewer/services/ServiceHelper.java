package com.itrifonov.weatherviewer.services;

import android.content.Context;
import android.content.Intent;

import com.itrifonov.weatherviewer.interfaces.IServiceHelperCallbackListener;
import com.itrifonov.weatherviewer.weatherapi.WeatherForecastUpdater;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ServiceHelper {
    private static ServiceHelper helperInstance;
    private static Context context;
    private ArrayList<IServiceHelperCallbackListener> callbackListeners = new ArrayList<>();
    private Realm realm;
    private RealmResults<ForecastListItem> realmResults;
    private RealmChangeListener callback = new RealmChangeListener() {
        @Override
        public void onChange() {
            dispatchCallbacks();
        }
    };

    public static ServiceHelper getInstance(Context context) {
        if (helperInstance == null) {
            synchronized (ServiceHelper.class) {
                if (helperInstance == null) {
                    helperInstance = new ServiceHelper(context);
                }
            }
        }
        return helperInstance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private ServiceHelper(Context context) {
        setContext(context);
        realm = Realm.getDefaultInstance();
        realmResults = realm.where(ForecastListItem.class).findAllAsync();
        realmResults.addChangeListener(callback);
    }

    public void addListener(IServiceHelperCallbackListener listener) {
        callbackListeners.add(listener);
    }

    public void removeListener(IServiceHelperCallbackListener listener) {
        callbackListeners.remove(listener);
    }

    private void dispatchCallbacks() {
        for (IServiceHelperCallbackListener callbackListener : callbackListeners) {
            if (callbackListener != null) {
                callbackListener.onServiceHelperCallback();
            }
        }
    }

    public void startUpdateService() {
        context.startService(new Intent(context, UpdateService.class));
    }

    public void stopUpdateService() {
        context.stopService(new Intent(context, UpdateService.class));
    }

    public void updateWeatherForecast() {
        // TODO: 10.12.2015 Add logic
        new WeatherForecastUpdater(null).execute();
    }
}