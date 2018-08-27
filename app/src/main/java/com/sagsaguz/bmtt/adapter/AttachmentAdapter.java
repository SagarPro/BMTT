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

public class AttachmentAdapter extends BaseAdapter {

    private Context context;
    private List<String> attachmentList = new ArrayList<>();
    private LayoutInflater inflater=null;

    public AttachmentAdapter(Context context, List<String> attachmentList){
        this.context = context;
        this.attachmentList = attachmentList;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return attachmentList.size();
    }

    @Override
    public Object getItem(int i) {
        return attachmentList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final Holder holder=new Holder();
        @SuppressLint({"ViewHolder", "InflateParams"})
        View rowView = inflater.inflate(R.layout.attachment_item, null);

        holder.tvFileName = rowView.findViewById(R.id.tvFileName);
        String fileName = attachmentList.get(i);
        //fileName = fileName.replace("_", " ");
        holder.tvFileName.setText(fileName);

        return rowView;
    }

    public class Holder{
        TextView tvFileName;
    }
}
