package com.itrifonov.weatherviewer.weatherapi.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class Clouds extends RealmObject {
    @SerializedName("all")
    private int cloudiness;

    public int getCloudiness() {
        return cloudiness;
    }

    public void setCloudiness(int cloudiness) {
        this.cloudiness = cloudiness;
    }
}
