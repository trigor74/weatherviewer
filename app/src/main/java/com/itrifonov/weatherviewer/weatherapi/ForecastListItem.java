package com.itrifonov.weatherviewer.weatherapi;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ForecastListItem {
    long dt;
    Conditions main;
    Weather[] weather;
    Clouds clouds;
    Wind wind;
    RainSnow rain;
    RainSnow snow;
    String dt_txt;

    class Conditions {
        float temp;
        float temp_min;
        float temp_max;
        float pressure;
        float sea_level;
        float grnd_level;
        float humidity;
        float temp_kf;
    }

    class Weather {
        int id;
        String main;
        String description;
        @Expose
        Bitmap iconBitmap;
        String icon;
    }

    class Clouds {
        int all;
    }

    class Wind {
        float speed;
        float deg;
    }

    class RainSnow {
        @SerializedName("3h")
        String m3h;
    }

    public long getTimestamp() {
        return dt;
    }

    public Bitmap getIconBitmap() {
        return weather[0].iconBitmap;
    }

    public float getTemp() {
        return main.temp;
    }

    public float getTempMin() {
        return main.temp_min;
    }

    public float getTempMax() {
        return main.temp_max;
    }

    public String getDescription() {
        return weather[0].description;
    }

    public float getPressure() {
        return main.pressure;
    }

    public float getHumidity() {
        return main.humidity;
    }

    public float getWindSpeed() {
        return wind.speed;
    }

    public float getWindDeg() {
        return wind.deg;
    }

    public String getWindDirection() {
        return degToDirection(wind.deg);
    }

    private String degToDirection(float deg) {
        int val = (int) (deg / 22.5 + .5);
        String[] dir = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        return dir[(val % 16)];
    }

    public int getClouds() {
        return clouds.all;
    }

    public String getRain() {
        return rain.m3h;
    }

    public String getSnow() {
        return snow.m3h;
    }
}

