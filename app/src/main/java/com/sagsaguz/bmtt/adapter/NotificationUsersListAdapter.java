package com.sagsaguz.bmtt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.sagsaguz.bmtt.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.sagsaguz.bmtt.NotificationActivity.notificationActivity;

public class NotificationUsersListAdapter extends BaseAdapter {

    private List<String> userList = new ArrayList<>();
    private List<String> userEmailList = new ArrayList<>();
    Context context;
    private LayoutInflater inflater=null;
    private boolean[] checkBoxState = null;
    private HashMap<String , Boolean> checkedForItem = new HashMap<>();

    public NotificationUsersListAdapter(Context context, List<String> userList, List<String> userEmailList) {
        this.userList = userList;
        this.userEmailList = userEmailList;
        this.context = context;
        notificationActivity.selectedList.clear();
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return userList.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.notification_users, parent, false);
            holder = new ViewHolder();
            holder.userName = convertView.findViewById(R.id.userName);
            holder.userEmail = convertView.findViewById(R.id.userEmail);
            holder.cbUser = convertView.findViewById(R.id.cbUser);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        checkBoxState = new boolean[userList.size()];
        holder.userName.setText(userList.get(position));
        holder.userEmail.setText(userEmailList.get(position));
        if(checkBoxState != null)
            holder.cbUser.setChecked(checkBoxState[position]);

        holder.cbUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((CheckBox)v).isChecked()) {
                    notificationActivity.selectedList.add(userEmailList.get(position));
                    checkBoxState[position] = true;
                    isChecked(position,true);
                }
                else {
                    notificationActivity.selectedList.remove(userEmailList.get(position));
                    checkBoxState[position] = false;
                    isChecked(position,false);
                }
            }
        });


        if (checkedForItem.get(userEmailList.get(position)) != null) {
            holder.cbUser.setChecked(checkedForItem.get(userEmailList.get(position)));
        }
        holder.cbUser.setTag(userEmailList.get(position));

        return convertView;
    }

    private class ViewHolder{
        TextView userName, userEmail;
        CheckBox cbUser;
    }

    private void isChecked(int position, boolean flag)
    {
        checkedForItem.put(this.userEmailList.get(position), flag);
    }

}
