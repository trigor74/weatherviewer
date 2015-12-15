package com.itrifonov.weatherviewer.weatherapi.interfaces;

public interface IWeatherUpdateListener {
    void onUpdateStarted();

    void onProgressUpdated(int progress);

    void onUpdateFinished(String errorMessage);
}
