package com.itrifonov.weatherviewer.fragments;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;

import java.text.SimpleDateFormat;

import io.realm.Realm;
import io.realm.RealmResults;

public class ForecastDetailFragment extends Fragment {

    public static String ARG_INDEX = "ARG_INDEX";
    private static int mIndex = -1;
    private TextView time;
    private TextView dayOfWeek;
    private TextView date;
    private ImageView icon;
    private TextView temp;
    private TextView description;
    private TextView temp_min;
    private TextView temp_max;
    private TextView pressure;
    private TextView humidity;
    private TextView wind_speed;
    private TextView wind_deg;
    private TextView clouds;
    private TextView rain;
    private TextView snow;
    private Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null && getArguments().containsKey(ARG_INDEX)) {
            mIndex = getArguments().getInt(ARG_INDEX, -1);
        }
        realm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast_detail, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dayOfWeek = (TextView) view.findViewById(R.id.forecast_detail_day_of_week);
        time = (TextView) view.findViewById(R.id.forecast_detail_time);
        date = (TextView) view.findViewById(R.id.forecast_detail_date);
        icon = (ImageView) view.findViewById(R.id.forecast_detail_icon);
        temp = (TextView) view.findViewById(R.id.forecast_detail_temp);
        description = (TextView) view.findViewById(R.id.forecast_detail_description);
        temp_min = (TextView) view.findViewById(R.id.forecast_detail_temp_min);
        temp_max = (TextView) view.findViewById(R.id.forecast_detail_temp_max);
        pressure = (TextView) view.findViewById(R.id.forecast_detail_pressure);
        humidity = (TextView) view.findViewById(R.id.forecast_detail_humidity);
        wind_speed = (TextView) view.findViewById(R.id.forecast_detail_wind_speed);
        wind_deg = (TextView) view.findViewById(R.id.forecast_detail_wind_deg);
        clouds = (TextView) view.findViewById(R.id.forecast_detail_clouds);
        rain = (TextView) view.findViewById(R.id.forecast_detail_rain);
        snow = (TextView) view.findViewById(R.id.forecast_detail_snow);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mIndex >= 0) {
            update(mIndex);
        }
    }

    public void update(int index) {
        mIndex = index;
        if (mIndex == -1)
            return;
        if (realm == null)
            return;
        RealmResults<ForecastListItem> realmResults = realm.allObjects(ForecastListItem.class);
        if (realmResults.size() == 0)
            return;

        ForecastListItem item = realmResults.get(mIndex);
        try {
            long timestamp = item.getTimeStamp() * 1000L;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
            dayOfWeek.setText(simpleDateFormat.format(timestamp));
            time.setText(DateFormat.getTimeFormat(getContext()).format(timestamp));
            date.setText(DateFormat.getMediumDateFormat(getContext()).format(timestamp));
            byte[] iconData = item.getWeather().get(0).getIconData();
            if (iconData != null && iconData.length > 0)
                icon.setImageBitmap(BitmapFactory.decodeByteArray(iconData, 0, iconData.length));
            temp.setText(getTemp(item.getConditions().getTemp()));
            description.setText(item.getWeather().get(0).getDescription());
            temp_min.setText(getString(R.string.txt_temp_min, getTemp(item.getConditions().getTempMin())));
            temp_max.setText(getString(R.string.txt_temp_max, getTemp(item.getConditions().getTempMax())));
            pressure.setText(getString(R.string.txt_pressure, item.getConditions().getPressure()));
            humidity.setText(getString(R.string.txt_humidity,
                    (int) Math.round(item.getConditions().getHumidity())));
            wind_speed.setText(getString(R.string.txt_wind_speed,
                    (int) Math.round(item.getWind().getSpeed())));
            wind_deg.setText(getString(R.string.txt_wind_direction,
                    Math.round(item.getWind().getDeg()), degToDirection(item.getWind().getDeg())));
            clouds.setText(getString(R.string.txt_clouds, item.getClouds().getCloudiness()));
            if (!item.getRain().getM3h().isEmpty()) {
                rain.setVisibility(View.VISIBLE);
                rain.setText(getString(R.string.txt_rain, item.getRain().getM3h()));
            } else {
                rain.setVisibility(View.GONE);
                rain.setText("");
            }
            if (!item.getSnow().getM3h().isEmpty()) {
                snow.setVisibility(View.VISIBLE);
                snow.setText(getString(R.string.txt_snow, item.getSnow().getM3h()));
            } else {
                snow.setVisibility(View.GONE);
                snow.setText("");
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private String getTemp(float f) {
        int i = Math.round(f);
        if (i > 0) {
            return String.format("+%d", i);
        } else {
            return Integer.toString(i);
        }

    }

    private String degToDirection(float deg) {
        int val = (int) (deg / 22.5 + .5);
        String[] dir = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        return dir[(val % 16)];
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
