package com.sagsaguz.bmtt.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sagsaguz.bmtt.R;
import com.sagsaguz.bmtt.utils.FileSubmissionDO;

import java.util.ArrayList;
import java.util.List;

public class FileSub_StuAdapter extends BaseAdapter {

    private Context context;
    private List<String> fStatus = new ArrayList<>();
    private List<String> fNames = new ArrayList<>();

    private LayoutInflater inflater=null;

    public FileSub_StuAdapter(Context context, List<String> fNames, List<String> fStatus){
        this.context = context;
        this.fStatus = fStatus;
        this.fNames = fNames;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return fNames.size();
    }

    @Override
    public Object getItem(int i) {
        return fNames.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final Holder holder=new Holder();
        @SuppressLint({"ViewHolder", "InflateParams"})
        View rowView = inflater.inflate(R.layout.file_sub_student_item, null);

        holder.fName = rowView.findViewById(R.id.fName);
        holder.fName.setText(fNames.get(i));

        holder.cbSubmitted = rowView.findViewById(R.id.cbSubmitted);
        holder.cbReceived = rowView.findViewById(R.id.cbReceived);
        holder.cbEvaluated = rowView.findViewById(R.id.cbEvaluated);

        String status = fStatus.get(i);

        switch (status){
            case "submitted":
                holder.cbSubmitted.setChecked(true);
                holder.cbReceived.setChecked(false);
                holder.cbEvaluated.setChecked(false);
                break;
            case "received":
                holder.cbSubmitted.setChecked(true);
                holder.cbReceived.setChecked(true);
                holder.cbEvaluated.setChecked(false);
                break;
            case "evaluated":
                holder.cbSubmitted.setChecked(true);
                holder.cbReceived.setChecked(true);
                holder.cbEvaluated.setChecked(true);
                break;
            case "not":
                holder.cbSubmitted.setChecked(false);
                holder.cbReceived.setChecked(false);
                holder.cbEvaluated.setChecked(false);
                break;
        }

        return rowView;
    }

    public class Holder{
        TextView fName;
        CheckBox cbSubmitted, cbReceived, cbEvaluated;
    }
}