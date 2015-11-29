package com.itrifonov.weatherviewer.weatherapi.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class City extends RealmObject {
    @SerializedName("id")
    private int cityId;
    @SerializedName("name")
    private String cityName;
    @SerializedName("coord")
    private Coordinates coordinates;
    private String country;

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
