package com.itrifonov.weatherviewer.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itrifonov.weatherviewer.R;
import com.itrifonov.weatherviewer.interfaces.IServiceHelperCallbackListener;
import com.itrifonov.weatherviewer.services.ServiceHelper;

import java.text.DateFormat;

public class InfobarFragment extends Fragment {

    private TextView mCityName;
    private TextView mLastUpdated;
    private IServiceHelperCallbackListener callback = new IServiceHelperCallbackListener() {
        @Override
        public void onServiceHelperCallback(Bundle event) {
            int eventId = event.getInt(ServiceHelper.SERVICE_HELPER_EVENT, -1);
            if (eventId != ServiceHelper.EVENT_UPDATE_STOPPED)
                return;
            updateInfo();
        }
    };


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
    }

    private void updateInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        mLastUpdated.setText(getString(R.string.txt_last_update,
                DateFormat.getDateTimeInstance().format(preferences.getLong(getString(R.string.last_update_key), -1))));
        mCityName.setText(preferences.getString(getString(R.string.current_city_key), getString(R.string.settings_city_default)));
    }
}
