package com.itrifonov.weatherviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ForecastDetailFragment extends Fragment {

    public static String ARG_INDEX = "ARG_INDEX";
    private static int mIndex = -1;
    private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_INDEX)) {
            mIndex = getArguments().getInt(ARG_INDEX, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast_detail, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textView = (TextView) view.findViewById(R.id.textView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        update(mIndex);
    }

    public void update (int index) {
        mIndex = index;
        // TODO: 18.11.2015 add logic
        if (textView != null) {
            textView.setText("Forecast Detail Index: " + Integer.toString(mIndex));
        }
    }
}
