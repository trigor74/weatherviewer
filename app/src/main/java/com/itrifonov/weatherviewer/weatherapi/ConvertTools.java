package com.itrifonov.weatherviewer.weatherapi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ConvertTools {

    public static String convertDirection(float deg) {
        int val = (int) (deg / 22.5 + .5);
        String[] dir = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        return dir[(val % 16)];
    }

    public static String convertTemp(float f) {
        int i = Math.round(f);
        if (i > 0) {
            return String.format("+%d", i);
        } else {
            return String.format("%d", i);
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
