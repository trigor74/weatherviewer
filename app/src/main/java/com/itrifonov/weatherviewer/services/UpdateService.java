package com.itrifonov.weatherviewer.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class UpdateService extends Service {

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
        Log.i("UpdateService","onCreate");
        Toast.makeText(this, "Weather Forecast UpdateService created",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("UpdateService", "onStartCommand " + startId + ": " + intent);
        Toast.makeText(this, "Weather Forecast UpdateService started",
                Toast.LENGTH_SHORT).show();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("UpdateService", "onDestroy");
        Toast.makeText(this, "Weather Forecast UpdateService stopped",
                Toast.LENGTH_SHORT).show();
    }
}
