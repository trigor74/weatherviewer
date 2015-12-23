package com.itrifonov.weatherviewer.fragments;

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

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.interfaces.IOnListItemSelectedListener;
import com.itrifonov.weatherviewer.services.interfaces.IServiceHelperCallbackListener;
import com.itrifonov.weatherviewer.services.ServiceHelper;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;
import com.itrifonov.weatherviewer.adapters.WeatherAdapter;

import io.realm.Realm;
import io.realm.RealmResults;

public class ForecastListFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {

    private Realm realm;
    private ListView mListView;
    private WeatherAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private IServiceHelperCallbackListener serviceHelperCallbackListener =
            new IServiceHelperCallbackListener() {
                @Override
                public void onServiceHelperCallback(Bundle event) {
                    int eventId = event.getInt(ServiceHelper.SERVICE_HELPER_EVENT, -1);
                    if (eventId == ServiceHelper.EVENT_UPDATE_STARTED)
                        swipeRefreshLayout.setRefreshing(true);
                    if (eventId == ServiceHelper.EVENT_UPDATE_STOPPED)
                        swipeRefreshLayout.setRefreshing(false);
                }
            };

    private IOnListItemSelectedListener listItemSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listItemSelectedListener = (IOnListItemSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        realm = Realm.getDefaultInstance();
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

        RealmResults<ForecastListItem> forecastList = realm.where(ForecastListItem.class).findAll();
        if (forecastList.size() == 0) {
            updateWeatherForecast();
        }
        if (mAdapter == null) {
            mAdapter = new WeatherAdapter(getActivity(), forecastList, true);
        }
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ForecastListItem item = (ForecastListItem) parent.getAdapter().getItem(position);
                listItemSelectedListener.onListItemSelected(item.getTimeStamp());
            }
        });
    }

    public void updateWeatherForecast() {
        swipeRefreshLayout.setRefreshing(true);
        ServiceHelper.getInstance(getContext()).updateWeatherForecast();
    }

    @Override
    public void onRefresh() {
        updateWeatherForecast();
    }

    @Override
    public void onResume() {
        super.onResume();
        ServiceHelper.getInstance(getContext()).addListener(serviceHelperCallbackListener);
    }

    @Override
    public void onPause() {
        ServiceHelper.getInstance(getContext()).removeListener(serviceHelperCallbackListener);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
