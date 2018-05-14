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
import java.util.HashMap;
import java.util.List;

public class CentresListAdapter extends BaseAdapter {

    private List<String> centreList = new ArrayList<>();
    Context context;
    private LayoutInflater inflater=null;

    public CentresListAdapter(Context context, List<String> centreList) {
        this.centreList = centreList;
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return centreList.size();
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

        CentresListAdapter.Holder holder=new CentresListAdapter.Holder();
        @SuppressLint({"ViewHolder", "InflateParams"})
        View rowView = inflater.inflate(R.layout.centres_list, null);
        holder.centreName=(TextView) rowView.findViewById(R.id.centreName);
        holder.centreName.setText(centreList.get(position));

        return rowView;
    }

    private class Holder{
        TextView centreName;
    }
}
