package com.itrifonov.weatherviewer.models;

import io.realm.RealmObject;

public class Settings extends RealmObject {
    private String city;
    private long lastUpdate;
    private String apiKey;
    private String units;
    private Boolean deleteOldData;
    private Boolean startUpdateService;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Boolean getDeleteOldData() {
        return deleteOldData;
    }

    public void setDeleteOldData(Boolean deleteOldData) {
        this.deleteOldData = deleteOldData;
    }

    public Boolean getStartUpdateService() {
        return startUpdateService;
    }

    public void setStartUpdateService(Boolean startUpdateService) {
        this.startUpdateService = startUpdateService;
    }
}
