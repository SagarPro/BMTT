package com.sagsaguz.bmtt.adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.sagsaguz.bmtt.R;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.ActivitiesDO;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ActivitiesAdapter extends BaseAdapter {

    private List<String> activityNameList = new ArrayList<>();
    private Context context;
    private ActivitiesDO activitiesDO;
    private List<String> doneList = new ArrayList<>();
    private List<String> attendedList = new ArrayList<>();
    private LayoutInflater inflater=null;

    public ActivitiesAdapter(Context context, ActivitiesDO activitiesDO, List<String> activityNameList) {
        this.context = context;
        this.activitiesDO = activitiesDO;
        this.activityNameList = activityNameList;
        doneList.addAll(activitiesDO.getDone());
        attendedList.addAll(activitiesDO.getAttended());
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return activityNameList.size();
    }

    @Override
    public Object getItem(int position) {
        return activityNameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Holder holder=new Holder();
        @SuppressLint({"ViewHolder", "InflateParams"}) final View rowView = inflater.inflate(R.layout.activities_item, null);
        holder.activityName=rowView.findViewById(R.id.activityName);
        holder.tvDone=rowView.findViewById(R.id.tvDone);
        holder.tvAttended=rowView.findViewById(R.id.tvAttended);

        holder.activityName.setText(activityNameList.get(position));
        if (doneList.contains(activityNameList.get(position))) {
            holder.tvDone.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvDone.setBackgroundColor(context.getResources().getColor(R.color.green));
        }
        if (attendedList.contains(activityNameList.get(position))) {
            holder.tvAttended.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvAttended.setBackgroundColor(context.getResources().getColor(R.color.green));
        }

        holder.tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!attendedList.contains(activityNameList.get(position))) {
                    String status;
                    if (doneList.contains(activityNameList.get(position))) {
                        doneList.remove(activityNameList.get(position));
                        holder.tvDone.setTextColor(context.getResources().getColor(R.color.grey));
                        holder.tvDone.setBackgroundColor(context.getResources().getColor(R.color.shadow));
                        status = "removed";
                    } else {
                        doneList.add(activityNameList.get(position));
                        holder.tvDone.setTextColor(context.getResources().getColor(R.color.white));
                        holder.tvDone.setBackgroundColor(context.getResources().getColor(R.color.green));
                        status = "added";
                    }
                    new UpdateActivityList().execute("done", status, activityNameList.get(position));
                }
            }
        });

        holder.tvAttended.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!doneList.contains(activityNameList.get(position))) {
                    String status;
                    if (attendedList.contains(activityNameList.get(position))) {
                        attendedList.remove(activityNameList.get(position));
                        holder.tvAttended.setTextColor(context.getResources().getColor(R.color.grey));
                        holder.tvAttended.setBackgroundColor(context.getResources().getColor(R.color.shadow));
                        status = "removed";
                    } else {
                        attendedList.add(activityNameList.get(position));
                        holder.tvAttended.setTextColor(context.getResources().getColor(R.color.white));
                        holder.tvAttended.setBackgroundColor(context.getResources().getColor(R.color.green));
                        status = "added";
                    }
                    new UpdateActivityList().execute("attended", status, activityNameList.get(position));
                }
            }
        });

        SharedPreferences userPref = context.getSharedPreferences("USERDETAILS", MODE_PRIVATE);
        switch (userPref.getString("USERTYPE", "SuperAdmin")){
            case "user":
                holder.tvAttended.setOnClickListener(null);
                break;
            case "admin":
                holder.tvDone.setOnClickListener(null);
                break;
            case "SuperAdmin":
                holder.tvDone.setOnClickListener(null);
                holder.tvAttended.setOnClickListener(null);
                break;
        }

        return rowView;
    }

    private class Holder{
        TextView activityName, tvDone, tvAttended;
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateActivityList extends AsyncTask<String, Void, Boolean> {

        ProgressDialog progressDialog;
        private String activityName, type, status;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Updating, please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                type = strings[0];
                status = strings[1];
                activityName = strings[2];
                AWSProvider awsProvider = new AWSProvider();
                AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(context));
                dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
                DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                        .dynamoDBClient(dynamoDBClient)
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .build();

                ActivitiesDO actDo = new ActivitiesDO();
                actDo.setEmailID(activitiesDO.getEmailID());
                actDo.setPhone(activitiesDO.getPhone());
                actDo.setDone(doneList);
                actDo.setAttended(attendedList);

                dynamoDBMapper.save(actDo);

                return true;
            } catch (AmazonClientException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            if (!result){
                Toast.makeText(context, "Failed to update, please try again.", Toast.LENGTH_LONG).show();
                switch (type){
                    case "done":
                        if (status.equals("added"))
                            doneList.remove(activityName);
                        else
                            doneList.add(activityName);
                        break;
                    case "attended":
                        if (status.equals("added"))
                            attendedList.remove(activityName);
                        else
                            attendedList.add(activityName);
                        break;
                }
                notifyDataSetChanged();
            }

        }
    }

}
