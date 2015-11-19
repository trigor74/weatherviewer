package com.itrifonov.weatherviewer.weatherapi;

import java.util.ArrayList;

public class ForecastListItem {
    int dt;
    Conditions main;
    ArrayList<Weather> weather;
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
        String icon;
    }

    class Clouds {
        int all;
    }

    class Wind {
        float speed;
        int deg;
    }

    class RainSnow {
        String _3h;
    }
}

