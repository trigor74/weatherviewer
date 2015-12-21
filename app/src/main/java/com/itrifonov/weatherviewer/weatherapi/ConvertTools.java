package com.itrifonov.weatherviewer.weatherapi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ConvertTools {

    private static String[] windDirection = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};

    public static String convertDirection(float deg) {
        int val = (int) (deg / 22.5 + .5);
        return windDirection[(val % 16)];
    }

    //Unicode Character 'DEGREE CELSIUS' (U+2103) ℃
    //Unicode Character 'DEGREE FAHRENHEIT' (U+2109) ℉

    /**
     * Convert float value of temperature to string with units suffix
     *
     * @param temp  float temperature
     * @param units string with "metric" for celsius, "imperial" for fahrenheit or empty sting for standard kelvin
     * @return String with units suffix
     */
    public static String convertTemp(float temp, String units) {
        String sf = "%d";
        if (units.equals("metric")) {
            sf = sf.concat(" ℃");
        } else if (units.equals("imperial")) {
            sf = sf.concat(" ℉");
        }

        int i = Math.round(temp);
        if (units.isEmpty() || (i <= 0)) {
            return String.format(sf, i);
        } else {
            return String.format("+".concat(sf), i);
        }
    }

    public static Bitmap arrayToBitmap(byte[] iconData) {
        if (iconData != null && iconData.length > 0) {
            return BitmapFactory.decodeByteArray(iconData, 0, iconData.length);
        } else {
            return null;
        }
    }
}
