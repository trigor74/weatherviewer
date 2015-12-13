package com.itrifonov.weatherviewer.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.activities.DetailActivity;
import com.itrifonov.weatherviewer.activities.MainActivity;
import com.itrifonov.weatherviewer.weatherapi.ConvertTools;
import com.itrifonov.weatherviewer.weatherapi.WeatherForecastUpdater;
import com.itrifonov.weatherviewer.weatherapi.interfaces.IWeatherUpdateListener;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;


public class UpdateService extends Service {

    public static final long UPDATE_INTERVAL = 60 * 1000; // 60 seconds
    public static final long INFO_INTERVAL = 120 * 1000;
    private Timer checkUpdateTimer = new Timer();
    private Timer currentWeatherInfoTimer = new Timer();
    private NotificationManager notificationManager;
    private static final int WEATHER_NOTIFICATION_ID = 1;
    private static final int SERVICE_NOTIFICATION_ID = 2;
    private static final int UPDATE_NOTIFICATION_ID = 3;
    private static final String NOTIFICATION_GROUP_KEY = "services";
    private static final String ACTION_UPDATE = "UPDATE";
    private static final int UPDATE_STATUS_AVAILABLE = 1;
    private static final int UPDATE_STATUS_INPROGRESS = 2;
    private static final int UPDATE_STATUS_STARTED = 3;
    private static final int UPDATE_STATUS_UPDATED = 4;
    private static final int UPDATE_STATUS_ERROR = 5;
    private Context context;
    private IWeatherUpdateListener updateListener = new IWeatherUpdateListener() {
        @Override
        public void onUpdateStarted() {
            sendUpdateStateNotification(UPDATE_STATUS_INPROGRESS);
        }

        @Override
        public void onUpdateFinished(String errorMessage) {
            if (errorMessage.isEmpty()) {
                sendUpdateStateNotification(UPDATE_STATUS_UPDATED);
            } else {
                sendUpdateStateNotification(UPDATE_STATUS_ERROR);
            }
        }
    };

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
        context = getApplicationContext();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        checkUpdateTimer.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        new CheckUpdateTask().execute();
                    }
                }, 0, UPDATE_INTERVAL);

        currentWeatherInfoTimer.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        sendCurrentWeatherNotification();
                    }
                }, 0, INFO_INTERVAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (startId == 1) {
            sendServiceStateNotification(true);
            sendCurrentWeatherNotification();
        }
        handleCommand(intent, startId);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (checkUpdateTimer != null) checkUpdateTimer.cancel();
        if (currentWeatherInfoTimer != null) currentWeatherInfoTimer.cancel();
        if (notificationManager != null) notificationManager.cancelAll();
        sendServiceStateNotification(false);
        super.onDestroy();
    }

    private void handleCommand(Intent intent, int startId) {
        if (intent == null)
            return;
        String action = intent.getAction();
        if (action == null)
            return;
        switch (action) {
            case ACTION_UPDATE:
                updateWeatherForecast();
                return;
            default:
                // unknown command
        }
    }

    private void sendServiceStateNotification(Boolean started) {
        String message;
        if (started) {
            message = "Weather Forecast UpdateService started";
        } else {
            message = "Weather Forecast UpdateService stopped";
        }

        Intent notifyIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_weather_sunny)
                        .setContentTitle(getString(R.string.app_name))
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setTicker(message)
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setGroup(NOTIFICATION_GROUP_KEY);

        notificationManager.notify(SERVICE_NOTIFICATION_ID, builder.build());
    }

    private void sendCurrentWeatherNotification() {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            long currentTimeStamp = System.currentTimeMillis() / 1000L;
            ForecastListItem forecastItem = realm.where(ForecastListItem.class)
                    .greaterThanOrEqualTo("timeStamp", currentTimeStamp).findFirst();
            if (forecastItem != null) {
                int mPosition = 0; // realm.allObjects(ForecastListItem.class).indexOf(forecastItem);

                long timestamp = forecastItem.getTimeStamp() * 1000L;
                String time = SimpleDateFormat.getTimeInstance().format(timestamp);
                String dateTime = SimpleDateFormat.getDateTimeInstance().format(timestamp);
                String temp = ConvertTools.convertTemp(forecastItem.getConditions().getTemp());
                Bitmap iconBitmap = ConvertTools.arrayToBitmap(forecastItem.getWeather().get(0).getIconData());
                String description = forecastItem.getWeather().get(0).getDescription();

                String message = time + ": " + temp + ", " + description;

                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                inboxStyle.setBigContentTitle("Weather forecast for " + dateTime);
                inboxStyle.addLine(description);
                inboxStyle.addLine(getString(R.string.txt_temp_min_max,
                        ConvertTools.convertTemp(forecastItem.getConditions().getTempMin()),
                        ConvertTools.convertTemp(forecastItem.getConditions().getTempMax())));
                inboxStyle.addLine(getString(R.string.txt_wind_speed_direction,
                        (int) Math.round(forecastItem.getWind().getSpeed()),
                        ConvertTools.convertDirection(forecastItem.getWind().getDeg())));
                inboxStyle.addLine(getString(R.string.txt_humidity,
                        (int) Math.round(forecastItem.getConditions().getHumidity())));

                Intent notifyIntent = new Intent(context, DetailActivity.class);
                notifyIntent.putExtra(DetailActivity.ARG_INDEX, mPosition);

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                        notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                NotificationCompat.Builder builder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_weather_sunny)
                                .setLargeIcon(iconBitmap)
                                .setContentTitle(getString(R.string.app_name))
                                .setWhen(System.currentTimeMillis())
                                .setAutoCancel(false)
                                .setTicker(message)
                                .setContentText(message)
                                .setStyle(inboxStyle)
                                .setContentIntent(pendingIntent)
                                .setOngoing(true)
                                .setPriority(NotificationCompat.PRIORITY_MIN);

                notificationManager.notify(WEATHER_NOTIFICATION_ID, builder.build());
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private void sendUpdateStateNotification(int updateStatus) {
        String message;
        String ticker;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        Intent notifyIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentTitle(getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setGroup(NOTIFICATION_GROUP_KEY);

        switch (updateStatus) {
            case UPDATE_STATUS_AVAILABLE:
                ticker = "Weather forecast update available";
                message = ticker;

                Intent serviceIntent = new Intent(context, UpdateService.class);
                serviceIntent.setAction(ACTION_UPDATE);
                PendingIntent servicePending = PendingIntent.getService(context, 0,
                        serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                builder.addAction(R.drawable.ic_refresh_white_48dp, "Update", servicePending);

                break;
            case UPDATE_STATUS_STARTED:
                ticker = "Weather forecast update started";
                message = ticker;
                break;
            case UPDATE_STATUS_INPROGRESS:
                ticker = "Weather forecast update in progress";
                message = ticker;
                break;
            case UPDATE_STATUS_UPDATED:
                ticker = "Weather forecast updated";
                message = getString(R.string.notification_last_update,
                        DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));

                break;
            case UPDATE_STATUS_ERROR:
                ticker = "Weather forecast update error";
                message = ticker;
                break;
            default:
                // unknown update status
                return;
        }

        builder.setSmallIcon(R.drawable.ic_weather_sunny)
                .setTicker(ticker)
                .setContentText(message);

        notificationManager.notify(UPDATE_NOTIFICATION_ID, builder.build());
    }

    private void updateWeatherForecast() {
        new WeatherForecastUpdater(updateListener).execute();
    }

    private Boolean checkUpdate() {
        // TODO: 11.12.2015 Add logic
        // Simulate checking updates
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    class CheckUpdateTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... noargs) {
            return checkUpdate();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                sendUpdateStateNotification(UPDATE_STATUS_AVAILABLE);
            }
        }
    }
}
