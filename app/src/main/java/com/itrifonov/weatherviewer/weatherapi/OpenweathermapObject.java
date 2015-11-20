package com.itrifonov.weatherviewer.weatherapi;

/*
    code Internal parameter
    message Internal parameter
    city
        city.id City ID
        city.name City name
        city.coord
            city.coord.lat City geo location, latitude
            city.coord.lon City geo location, longitude
        city.country Country code (GB, JP etc.)
    cnt Number of lines returned by this API call
    list
        list.dt Time of data forecasted, unix, UTC
        list.main
            list.main.temp Temperature. Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
            list.main.temp_min Minimum temperature at the moment of calculation. This is deviation from 'temp' that is possible for large cities and megalopolises geographically expanded (use these parameter optionally). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
            list.main.temp_max Maximum temperature at the moment of calculation. This is deviation from 'temp' that is possible for large cities and megalopolises geographically expanded (use these parameter optionally). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
            list.main.pressure Atmospheric pressure on the sea level by default, hPa
            list.main.sea_level Atmospheric pressure on the sea level, hPa
            list.main.grnd_level Atmospheric pressure on the ground level, hPa
            list.main.humidity Humidity, %
            list.main.temp_kf Internal parameter
        list.weather (more info Weather condition codes)
            list.weather.id Weather condition id
            list.weather.main Group of weather parameters (Rain, Snow, Extreme etc.)
            list.weather.description Weather condition within the group
            list.weather.icon Weather icon id (http://openweathermap.org/img/w/{ID}.png)
        list.clouds
            list.clouds.all Cloudiness, %
        list.wind
            list.wind.speed Wind speed. Unit Default: meter/sec, Metric: meter/sec, Imperial: miles/hour.
            list.wind.deg Wind direction, degrees (meteorological)
        list.rain
            list.rain.3h Rain volume for last 3 hours, mm
        list.snow
            list.snow.3h Snow volume for last 3 hours
        list.dt_txt Data/time of caluclation, UTC
*/

import java.util.ArrayList;

public class OpenweathermapObject {
    private City city;
    private int cnt;
    ArrayList<ForecastListItem> list;

    public ArrayList<ForecastListItem> getWeatherForecastList() {
        return list;
    }

    public String getCityName() {
        return city.name;
    }

    public String getCountry() {
        return city.country;
    }

    public int getCityId() {
        return city.id;
    }

    private class City {
        int id;
        String name;
        Coord coord;
        String country;

        private class Coord {
            float lon;
            float lat;
        }
    }
}
