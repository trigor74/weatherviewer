package com.itrifonov.weatherviewer.weatherapi;

import java.util.ArrayList;

public class WeatherForecastData {
    private String mCityName;
    private int mCityId;
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
    }

    WeatherForecastData(String cityName) {
        mCityId = -1;
        mCityName = cityName;
        mWeatherForecast = null;
    }

    WeatherForecastData(int cityId) {
        mCityId = cityId;
        mCityName = "";
        mWeatherForecast = null;
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

    public void reloadData() {
        if (mCityId > 0) {
            // TODO: 19.11.15 add load data by city id
        } else if (!mCityName.isEmpty()) {
            // TODO: 19.11.15 add load data by city name
        }
    }

    private class OpenweathermapHTTPClient {
        private String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast?";
        private String IMG_URL = "http://openweathermap.org/img/w/";
        private String IMG_EXT = ".png";
        private String API_KEY = "&appid=28bfbe7a35614f03ddaaf3b091f2a414";


    }
}
