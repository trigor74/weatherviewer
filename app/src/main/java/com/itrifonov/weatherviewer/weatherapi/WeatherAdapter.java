package com.itrifonov.weatherviewer.weatherapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itrifonov.weatherviewer.R;

import java.util.List;

public class WeatherAdapter extends ArrayAdapter<ForecastListItem> {

    private final Context context;
    private final int layoutResourceId;

    public WeatherAdapter(Context context, int resource, List<ForecastListItem> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layoutResourceId = resource;
    }

    public WeatherAdapter(Context context, List<ForecastListItem> objects) {
        super(context, R.layout.weather_forecast_list_item, objects);
        this.context = context;
        this.layoutResourceId = R.layout.weather_forecast_list_item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ForecastListItem item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);
        }

        ImageView icon = (ImageView) convertView.findViewById(R.id.weather_icon);
        icon.setImageBitmap(item.weather[0].iconBitmap);

        TextView weather = (TextView) convertView.findViewById(R.id.weather_text);
        weather.setText(item.weather[0].description);

        return convertView;
    }
}
