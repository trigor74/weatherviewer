package com.itrifonov.weatherviewer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itrifonov.weatherviewer.weatherapi.WeatherForecastData;

import java.text.DateFormat;

public class InfobarFragment extends Fragment implements WeatherForecastData.OnWeatherForecastUpdatedListener {

    private WeatherForecastData mWeatherForecast;
    private TextView mCityName;
    private TextView mLastUpdated;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mWeatherForecast = WeatherForecastData.getInstance(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_infobar, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCityName = (TextView) view.findViewById(R.id.text_view_city_info);
        mLastUpdated = (TextView) view.findViewById(R.id.text_view_last_update_info);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onWeatherForecastUpdated();
    }

    @Override
    public void onWeatherForecastUpdated() {
        if (mWeatherForecast != null) {
            if (mCityName != null)
                mCityName.setText(mWeatherForecast.getCityName());
            long timestamp = mWeatherForecast.getUpdateState() * 1000L;
            if (mLastUpdated != null) {
                mLastUpdated.setText(getString(R.string.txt_last_update,
                        DateFormat.getDateTimeInstance().format(timestamp)));
            }
        }
    }

    @Override
    public void onDetach() {
        if (mWeatherForecast != null) {
            mWeatherForecast.removeOnWeatherForecastUpdatedListner(this);
        }
        super.onDetach();
    }
}
