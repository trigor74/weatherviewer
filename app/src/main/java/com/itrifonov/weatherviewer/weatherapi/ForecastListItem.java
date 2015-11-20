package com.itrifonov.weatherviewer.weatherapi;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ForecastListItem {
    int dt;
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
}

