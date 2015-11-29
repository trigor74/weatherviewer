package com.itrifonov.weatherviewer.weatherapi.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class Weather extends RealmObject {
    private int id;
    private String main;
    private String description;
    @SerializedName("icon")
    private String iconName;
    @Expose
    private byte[] iconData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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
