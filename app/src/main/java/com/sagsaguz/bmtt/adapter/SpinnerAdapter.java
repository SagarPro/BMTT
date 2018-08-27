package com.sagsaguz.bmtt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sagsaguz.bmtt.R;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {

    private List<String> objects;
    private Context context;

    public SpinnerAdapter(Context context, int resourceId, List<String> objects) {
        super(context, resourceId, objects);
        this.objects = objects;
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=(LayoutInflater) context.getSystemService(  Context.LAYOUT_INFLATER_SERVICE );
        assert inflater != null;
        View row=inflater.inflate(R.layout.centre_spinner_item, parent, false);
        TextView label=row.findViewById(R.id.tvCentreSpinner);
        if (objects.size()!=0)
            label.setText(objects.get(position));

        /*if (position == 0) {//Special style for dropdown header
            label.setTextColor(context.getResources().getColor(R.color.text_hint_color));
        }*/

        return row;
    }

}
