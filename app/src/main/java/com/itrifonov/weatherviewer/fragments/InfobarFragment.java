package com.itrifonov.weatherviewer.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.interfaces.IServiceHelperCallbackListener;
import com.itrifonov.weatherviewer.models.Settings;
import com.itrifonov.weatherviewer.services.ServiceHelper;

import java.text.DateFormat;

import io.realm.Realm;

public class InfobarFragment extends Fragment {

    private TextView mCityName;
    private TextView mLastUpdated;
    private Realm realm;
    private IServiceHelperCallbackListener callback = new IServiceHelperCallbackListener() {
        @Override
        public void onServiceHelperCallback() {
            updateInfo();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        realm = Realm.getDefaultInstance();
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
        updateInfo();
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

    private void updateInfo() {
        if (realm != null) {
            Settings settings = realm.where(Settings.class).findFirst();
            if (settings != null) {
                if (mLastUpdated != null) {
                    mLastUpdated.setText(getString(R.string.txt_last_update,
                            DateFormat.getDateTimeInstance().format(settings.getLastUpdate())));
                }
                // TODO: 13.12.15 get city name from received forecast data
                if (mCityName != null) {
                    mCityName.setText(settings.getCity());
                }
            }
        }
    }
}
