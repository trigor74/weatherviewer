package com.itrifonov.weatherviewer.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.itrifonov.weatherviewer.interfaces.IServiceHelperCallbackListener;
import com.itrifonov.weatherviewer.weatherapi.WeatherForecastUpdater;
import com.itrifonov.weatherviewer.weatherapi.interfaces.IWeatherUpdateListener;

import java.util.ArrayList;

public class ServiceHelper {
    private static ServiceHelper helperInstance;
    private static Context context;
    private static ArrayList<IServiceHelperCallbackListener> callbackListeners = new ArrayList<>();

    private static Boolean isServiceBound;
    private UpdateService.UpdateServiceBinder updateServiceBinder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            updateServiceBinder = (UpdateService.UpdateServiceBinder) service;
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
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
        isServiceBound = false;
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
        Intent intent = new Intent(context, UpdateService.class);
        context.startService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stopUpdateService() {
        if (isServiceBound)
            context.unbindService(serviceConnection);
        context.stopService(new Intent(context, UpdateService.class));
    }

    public void updateWeatherForecast() {
        // TODO: 13.12.15 fix service callback before use
//        if (isServiceBound) {
//            updateServiceBinder.updateWeatherForecast();
//        } else {
            new WeatherForecastUpdater(weatherUpdateListener).execute();
//        }
    }

    private IWeatherUpdateListener weatherUpdateListener = new IWeatherUpdateListener() {
        @Override
        public void onUpdateStarted() {
        }

        @Override
        public void onUpdateFinished(String errorMessage) {
            dispatchCallbacks();
        }
    };
}