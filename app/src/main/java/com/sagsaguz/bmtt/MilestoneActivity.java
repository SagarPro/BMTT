package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sagsaguz.bmtt.fragments.MilestoneBmttPart1;
import com.sagsaguz.bmtt.fragments.MilestoneBmttPart2;
import com.sagsaguz.bmtt.fragments.MilestoneBmttPart3;
import com.sagsaguz.bmtt.utils.AWSProvider;
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

public class MilestoneActivity extends AppCompatActivity {

    private TabLayout tabParts;
    private ViewPager viewPager;
    @SuppressLint("StaticFieldLeak")
    private static ImageView ivTimeCompletion, ivCourseCompletion;
    private TextView tvAdmissionDate, tvExpiryDate;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvCourseCompletion, tvDaysLeft;

    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout rlTimeCompletion, rlCourseCompletion;

    @SuppressLint("StaticFieldLeak")
    private static ProgressBar pbMilestone;

    private DynamoDBMapper dynamoDBMapper;
    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout rlMilestone;
    private static HashMap<String, Integer> videoCount = new HashMap<>();

    private List<String> partsList = new ArrayList<>();
    private Map<String, Map<String, List<String>>> allList = new HashMap<>();

    //private static List<S3ObjectSummary> totalCount = new ArrayList<>();
    private static int totalCount = 0;

    private static Map<String, Integer> vc = new HashMap<>();
    private static Map<String, List<Integer>> mc = new HashMap<>();

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    @SuppressLint("StaticFieldLeak")
    private static MilestoneActivity milestoneActivity;

    private static String email, phone, createdDate, expiryDate;
    private static Boolean bmtt1, bmtt2, bmtt3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.milestone_layout);

        milestoneActivity = MilestoneActivity.this;
        context = MilestoneActivity.this;

        rlMilestone = findViewById(R.id.rlMilestone);

        pbMilestone = findViewById(R.id.pbMilestone);
        pbMilestone.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
        pbMilestone.setVisibility(View.GONE);

        rlTimeCompletion = findViewById(R.id.rlTimeCompletion);
        rlCourseCompletion = findViewById(R.id.rlCourseCompletion);

        ivTimeCompletion = findViewById(R.id.ivTimeCompletion);
        ivTimeCompletion.setVisibility(View.INVISIBLE);
        ivCourseCompletion = findViewById(R.id.ivCourseCompletion);
        ivCourseCompletion.setVisibility(View.INVISIBLE);

        tvDaysLeft = findViewById(R.id.tvDaysLeft);
        tvAdmissionDate = findViewById(R.id.tvAdmissionDate);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        tvCourseCompletion = findViewById(R.id.tvCourseCompletion);

        AWSProvider awsProvider = new AWSProvider();
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        Intent intent = getIntent();
        email = intent.getStringExtra("EMAIL");
        phone = intent.getStringExtra("PHONE");
        createdDate = intent.getStringExtra("CREATED");
        expiryDate = intent.getStringExtra("EXPIRY");
        bmtt1 = intent.getBooleanExtra("BMTT1", false);
        bmtt2 = intent.getBooleanExtra("BMTT2", false);
        bmtt3 = intent.getBooleanExtra("BMTT3", false);

        tvAdmissionDate.setText(createdDate);
        tvExpiryDate.setText(expiryDate);

        new GetCountValues(dynamoDBMapper).execute(email, phone);

    }

    public void basicSnackBar(String message){
        Snackbar snackbar = Snackbar.make(rlMilestone, message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        snackbar.show();
    }

    public void showSnackBar(String message, final String type){
        Snackbar snackbar = Snackbar.make(rlMilestone, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(type.equals("update")){
                            new UpdateCounterValue(dynamoDBMapper).execute();
                        } else {
                            new GetCountValues(dynamoDBMapper).execute(email, phone);
                        }
                    }
                });
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent));

        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    private void setListView(CountValuesDO countValuesDO){

        videoCount.clear();

        if(countValuesDO == null){
            milestoneActivity.basicSnackBar("Not completed any chapter");
            vc.clear();
        } else {

            Map<String, Integer> video_count = countValuesDO.getVideoCounts();
            vc = video_count;

            Set vkeys = video_count.keySet();
            for (Object key1 : vkeys) {
                String key = (String) key1;
                int value = video_count.get(key);
                videoCount.put(key, value);
            }

            mc = countValuesDO.getMcqCounts();
        }
        setMilestoneValues();
    }

    private void setMilestoneValues(){

        //course completion
        int courseCompletion = 0;
        if(vc.size() != 0) {
            courseCompletion = ((vc.size() * 100) / totalCount);
        }
        if(courseCompletion>100){
            courseCompletion = 100;
        }
        String cc = courseCompletion + " %";
        tvCourseCompletion.setText(cc);

        ivCourseCompletion.setVisibility(View.VISIBLE);
        ivTimeCompletion.setVisibility(View.VISIBLE);

        float width = rlTimeCompletion.getMeasuredWidth() / 100;

        int translateValue;
        if(courseCompletion == 0){
            translateValue = rlTimeCompletion.getMeasuredWidth() / 10;
        } else if (courseCompletion < 10){
            translateValue = (rlTimeCompletion.getMeasuredWidth() / 10) - courseCompletion;
        } else {
            translateValue = 100 - courseCompletion;
        }

        Animation animation1 = new TranslateAnimation(-rlTimeCompletion.getMeasuredWidth(), -width*translateValue,0,0);
        animation1.setDuration(1500);
        ivCourseCompletion.startAnimation(animation1);
        animation1.setFillAfter(true);

        //time completion
        Calendar c = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        String formattedDate = sdf.format(c.getTime());

        Date sDate = null, eDate = null, cDate = null;
        try {
            sDate = sdf.parse(createdDate);
            eDate = sdf.parse(expiryDate);
            cDate = sdf.parse(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = 0, remaining = 0;
        if (sDate != null && eDate != null && cDate != null) {
            diff = eDate.getTime() - sDate.getTime();
            remaining = eDate.getTime() - cDate.getTime();
        }
        int numOfDays = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        int remainingDays = (int) TimeUnit.DAYS.convert(remaining, TimeUnit.MILLISECONDS);
        String daysLeft = remainingDays + " days left";

        float width2 = rlCourseCompletion.getMeasuredWidth() / numOfDays;

        float movement = rlCourseCompletion.getMeasuredWidth()-(width2*(numOfDays-remainingDays));
        if(remainingDays <= 0) {
            tvDaysLeft.setText("Expired");
            movement = 0;
        } else if(remainingDays == 1){
            tvDaysLeft.setText(daysLeft.replace("s",""));
        } else {
            tvDaysLeft.setText(daysLeft);
        }

        Animation animation2 = new TranslateAnimation(-rlCourseCompletion.getMeasuredWidth(), -movement,0,0);
        animation2.setDuration(1500);
        ivTimeCompletion.startAnimation(animation2);
        animation2.setFillAfter(true);

        viewPager = findViewById(R.id.msViewPager);
        viewPager.setOffscreenPageLimit(1);

        tabParts = findViewById(R.id.tabParts);
        tabParts.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimary));

        setTabDetails();

    }

    private void setupViewPager(ViewPager viewPager) {
        if (partsList.size() != 0) {
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
            adapter.addFrag(MilestoneBmttPart1.newInstance(allList.get(partsList.get(0)), videoCount), "Part 1");
            adapter.addFrag(MilestoneBmttPart2.newInstance(allList.get(partsList.get(1)), videoCount), "Part 2");
            adapter.addFrag(MilestoneBmttPart3.newInstance(allList.get(partsList.get(2)), videoCount), "Part 3");
            viewPager.setAdapter(adapter);
        } else {
            showSnackBar("Network connection error!!", "get");
        }
    }

    private void setTabDetails(){
        if (!bmtt1) {
            allList.get(partsList.get(0)).clear();
        }
        if (!bmtt2) {
            allList.get(partsList.get(1)).clear();
        }
        if (!bmtt3) {
            allList.get(partsList.get(2)).clear();
        }
        setupViewPager(viewPager);
        tabParts.setupWithViewPager(viewPager);
    }

    public void showStatisticsDialog(String videoName){
        List<Integer> mcq_result = mc.get(videoName);
        if(mcq_result != null) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.statistics_dialog);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            GraphView graphView = dialog.findViewById(R.id.graphView);

            int graphColor = Color.argb(90, 52, 152, 219);
            int graphGridColor = Color.argb(50, 236, 240, 241);
            int graphAxisColor = getResources().getColor(R.color.colorAccent);

            DataPoint[] dataPoints = new DataPoint[mcq_result.size() + 1];
            dataPoints[0] = new DataPoint(0, 0);
            String[] xAxis = new String[mcq_result.size() + 1];
            xAxis[0] = "0";
            for (int i = 0; i < mcq_result.size(); i++) {
                dataPoints[i + 1] = new DataPoint(i + 1, mcq_result.get(i));
                xAxis[i + 1] = "" + (i + 1);
            }

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
            series.setTitle("MCQ Results");
            series.setColor(graphColor);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(7);
            series.setThickness(5);
            series.setBackgroundColor(graphColor);
            series.setDrawBackground(true);
            graphView.getGridLabelRenderer().setGridColor(graphGridColor);
            graphView.getGridLabelRenderer().setHorizontalLabelsColor(graphAxisColor);
            graphView.getGridLabelRenderer().setVerticalLabelsColor(graphAxisColor);
            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
            staticLabelsFormatter.setHorizontalLabels(xAxis);
            staticLabelsFormatter.setVerticalLabels(new String[]{"0", "1", "2", "3", "4", "5"});
            graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
            /*graphView.getViewport().setScrollable(true);
            graphView.getViewport().setScalableY(true);*/

            graphView.addSeries(series);

            dialog.show();

            graphView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.show();
                }
            });

        } else {
            basicSnackBar("MCQ is not done for this video");
        }
    }

    public void updateCountValues(String key, Integer value){
        vc.put(key, value);
        new UpdateCounterValue(dynamoDBMapper).execute();
    }


    private static class GetCountValues extends AsyncTask<String, Void, CountValuesDO> {

        DynamoDBMapper dynamoDBMapper;
        CountValuesDO countValuesDO = new CountValuesDO();

        GetCountValues(DynamoDBMapper dynamoDBMapper){
            this.dynamoDBMapper = dynamoDBMapper;
        }

        @Override
        protected void onPreExecute() {
            if(pbMilestone != null)
                pbMilestone.setVisibility(View.VISIBLE);
            milestoneActivity.partsList.clear();
            milestoneActivity.allList.clear();
            totalCount = 0;
        }

        @Override
        protected CountValuesDO doInBackground(String... strings) {

            AWSCredentials myCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
            AmazonS3 s3client = new AmazonS3Client(myCredentials);

            try {
                ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, "bmtt");
                final List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                for(S3ObjectSummary objectSummary : summaries){
                    String key = objectSummary.getKey();
                    String s = key.substring(0, key.indexOf("/"));
                    if (!milestoneActivity.partsList.contains(s))
                        milestoneActivity.partsList.add(s);
                    if (bmtt1){
                        if (key.contains("bmttPart1") && key.contains("video"))
                            totalCount = totalCount + 1;
                    }
                    if (bmtt2){
                        if (key.contains("bmttPart2") && key.contains("video"))
                            totalCount = totalCount + 1;
                    }
                    if (bmtt3){
                        if (key.contains("bmttPart3") && key.contains("video"))
                            totalCount = totalCount + 1;
                    }
                }
            } catch (AmazonClientException e){
                milestoneActivity.showSnackBar("Network connection error!!", "get");
                return null;
            }

            for (int i=0; i<milestoneActivity.partsList.size(); i++) {
                List<String> foldersList = new ArrayList<>();
                HashMap<String, List<String>> mapList = new HashMap<>();
                try {
                    ObjectListing listing = s3client.listObjects(Config.BUCKETNAME, milestoneActivity.partsList.get(i));
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
                    milestoneActivity.showSnackBar("Network connection error!!", "get");
                    return null;
                }

                try{
                    for(int i1=0; i1<foldersList.size(); i1++){
                        List<String> videosList = new ArrayList<>();
                        String prefix = milestoneActivity.partsList.get(i)+"/"+foldersList.get(i1)+"/video";
                        ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, prefix);
                        final List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                        for(S3ObjectSummary objectSummary : summaries){
                            String key = objectSummary.getKey();
                            videosList.add(key);
                        }
                        mapList.put(foldersList.get(i1), videosList);
                    }
                } catch (AmazonClientException e) {
                    milestoneActivity.showSnackBar("Network connection error!!", "get");
                    return null;
                }
                milestoneActivity.allList.put(milestoneActivity.partsList.get(i), mapList);
            }

            countValuesDO = dynamoDBMapper.load(CountValuesDO.class, strings[0], strings[1]);
            return countValuesDO;

            /*try {
                ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, "bmtt");
                final List<S3ObjectSummary> summaries = listing.getObjectSummaries();
                for(S3ObjectSummary objectSummary : summaries){
                    String key = objectSummary.getKey();
                    if (bmtt1){
                        if (key.contains("bmttPart1") && key.contains("video"))
                            totalCount = totalCount + 1;
                    }
                    if (bmtt2){
                        if (key.contains("bmttPart2") && key.contains("video"))
                            totalCount = totalCount + 1;
                    }
                    if (bmtt3){
                        if (key.contains("bmttPart3") && key.contains("video"))
                            totalCount = totalCount + 1;
                    }
                }
                countValuesDO = dynamoDBMapper.load(CountValuesDO.class, strings[0], strings[1]);
                return countValuesDO;
            } catch (AmazonClientException e){
                milestoneActivity.showSnackBar("Network connection error!!", "get");
                return null;
            }*/
        }

        @Override
        protected void onPostExecute(CountValuesDO countValuesDO) {
            if(pbMilestone != null)
                pbMilestone.setVisibility(View.GONE);
            milestoneActivity.setListView(countValuesDO);
        }
    }


    private static class UpdateCounterValue extends AsyncTask<Void, Void, Boolean>{

        CountValuesDO countValuesDO = new  CountValuesDO();
        DynamoDBMapper dynamoDBMapper;

        UpdateCounterValue(DynamoDBMapper dynamoDBMapper){
            this.dynamoDBMapper = dynamoDBMapper;
        }

        @Override
        protected void onPreExecute() {
            countValuesDO.setEmailId(email);
            countValuesDO.setPhone(phone);
            countValuesDO.setVideoCounts(vc);
            countValuesDO.setMcqCounts(mc);
            if(pbMilestone != null)
                pbMilestone.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                dynamoDBMapper.save(countValuesDO);
                return true;
            } catch (AmazonClientException e){
                milestoneActivity.showSnackBar("Network connection error!!", "update");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(pbMilestone != null)
                pbMilestone.setVisibility(View.GONE);
            if(result){
                milestoneActivity.basicSnackBar("Successfully update");
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
