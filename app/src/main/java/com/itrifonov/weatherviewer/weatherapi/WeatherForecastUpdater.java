package com.itrifonov.weatherviewer.weatherapi;

import android.os.AsyncTask;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itrifonov.weatherviewer.weatherapi.interfaces.IWeatherUpdateListener;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import io.realm.RealmObject;

/**
 * Downloading weather forecast data from http://api.openweathermap.org/data/2.5/forecast.
 *
 * @param city  string with a city name or id
 * @param apiid string with an API key to get access to weather API
 * @param units string with "metric" for celsius, "imperial" for fahrenheit or empty sting for standard kelvin
 * @param lang  string with a language code
 * @return OpenweathermapObject object, with message in errorMessage field if error or empty if no errors
 */
public class WeatherForecastUpdater extends AsyncTask<String, Integer, OpenweathermapObject> {

    private String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
    private String API_KEY = "&appid=";
    private String UNITS_KEY = "&units=";
    private String LANG_KEY = "&lang=";
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
    protected OpenweathermapObject doInBackground(String... params) {
        publishProgress(0);
        OpenweathermapObject openweathermapResult = null;

        String city = params[0];
        if (city.matches("\\d*")) {
            city = "cityId=".concat(city);
        } else {
            city = "q=".concat(city);
        }
        String apiKey = API_KEY.concat(params[1]);
        String units = params[2];
        if (!units.isEmpty()) {
            units = UNITS_KEY.concat(units);
        }
        String lang = params[3];
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

            publishProgress(50);

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

            publishProgress(99);
            openweathermapResult.setErrorMessage("");

        } catch (IOException e) {
            if (openweathermapResult != null) {
                openweathermapResult.setErrorMessage(e.getMessage());
            }
            e.printStackTrace();
        }

        publishProgress(100);

        return openweathermapResult;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (updateListener != null) {
            updateListener.onProgressUpdated(values[0]);
        }
    }

    @Override
    protected void onPostExecute(OpenweathermapObject result) {
        if (updateListener != null) {
            updateListener.onUpdateFinished(result);
        }
    }
}
