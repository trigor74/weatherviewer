package com.itrifonov.weatherviewer.weatherapi.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ForecastListItem extends RealmObject {
    @PrimaryKey
    @SerializedName("dt")
    private long timeStamp;
    @SerializedName("main")
    private Conditions conditions;
    private RealmList<Weather> weather;
    private Clouds clouds;
    private Wind wind;
    private RainSnow rain;
    private RainSnow snow;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Conditions getConditions() {
        return conditions;
    }

    public void setConditions(Conditions conditions) {
        this.conditions = conditions;
    }

    public RealmList<Weather> getWeather() {
        return weather;
    }

    public void setWeather(RealmList<Weather> weather) {
        this.weather = weather;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public RainSnow getRain() {
        return rain;
    }

    public void setRain(RainSnow rain) {
        this.rain = rain;
    }

    public RainSnow getSnow() {
        return snow;
    }

    public void setSnow(RainSnow snow) {
        this.snow = snow;
    }
}

