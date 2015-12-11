package com.itrifonov.weatherviewer.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class UpdateService extends Service {

    public static final long UPDATE_INTERVAL = 60 * 1000; // 60 seconds
    private Timer timer = new Timer();

    public UpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        // TODO: 11.12.2015 http://habrahabr.ru/post/136942/
                        new UpdateTask().execute();
                    }
                },
                0,
                UPDATE_INTERVAL);
        Log.i("UpdateService", "onCreate");
        Toast.makeText(getApplicationContext(), "Weather Forecast UpdateService created",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("UpdateService", "onStartCommand " + startId + ": " + intent);
        Toast.makeText(getApplicationContext(), "Weather Forecast UpdateService started",
                Toast.LENGTH_SHORT).show();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        Log.i("UpdateService", "onDestroy");
        Toast.makeText(getApplicationContext(), "Weather Forecast UpdateService stopped",
                Toast.LENGTH_SHORT).show();
    }

    private void updateWeatherForecast() {
        // TODO: 11.12.2015 Add logic
        for (int i = 0; i < 1000; i++) {
            Log.i("UpdateService", "updateWeatherForecast" + i);
        }
    }

    class UpdateTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... noargs) {
            updateWeatherForecast();
            return "OK";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "updateWeatherForecast finished - " + result,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
