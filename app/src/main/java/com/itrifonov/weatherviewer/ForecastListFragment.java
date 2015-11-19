package com.itrifonov.weatherviewer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.itrifonov.weatherviewer.weatherapi.ForecastListItem;
import com.itrifonov.weatherviewer.weatherapi.WeatherForecastData;

import java.util.ArrayList;

public class ForecastListFragment extends Fragment {

    private ListView mListView;
    private ArrayAdapter<String> mAdapter;

    private String[] testlist = { "Красны", "Оранжевый", "Желтый", "Зелёный", "Голубой", "Синий", "Фиолетовый"};

    public interface OnListItemSelectedListener {
        void onListItemSelected (int position);
    }

    private OnListItemSelectedListener mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnListItemSelectedListener) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // TODO: 19.11.15 load forecast data from site
        WeatherForecastData forecast = WeatherForecastData.getInstance();
        forecast.setCityName("cherkassy,ua");
        forecast.reloadData();
        ArrayList<ForecastListItem> list = forecast.getWeatherForecastList();

        mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, testlist);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(R.id.list_view_forecast);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.onListItemSelected(position);
            }
        });
    }
}
