package com.itrifonov.weatherviewer.services;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.itrifonov.weatherviewer.services.interfaces.IServiceHelperCallbackListener;

import java.util.ArrayList;

public class ServiceHelper {

    public final static int EVENT_DEFAULT = 0;

    private static ServiceHelper helperInstance;
    private static Context context;
    private static ArrayList<IServiceHelperCallbackListener> callbackListeners = new ArrayList<>();

    private static Boolean isServiceBound;
    private NotificationService.UpdateServiceBinder notificationServiceBinder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notificationServiceBinder = (NotificationService.UpdateServiceBinder) service;
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(UpdateService.PARAM_STATUS, -1);
            switch (status) {
                case UpdateService.STATUS_START:
                    // TODO: 15.12.2015 add logic
                    dispatchCallbacks(EVENT_DEFAULT);
                    break;
                case UpdateService.STATUS_PROGRESS:
                    int progress = intent.getIntExtra(UpdateService.PARAM_PROGRESS, -1);
                    // TODO: 15.12.2015 add logic
                    dispatchCallbacks(EVENT_DEFAULT);
                    break;
                case UpdateService.STATUS_FINISH:
                    String errorMessage = intent.getStringExtra(UpdateService.PARAM_RESULT);
                    // TODO: 15.12.2015 add logic
                    dispatchCallbacks(EVENT_DEFAULT);
                    break;
            }
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
        context.registerReceiver(broadcastReceiver, new IntentFilter(UpdateService.BROADCAST_ACTION));
    }

    public void addListener(IServiceHelperCallbackListener listener) {
        callbackListeners.add(listener);
    }

    public void removeListener(IServiceHelperCallbackListener listener) {
        callbackListeners.remove(listener);
    }


    private void dispatchCallbacks(int event) {
        for (IServiceHelperCallbackListener callbackListener : callbackListeners) {
            if (callbackListener != null) {
                callbackListener.onServiceHelperCallback(event);
            }
        }
    }

    public void startNotificationService() {
        Intent intent = new Intent(context, NotificationService.class);
        context.startService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void stopNotificationService() {
        if (isServiceBound)
            context.unbindService(serviceConnection);
        context.stopService(new Intent(context, NotificationService.class));
    }

    public void updateWeatherForecast() {
        Intent intent = new Intent(context, UpdateService.class);
        context.startService(intent);
    }
}