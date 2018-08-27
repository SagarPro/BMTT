package com.sagsaguz.bmtt.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sagsaguz.bmtt.R;
import com.sagsaguz.bmtt.adapter.ActivitiesAdapter;
import com.sagsaguz.bmtt.utils.ActivitiesDO;

import java.util.ArrayList;
import java.util.List;

public class Arithmetic extends Fragment {

    private static List<String> arithmeticList = new ArrayList<>();
    private static ActivitiesDO activitiesDO;

    public Arithmetic(){
    }

    public static Arithmetic newInstance(ActivitiesDO act, List<String> list) {
        Bundle args = new Bundle();
        Arithmetic fragment = new Arithmetic();
        fragment.setArguments(args);
        activitiesDO = act;
        arithmeticList = list;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activities_layout, container, false);

        ListView lvActivities = view.findViewById(R.id.lvActivities);

        ActivitiesAdapter activitiesAdapter = new ActivitiesAdapter(getContext(), activitiesDO, arithmeticList);
        lvActivities.setAdapter(activitiesAdapter);

        return view;
    }

}
