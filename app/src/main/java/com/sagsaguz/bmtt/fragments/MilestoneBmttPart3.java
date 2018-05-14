package com.sagsaguz.bmtt.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.sagsaguz.bmtt.MilestoneActivity;
import com.sagsaguz.bmtt.R;
import com.sagsaguz.bmtt.adapter.ExpandableMilestoneAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MilestoneBmttPart3 extends Fragment {

    private static Map<String, List<String>> videosList = new HashMap<>();
    private static HashMap<String, Integer> videoCount = new HashMap<>();

    public MilestoneBmttPart3(){

    }

    public static MilestoneBmttPart3 newInstance(Map<String, List<String>> list, HashMap<String, Integer> count) {
        Bundle args = new Bundle();
        MilestoneBmttPart3 fragment = new MilestoneBmttPart3();
        fragment.setArguments(args);
        videosList = list;
        videoCount = count;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.expand_fragment_layout, container, false);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText("User is not enrolled in this course.");
        ExpandableListView expandableListView = view.findViewById(R.id.expFragment);

        if(videosList.size() == 0){
            expandableListView.setVisibility(View.GONE);
            tvMessage.setVisibility(View.VISIBLE);
        } else {
            tvMessage.setVisibility(View.GONE);
            expandableListView.setVisibility(View.VISIBLE);
        }

        final List<String> headers = new ArrayList<>();
        Set mkeys = videosList.keySet();
        for (Object key1 : mkeys) {
            String key = (String) key1;
            headers.add(key);
        }
        Collections.sort(headers);

        ExpandableMilestoneAdapter expandableMilestoneAdapter = new ExpandableMilestoneAdapter(getActivity(), headers, videosList, videoCount);
        expandableListView.setAdapter(expandableMilestoneAdapter);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int gp, int cp, long l) {
                if(videosList.size() != 0) {
                    String text = videosList.get(headers.get(gp)).get(cp);
                    String key = text.substring(text.indexOf("_", text.indexOf("_") + 1), text.indexOf("."));
                    String subKey = key.substring(1);
                    subKey = subKey.replace("_", " ");
                    if (((MilestoneActivity) getActivity()) != null) {
                        ((MilestoneActivity) getActivity()).showStatisticsDialog(subKey);
                    } else {
                        assert ((MilestoneActivity) getActivity()) != null;
                        ((MilestoneActivity) getActivity()).showSnackBar("Network connection error!!", "get");
                    }
                } else {
                    assert ((MilestoneActivity) getActivity()) != null;
                    ((MilestoneActivity) getActivity()).showSnackBar("Network connection error!!", "get");
                }
                return true;
            }
        });

        return view;
    }

}
