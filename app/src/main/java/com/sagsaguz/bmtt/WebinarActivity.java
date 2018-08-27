package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.sagsaguz.bmtt.adapter.WebinarAdapter;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.BmttAdminsDO;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.WebinarDO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WebinarActivity extends AppCompatActivity {

    private ProgressBar pbWebinar;
    private TextView tvWebinar;
    private ListView lvWebinar;
    private FloatingActionButton fb_webinar;

    private MenuItem webinar_link, webinar_question;

    private List<WebinarDO> webinarList = new ArrayList<>();
    private List<WebinarDO> webinarMessageList = new ArrayList<>();
    private List<WebinarDO> webinarLinkList = new ArrayList<>();
    private String email, name, userType = "user";

    private WebinarAdapter webinarAdapter;

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper dynamoDBMapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webinar_layout);

        pbWebinar = findViewById(R.id.pbWebinar);
        pbWebinar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.green), android.graphics.PorterDuff.Mode.MULTIPLY);
        lvWebinar = findViewById(R.id.lvWebinar);
        tvWebinar = findViewById(R.id.tvWebinar);
        tvWebinar.setText("Webinar Questions");

        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        fb_webinar = findViewById(R.id.fb_webinar);
        fb_webinar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postDialog();
            }
        });

        Intent intent = getIntent();
        userType = intent.getStringExtra("USERTYPE");
        if(userType.equals("user")){
            email = intent.getStringExtra("EMAIL");
            name = intent.getStringExtra("NAME");
        }
        new WebinarMessages().execute();

        webinarAdapter = new WebinarAdapter(this, webinarList);
        lvWebinar.setAdapter(webinarAdapter);
        lvWebinar.smoothScrollToPosition(0);

        lvWebinar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!webinar_link.isVisible()) {
                    Uri uri = Uri.parse(webinarLinkList.get(i).getMessage());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        lvWebinar.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (webinar_link.isVisible()) {
                    if (webinarMessageList.get(i).getMessage().equals("idiot") || webinarMessageList.get(i).getMessage().equals("sangeetha") || webinarMessageList.get(i).getMessage().equals("Sangeetha")){
                        Toast.makeText(WebinarActivity.this, "You cannot delete this...", Toast.LENGTH_LONG).show();
                    } else {
                        deleteWebinarMessage(webinarMessageList.get(i));
                    }
                } else {
                    if (webinarLinkList.get(i).getMessage().equals("idiot") || webinarLinkList.get(i).getMessage().equals("sangeetha") || webinarLinkList.get(i).getMessage().equals("Sangeetha")){
                        Toast.makeText(WebinarActivity.this, "You cannot delete this...", Toast.LENGTH_LONG).show();
                    } else {
                        deleteWebinarMessage(webinarLinkList.get(i));
                    }
                }

                return true;
            }
        });

        if (userType.equals("user"))
            lvWebinar.setOnItemLongClickListener(null);

    }

    private void deleteWebinarMessage(final WebinarDO object){
        final Dialog dialog = new Dialog(WebinarActivity.this);
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
                new DeleteWebinar().execute(object);
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

    private void postDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.post_question_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        final EditText etQuestion = dialog.findViewById(R.id.etQuestion);
        Button btnPost = dialog.findViewById(R.id.btnPost);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        Date c = Calendar.getInstance().getTime();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        final String currentDate = df.format(c);

        final WebinarDO webinarDO = new WebinarDO();
        webinarDO.setWhen(currentDate);

        if (userType.equals("user")) {
            etQuestion.setHint("Enter your doubt question here...");
            tvTitle.setText("Ask your doubt");
            webinarDO.setName(name);
            webinarDO.setType("question");
        } else{
            etQuestion.setHint("Enter your webinar link here");
            tvTitle.setText("Post your link");
            webinarDO.setName("Head Office");
            webinarDO.setType("link");
        }

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(etQuestion.getText().toString())){
                    Toast.makeText(WebinarActivity.this, "Please enter your message.", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    webinarDO.setMessage(etQuestion.getText().toString());
                    new PostMessage().execute(webinarDO);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webinar_menu, menu);
        webinar_link = menu.findItem(R.id.webinar_link);
        webinar_question = menu.findItem(R.id.webinar_question);
        webinar_question.setVisible(false);
        if (userType.equals("user"))
            fb_webinar.setVisibility(View.VISIBLE);
        else fb_webinar.setVisibility(View.GONE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.webinar_question) {
            webinar_link.setVisible(true);
            webinar_question.setVisible(false);
            if (userType.equals("user"))
                fb_webinar.setVisibility(View.VISIBLE);
            else fb_webinar.setVisibility(View.GONE);
            webinarList.clear();
            webinarList.addAll(webinarMessageList);
            webinarAdapter.notifyDataSetChanged();
            lvWebinar.smoothScrollToPosition(0);
            tvWebinar.setText("Webinar Questions");
            return true;
        }

        if (id == R.id.webinar_link){
            webinar_link.setVisible(false);
            webinar_question.setVisible(true);
            if (userType.equals("user"))
                fb_webinar.setVisibility(View.GONE);
            else fb_webinar.setVisibility(View.VISIBLE);
            webinarList.clear();
            webinarList.addAll(webinarLinkList);
            webinarAdapter.notifyDataSetChanged();
            lvWebinar.smoothScrollToPosition(0);
            tvWebinar.setText("Previous Webinar Links\nClick to view");
            return true;
        }

        if (id == R.id.goto_webinar){
            PackageManager pm = getBaseContext().getPackageManager();
            Intent appStartIntent = pm.getLaunchIntentForPackage("com.logmein.gotowebinar");
            if (null != appStartIntent) {
                getBaseContext().startActivity(appStartIntent);
            } else {
                Toast.makeText(getBaseContext(), "Please Install GoToWebinar App", Toast.LENGTH_SHORT).show();
                Intent goToMarket = new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://play.google.com/store/apps/details?id=com.logmein.gotowebinar"));
                startActivity(goToMarket);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private class WebinarMessages extends AsyncTask<Void, Void, Boolean> {

        private WebinarDO webinarDO;

        @Override
        protected void onPreExecute() {
            webinarMessageList.clear();
            webinarLinkList.clear();
            if (pbWebinar !=null)
                pbWebinar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                ScanRequest request = new ScanRequest().withTableName(Config.WEBINARTABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> rows = response.getItems();
                for(Map<String, AttributeValue> map : rows){
                    webinarDO = new WebinarDO();
                    webinarDO.setMessage(map.get("message").getS());
                    webinarDO.setName(map.get("name").getS());
                    webinarDO.setType(map.get("type").getS());
                    webinarDO.setWhen(map.get("when").getS());
                    if(webinarDO.getType().equals("question")) {
                        webinarMessageList.add(webinarDO);
                    } else {
                        webinarLinkList.add(webinarDO);
                    }
                }
                return true;
            } catch (AmazonClientException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (pbWebinar !=null)
                pbWebinar.setVisibility(View.GONE);
            if (aBoolean) {
                Collections.sort(webinarMessageList, new DateComparator());
                Collections.sort(webinarLinkList, new DateComparator());
                Collections.reverse(webinarMessageList);
                Collections.reverse(webinarLinkList);
                webinarList.clear();
                if (webinar_link.isVisible())
                    webinarList.addAll(webinarMessageList);
                else webinarList.addAll(webinarLinkList);
                webinarAdapter.notifyDataSetChanged();
                lvWebinar.smoothScrollToPosition(0);
            } else
                Toast.makeText(WebinarActivity.this, "Check your internet connection and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private class PostMessage extends AsyncTask<WebinarDO, Void, Boolean>{

        ProgressDialog progressDialog;
        WebinarDO webinarDO = new WebinarDO();

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(WebinarActivity.this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Posting, please wait.");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(WebinarDO... webinarDOS) {

            try {
                webinarDO = webinarDOS[0];
                dynamoDBMapper.save(webinarDOS[0]);
                if (userType.equals("user")) {
                    BmttAdminsDO bmttAdminsDO = dynamoDBMapper.load(BmttAdminsDO.class, Config.SUPERADMIN, Config.SAPHONE);

                    AWSCredentials awsCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
                    AmazonSNSClient snsClient = new AmazonSNSClient(awsCredentials);

                    String notificationARN = bmttAdminsDO.getNotificationARN();
                    String message = name + " asked a webinar question.";
                    PublishRequest publishRequest = new PublishRequest();
                    publishRequest.setMessage(9 + notificationARN + "$" + message);
                    publishRequest.setSubject("BMTT Webinar");
                    publishRequest.withTargetArn(notificationARN);
                    snsClient.publish(publishRequest);
                }
                return true;
            } catch (AmazonClientException e){
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            if (aBoolean){
                Toast.makeText(WebinarActivity.this, "Successfully posted", Toast.LENGTH_SHORT).show();
                if (userType.equals("user")){
                    webinarMessageList.add(webinarDO);
                    Collections.sort(webinarMessageList, new DateComparator());
                    Collections.reverse(webinarMessageList);
                    webinarList.clear();
                    webinarList.addAll(webinarMessageList);
                    webinarAdapter.notifyDataSetChanged();
                } else {
                    webinarLinkList.add(webinarDO);
                    Collections.sort(webinarLinkList, new DateComparator());
                    Collections.reverse(webinarLinkList);
                    webinarList.clear();
                    webinarList.addAll(webinarLinkList);
                    webinarAdapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(WebinarActivity.this, "Network connection error, please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteWebinar extends AsyncTask<WebinarDO, Void, Boolean>{

        ProgressDialog progressDialog;
        WebinarDO webinarDO;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(WebinarActivity.this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Deleting, please wait.");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(WebinarDO... webinarDOS) {

            try {
                webinarDO = new WebinarDO();
                webinarDO.setMessage(webinarDOS[0].getMessage());
                webinarDO.setName(webinarDOS[0].getName());
                dynamoDBMapper.delete(webinarDO);
                return true;
            } catch (AmazonClientException e){
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            if (aBoolean){
                new WebinarMessages().execute();
                Toast.makeText(WebinarActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(WebinarActivity.this, "Failed to delete, try again", Toast.LENGTH_SHORT).show();
            }
        }
    }


    class DateComparator implements Comparator<WebinarDO>
    {
        int dt = 0;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        @Override
        public int compare(WebinarDO webinarDO1, WebinarDO webinarDO2) {
            try {
                dt = dateFormat.parse(webinarDO1.getWhen()).compareTo(dateFormat.parse(webinarDO2.getWhen()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return dt;
        }
    }

}
