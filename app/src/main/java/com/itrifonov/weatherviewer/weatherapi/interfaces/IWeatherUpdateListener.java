package com.itrifonov.weatherviewer.weatherapi.interfaces;

import com.itrifonov.weatherviewer.weatherapi.OpenweathermapObject;

public interface IWeatherUpdateListener {
    void onUpdateStarted();

    void onProgressUpdated(int progress);

    void onUpdateFinished(OpenweathermapObject data);
}
