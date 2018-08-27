package com.sagsaguz.bmtt.adapter;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sagsaguz.bmtt.R;
import com.sagsaguz.bmtt.UserDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.sagsaguz.bmtt.MainBranchActivity.mainBranchActivity;

public class UsersListAdapter extends BaseAdapter {

    private List<String> usersList = new ArrayList<>();
    HashMap<String, String> userCentre = new HashMap<String, String>();
    HashMap<String, String> userPic = new HashMap<String, String>();
    HashMap<String, String> userId = new HashMap<String, String>();
    Context context;
    private LayoutInflater inflater=null;

    public UsersListAdapter(Context context, List<String> usersList, HashMap<String, String> userCentre, HashMap<String, String> userPic, HashMap<String, String> userId) {
        this.usersList = usersList;
        this.userCentre = userCentre;
        this.userPic = userPic;
        this.userId = userId;
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public Object getItem(int position) {
        return usersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Holder holder=new Holder();
        @SuppressLint({"ViewHolder", "InflateParams"})
        View rowView = inflater.inflate(R.layout.users_list, null);
        holder.userName= rowView.findViewById(R.id.userName);
        String name_email = usersList.get(position);
        String name = name_email.substring(0, name_email.indexOf("_"));
        holder.userName.setText(name);
        /*if (name_email.contains("_")) {

        } else {
            holder.userName.setText(name_email);
        }*/
        holder.userCentre= rowView.findViewById(R.id.userCentre);
        holder.userCentre.setText(userCentre.get(usersList.get(position)));
        holder.userId = rowView.findViewById(R.id.userId);
        holder.userId.setText(userId.get(usersList.get(position)));
        holder.userImage = rowView.findViewById(R.id.ivUserPic);
        String pic = userPic.get(usersList.get(position));
        Glide.with(context).load(pic).into(holder.userImage);
        //Picasso.with(context).load(pic).into(holder.userImage);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.userImage.buildDrawingCache ();

                Bundle extras = new Bundle ();
                extras.putParcelable ("IMAGE", holder.userImage.getDrawingCache ());

                Intent sharedIntent = new Intent(context, UserDetailsActivity.class);
                sharedIntent.putExtra("EMAIL", mainBranchActivity.getUserEmail(position));
                sharedIntent.putExtra("PHONE", mainBranchActivity.getUserPhone(position));
                sharedIntent.putExtras (extras);
                Pair pair = new Pair<View, String>(holder.userImage, "profilePic");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(mainBranchActivity, pair);
                context.startActivity(sharedIntent, options.toBundle());
            }
        });

        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mainBranchActivity.deleteUser(position);
                return true;
            }
        });

        return rowView;
    }

    public class Holder{
        TextView userName, userCentre, userId;
        ImageView userImage;
    }
}
