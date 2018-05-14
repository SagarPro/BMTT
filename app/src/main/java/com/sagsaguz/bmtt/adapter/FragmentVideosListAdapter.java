package com.sagsaguz.bmtt.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sagsaguz.bmtt.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentVideosListAdapter extends BaseAdapter {

    private List<String> videosList = new ArrayList<>();
    Context context;
    private LayoutInflater inflater=null;

    public FragmentVideosListAdapter(Context context, List<String> videosList) {
        this.videosList = videosList;
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return videosList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FragmentVideosListAdapter.Holder holder = new FragmentVideosListAdapter.Holder();
        @SuppressLint({"ViewHolder", "InflateParams"})
        View rowView = inflater.inflate(R.layout.fragment_videos_list, null);
        holder.videoName = rowView.findViewById(R.id.centreName);
        holder.videoName.setText(videosList.get(position));

        return rowView;
    }

    private class Holder{
        TextView videoName;
    }
}
