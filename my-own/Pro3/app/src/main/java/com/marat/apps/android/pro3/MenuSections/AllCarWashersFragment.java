package com.marat.apps.android.pro3.MenuSections;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marat.apps.android.pro3.R;

public class AllCarWashersFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_all_car_washers, container, false);

        return v;
    }
}
