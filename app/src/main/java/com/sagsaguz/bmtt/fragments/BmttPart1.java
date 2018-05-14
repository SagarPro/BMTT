package com.sagsaguz.bmtt.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sagsaguz.bmtt.HomePageActivity;
import com.sagsaguz.bmtt.R;
import com.sagsaguz.bmtt.adapter.ExpandableVideosListAdapter;
import com.sagsaguz.bmtt.adapter.FragmentVideosListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BmttPart1 extends Fragment {

    private static Map<String, List<String>> videosList = new HashMap<>();

    public BmttPart1(){

    }

    public static BmttPart1 newInstance(Map<String, List<String>> list) {
        Bundle args = new Bundle();
        BmttPart1 fragment = new BmttPart1();
        fragment.setArguments(args);
        videosList = list;
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

        ExpandableVideosListAdapter expandableVideosListAdapter = new ExpandableVideosListAdapter(getActivity(), headers, videosList);
        expandableListView.setAdapter(expandableVideosListAdapter);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int gp, int cp, long l) {
                if(videosList.size() != 0) {
                    if (((HomePageActivity) getActivity()) != null) {
                        ((HomePageActivity) getActivity()).updateCounter(videosList.get(headers.get(gp)).get(cp));
                    } else {
                        assert ((HomePageActivity) getActivity()) != null;
                        ((HomePageActivity) getActivity()).showSnackBar("Network connection error!!");
                    }
                } else {
                    assert ((HomePageActivity) getActivity()) != null;
                    ((HomePageActivity) getActivity()).showSnackBar("Network connection error!!");
                }
                return true;
            }
        });

        return view;
    }

}
