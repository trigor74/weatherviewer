package com.itrifonov.weatherviewer.weatherapi.models;

import io.realm.RealmObject;

public class Coordinates extends RealmObject {
    private float lon;
    private float lat;

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }
}
