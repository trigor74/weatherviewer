package com.itrifonov.weatherviewer.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.itrifonov.weatherviewer.weatherapi.WeatherForecastUpdater;
import com.itrifonov.weatherviewer.weatherapi.interfaces.IWeatherUpdateListener;

public class UpdateService extends Service {

    public final static int STATUS_START = 1;
    public final static int STATUS_PROGRESS = 2;
    public final static int STATUS_FINISH = 3;

    public final static String PARAM_STATUS = "STATUS";
    public final static String PARAM_PROGRESS = "PROGRESS";
    public final static String PARAM_RESULT = "RESULT";

    public final static String BROADCAST_ACTION = "com.itrifonov.weatherviewer.services.updateservice";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (startId == 1) {
            new WeatherForecastUpdater(updateListener).execute();
        }
        return Service.START_NOT_STICKY;
    }

    private IWeatherUpdateListener updateListener = new IWeatherUpdateListener() {
        @Override
        public void onUpdateStarted() {
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra(PARAM_STATUS, STATUS_START);
            sendBroadcast(intent);
        }

        @Override
        public void onProgressUpdated(int progress) {
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra(PARAM_STATUS, STATUS_PROGRESS);
            intent.putExtra(PARAM_PROGRESS, progress);
            sendBroadcast(intent);
        }

        @Override
        public void onUpdateFinished(String errorMessage) {
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra(PARAM_STATUS, STATUS_FINISH);
            intent.putExtra(PARAM_RESULT, errorMessage);
            sendBroadcast(intent);
            stopSelf();
        }
    };
}
