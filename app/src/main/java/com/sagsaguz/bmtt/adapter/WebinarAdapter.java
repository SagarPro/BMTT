package com.sagsaguz.bmtt.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sagsaguz.bmtt.R;
import com.sagsaguz.bmtt.utils.WebinarDO;

import java.util.List;

public class WebinarAdapter extends BaseAdapter {

    private Context context;
    private List<WebinarDO> webinarList;
    private LayoutInflater inflater=null;

    public WebinarAdapter(Context context, List<WebinarDO> webinarList){
        this.context = context;
        this.webinarList = webinarList;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return webinarList.size();
    }

    @Override
    public Object getItem(int i) {
        return webinarList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final Holder holder=new Holder();
        @SuppressLint({"ViewHolder", "InflateParams"})
        View rowView = inflater.inflate(R.layout.webinar_list_item, null);

        holder.tvMessage = rowView.findViewById(R.id.tvMessage);
        holder.tvWhoWhen = rowView.findViewById(R.id.tvWhoWhen);

        if (webinarList.get(i).getType().equals("link"))
            holder.tvMessage.setText("Webinar");
        else
            holder.tvMessage.setText(webinarList.get(i).getMessage());
        String who_when = "by " + webinarList.get(i).getName() + ", on " + webinarList.get(i).getWhen();
        holder.tvWhoWhen.setText(who_when);

        return rowView;
    }

    public class Holder{
        TextView tvMessage, tvWhoWhen;
    }
}
