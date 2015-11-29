package com.itrifonov.weatherviewer.weatherapi;

import android.os.AsyncTask;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itrifonov.weatherviewer.Settings;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class WeatherForecastUpdater extends AsyncTask<Void, Void, Void> {

    private String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
    private String API_KEY = "&appid=";
    private String EXTRA_KEY = "&units=";
    private String IMG_URL = "http://openweathermap.org/img/w/";
    private String IMG_EXT = ".png";

    @Override
    protected Void doInBackground(Void... params) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            RealmResults<Settings> realmResults = realm.allObjects(Settings.class);
            if (realmResults.size() != 0) {
                Settings settings = realmResults.first();
                String city = settings.getCity();
                if (city.matches("\\d*")) {
                    city = "cityId=".concat(city);
                } else {
                    city = "q=".concat(city);
                }
                String apiKey = API_KEY.concat(settings.getApiKey());
                String extraKey = EXTRA_KEY.concat(settings.getUnits());
                String url = BASE_URL.concat(city.concat(apiKey).concat(extraKey));
                OpenweathermapObject openweathermapResult = null;
                try {
                    HttpURLConnection connection = (HttpURLConnection) new java.net.URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(10000);
                    connection.connect();
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());

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

                } catch (IOException e) {
                    if (realm.isInTransaction())
                        realm.cancelTransaction();
                    realm.refresh();
                    e.printStackTrace();
                }
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return null;
    }
}
