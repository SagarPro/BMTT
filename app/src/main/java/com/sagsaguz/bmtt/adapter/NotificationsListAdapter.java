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

public class NotificationsListAdapter extends BaseAdapter {

    private Map<String, String> messages = new HashMap<>();
    private List<String> dateTime = new ArrayList<>();
    Context context;
    private LayoutInflater inflater=null;

    public NotificationsListAdapter(Context context, List<String> dateTime, Map<String, String> messages) {
        this.messages = messages;
        this.dateTime = dateTime;
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dateTime.size();
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
        View rowView = inflater.inflate(R.layout.notification_list, null);
        holder.message= rowView.findViewById(R.id.message);
        holder.message.setText(messages.get(dateTime.get(position)));
        holder.dateTime= rowView.findViewById(R.id.dateTime);
        holder.dateTime.setText(dateTime.get(position));

        return rowView;
    }

    private class Holder{
        TextView message, dateTime;
    }
}
