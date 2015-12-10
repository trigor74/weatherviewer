package com.itrifonov.weatherviewer.services;

import android.content.Context;
import android.content.Intent;

import com.itrifonov.weatherviewer.interfaces.IServiceCallbackListener;

import java.util.ArrayList;

public class ServiceHelper {
    private static ServiceHelper helperInstance;
    private static Context context;

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
        context.startService(new Intent(context, UpdateService.class));
    }

    public void stopUpdateService() {
        context.stopService(new Intent(context, UpdateService.class));
    }

    public void updateWeatherForecast() {
        // TODO: 10.12.2015 Add logic
    }
}