package com.itrifonov.weatherviewer.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.activities.DetailActivity;
import com.itrifonov.weatherviewer.activities.MainActivity;
import com.itrifonov.weatherviewer.weatherapi.ConvertTools;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;


public class NotificationService extends Service {

    private long updateWeatherInfoInterval = 600 * 1000; // 10 min
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        sheduleTasks();
        context.registerReceiver(broadcastReceiver, new IntentFilter(UpdateService.BROADCAST_ACTION));
    }

    private void sheduleTasks() {
        currentWeatherInfoTimer.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        sendCurrentWeatherNotification();
                    }
                }, 0, updateWeatherInfoInterval);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (startId == 1) {
            sendServiceStateNotification(true);
            sendCurrentWeatherNotification();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (currentWeatherInfoTimer != null) currentWeatherInfoTimer.cancel();
        if (notificationManager != null) notificationManager.cancelAll();
        sendServiceStateNotification(false);
        context.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void sendServiceStateNotification(Boolean started) {
        String message;
        if (started) {
            message = "Weather Forecast NotificationService started";
        } else {
            message = "Weather Forecast NotificationService stopped";
        }

        Intent notifyIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(getString(R.string.app_name))
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setTicker(message)
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
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
                notifyIntent.putExtra(DetailActivity.ARG_TIMESTAMP, forecastItem.getTimeStamp());

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                        notifyIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                NotificationCompat.Builder builder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setLargeIcon(iconBitmap)
                                .setContentTitle(getString(R.string.app_name))
                                .setWhen(System.currentTimeMillis())
                                .setAutoCancel(false)
                                .setTicker(message)
                                .setContentText(message)
                                .setStyle(inboxStyle)
                                .setContentIntent(pendingIntent)
                                .setOngoing(true)
                                .setPriority(NotificationCompat.PRIORITY_LOW);

                //Update button
                Intent serviceIntent = new Intent(context, UpdateService.class);
                PendingIntent servicePending = PendingIntent.getService(context, 0,
                        serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.addAction(R.drawable.ic_sync_white_18dp, "Update", servicePending);
                //

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

                Intent serviceIntent = new Intent(context, NotificationService.class);
                serviceIntent.setAction(ACTION_UPDATE);
                PendingIntent servicePending = PendingIntent.getService(context, 0,
                        serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                builder.addAction(R.drawable.ic_sync_white_18dp, "Update", servicePending);

                break;
            case UPDATE_STATUS_STARTED:
                ticker = "Weather forecast update started";
                builder.setProgress(0, 0, true);
                message = ticker;
                break;
            case UPDATE_STATUS_INPROGRESS:
                ticker = "Weather forecast update in progress";
                message = ticker;
                builder.setProgress(0, 0, true);
                break;
            case UPDATE_STATUS_UPDATED:
                ticker = "Weather forecast updated";
                message = getString(R.string.notification_last_update,
                        DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));
                builder.setProgress(0, 0, false);
                break;
            case UPDATE_STATUS_ERROR:
                ticker = "Weather forecast update error";
                message = ticker;
                break;
            default:
                // unknown update status
                return;
        }

        builder.setSmallIcon(R.drawable.ic_notification)
                .setTicker(ticker)
                .setContentText(message);

        notificationManager.notify(UPDATE_NOTIFICATION_ID, builder.build());
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(UpdateService.PARAM_STATUS, -1);
            switch (status) {
                case UpdateService.STATUS_START:
                    sendUpdateStateNotification(UPDATE_STATUS_STARTED);
                    break;
                case UpdateService.STATUS_PROGRESS:
                    int progress = intent.getIntExtra(UpdateService.PARAM_PROGRESS, -1);
                    sendUpdateStateNotification(UPDATE_STATUS_INPROGRESS);
                    break;
                case UpdateService.STATUS_FINISH:
                    String result = intent.getStringExtra(UpdateService.PARAM_RESULT);
                    if (result.isEmpty()) {
                        sendUpdateStateNotification(UPDATE_STATUS_UPDATED);
                    } else {
                        sendUpdateStateNotification(UPDATE_STATUS_ERROR);
                    }
                    break;
            }
        }
    };

}
