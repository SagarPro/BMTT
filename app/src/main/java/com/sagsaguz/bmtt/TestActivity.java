package com.sagsaguz.bmtt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
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
import java.util.concurrent.TimeUnit;

public class TestActivity extends AppCompatActivity {

    private static List<Integer> mcq = new ArrayList<>();

    DynamoDBMapper dynamoDBMapper;
    AmazonDynamoDBClient dynamoDBClient;

    private static Context context;

    private static List<Integer> list = new ArrayList<>();

    private static int vValue = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        context = TestActivity.this;

        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        //new AddUser(dynamoDBClient).execute(dynamoDBMapper);
        //new ShowAllUsers(dynamoDBMapper, dynamoDBClient).execute();
        new ListVideos().execute("new");

    }

    private static class ListVideos extends AsyncTask<String,Void,BmttUsersDO>{

        BmttUsersDO bmttUsersDO = new BmttUsersDO();
        Date currentDate, expiryDate;
        SimpleDateFormat df;
        Boolean expired;
        List<String> l = new ArrayList<>();

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected BmttUsersDO doInBackground(String... strings) {

            //String[] parts = {"bmttPart1", "bmttPart2", "bmttPart3"};

            AWSCredentials myCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
            AmazonS3 s3client = new AmazonS3Client(myCredentials);

            try {
                ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, "bmtt");
                final List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                for(S3ObjectSummary objectSummary : summaries){
                    String key = objectSummary.getKey();
                    String s = key.substring(0, key.indexOf("/"));
                    if (!l.contains(s))
                        l.add(s);
                    /*String subKey = key.substring(key.indexOf("_")+1, key.indexOf("."));
                    subKey = subKey.replace("_", " ");*/
                }
            } catch (AmazonClientException e){
            }

            /*try {
                ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, l.get(0));
                final List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                for(S3ObjectSummary objectSummary : summaries){
                    String key = objectSummary.getKey();
                    String s = key.substring(key.indexOf("/")+1,key.indexOf("/", key.indexOf("/")+1));
                    if (!l.contains(s))
                        l.add(s);
                    *//*String subKey = key.substring(key.indexOf("_")+1, key.indexOf("."));
                    subKey = subKey.replace("_", " ");*//*
                }
            } catch (AmazonClientException e){
            }*/

            try {
                String p = l.get(1)+"/video";
                ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, p);
                final List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                for(S3ObjectSummary objectSummary : summaries){
                    String key = objectSummary.getKey();
                    String s = key.substring(key.indexOf("_")+1, key.indexOf("."));
                    if (!l.contains(s))
                        l.add(s);
                    //subKey = subKey.replace("_", " ");
                }
            } catch (AmazonClientException e){
            }

            /*try{
                for(int i=0; i<3; i++){
                    String prefix = parts[i]+"/video";
                    ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, prefix);
                    final List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                    for(S3ObjectSummary objectSummary : summaries){
                        String key = objectSummary.getKey();
                        String subKey = key.substring(key.indexOf("_")+1, key.indexOf("."));
                        subKey = subKey.replace("_", " ");
                        objectNames.add(key);
                        fileNames.add(subKey);
                        part.get(i).add(key);
                    }
                }

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
                //homePageActivity.showSnackBar("Network connection error!!");
            }*/

            return bmttUsersDO;
        }

        @Override
        protected void onPostExecute(BmttUsersDO bmttUsersDO) {
        }
    }

    private static class AddUser extends AsyncTask<DynamoDBMapper, Void, Boolean> {

        CountValuesDO videoCountDO = new CountValuesDO();
        AmazonDynamoDBClient dynamoDBClient;

        AddUser(AmazonDynamoDBClient dynamoDBClient){
            this.dynamoDBClient = dynamoDBClient;
        }

        @Override
        protected void onPreExecute() {

            videoCountDO.setEmailId("sagar@gmail.com");
            videoCountDO.setPhone("sagar");

            Map<String, Integer> video_count = new HashMap<>();
            video_count.put("video1", 20);
            video_count.put("video2", 13);
            videoCountDO.setVideoCounts(video_count);

            Map<String, List<Integer>> mcq_count = new HashMap<>();
            List<Integer> list1 = new ArrayList<>();
            list1.add(20);
            list1.add(13);
            mcq_count.put("mcq1", list1);
            List<Integer> list2 = new ArrayList<>();
            list2.add(20);
            list2.add(13);
            list2.addAll(list);
            mcq_count.put("mcq2", list2);
            videoCountDO.setMcqCounts(mcq_count);
        }

        @Override
        protected Boolean doInBackground(DynamoDBMapper...dynamoDBMappers) {

            dynamoDBMappers[0].save(videoCountDO);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
        }
    }


    private static class ShowAllUsers extends AsyncTask<Void, Void, List<AttributeValue>>{

        ScanResult response;
        AmazonDynamoDBClient dynamoDBClient;
        Map<String, AttributeValue> v = new HashMap<>();
        Map<String, AttributeValue> m = new HashMap<>();
        DynamoDBMapper dynamoDBMapper;

        ShowAllUsers(DynamoDBMapper dynamoDBMapper, AmazonDynamoDBClient dynamoDBClient){
            this.dynamoDBMapper = dynamoDBMapper;
            this.dynamoDBClient = dynamoDBClient;
        }

        @Override
        protected void onPreExecute() {
            mcq.clear();
        }

        @Override
        protected List<AttributeValue> doInBackground(Void... voids) {
            ScanRequest request = new ScanRequest().withTableName(Config.COUNTTABLENAME);
            response = dynamoDBClient.scan(request);
            List<Map<String, AttributeValue>> rows = response.getItems();
            for(Map<String, AttributeValue> map : rows){
                try{
                    AttributeValue videoCounts = map.get("videoCounts");
                    v = videoCounts.getM();
                    vValue = Integer.valueOf(v.get("video2").getN());
                    AttributeValue mcqCounts = map.get("mcqCounts");
                    m = mcqCounts.getM();
                    /*AttributeValue firstName = map.get("firstName");
                    AttributeValue lastName = map.get("lastName");
                    String name = firstName.getS() + " " + lastName.getS();
                    userNames.add(name);
                    userCentres.put(name, map.get("password").getS());
                    userEmailAddress.put(name, map.get("emailId").getS());*/
                } catch (NumberFormatException e){
                    Log.d("number_format_exception", e.getMessage());
                }
            }
            //return Integer.valueOf(v.get("video2").getN());
            return m.get("mcq2").getL();
        }

        @Override
        protected void onPostExecute(List<AttributeValue> attributeValue) {
            for (int i=0; i<attributeValue.size(); i++) {
                list.add(Integer.valueOf(attributeValue.get(i).getN()));
                //Toast.makeText(context, "" + attributeValue.get(i).getN(), Toast.LENGTH_SHORT).show();
            }
            //new AddUser(dynamoDBClient).execute(dynamoDBMapper);
            new UpdateVideoCount(dynamoDBClient).execute(dynamoDBMapper);
        }
    }

    private static class UpdateVideoCount extends AsyncTask<DynamoDBMapper, Void, Boolean> {

        CountValuesDO videoCountDO = new CountValuesDO();
        AmazonDynamoDBClient dynamoDBClient;

        UpdateVideoCount(AmazonDynamoDBClient dynamoDBClient){
            this.dynamoDBClient = dynamoDBClient;
        }

        @Override
        protected void onPreExecute() {

            vValue++;

            videoCountDO.setEmailId("sagar@brightkidmont.com");
            videoCountDO.setPhone("sagar");

            Map<String, Integer> video_count = new HashMap<>();
            video_count.put("video1", 20);
            video_count.put("video2", vValue);
            videoCountDO.setVideoCounts(video_count);

            Map<String, List<Integer>> mcq_count = new HashMap<>();
            List<Integer> list1 = new ArrayList<>();
            list1.add(20);
            list1.add(13);
            mcq_count.put("mcq1", list1);
            List<Integer> list2 = new ArrayList<>();
            list2.add(20);
            list2.add(13);
            list2.addAll(list);
            mcq_count.put("mcq2", list2);
            videoCountDO.setMcqCounts(mcq_count);
        }

        @Override
        protected Boolean doInBackground(DynamoDBMapper...dynamoDBMappers) {

            dynamoDBMappers[0].save(videoCountDO);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show();
        }
    }

}
