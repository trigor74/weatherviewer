package com.itrifonov.weatherviewer.services;

import com.itrifonov.weatherviewer.interfaces.IServiceCallbackListener;

import java.util.ArrayList;

public class ServiceHelper {
    private static ServiceHelper helperInstance;

    public static ServiceHelper getInstance() {
        if (helperInstance == null) {
            synchronized (ServiceHelper.class) {
                if (helperInstance == null) {
                    helperInstance = new ServiceHelper();
                }
            }
        }
        return helperInstance;
    }

    private ServiceHelper() {
    }

    private ArrayList<IServiceCallbackListener> callbackListeners = new ArrayList<>();

    public void addListener(IServiceCallbackListener listener) {
        callbackListeners.add(listener);
    }

    public void removeListener(IServiceCallbackListener listener) {
        callbackListeners.remove(listener);
    }

    private void dispatchCallbacks() {
        for (IServiceCallbackListener callbackListener : callbackListeners) {
            if (callbackListener != null) {
                callbackListener.onServiceCallback();
            }
        }
    }

    public void startUpdateService() {
        // TODO: 10.12.2015 Add logic
        //application.startService(new Intent(getApplicationContext(), UpdateService.class));
    }

    public void stopUpdateService() {
        // TODO: 10.12.2015 Add logic
        //stopService(new Intent(getApplicationContext(), UpdateService.class));
    }

    public void updateWeatherForecast() {
        // TODO: 10.12.2015 Add logic
    }
}