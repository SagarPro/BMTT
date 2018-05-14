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
import java.util.Map;

public class PracticalsListAdapter extends BaseAdapter {

    private Map<String, String> practicalsList = new HashMap<>();
    private List<String> p_name = new ArrayList<>();
    Context context;
    private LayoutInflater inflater=null;

    public PracticalsListAdapter(Context context, List<String> p_name, Map<String, String> practicalsList) {
        this.practicalsList = practicalsList;
        this.p_name = p_name;
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return p_name.size();
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

        Holder holder=new Holder();
        @SuppressLint({"ViewHolder", "InflateParams"})
        View rowView = inflater.inflate(R.layout.practical_list, null);
        holder.practicalName=(TextView) rowView.findViewById(R.id.practicalName);
        holder.practicalName.setText(p_name.get(position));
        holder.practicalDT=(TextView) rowView.findViewById(R.id.practicalDT);
        holder.practicalDT.setText(practicalsList.get(p_name.get(position)));
        int color;
        if(position%3 == 0){
            color = context.getResources().getColor(R.color.orange);
        } else if(position%3 == 1){
            color = context.getResources().getColor(R.color.colorPrimary);
        } else {
            color = context.getResources().getColor(R.color.green);
        }
        holder.pName = rowView.findViewById(R.id.pName);
        holder.pName.setTextColor(color);
        holder.view = rowView.findViewById(R.id.view);
        holder.view.setBackgroundColor(color);

        return rowView;
    }

    private class Holder{
        TextView practicalName, practicalDT, pName;
        View view;
    }
}