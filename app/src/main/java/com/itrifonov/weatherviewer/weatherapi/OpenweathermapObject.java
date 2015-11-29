package com.itrifonov.weatherviewer.weatherapi;

import com.google.gson.annotations.SerializedName;
import com.itrifonov.weatherviewer.weatherapi.models.City;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import java.util.ArrayList;

public class OpenweathermapObject {
    private City city;
    @SerializedName("cnt")
    private int count;
    @SerializedName("list")
    ArrayList<ForecastListItem> weatherForecastList;

    public ArrayList<ForecastListItem> getWeatherForecastList() {
        return weatherForecastList;
    }

    public String getCityName() {
        return city.getCityName();
    }

    public String getCountry() {
        return city.getCountry();
    }

    public int getCityId() {
        return city.getCityId();
    }

}
