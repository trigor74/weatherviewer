package com.itrifonov.weatherviewer.weatherapi.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class WeatherIcon extends RealmObject {
    @PrimaryKey
    private String iconName;
    private byte[] iconData;

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public byte[] getIconData() {
        return iconData;
    }

    public void setIconData(byte[] iconData) {
        this.iconData = iconData;
    }
}
