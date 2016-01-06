package com.itrifonov.weatherviewer.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
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

    private Timer currentWeatherInfoTimer = null;
    private NotificationManager notificationManager;
    private static final int WEATHER_NOTIFICATION_ID = 1;
    private static final int SERVICE_NOTIFICATION_ID = 2;
    private static final int UPDATE_NOTIFICATION_ID = 3;
    private static final String NOTIFICATION_GROUP_KEY = "services";
    private static final String ACTION_UPDATE = "UPDATE";
    private static final int UPDATE_STATUS_INPROGRESS = 1;
    private static final int UPDATE_STATUS_STARTED = 2;
    private static final int UPDATE_STATUS_UPDATED = 3;
    private static final int UPDATE_STATUS_ERROR = 4;
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
        scheduleTasks();
        context.registerReceiver(broadcastReceiver, new IntentFilter(UpdateIntentService.BROADCAST_ACTION));
    }

    private void scheduleTasks() {
        if (currentWeatherInfoTimer != null) {
            currentWeatherInfoTimer.cancel();
        }
        currentWeatherInfoTimer = new Timer();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int interval = Integer.parseInt(preferences.getString(getString(R.string.settings_notification_interval_key),
                getString(R.string.settings_notification_interval_default)));

        currentWeatherInfoTimer.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        sendCurrentWeatherNotification();
                    }
                }, 0, interval);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (startId == 1) {
            sendServiceStateNotification(true);
            sendCurrentWeatherNotification();
        } else {
            scheduleTasks();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (currentWeatherInfoTimer != null) {
            currentWeatherInfoTimer.cancel();
            currentWeatherInfoTimer = null;
        }
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
        sendServiceStateNotification(false);
        context.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void sendServiceStateNotification(Boolean started) {
        String message;
        if (started) {
            message = getString(R.string.notification_service_start);
        } else {
            message = getString(R.string.notification_service_stop);
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String units = preferences.getString(getString(R.string.settings_units_key), getString(R.string.settings_units_default));
        String city = preferences.getString(getString(R.string.current_city_key), getString(R.string.settings_city_default));

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
                String temp = ConvertTools.convertTemp(forecastItem.getConditions().getTemp(), units);
                Bitmap iconBitmap = ConvertTools.arrayToBitmap(forecastItem.getWeather().get(0).getIconData());
                String description = forecastItem.getWeather().get(0).getDescription();

                String title = getString(R.string.notification_current_weather_title, city, time);
                String ticker = getString(R.string.notification_current_weather_ticker, city, time, temp, description);
                String message = getString(R.string.notification_current_weather_message, temp, description);

                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                inboxStyle.setBigContentTitle(getString(R.string.notification_current_weather_big_title, city, dateTime));
                inboxStyle.addLine(description);
                inboxStyle.addLine(getString(R.string.txt_temp_min_max,
                        ConvertTools.convertTemp(forecastItem.getConditions().getTempMin(), units),
                        ConvertTools.convertTemp(forecastItem.getConditions().getTempMax(), units)));
                inboxStyle.addLine(getString(R.string.txt_wind_speed_direction,
                        (int) Math.round(forecastItem.getWind().getSpeed()),
                        ConvertTools.convertDirection(forecastItem.getWind().getDeg())));
                inboxStyle.addLine(getString(R.string.txt_humidity,
                        (int) Math.round(forecastItem.getConditions().getHumidity())));

                Intent detailIntent = new Intent(context, DetailActivity.class);
                detailIntent.putExtra(getString(R.string.current_timestamp_key), forecastItem.getTimeStamp());
                Intent mainIntent = new Intent(context, MainActivity.class);
                mainIntent.putExtra(getString(R.string.current_timestamp_key), forecastItem.getTimeStamp());
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(mainIntent);
                stackBuilder.addNextIntent(detailIntent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setLargeIcon(iconBitmap)
                                .setContentTitle(title)
                                .setWhen(System.currentTimeMillis())
                                .setAutoCancel(false)
                                .setTicker(ticker)
                                .setContentText(message)
                                .setStyle(inboxStyle)
                                .setContentIntent(pendingIntent)
                                .setOngoing(true)
                                .setPriority(NotificationCompat.PRIORITY_LOW);

                //Update button
                Intent serviceIntent = new Intent(context, UpdateIntentService.class);
                PendingIntent servicePending = PendingIntent.getService(context, 0,
                        serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder.addAction(R.drawable.ic_sync_white_18dp,
                        getString(R.string.notification_text_update), servicePending);
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
            case UPDATE_STATUS_STARTED:
                ticker = getString(R.string.notification_update_started);
                builder.setProgress(0, 0, true);
                message = ticker;
                break;
            case UPDATE_STATUS_INPROGRESS:
                ticker = getString(R.string.notification_update_in_progress);
                message = ticker;
                builder.setProgress(0, 0, true);
                break;
            case UPDATE_STATUS_UPDATED:
                ticker = getString(R.string.notification_update_updated);
                message = getString(R.string.notification_last_update,
                        DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));
                builder.setProgress(0, 0, false);
                break;
            case UPDATE_STATUS_ERROR:
                ticker = getString(R.string.notification_update_error);
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
            int status = intent.getIntExtra(UpdateIntentService.PARAM_STATUS, -1);
            switch (status) {
                case UpdateIntentService.STATUS_START:
                    sendUpdateStateNotification(UPDATE_STATUS_STARTED);
                    break;
                case UpdateIntentService.STATUS_PROGRESS:
                    int progress = intent.getIntExtra(UpdateIntentService.PARAM_PROGRESS, -1);
                    sendUpdateStateNotification(UPDATE_STATUS_INPROGRESS);
                    break;
                case UpdateIntentService.STATUS_FINISH:
                    String result = intent.getStringExtra(UpdateIntentService.PARAM_RESULT);
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
