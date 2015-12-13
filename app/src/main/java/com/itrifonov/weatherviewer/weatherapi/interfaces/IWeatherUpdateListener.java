package com.itrifonov.weatherviewer.weatherapi.interfaces;

public interface IWeatherUpdateListener {
    void onUpdateStarted();
    void onUpdateFinished(String errorMessage);
}
