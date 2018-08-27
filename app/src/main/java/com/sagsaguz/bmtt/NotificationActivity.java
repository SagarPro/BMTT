package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SetEndpointAttributesRequest;
import com.google.gson.Gson;
import com.sagsaguz.bmtt.adapter.NotificationUsersListAdapter;
import com.sagsaguz.bmtt.adapter.NotificationsListAdapter;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.NotificationsDO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    private ListView lvNotification;
    private FloatingActionButton fb_sendNotification;
    private RelativeLayout rlNotification;
    private TextView tvMessage;

    private ProgressBar pbNotification;
    private ProgressBar pbDNotification;

    private Map<String, String> notificationsM = new HashMap<>();
    private List<String> notificationsDT = new ArrayList<>();
    private List<NotificationsDO> n = new ArrayList<>();
    private List<String> usersName = new ArrayList<>();
    private List<String> usersEmail = new ArrayList<>();
    private Map<String, String> notificationARN = new HashMap<>();
    public List<String> selectedList = new ArrayList<>();

    private String centre, email, userType;

    private NotificationsListAdapter adapter;
    private Dialog dialog;

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;

    @SuppressLint("StaticFieldLeak")
    public static NotificationActivity notificationActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_layout);

        notificationActivity = NotificationActivity.this;

        Intent intent = getIntent();
        centre = intent.getStringExtra("CENTRE");
        email = intent.getStringExtra("EMAIL");
        userType = intent.getStringExtra("USERTYPE");

        rlNotification = findViewById(R.id.rlNotification);
        lvNotification = findViewById(R.id.lvNotification);
        fb_sendNotification = findViewById(R.id.fb_sendNotification);
        fb_sendNotification.setVisibility(View.GONE);
        tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setVisibility(View.GONE);

        pbNotification = findViewById(R.id.pbNotification);
        pbNotification.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.red), android.graphics.PorterDuff.Mode.MULTIPLY);
        pbNotification.setVisibility(View.GONE);

        adapter = new NotificationsListAdapter(notificationActivity, notificationsDT, notificationsM);
        lvNotification.setAdapter(adapter);

        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        new ShowNotifications(dynamoDBMapper).execute(centre);

        fb_sendNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotificationDialog();
            }
        });

        lvNotification.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (n.get(i).getMessage().contains("idiot")|| n.get(i).getMessage().contains("sangeetha") || n.get(i).getMessage().contains("Sangeetha")){
                    Toast.makeText(NotificationActivity.this, "You cannot delete this...", Toast.LENGTH_LONG).show();
                } else {
                    deleteNotificationMsg(n.get(i));
                }

                return true;
            }
        });

    }

    private void deleteNotificationMsg(final NotificationsDO object){
        final Dialog dialog = new Dialog(NotificationActivity.this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        tvTitle.setText("Deletion");
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        tvMessage.setText("Are you sure, you want to delete");
        Button btnDelete = dialog.findViewById(R.id.btnRemove);
        btnDelete.setText("Delete");
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new DeleteNotification().execute(object);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showNotifications(List<NotificationsDO> notificationsDOList){
        Collections.sort(n, new DateComparator());
        Collections.reverse(n);
        notificationsM.clear();
        notificationsDT.clear();
        if(notificationsDOList.size() == 0){
            basicSnackBar("You don't have any notifications yet.");
        } else {
            for (NotificationsDO some : notificationsDOList) {
                if(userType.equals("user")) {
                    fb_sendNotification.setVisibility(View.GONE);
                    lvNotification.setOnItemLongClickListener(null);
                    if(some.getWho().contains(email)){
                        String dateTime = "by "+some.getCentre() + ", "+some.getWhen();
                        notificationsDT.add(dateTime);
                        notificationsM.put(dateTime, some.getMessage());
                    }
                } else if (userType.equals("admin")){
                    fb_sendNotification.setVisibility(View.VISIBLE);
                    if (some.getCentre().equals(centre)) {
                        String dateTime = "by "+some.getCentre() + ", "+some.getWhen();
                        notificationsDT.add(dateTime);
                        notificationsM.put(dateTime, some.getMessage());
                    }
                } else {
                    fb_sendNotification.setVisibility(View.VISIBLE);
                    String dateTime = "by "+some.getCentre() + ", "+some.getWhen();
                    notificationsDT.add(dateTime);
                    notificationsM.put(dateTime, some.getMessage());
                }
            }
            /*Collections.sort(notificationsDT, new StringDateComparator());
            Collections.reverse(notificationsDT);*/
            adapter.notifyDataSetChanged();
            lvNotification.smoothScrollToPosition(0);
        }
        if (notificationsM.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);
        } else {
            tvMessage.setVisibility(View.GONE);
        }
    }

    private void sendNotificationDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_notification_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final EditText etMessage = dialog.findViewById(R.id.etMessage);
        final TextView tvTo = dialog.findViewById(R.id.tvTo);
        pbDNotification = dialog.findViewById(R.id.pbDNotification);
        pbDNotification.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
        pbDNotification.setVisibility(View.GONE);
        Button btnSend = dialog.findViewById(R.id.btnNSend);
        Button btnCancel = dialog.findViewById(R.id.btnNCancel);

        tvTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog d = new Dialog(NotificationActivity.this);
                d.setContentView(R.layout.notification_to_users_dialog);
                d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                final ListView lvNUsers = d.findViewById(R.id.lvNUsers);
                NotificationUsersListAdapter adapter = new NotificationUsersListAdapter(NotificationActivity.this, usersName, usersEmail);
                lvNUsers.setAdapter(adapter);

                Button btnNClose = d.findViewById(R.id.btnNClose);
                Button btnNSelectAll = d.findViewById(R.id.btnNSelectAll);
                Button btnNOk = d.findViewById(R.id.btnNOk);

                btnNClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        d.dismiss();
                    }
                });

                btnNSelectAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedList.clear();
                        selectedList.addAll(usersEmail);
                        tvTo.setText("");
                        for (int i=0; i<selectedList.size(); i++){
                            if(i+1 == selectedList.size()){
                                tvTo.append(selectedList.get(i));
                            } else {
                                tvTo.append(selectedList.get(i) + "\n");
                            }
                        }
                        d.dismiss();
                    }
                });

                btnNOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tvTo.setText("");
                        for (int i=0; i<selectedList.size(); i++){
                            if(i+1 == selectedList.size()){
                                tvTo.append(selectedList.get(i));
                            } else {
                                tvTo.append(selectedList.get(i) + "\n");
                            }
                        }
                        d.dismiss();
                    }
                });
                d.show();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(etMessage.getText().toString())) {
                    if (!TextUtils.isEmpty(tvTo.getText().toString()) && !tvTo.getText().toString().equals("Click here")) {
                        //new SendNotifications().execute(etMessage.getText().toString());
                        new StoreNotifications(dynamoDBMapper).execute(centre, etMessage.getText().toString());
                    } else {
                        basicSnackBar("Select user");
                    }
                } else {
                    basicSnackBar("Enter your message.");
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void basicSnackBar(String message){
        Snackbar snackbar = Snackbar.make(rlNotification, message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(notificationActivity, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(notificationActivity, R.color.colorAccent));
        snackbar.show();
    }

    private void showSnackBar(String message, final String type){
        Snackbar snackbar = Snackbar.make(rlNotification, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(type.equals("show")){
                            new ShowNotifications(dynamoDBMapper).execute(centre);
                        }
                    }
                });
        snackbar.setActionTextColor(ContextCompat.getColor(notificationActivity, R.color.colorAccent));

        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(notificationActivity, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(notificationActivity, R.color.colorAccent));
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.show();
    }


    private static class ShowNotifications extends AsyncTask<String, Void, Boolean> {

        NotificationsDO notificationsDO = new NotificationsDO();
        NotificationsDO notificationsDO2 = new NotificationsDO();
        DynamoDBQueryExpression<NotificationsDO> queryExpression;
        PaginatedQueryList result;
        DynamoDBMapper dynamoDBMapper;

        ShowNotifications(DynamoDBMapper dynamoDBMapper){
            this.dynamoDBMapper = dynamoDBMapper;
        }

        @Override
        protected void onPreExecute() {
            if(notificationActivity.pbNotification != null)
                notificationActivity.pbNotification.setVisibility(View.VISIBLE);
            notificationActivity.fb_sendNotification.setVisibility(View.GONE);
            notificationActivity.n.clear();
            notificationActivity.usersName.clear();
            notificationActivity.usersEmail.clear();
            notificationActivity.notificationARN.clear();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                /*if(!notificationActivity.userType.equals("SuperAdmin")) {
                    notificationsDO.setCentre(strings[0]);
                    queryExpression = new DynamoDBQueryExpression<NotificationsDO>()
                            .withHashKeyValues(notificationsDO)
                            .withConsistentRead(false);
                    result = dynamoDBMapper.query(NotificationsDO.class, queryExpression);

                    Gson gson = new Gson();
                    for (int i = 0; i < result.size(); i++) {
                        String jsonFormOfItem = gson.toJson(result.get(i));
                        notificationsDO = gson.fromJson(jsonFormOfItem, NotificationsDO.class);
                        notificationActivity.n.add(notificationsDO);
                    }
                }*/
                /*notificationsDO2.setCentre("All users");
                queryExpression = new DynamoDBQueryExpression<NotificationsDO>()
                        .withHashKeyValues(notificationsDO2)
                        .withConsistentRead(false);
                result = dynamoDBMapper.query(NotificationsDO.class, queryExpression);

                Gson gson = new Gson();
                for (int i = 0; i < result.size(); i++) {
                    String jsonFormOfItem = gson.toJson(result.get(i));
                    notificationsDO2 = gson.fromJson(jsonFormOfItem,NotificationsDO.class);
                    notificationActivity.n.add(notificationsDO2);
                }*/

                ScanRequest request1 = new ScanRequest().withTableName(Config.NOTIFICATIONTABLENAME);
                ScanResult response1 = notificationActivity.dynamoDBClient.scan(request1);
                List<Map<String, AttributeValue>> uRows = response1.getItems();
                for (Map<String, AttributeValue> map : uRows) {

                    NotificationsDO nn = new NotificationsDO();
                    nn.setCentre(map.get("centre").getS());
                    nn.setWhen(map.get("when").getS());
                    nn.setMessage(map.get("message").getS());
                    List<AttributeValue> users = new ArrayList<>();
                    users.addAll(map.get("who").getL());
                    List<String> usersList = new ArrayList<>();
                    for (int i =0; i<users.size(); i++){
                        usersList.add(users.get(i).getS());
                    }
                    nn.setWho(usersList);

                    notificationActivity.n.add(nn);

                    /*try {
                        notificationActivity.usersName.add(map.get("firstName").getS() + " " + map.get("lastName").getS());
                        notificationActivity.usersEmail.add(map.get("emailId").getS());
                        notificationActivity.notificationARN.put(map.get("emailId").getS(),map.get("notificationARN").getS());
                    } catch (NumberFormatException e) {
                        Log.d("number_format_exception", e.getMessage());
                    }*/
                }

                if (notificationActivity.userType.equals("SuperAdmin")){
                    ScanRequest request = new ScanRequest().withTableName(Config.USERSTABLENAME);
                    ScanResult response = notificationActivity.dynamoDBClient.scan(request);
                    List<Map<String, AttributeValue>> userRows = response.getItems();
                    for (Map<String, AttributeValue> map : userRows) {
                        try {
                            notificationActivity.usersName.add(map.get("firstName").getS() + " " + map.get("lastName").getS());
                            notificationActivity.usersEmail.add(map.get("emailId").getS());
                            notificationActivity.notificationARN.put(map.get("emailId").getS(),map.get("notificationARN").getS());
                        } catch (NumberFormatException e) {
                            Log.d("number_format_exception", e.getMessage());
                        }
                    }
                    return true;
                }

                ScanRequest request = new ScanRequest().withTableName(Config.USERSTABLENAME);
                ScanResult response = notificationActivity.dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> userRows = response.getItems();
                for (Map<String, AttributeValue> map : userRows) {
                    try {
                        if (map.get("centre").getS().equals(strings[0])) {
                            notificationActivity.usersName.add(map.get("firstName").getS() + " " + map.get("lastName").getS());
                            notificationActivity.usersEmail.add(map.get("emailId").getS());
                            notificationActivity.notificationARN.put(map.get("emailId").getS(),map.get("notificationARN").getS());
                        }
                    } catch (NumberFormatException e) {
                        Log.d("number_format_exception", e.getMessage());
                    }
                }
                return true;
            } catch (AmazonClientException e){
                notificationActivity.showSnackBar("Network connection error!!!", "show");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(notificationActivity.pbNotification != null)
                notificationActivity.pbNotification.setVisibility(View.GONE);
            if (result) {
                notificationActivity.showNotifications(notificationActivity.n);
            }
        }
    }


    private static class StoreNotifications extends AsyncTask<String, Void, Boolean> {

        DynamoDBMapper dynamoDBMapper;
        String dateTime;
        String notificationMessage;

        StoreNotifications(DynamoDBMapper dynamoDBMapper){
            this.dynamoDBMapper = dynamoDBMapper;
        }

        @Override
        protected void onPreExecute() {
            if (notificationActivity.pbDNotification != null)
                notificationActivity.pbDNotification.setVisibility(View.VISIBLE);
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a");
            dateTime = sdf.format(c.getTime());
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                notificationMessage = strings[0];
                if(dateTime != null) {
                    NotificationsDO notificationsDO = new NotificationsDO();
                    notificationsDO.setCentre(notificationMessage);
                    notificationsDO.setWhen(dateTime);
                    notificationsDO.setMessage(strings[1]);
                    notificationsDO.setWho(notificationActivity.selectedList);
                    dynamoDBMapper.save(notificationsDO);
                    return true;
                }
                return false;
            } catch (AmazonClientException e){
                notificationActivity.showSnackBar("Network connection error!!!", "send");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                new SendNotifications().execute(notificationMessage);
            } else {
                if (notificationActivity.pbDNotification != null)
                    notificationActivity.pbDNotification.setVisibility(View.GONE);
                if(notificationActivity.dialog != null && notificationActivity.dialog.isShowing()) {
                    notificationActivity.dialog.dismiss();
                    notificationActivity.selectedList.clear();
                }
            }
        }
    }


    private static class SendNotifications extends AsyncTask<String, Void, Boolean>{

        String notificationMessage;

        @Override
        protected void onPreExecute() {
            if (notificationActivity.pbDNotification != null)
                notificationActivity.pbDNotification.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            notificationMessage = strings[0];
            try {
                AWSCredentials awsCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
                AmazonSNSClient snsClient = new AmazonSNSClient(awsCredentials);

                for (int i=0; i<notificationActivity.selectedList.size(); i++) {
                    String notificationARN = notificationActivity.notificationARN.get(notificationActivity.selectedList.get(i));
                    if (!notificationARN.equals("null")) {
                        PublishRequest publishRequest = new PublishRequest();
                        publishRequest.setMessage(0 + notificationARN +"$" + strings[0]);
                        publishRequest.setSubject("BMTT Notification");
                        publishRequest.withTargetArn(notificationARN);
                        snsClient.publish(publishRequest);
                    }
                }
                return true;
            } catch (AmazonClientException e){
                notificationActivity.basicSnackBar("Couldn't send notifications to some students.");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(notificationActivity.pbNotification != null)
                notificationActivity.pbNotification.setVisibility(View.GONE);
            if (notificationActivity.pbDNotification != null)
                notificationActivity.pbDNotification.setVisibility(View.GONE);
            if(notificationActivity.dialog != null && notificationActivity.dialog.isShowing()) {
                notificationActivity.dialog.dismiss();
                notificationActivity.selectedList.clear();
                new ShowNotifications(notificationActivity.dynamoDBMapper).execute(notificationActivity.centre);
            }
        }
    }

    private class DeleteNotification extends AsyncTask<NotificationsDO, Void, Boolean>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(NotificationActivity.this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Deleting, please wait.");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(NotificationsDO... notificationsDOS) {

            try {
                //NotificationsDO notificationsDO = new NotificationsDO();
                dynamoDBMapper.delete(notificationsDOS[0]);
                return true;
            } catch (AmazonClientException e){
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            if (aBoolean){
                new ShowNotifications(dynamoDBMapper).execute(centre);
                Toast.makeText(NotificationActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NotificationActivity.this, "Failed to delete, try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class StringDateComparator implements Comparator<String>
    {
        int dt = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a");
        public int compare(String lhs, String rhs)
        {
            try {
                dt = dateFormat.parse(lhs).compareTo(dateFormat.parse(rhs));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return dt;
        }
    }

    class DateComparator implements Comparator<NotificationsDO>
    {
        int dt = 0;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a");
        @Override
        public int compare(NotificationsDO notificationsDO1, NotificationsDO notificationsDO2) {
            try {
                dt = dateFormat.parse(notificationsDO1.getWhen()).compareTo(dateFormat.parse(notificationsDO2.getWhen()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return dt;
        }
    }

}
