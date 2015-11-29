package com.itrifonov.weatherviewer.weatherapi;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;
import com.itrifonov.weatherviewer.weatherapi.models.WeatherIcon;

import java.text.SimpleDateFormat;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class WeatherAdapter extends RealmBaseAdapter<ForecastListItem> {

    private static class ViewHolderItem {
        TextView time;
        TextView dayOfWeek;
        TextView date;
        TextView temp;
        ImageView icon;
        TextView weather;
    }

    public WeatherAdapter(Context context, RealmResults<ForecastListItem> objects, boolean automaticUpdate) {
        super(context, objects, automaticUpdate);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.weather_forecast_list_item, parent, false);

            viewHolder = new ViewHolderItem();

            viewHolder.time = (TextView) convertView.findViewById(R.id.forecast_item_time);
            viewHolder.dayOfWeek = (TextView) convertView.findViewById(R.id.forecast_item_day_of_week);
            viewHolder.date = (TextView) convertView.findViewById(R.id.forecast_item_date);
            viewHolder.temp = (TextView) convertView.findViewById(R.id.forecast_item_temp);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.forecast_item_icon);
            viewHolder.weather = (TextView) convertView.findViewById(R.id.forecast_item_description);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        ForecastListItem item = getItem(position);

        if (item != null) {
            long timestamp = item.getTimeStamp() * 1000L;
            viewHolder.time.setText(DateFormat.getTimeFormat(context).format(timestamp));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
            viewHolder.dayOfWeek.setText(simpleDateFormat.format(timestamp));
            viewHolder.date.setText(DateFormat.getMediumDateFormat(context).format(timestamp));
            viewHolder.temp.setText(getTemp(item.getConditions().getTemp()));
//            byte[] byteArray = item.getIconBitmap();
//            viewHolder.icon.setImageBitmap(BitmapFactory.decodeByteArray(byteArray,0, byteArray.length));
            viewHolder.weather.setText(item.getWeather().get(0).getDescription());
        }
        return convertView;
    }

    public RealmResults<ForecastListItem> getRealmResults() {
        return realmResults;
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
