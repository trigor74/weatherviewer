package com.itrifonov.weatherviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.itrifonov.weatherviewer.weatherapi.ForecastListItem;
import com.itrifonov.weatherviewer.weatherapi.WeatherForecastData;

import java.text.SimpleDateFormat;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_INDEX)) {
            mIndex = getArguments().getInt(ARG_INDEX, -1);
        }
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

        update(mIndex);
    }

    public void update(int index) {
        mIndex = index;
        ForecastListItem item = WeatherForecastData.getInstance().getWeatherForecastList().get(mIndex);
        try {
            long timestamp = item.getTimestamp() * 1000L;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
            dayOfWeek.setText(simpleDateFormat.format(timestamp));
            time.setText(DateFormat.getTimeFormat(getContext()).format(timestamp));
            date.setText(DateFormat.getMediumDateFormat(getContext()).format(timestamp));
            icon.setImageBitmap(item.getIconBitmap());
            temp.setText(getTemp(item.getTemp()));
            description.setText(item.getDescription());
            temp_min.setText(getString(R.string.txt_temp_min, getTemp(item.getTempMin())));
            temp_max.setText(getString(R.string.txt_temp_max, getTemp(item.getTempMax())));
            pressure.setText(getString(R.string.txt_pressure, item.getPressure()));
            humidity.setText(getString(R.string.txt_humidity, (int) Math.round(item.getHumidity())));
            wind_speed.setText(getString(R.string.txt_wind_speed, (int) Math.round(item.getWindSpeed())));
            wind_deg.setText(getString(R.string.txt_wind_direction, Math.round(item.getWindDeg()), item.getWindDirection()));
            clouds.setText(getString(R.string.txt_clouds, item.getClouds()));
            if ((item.getRain() != null) && !item.getRain().isEmpty()) {
                rain.setVisibility(View.VISIBLE);
                rain.setText(getString(R.string.txt_rain, item.getRain()));
            } else {
                rain.setVisibility(View.INVISIBLE);
                rain.setText("");
            }
            if ((item.getSnow() != null) && !item.getSnow().isEmpty()) {
                snow.setVisibility(View.VISIBLE);
                snow.setText(getString(R.string.txt_snow, item.getSnow()));
            } else {
                snow.setVisibility(View.INVISIBLE);
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
}
