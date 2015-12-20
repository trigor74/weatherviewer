package com.itrifonov.weatherviewer.weatherapi;

import android.os.AsyncTask;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itrifonov.weatherviewer.models.Settings;
import com.itrifonov.weatherviewer.weatherapi.interfaces.IWeatherUpdateListener;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class WeatherForecastUpdater extends AsyncTask<String, Integer, String> {

    private String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
    private String API_KEY = "&appid=";
    private String UNITS_KEY = "&units=";
    private String LANG_KEY = "&lang=";
    private String DEL_OLD = "false";
    private String IMG_URL = "http://openweathermap.org/img/w/";
    private String IMG_EXT = ".png";
    private IWeatherUpdateListener updateListener;

    public WeatherForecastUpdater(IWeatherUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    @Override
    protected void onPreExecute() {
        if (updateListener != null) {
            updateListener.onUpdateStarted();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        publishProgress(0);
        String updateResult = "";
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            // TODO: 20.12.15 process params - APPID, UNITS, LANG, DELETE OLD DATA
            RealmResults<Settings> realmResults = realm.allObjects(Settings.class);
            if (realmResults.size() != 0) {
                Settings settings = realmResults.first();
                String city = settings.getCity();
                if (city.matches("\\d*")) {
                    city = "cityId=".concat(city);
                } else {
                    city = "q=".concat(city);
                }
                publishProgress(5);
                String apiKey = API_KEY.concat(settings.getApiKey());
                String extraKey = UNITS_KEY.concat(settings.getUnits());
                String url = BASE_URL.concat(city.concat(apiKey).concat(extraKey));
                OpenweathermapObject openweathermapResult = null;
                try {
                    HttpURLConnection connection = (HttpURLConnection) new java.net.URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(10000);
                    connection.connect();
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());

                    publishProgress(10);

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

                    publishProgress(30);

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

                    publishProgress(70);

                    realm.beginTransaction();

                    if (settings.getDeleteOldData()) {
                        Long timeStamp = openweathermapResult.getWeatherForecastList().get(0).getTimeStamp();
                        RealmResults<ForecastListItem> result = realm.where(ForecastListItem.class)
                                .lessThan("timeStamp", timeStamp).findAll();
                        result.clear();
                    }

                    realm.copyToRealmOrUpdate(openweathermapResult.getWeatherForecastList());
                    realm.allObjects(Settings.class).first().setLastUpdate(System.currentTimeMillis());

                    realm.commitTransaction();

                    publishProgress(99);

                } catch (IOException e) {
                    if (realm.isInTransaction()) {
                        realm.cancelTransaction();
                    }
                    updateResult = e.getMessage();
                    e.printStackTrace();
                }
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        publishProgress(100);

        return updateResult;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (updateListener != null) {
            updateListener.onProgressUpdated(values[0]);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (updateListener != null) {
            updateListener.onUpdateFinished(result);
        }
    }
}
