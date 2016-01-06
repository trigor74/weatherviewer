package com.itrifonov.weatherviewer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.interfaces.IServiceHelperCallbackListener;

import java.util.HashSet;

public class ServiceHelper {

    public final static String SERVICE_HELPER_EVENT = "EVENT";

    public final static int EVENT_UPDATE_STARTED = 101;
    public final static int EVENT_UPDATE_PROGRESS = 102;
    public final static int EVENT_UPDATE_STOPPED = 103;

    public final static String PARAM_UPDATE_PROGRESS = "PROGRESS";
    public final static String PARAM_UPDATE_RESULT = "RESULT";

    private static ServiceHelper helperInstance;
    private static Context context;
    private static HashSet<IServiceHelperCallbackListener> callbackListeners = new HashSet<>();

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
        context.registerReceiver(broadcastReceiver, new IntentFilter(UpdateIntentService.BROADCAST_ACTION));
    }

    public void addListener(IServiceHelperCallbackListener listener) {
        callbackListeners.add(listener);
    }

    public void removeListener(IServiceHelperCallbackListener listener) {
        callbackListeners.remove(listener);
    }


    private void dispatchCallbacks(Bundle event) {
        for (IServiceHelperCallbackListener callbackListener : callbackListeners) {
            if (callbackListener != null) {
                callbackListener.onServiceHelperCallback(event);
            }
        }
    }

    public void startNotificationService() {
        Intent intent = new Intent(context, NotificationService.class);
        context.startService(intent);
    }

    public void stopNotificationService() {
        context.stopService(new Intent(context, NotificationService.class));
    }

    public void updateWeatherForecast() {
        if (isInternetOn()) {
            Intent intent = new Intent(context, UpdateIntentService.class);
            context.startService(intent);
        } else {
            Bundle event = new Bundle();
            event.putInt(SERVICE_HELPER_EVENT, EVENT_UPDATE_STOPPED);
            event.putString(PARAM_UPDATE_RESULT, context.getString(R.string.txt_no_internet_connection));
            dispatchCallbacks(event);
        }
    }

    public boolean isInternetOn() {
        NetworkInfo networkInfo = (NetworkInfo) ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        } else if (!networkInfo.isConnected() || networkInfo.isRoaming()) {
            return false;
        }
        return true;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle event = new Bundle();
            int status = intent.getIntExtra(UpdateIntentService.PARAM_STATUS, -1);
            switch (status) {
                case UpdateIntentService.STATUS_START:
                    event.putInt(SERVICE_HELPER_EVENT, EVENT_UPDATE_STARTED);
                    dispatchCallbacks(event);
                    break;
                case UpdateIntentService.STATUS_PROGRESS:
                    int progress = intent.getIntExtra(UpdateIntentService.PARAM_PROGRESS, -1);
                    event.putInt(SERVICE_HELPER_EVENT, EVENT_UPDATE_PROGRESS);
                    event.putInt(PARAM_UPDATE_PROGRESS, progress);
                    dispatchCallbacks(event);
                    break;
                case UpdateIntentService.STATUS_FINISH:
                    String errorMessage = intent.getStringExtra(UpdateIntentService.PARAM_RESULT);
                    event.putInt(SERVICE_HELPER_EVENT, EVENT_UPDATE_STOPPED);
                    event.putString(PARAM_UPDATE_RESULT, errorMessage);
                    dispatchCallbacks(event);
                    break;
            }
        }
    };
}