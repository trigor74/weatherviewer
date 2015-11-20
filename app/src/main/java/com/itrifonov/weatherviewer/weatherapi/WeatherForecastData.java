package com.itrifonov.weatherviewer.weatherapi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class WeatherForecastData {
    private String mCityName;
    private int mCityId;
    private long updateState;
    private OpenweathermapObject mWeatherForecast;

    private static WeatherForecastData INSTANCE = null;

    public static WeatherForecastData getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WeatherForecastData();
        }
        return INSTANCE;
    }

    WeatherForecastData() {
        mCityId = -1;
        mCityName = "";
        mWeatherForecast = null;
        updateState = -1;
    }

    WeatherForecastData(String cityName) {
        mCityId = -1;
        mCityName = cityName;
        mWeatherForecast = null;
        updateState = -1;
    }

    WeatherForecastData(int cityId) {
        mCityId = cityId;
        mCityName = "";
        mWeatherForecast = null;
        updateState = -1;
    }

    WeatherForecastData(String cityName, Boolean download) {
        this(cityName);
        if (download) {
            reloadData();
        }
    }

    WeatherForecastData(int cityId, Boolean download) {
        this(cityId);
        if (download) {
            reloadData();
        }
    }

    public String getCityName() {
        if (mWeatherForecast != null) {
            return mWeatherForecast.getCityName().concat(",".concat(mWeatherForecast.getCountry()));
        } else {
            return mCityName;
        }
    }

    public void setCityName(String mCityName) {
        this.mCityName = mCityName;
    }

    public int getCityId() {
        if (mCityId > 0) {
            return mCityId;
        } else {
            if (mWeatherForecast != null) {
                return mWeatherForecast.getCityId();
            } else {
                return -1;
            }
        }
    }

    public void setCityId(int mCityId) {
        this.mCityId = mCityId;
    }

    public ArrayList<ForecastListItem> getWeatherForecastList() {
        if (mWeatherForecast != null) {
            return mWeatherForecast.getWeatherForecastList();
        } else {
            return null;
        }
    }

    public long getUpdateState() {
        return updateState;
    }

    public void reloadData() {
        if (mCityId > 0) {
            new OpenweathermapGetDataTask().execute("id=".concat(Integer.toString(mCityId)));
        } else if (!mCityName.isEmpty()) {
            new OpenweathermapGetDataTask().execute("q=".concat(mCityName));
        }
    }

    private class OpenweathermapGetDataTask extends AsyncTask<String, Void, OpenweathermapObject> {
        private String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
        private String IMG_URL = "http://openweathermap.org/img/w/";
        private String IMG_EXT = ".png";
        private String API_KEY = "&appid=28bfbe7a35614f03ddaaf3b091f2a414";

        @Override
        protected OpenweathermapObject doInBackground(String... params) {
            String url = BASE_URL.concat(params[0].concat(API_KEY));
            OpenweathermapObject openweathermapResult = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) new java.net.URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.connect();
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());

                Gson gson = new Gson();
                openweathermapResult = gson.fromJson(inputStreamReader, OpenweathermapObject.class);
                connection.disconnect();
                connection = null;

                for (final ForecastListItem item : openweathermapResult.getWeatherForecastList()) {
                    Bitmap bitmap = null;
                    String iconUrl = IMG_URL.concat(item.weather[0].icon.concat(IMG_EXT));
                    try {
                        InputStream inputStream = new java.net.URL(iconUrl).openStream();
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    item.weather[0].iconBitmap = bitmap; // imageView.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return openweathermapResult;
        }

        @Override
        protected void onPostExecute(OpenweathermapObject openweathermapObject) {
            mWeatherForecast = openweathermapObject;
            updateState = System.currentTimeMillis() / 1000L; //latest update timestamp
        }
    }
}
