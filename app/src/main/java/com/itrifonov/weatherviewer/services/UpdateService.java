package com.itrifonov.weatherviewer.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.weatherapi.OpenweathermapObject;
import com.itrifonov.weatherviewer.weatherapi.WeatherForecastUpdater;
import com.itrifonov.weatherviewer.weatherapi.interfaces.IWeatherUpdateListener;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import io.realm.Realm;
import io.realm.RealmResults;

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
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String city = preferences.getString(getString(R.string.settings_city_key), getString(R.string.settings_city_default));
            String apiid = preferences.getString(getString(R.string.settings_appid_key), getString(R.string.settings_appid_default));
            String units = preferences.getString(getString(R.string.settings_units_key), getString(R.string.settings_units_default));
            String lang = preferences.getString(getString(R.string.settings_language_key), getString(R.string.settings_language_default));
            new WeatherForecastUpdater(updateListener).execute(city, apiid, units, lang);
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
        public void onUpdateFinished(OpenweathermapObject data) {
            String errorMessage = getString(R.string.txt_unknown_error);

            if (data != null) {
                errorMessage = data.getErrorMessage();
            }

            if (errorMessage.isEmpty()) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Boolean del_old = preferences.getBoolean(getString(R.string.settings_delete_old_data_key), getResources().getBoolean(R.bool.settings_delete_old_data_default));
                Realm realm = null;
                try {
                    realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    if (del_old) {
                        Long timeStamp = data.getWeatherForecastList().get(0).getTimeStamp();
                        RealmResults<ForecastListItem> result = realm.where(ForecastListItem.class)
                                .lessThan("timeStamp", timeStamp).findAll();
                        result.clear();
                    }
                    realm.copyToRealmOrUpdate(data.getWeatherForecastList());
                    realm.commitTransaction();

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putLong(getString(R.string.last_update_key), System.currentTimeMillis());
                    String city = data.getCityName().concat(", ".concat(data.getCountry()));
                    editor.putString(getString(R.string.current_city_key), city);
                    editor.commit();

                } finally {
                    if (realm != null) {
                        realm.close();
                    }
                }
            }
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra(PARAM_STATUS, STATUS_FINISH);
            intent.putExtra(PARAM_RESULT, errorMessage);
            sendBroadcast(intent);
            stopSelf();
        }
    };
}
