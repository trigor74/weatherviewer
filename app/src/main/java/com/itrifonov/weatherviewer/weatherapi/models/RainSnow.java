package com.itrifonov.weatherviewer.weatherapi.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class RainSnow extends RealmObject {
    @SerializedName("3h")
    private String m3h;

    public String getM3h() {
        return m3h;
    }

    public void setM3h(String m3h) {
        this.m3h = m3h;
    }
}

