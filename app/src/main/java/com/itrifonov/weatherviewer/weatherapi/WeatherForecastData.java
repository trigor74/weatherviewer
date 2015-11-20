package com.itrifonov.weatherviewer.weatherapi;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WeatherForecastData {
    private String mCityName;
    private int mCityId;
    private long updateState;
    private OpenweathermapObject mWeatherForecast;
    //private ArrayList<pic> mForecastPics;

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
            String path = BASE_URL.concat(params[0].concat(API_KEY));
            OpenweathermapObject weatherforecast;
            weatherforecast = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.connect();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder buffer = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                // TODO: 20.11.2015 handle buf.toString() with GSON
                // weatherforecast = ....;
                // TODO: 20.11.2015 load images (icons)

            } catch (IOException e) {
                e.printStackTrace();
                // TODO: 20.11.2015 handle exception
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return weatherforecast;
        }

        @Override
        protected void onPostExecute(OpenweathermapObject openweathermapObject) {
            mWeatherForecast = openweathermapObject;
            updateState = System.currentTimeMillis() / 1000L; //latest update timestamp
        }
    }
}
