package com.sagsaguz.bmtt.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sagsaguz.bmtt.R;
import com.sagsaguz.bmtt.utils.EnquiryDO;

import java.util.ArrayList;
import java.util.List;

public class EnquiryAdapter extends BaseAdapter {

    private List<EnquiryDO> enquiryList = new ArrayList<>();
    Context context;
    private LayoutInflater inflater=null;

    public EnquiryAdapter(Context context, List<EnquiryDO> enquiryList) {
        this.enquiryList = enquiryList;
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return enquiryList.size();
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
        View rowView = inflater.inflate(R.layout.enquiry_list, null);
        holder.name = rowView.findViewById(R.id.name);
        holder.name.setText(enquiryList.get(position).getEnqName());
        holder.phone = rowView.findViewById(R.id.phone);
        holder.phone.setText(enquiryList.get(position).getEnqPhone());
        holder.status = rowView.findViewById(R.id.tvStatus);
        holder.status.setText(enquiryList.get(position).getEnqStatus());
        holder.followUp = rowView.findViewById(R.id.tvFollowUp);
        holder.followUp.setText(enquiryList.get(position).getEnqEmail());
        holder.assignedTo = rowView.findViewById(R.id.tvAssignedTo);
        holder.assignedTo.setText(enquiryList.get(position).getEnqAssignTo());

        return rowView;

    }

    private class Holder{
        TextView name, phone, status, followUp, assignedTo;
    }
}
