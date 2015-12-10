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
import com.itrifonov.weatherviewer.interfaces.IServiceCallbackListener;
import com.itrifonov.weatherviewer.services.ServiceHelper;
import com.itrifonov.weatherviewer.weatherapi.models.ForecastListItem;
import com.itrifonov.weatherviewer.weatherapi.WeatherAdapter;

import io.realm.Realm;
import io.realm.RealmResults;

public class ForecastListFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener {

    private ListView mListView;
    private WeatherAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Realm realm;
    private IServiceCallbackListener callback = new IServiceCallbackListener() {
        @Override
        public void onServiceCallback() {
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    private IOnListItemSelectedListener mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (IOnListItemSelectedListener) context;
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
        if (mAdapter == null)
            mAdapter = new WeatherAdapter(getActivity(), forecastList, true);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.onListItemSelected(position);
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
        ServiceHelper.getInstance(getContext()).addListener(callback);
    }

    @Override
    public void onPause() {
        super.onPause();
        ServiceHelper.getInstance(getContext()).removeListener(callback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
