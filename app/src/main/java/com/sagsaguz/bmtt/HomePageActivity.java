package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.sagsaguz.bmtt.fragments.BmttPart1;
import com.sagsaguz.bmtt.fragments.BmttPart2;
import com.sagsaguz.bmtt.fragments.BmttPart3;
import com.sagsaguz.bmtt.services.FirebaseDispatcher;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.BmttUsersDO;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.CountValuesDO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HomePageActivity extends AppCompatActivity {

    private TabLayout tabParts;
    private ViewPager viewPager;
    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout rlHomePage;

    private ImageView ivLogout, ivProfile, ivQA, ivPracticals, ivWebinar, ivAttachment, ivNotifications;
    @SuppressLint("StaticFieldLeak")
    public static View notificationIndicator;
    private LinearLayout llBottomMenu;

    @SuppressLint("StaticFieldLeak")
    public static ProgressBar pbHomePage;

    private static List<String> part1 = new ArrayList<>();
    private static List<String> part2 = new ArrayList<>();
    private static List<String> part3 = new ArrayList<>();
    private List<String> partsList = new ArrayList<>();
    private Map<String, Map<String, List<String>>> allList = new HashMap<>();

    private static List<String> objectNames = new ArrayList<>();
    private static List<String> fileNames = new ArrayList<>();
    List<String> exceededList = new ArrayList<>();

    private DynamoDBMapper dynamoDBMapper;
    @SuppressLint("StaticFieldLeak")
    static HomePageActivity homePageActivity;
    private int totalVideoCount = 0;

    private static SharedPreferences userPreferences;
    private SharedPreferences notificationPref;

    private static String email, phone, name, centre, login;
    private CountValuesDO cValuesDo = new CountValuesDO();
    private Intent autoStartIntent;

    private static Context context;

    FirebaseJobDispatcher firebaseJobDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page_layout);

        context = HomePageActivity.this;

        homePageActivity = HomePageActivity.this;

        userPreferences = getSharedPreferences("USERDETAILS", MODE_PRIVATE);
        email = userPreferences.getString("EMAIL", null);
        phone = userPreferences.getString("PHONE", null);
        name = userPreferences.getString("NAME", null);
        centre = userPreferences.getString("CENTRE", null);
        login = userPreferences.getString("LOGIN", null);

        SharedPreferences autoStart = getSharedPreferences("AUTOSTART", MODE_PRIVATE);
        Boolean enable = autoStart.getBoolean("ENABLE", false);
        if (!enable){
            autoStartIntent = new Intent();
            if ("xiaomi".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                autoStartIntent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                autoStartDialog();
            }/* else if ("oppo".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                autoStartIntent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
                autoStartDialog();
            } else if ("vivo".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                autoStartIntent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.MainGuideActivity."));
                autoStartDialog();
            }*/
            SharedPreferences.Editor editor = autoStart.edit();
            editor.putBoolean("ENABLE", true);
            editor.apply();
        }

        rlHomePage = findViewById(R.id.rlHomePage);

        pbHomePage = findViewById(R.id.pbHomePage);
        pbHomePage.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
        pbHomePage.setVisibility(View.GONE);

        ivLogout = findViewById(R.id.ivLogout);
        ivProfile = findViewById(R.id.ivProfile);
        ivPracticals = findViewById(R.id.ivPracticals);
        ivPracticals.setVisibility(View.GONE);
        ivQA = findViewById(R.id.ivQA);
        ivWebinar = findViewById(R.id.ivWebinar);
        ivAttachment = findViewById(R.id.ivAttachment);
        ivNotifications = findViewById(R.id.ivNotifications);

        firebaseJobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job job = firebaseJobDispatcher.newJobBuilder()
                .setService(FirebaseDispatcher.class)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTag("1")
                .setTrigger(Trigger.executionWindow(300,480))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .setConstraints(Constraint.ON_ANY_NETWORK).build();

        firebaseJobDispatcher.mustSchedule(job);

        notificationIndicator = findViewById(R.id.notificationIndicator);
        notificationPref = getSharedPreferences("NOTIFICATIONINDICATOR", MODE_PRIVATE);
        if (notificationPref.getBoolean("NINDICATOR", false)){
            notificationIndicator.setVisibility(View.VISIBLE);
        } else {
            notificationIndicator.setVisibility(View.GONE);
        }

        llBottomMenu = findViewById(R.id.llBottomMenu);
        llBottomMenu.setVisibility(View.INVISIBLE);

        setList();

        //lvNames = (ListView) findViewById(R.id.lvNames);

        /*AWSCredentials myCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
        AmazonS3 s3client = new AmazonS3Client(myCredentials);

        ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, "video" );
        final List<S3ObjectSummary> summaries = listing.getObjectSummaries();
        //Toast.makeText(getBaseContext()," "  + summaries.get(0),Toast.LENGTH_LONG).show();
        //tvText.setText(" " + summaries.get(0).getLastModified());

        TextView tvText = findViewById(R.id.tvText);

        for(S3ObjectSummary objectSummary : summaries){
            String key = objectSummary.getKey();
            String subKey = key.substring(key.indexOf("_")+1, key.indexOf("."));
            subKey = subKey.replace("_", " ");
            objectNames.add(key);
            fileNames.add(subKey);
            tvText.setText(" " + summaries+"\n");
        }*/

        /*adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, fileNames);
        lvNames.setAdapter(adapter);

        videoCount();

        lvNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                videoCount();
                String validateVideo = objectNames.get(position).substring(objectNames.get(position).indexOf("_")+1,
                                                                            objectNames.get(position).indexOf("."));
                validateVideo = validateVideo.replace("_", " ");
                if(exceededList.contains(validateVideo)){
                    Toast.makeText(HomePageActivity.this, "Limit has been exceeded", Toast.LENGTH_SHORT).show();
                } else {
                    new UpdateVideoCount(mapList, validateVideo, position).execute(dynamoDBMapper);
                }

            }
        });*/

        ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = userPreferences.edit();
                editor.putString("LOGIN", "logout");
                editor.apply();
                startActivity(new Intent(getBaseContext(), LoginActivity.class));
                finish();
            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("PHONE", phone);
                intent.putExtra("TOTALCOUNT", totalVideoCount);
                startActivity(intent);
            }
        });

        ivPracticals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), PracticalResultsActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("PHONE", phone);
                intent.putExtra("USERTYPE", "user");
                startActivity(intent);
            }
        });

        ivQA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), QAActivity.class);
                intent.putExtra("USERTYPE", "user");
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                startActivity(intent);
            }
        });

        ivWebinar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), WebinarActivity.class);
                intent.putExtra("USERTYPE", "user");
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                startActivity(intent);
            }
        });

        ivAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AttachmentsActivity.class);
                intent.putExtra("USERTYPE", "user");
                startActivity(intent);
            }
        });

        ivNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationIndicator.setVisibility(View.GONE);
                SharedPreferences.Editor editor = notificationPref.edit();
                editor.putBoolean("NINDICATOR", false);
                editor.apply();
                Intent intent = new Intent(getBaseContext(), NotificationActivity.class);
                intent.putExtra("USERTYPE", "user");
                intent.putExtra("EMAIL", email);
                intent.putExtra("CENTRE", centre);
                startActivity(intent);
            }
        });

    }

    private void autoStartDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        tvTitle.setText("Auto Start permission");
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        tvMessage.setText("Click OK to enable auto start permission for receiving notifications or you can allow it later manually.");
        Button btnOk = dialog.findViewById(R.id.btnRemove);
        btnOk.setText("OK");
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(autoStartIntent);
                dialog.dismiss();
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

    private void setList(){
        //mapList = new HashMap<>();
        if(email != null){
            new ListVideos().execute(email);

            viewPager = findViewById(R.id.viewPager);
            viewPager.setOffscreenPageLimit(1);

            tabParts = findViewById(R.id.tabParts);
            tabParts.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimary));
        } else {
            basicSnackBar("Empty details");
        }
    }

    public void basicSnackBar(String message){
        Snackbar snackbar = Snackbar.make(rlHomePage, message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        snackbar.show();
    }

    public void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(rlHomePage, message, Snackbar.LENGTH_SHORT)
                                    .setAction("Try Again", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            homePageActivity.setList();
                                        }
                                    });
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    private void setupViewPager(ViewPager viewPager) {
        if (partsList.size() != 0) {
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
            adapter.addFrag(BmttPart1.newInstance(allList.get(partsList.get(0))), "Part 1");
            adapter.addFrag(BmttPart2.newInstance(allList.get(partsList.get(1))), "Part 2");
            adapter.addFrag(BmttPart3.newInstance(allList.get(partsList.get(2))), "Part 3");
            viewPager.setAdapter(adapter);
        } else {
            showSnackBar("Network connection error!!");
        }
    }

    private void setTabDetails(BmttUsersDO bmttUsersDO){
        if (!bmttUsersDO.getBmttPart1()) {
            allList.get(partsList.get(0)).clear();
        }
        if (!bmttUsersDO.getBmttPart2()) {
            allList.get(partsList.get(1)).clear();
        }
        if (!bmttUsersDO.getBmttPart3()) {
            allList.get(partsList.get(2)).clear();
        }
        //int n = totalVideoCount;
        setupViewPager(viewPager);
        tabParts.setupWithViewPager(viewPager);
    }

    public void updateCounter(String videoName){
        AWSProvider awsProvider = new AWSProvider();
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
        //new GetCountValues(dynamoDBClient).execute(videoName);
        new GetCountValues(dynamoDBMapper).execute(videoName, email, phone);
    }

    public void videoCount(CountValuesDO countValuesDO, String videoName){
        exceededList.clear();
        AWSProvider awsProvider = new AWSProvider();
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
        String fName = videoName.substring(videoName.indexOf("_", videoName.indexOf("_") + 1), videoName.indexOf("."));
        String subKey = fName.substring(1);
        fName = subKey.replace("_", " ");

        cValuesDo = countValuesDO;

        if(countValuesDO != null) {
            Map<String, Integer> vCount = countValuesDO.getVideoCounts();
            Map<String, Integer> video_count = new HashMap<>();
            Set vkeys = vCount.keySet();
            for (Object key1 : vkeys) {
                String key = (String) key1;
                video_count.put(key, vCount.get(key));
                if (video_count.get(key) == 3) {
                    exceededList.add(key);
                }
            }
            if (exceededList.contains(fName)) {
                basicSnackBar("Limit has been exceeded");
            } else {
                Intent intent = new Intent(homePageActivity, VideoViewActivity.class);
                intent.putExtra("VIDEOFILE", videoName);
                intent.putExtra("EMAIL", email);
                intent.putExtra("PHONE", phone);
                homePageActivity.startActivity(intent);
            }
        } else {
            Intent intent = new Intent(homePageActivity, VideoViewActivity.class);
            intent.putExtra("VIDEOFILE", videoName);
            intent.putExtra("EMAIL", email);
            intent.putExtra("PHONE", phone);
            homePageActivity.startActivity(intent);
        }

    }

    public void updateCValues(String videoName){
        String fName = videoName.substring(videoName.indexOf("_", videoName.indexOf("_") + 1), videoName.indexOf("."));
        fName = fName.substring(1);
        fName = fName.replace("_", " ");

        if(cValuesDo != null) {
            new UpdateVideoCount(cValuesDo, fName, videoName).execute(dynamoDBMapper);
        } else {
            new NewUserVideoCount(fName, videoName).execute(dynamoDBMapper);
        }
    }


    public static class UpdateVideoCount extends AsyncTask<DynamoDBMapper, Void, Boolean> {

        CountValuesDO countValuesDO = new CountValuesDO();
        String fileName, videoName;

        UpdateVideoCount(CountValuesDO countValuesDO, String fileName, String videoName){
            //this.mapList = mapList;
            this.fileName = fileName;
            this.videoName = videoName;
            this.countValuesDO = countValuesDO;
        }

        @Override
        protected void onPreExecute() {

            countValuesDO.setEmailId(email);
            countValuesDO.setPhone(phone);

            Map<String, Integer> vCount = countValuesDO.getVideoCounts();
            Map<String, Integer> video_count = new HashMap<>();
            Set vkeys = vCount.keySet();
            for (Object key1 : vkeys) {
                String key = (String) key1;
                video_count.put(key, vCount.get(key));
            }
            if(video_count.get(fileName) != null){
                video_count.put(fileName, video_count.get(fileName)+1);
            } else {
                video_count.put(fileName, 1);
            }
            countValuesDO.setVideoCounts(video_count);
            countValuesDO.setMcqCounts(countValuesDO.getMcqCounts());
        }

        @Override
        protected Boolean doInBackground(DynamoDBMapper...dynamoDBMappers) {
            try {
                dynamoDBMappers[0].save(countValuesDO);
                return true;
            } catch (AmazonClientException e){
                homePageActivity.showSnackBar("Network connection error!!");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            /*if(result) {
                Intent intent = new Intent(homePageActivity, VideoViewActivity.class);
                intent.putExtra("VIDEOFILE", videoName);
                intent.putExtra("EMAIL", email);
                intent.putExtra("PHONE", phone);
                homePageActivity.startActivity(intent);
            }*/
        }
    }


    public static class NewUserVideoCount extends AsyncTask<DynamoDBMapper, Void, Boolean> {

        CountValuesDO countValuesDO = new CountValuesDO();
        String fileName, videoName;

        NewUserVideoCount(String fileName, String videoName){
            this.fileName = fileName;
            this.videoName = videoName;
        }

        @Override
        protected void onPreExecute() {

            countValuesDO.setEmailId(email);
            countValuesDO.setPhone(phone);

            Map<String, Integer> video_count = new HashMap<>();
            video_count.put(fileName, 1);
            countValuesDO.setVideoCounts(video_count);

            Map<String, List<Integer>> mcq_count = new HashMap<>();
            countValuesDO.setMcqCounts(mcq_count);

        }

        @Override
        protected Boolean doInBackground(DynamoDBMapper...dynamoDBMappers) {
            try {
                dynamoDBMappers[0].save(countValuesDO);
                return true;
            } catch (AmazonClientException e){
                homePageActivity.showSnackBar("Network connection error!!");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            /*if(result) {
                Intent intent = new Intent(homePageActivity, VideoViewActivity.class);
                intent.putExtra("VIDEOFILE", videoName);
                intent.putExtra("EMAIL", email);
                intent.putExtra("PHONE", phone);
                homePageActivity.startActivity(intent);
            }*/
        }
    }


    private static class ListVideos extends AsyncTask<String,Void,BmttUsersDO>{

        BmttUsersDO bmttUsersDO = new BmttUsersDO();
        Date currentDate, expiryDate;
        SimpleDateFormat df;
        Boolean expired, awsException;

        @Override
        protected void onPreExecute() {
            if(pbHomePage != null){
                pbHomePage.setVisibility(View.VISIBLE);
            }
            homePageActivity.partsList.clear();
            homePageActivity.allList.clear();
            homePageActivity.totalVideoCount = 0;
            expired = false;
            awsException = false;
            Date c = Calendar.getInstance().getTime();
            df = new SimpleDateFormat("MMM dd, yyyy");
            String formattedDate = df.format(c.getTime());
            try {
                currentDate = df.parse(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected BmttUsersDO doInBackground(String... strings) {

            AWSCredentials myCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
            AmazonS3 s3client = new AmazonS3Client(myCredentials);

            try {
                ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, "bmtt");
                final List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                for(S3ObjectSummary objectSummary : summaries){
                    String key = objectSummary.getKey();
                    String s = key.substring(0, key.indexOf("/"));
                    if (!homePageActivity.partsList.contains(s))
                        homePageActivity.partsList.add(s);
                }
            } catch (AmazonClientException e){
                homePageActivity.showSnackBar("Network connection error!!");
            }

            for (int i=0; i<homePageActivity.partsList.size(); i++) {
                List<String> foldersList = new ArrayList<>();
                HashMap<String, List<String>> mapList = new HashMap<>();
                try {
                    ObjectListing listing = s3client.listObjects(Config.BUCKETNAME, homePageActivity.partsList.get(i));
                    final List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                    for (S3ObjectSummary objectSummary : summaries) {
                        String key = objectSummary.getKey();
                        try {
                            String s = key.substring(key.indexOf("/") + 1, key.indexOf("/", key.indexOf("/") + 1));
                            if (!foldersList.contains(s))
                                foldersList.add(s);
                        } catch (Exception e) {
                            Log.d("Repeate", "repeated exception");
                        }
                    }
                } catch (AmazonClientException e) {
                    homePageActivity.showSnackBar("Network connection error!!");
                }

                try{
                    for(int i1=0; i1<foldersList.size(); i1++){
                        List<String> videosList = new ArrayList<>();
                        String prefix = homePageActivity.partsList.get(i)+"/"+foldersList.get(i1)+"/video";
                        ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, prefix);
                        final List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                        for(S3ObjectSummary objectSummary : summaries){
                            String key = objectSummary.getKey();
                            videosList.add(key);
                            homePageActivity.totalVideoCount++;
                        }
                        mapList.put(foldersList.get(i1), videosList);
                    }
                } catch (AmazonClientException e) {
                    homePageActivity.showSnackBar("Network connection error!!");
                }
                homePageActivity.allList.put(homePageActivity.partsList.get(i), mapList);
            }

            try {
                AWSProvider awsProvider = new AWSProvider();
                AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(context));
                dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
                DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                        .dynamoDBClient(dynamoDBClient)
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .build();

                bmttUsersDO.setEmailId(strings[0]);

                DynamoDBQueryExpression<BmttUsersDO> queryExpression = new DynamoDBQueryExpression<BmttUsersDO>()
                        .withHashKeyValues(bmttUsersDO)
                        .withConsistentRead(false);
                PaginatedQueryList<BmttUsersDO> result = dynamoDBMapper.query(BmttUsersDO.class, queryExpression);

                bmttUsersDO = result.get(0);
                try {
                    expiryDate = df.parse(bmttUsersDO.getExpiryDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long remaining = expiryDate.getTime() - currentDate.getTime();
                int remainingDays = (int) TimeUnit.DAYS.convert(remaining, TimeUnit.MILLISECONDS);
                if (remainingDays < 0){
                    expired = true;
                }
            } catch (AmazonClientException e){
                awsException =true;
                homePageActivity.showSnackBar("Network connection error!!");
            }

            return bmttUsersDO;
        }

        @Override
        protected void onPostExecute(BmttUsersDO bmttUsersDO) {
            if(pbHomePage != null){
                pbHomePage.setVisibility(View.GONE);
            }
            if (!expired) {
                if (bmttUsersDO.getEmailId() != null && !awsException) {
                    homePageActivity.setTabDetails(bmttUsersDO);
                    homePageActivity.llBottomMenu.setVisibility(View.INVISIBLE);
                }
            } else {
                SharedPreferences.Editor editor = userPreferences.edit();
                editor.putString("LOGIN", "logout");
                editor.apply();
                homePageActivity.basicSnackBar("Your account is expired\nPlease contact centre for more details.");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        homePageActivity.startActivity(new Intent(homePageActivity, LoginActivity.class));
                        homePageActivity.finish();
                    }
                }, 1500);
            }
        }
    }


    public static class GetCountValues extends AsyncTask<String, Void, Boolean>{

        DynamoDBMapper dynamoDBMapper;
        String videoName;
        CountValuesDO countValuesDO = new CountValuesDO();

        GetCountValues(DynamoDBMapper dynamoDBMapper){
            this.dynamoDBMapper = dynamoDBMapper;
        }

        @Override
        protected void onPreExecute() {
            if(pbHomePage != null){
                pbHomePage.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            videoName = strings[0];
            try {
                countValuesDO = dynamoDBMapper.load(CountValuesDO.class, strings[1], strings[2]);
                return true;
            } catch (AmazonClientException e){
                homePageActivity.showSnackBar("Network connection error!!");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(pbHomePage != null){
                pbHomePage.setVisibility(View.GONE);
            }
            if (result) {
                homePageActivity.videoCount(countValuesDO, videoName);
            }
        }
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
