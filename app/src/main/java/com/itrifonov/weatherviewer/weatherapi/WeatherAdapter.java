package com.itrifonov.weatherviewer.weatherapi;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itrifonov.weatherviewer.R;

import java.text.SimpleDateFormat;
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

        long timestamp = item.dt * 1000L;
        TextView time = (TextView) convertView.findViewById(R.id.forecast_item_time);
        time.setText(DateFormat.getTimeFormat(context).format(timestamp));

        TextView dayOfWeek = (TextView) convertView.findViewById(R.id.forecast_item_day_of_week);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        dayOfWeek.setText(simpleDateFormat.format(timestamp));

        TextView date = (TextView) convertView.findViewById(R.id.forecast_item_date);
        date.setText(DateFormat.getMediumDateFormat(context).format(timestamp));

        TextView temp = (TextView) convertView.findViewById(R.id.forecast_item_temp);
        temp.setText(getTemp(item.main.temp));

        ImageView icon = (ImageView) convertView.findViewById(R.id.forecast_item_icon);
        icon.setImageBitmap(item.weather[0].iconBitmap);

        TextView weather = (TextView) convertView.findViewById(R.id.forecast_item_description);
        weather.setText(item.weather[0].description);

        return convertView;
    }

    private String getTemp(float f) {
        int i = Math.round(f);
        if (i > 0) {
            return String.format("+%d", i);
        } else {
            return String.format("%d", i);
        }
    }
}
