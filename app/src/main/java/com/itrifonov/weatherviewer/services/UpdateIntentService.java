package com.itrifonov.weatherviewer.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.weatherapi.OpenweathermapObject;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class UpdateIntentService extends IntentService {
    public final static int STATUS_START = 1;
    public final static int STATUS_PROGRESS = 2;
    public final static int STATUS_FINISH = 3;

    public final static String PARAM_STATUS = "STATUS";
    public final static String PARAM_PROGRESS = "PROGRESS";
    public final static String PARAM_RESULT = "RESULT";

    public final static String BROADCAST_ACTION = "com.itrifonov.weatherviewer.services.updateintentservice";

    private final static String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
    private final static String API_KEY = "&appid=";
    private final static String UNITS_KEY = "&units=";
    private final static String LANG_KEY = "&lang=";
    private final static String IMG_URL = "http://openweathermap.org/img/w/";
    private final static String IMG_EXT = ".png";

    public UpdateIntentService() {
        super("Weather Forecast Update Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (checkInternetConnection()) {
                updateStarted();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String city = preferences.getString(getString(R.string.settings_city_key), getString(R.string.settings_city_default));
                String apiid = preferences.getString(getString(R.string.settings_appid_key), getString(R.string.settings_appid_default));
                String units = preferences.getString(getString(R.string.settings_units_key), getString(R.string.settings_units_default));
                String lang = preferences.getString(getString(R.string.settings_language_key), getString(R.string.settings_language_default));
                updateWeatherForecast(city, apiid, units, lang);
            }
        }
    }

    private boolean checkInternetConnection() {
        boolean internetAvailable = true;
        NetworkInfo networkInfo = (NetworkInfo) ((ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null) {
            internetAvailable = false;
        } else if (!networkInfo.isConnected() || networkInfo.isRoaming()) {
            internetAvailable = false;
        }
        if (!internetAvailable) {
            Intent intent = new Intent(BROADCAST_ACTION);
            intent.putExtra(PARAM_STATUS, STATUS_FINISH);
            intent.putExtra(PARAM_RESULT, getString(R.string.txt_no_internet_connection));
            sendBroadcast(intent);
        }
        return internetAvailable;
    }

    private void updateStarted() {
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(PARAM_STATUS, STATUS_START);
        sendBroadcast(intent);
    }

    private void updateProgress(int progress) {
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(PARAM_STATUS, STATUS_PROGRESS);
        intent.putExtra(PARAM_PROGRESS, progress);
        sendBroadcast(intent);
    }

    public void updateFinished(OpenweathermapObject data) {
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
    }

    private void updateWeatherForecast(String aCity, String apiId, String aUnits, String language) {
        updateProgress(0);
        OpenweathermapObject openweathermapResult = null;

        String city = aCity;
        if (city.matches("\\d*")) {
            city = "cityId=".concat(city);
        } else {
            city = "q=".concat(city);
        }
        String apiKey = API_KEY.concat(apiId);
        String units = aUnits;
        if (!units.isEmpty()) {
            units = UNITS_KEY.concat(units);
        }
        String lang = language;
        if (!lang.isEmpty()) {
            lang = LANG_KEY.concat(lang);
        }
        String url = BASE_URL.concat(city.concat(apiKey).concat(units.concat(lang)));

        try {
            HttpURLConnection connection = (HttpURLConnection) new java.net.URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            connection.connect();
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());

            updateProgress(10);

            Gson gson = new GsonBuilder()
                    .setExclusionStrategies(new ExclusionStrategy() {
                        @Override
                        public boolean shouldSkipField(FieldAttributes f) {
                            return f.getDeclaringClass().equals(RealmObject.class);
                        }

                        @Override
                        public boolean shouldSkipClass(Class<?> clazz) {
                            return false;
                        }
                    })
                    .create();
            openweathermapResult = gson.fromJson(inputStreamReader, OpenweathermapObject.class);
            connection.disconnect();
            connection = null;

            updateProgress(50);

            for (final ForecastListItem item : openweathermapResult.getWeatherForecastList()) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                String iconName = item.getWeather().get(0).getIconName();
                String iconUrl = IMG_URL.concat(iconName.concat(IMG_EXT));
                try {
                    InputStream inputStream = new java.net.URL(iconUrl).openStream();
                    byte[] data = new byte[1024];
                    int count;
                    while ((count = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, count);
                    }
                    buffer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                item.getWeather().get(0).setIconData(buffer.toByteArray());
            }

            updateProgress(99);
            openweathermapResult.setErrorMessage("");

        } catch (IOException e) {
            if (openweathermapResult != null) {
                openweathermapResult.setErrorMessage(e.getMessage());
            }
            e.printStackTrace();
        }

        updateProgress(100);
        updateFinished(openweathermapResult);
    }
}
