package com.itrifonov.weatherviewer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.itrifonov.weatherviewer.weatherapi.ForecastListItem;
import com.itrifonov.weatherviewer.weatherapi.WeatherAdapter;
import com.itrifonov.weatherviewer.weatherapi.WeatherForecastData;

import java.util.ArrayList;

public class ForecastListFragment extends Fragment
        implements WeatherForecastData.OnWeatherForecastUpdatedListener,
        SwipeRefreshLayout.OnRefreshListener {

    private WeatherForecastData weatherForecastData;
    private ListView mListView;
    private WeatherAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public interface OnListItemSelectedListener {
        void onListItemSelected(int position);
    }

    private OnListItemSelectedListener mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnListItemSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
        weatherForecastData = WeatherForecastData.getInstance(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);

        // https://yassirh.com/2014/05/how-to-use-swiperefreshlayout-the-right-way/
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (mListView != null && mListView.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = mListView.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = mListView.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (weatherForecastData.getUpdateState() < 0) {
            weatherForecastData.setCityName("cherkassy,ua");
            updateWeatherForecast();
        } else {
            if (mAdapter == null)
                mAdapter = new WeatherAdapter(getActivity(), weatherForecastData.getWeatherForecastList());
            mListView.setAdapter(mAdapter);
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.onListItemSelected(position);
            }
        });
    }

    public void updateWeatherForecast() {
        swipeRefreshLayout.setRefreshing(true);
        weatherForecastData.reloadData();
    }

    @Override
    public void onWeatherForecastUpdated() {
        ArrayList<ForecastListItem> list = weatherForecastData.getWeatherForecastList();
        if (list != null) {
            if (mAdapter == null) {
                mAdapter = new WeatherAdapter(getActivity(), list);
                if (mListView != null)
                    mListView.setAdapter(mAdapter);
            } else {
                mAdapter.clear();
                mAdapter.addAll(list);
                mAdapter.notifyDataSetChanged();
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        updateWeatherForecast();
    }
}
