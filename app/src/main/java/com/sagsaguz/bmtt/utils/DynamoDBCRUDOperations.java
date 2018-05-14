package com.sagsaguz.bmtt.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.sagsaguz.bmtt.HomePageActivity;
import com.sagsaguz.bmtt.MCQActivity;
import com.sagsaguz.bmtt.MainBranchActivity;
import com.sagsaguz.bmtt.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sagsaguz.bmtt.HomePageActivity.pbHomePage;

public class DynamoDBCRUDOperations {

    public Boolean createItem(final DynamoDBMapper dynamoDBMapper, UserModel userModel) {
        final BmttUsersDO bmttUsersDO = new BmttUsersDO();

        bmttUsersDO.setEmailId(userModel.getEmailId());
        bmttUsersDO.setFirstName(userModel.getFirstName());
        bmttUsersDO.setLastName(userModel.getLastName());
        bmttUsersDO.setAddress(userModel.getAddress());
        bmttUsersDO.setPhone(userModel.getPhone());
        bmttUsersDO.setPassword(userModel.getPassword());
        bmttUsersDO.setBmttPart1(userModel.getBmttPart1());
        bmttUsersDO.setBmttPart2(userModel.getBmttPart2());
        bmttUsersDO.setBmttPart3(userModel.getBmttPart3());
        bmttUsersDO.setCreatedDate(userModel.getCreatedDate());
        bmttUsersDO.setExpiryDate(userModel.getExpiryDate());
        bmttUsersDO.setNotificationARN(userModel.getNotificationARN());

        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(bmttUsersDO);
            }
        }).start();
        return true;
    }

    public void getUserDetails(DynamoDBMapper dynamoDBMapper, String email){
        BmttUsersDO bmttUsersDO = dynamoDBMapper.load(BmttUsersDO.class, email);
    }

    public static class GetNames extends AsyncTask<AmazonDynamoDBClient, Void, List<String>>{
        List<String> userNames = new ArrayList<>();
        @Override
        protected List<String> doInBackground(AmazonDynamoDBClient... amazonDynamoDBClients) {

            ScanResult result = null;
            do{
                ScanRequest req = new ScanRequest();
                req.setTableName(Config.USERSTABLENAME);
                if(result != null){
                    req.setExclusiveStartKey(result.getLastEvaluatedKey());
                }
                result = amazonDynamoDBClients[0].scan(req);
                List<Map<String, AttributeValue>> rows = result.getItems();
                for(Map<String, AttributeValue> map : rows){
                    try{
                        AttributeValue firstName = map.get("firstName");
                        AttributeValue lastName = map.get("lastName");
                        String name = firstName.getS()+" "+lastName.getS();
                        userNames.add(name);
                    } catch (NumberFormatException e){
                        System.out.println(e.getMessage());
                    }
                }
            } while(result.getLastEvaluatedKey() != null);

            return userNames;
        }
    }

    public static class GetCentres extends AsyncTask<AmazonDynamoDBClient, Void, HashMap<String, String>>{
        HashMap<String, String> userCentre = new HashMap<String, String>();
        @Override
        protected HashMap<String, String> doInBackground(AmazonDynamoDBClient... amazonDynamoDBClients) {

            ScanResult result = null;
            do{
                ScanRequest req = new ScanRequest();
                req.setTableName(Config.USERSTABLENAME);
                if(result != null){
                    req.setExclusiveStartKey(result.getLastEvaluatedKey());
                }
                result = amazonDynamoDBClients[0].scan(req);
                List<Map<String, AttributeValue>> rows = result.getItems();
                for(Map<String, AttributeValue> map : rows){
                    try{
                        AttributeValue firstName = map.get("firstName");
                        AttributeValue lastName = map.get("lastName");
                        String name = firstName.getS()+" "+lastName.getS();
                        userCentre.put(name, map.get("password").getS());
                    } catch (NumberFormatException e){
                        System.out.println(e.getMessage());
                    }
                }
            } while(result.getLastEvaluatedKey() != null);

            return userCentre;
        }
    }


    public static class GetCentreNames extends AsyncTask<AmazonDynamoDBClient, Void, List<String>>{
        List<String> centreNames = new ArrayList<>();
        @Override
        protected List<String> doInBackground(AmazonDynamoDBClient... amazonDynamoDBClients) {

            ScanResult result = null;
            do{
                ScanRequest req = new ScanRequest();
                req.setTableName(Config.USERSTABLENAME);
                if(result != null){
                    req.setExclusiveStartKey(result.getLastEvaluatedKey());
                }
                result = amazonDynamoDBClients[0].scan(req);
                List<Map<String, AttributeValue>> rows = result.getItems();
                for(Map<String, AttributeValue> map : rows){
                    try{
                        if(!centreNames.contains(map.get("centre").getS())) {
                            centreNames.add(map.get("centre").getS());
                        }
                    } catch (NumberFormatException e){
                        System.out.println(e.getMessage());
                    }
                }
            } while(result.getLastEvaluatedKey() != null);

            return centreNames;
        }
    }


    public static class GetCentreDetails extends AsyncTask<AmazonDynamoDBClient, Void, List<List<String>>>{
        List<String> centreNames = new ArrayList<>();
        List<String> centreEmail = new ArrayList<>();
        List<String> centrePhone = new ArrayList<>();
        List<String> centrePassword = new ArrayList<>();
        List<List<String>> cd = new ArrayList<>();
        @SuppressLint("StaticFieldLeak")
        MainBranchActivity mainBranchActivity;

        public GetCentreDetails(MainBranchActivity mainBranchActivity){
            this.mainBranchActivity = mainBranchActivity;
        }

        @Override
        protected List<List<String>> doInBackground(AmazonDynamoDBClient... amazonDynamoDBClients) {

            try {
                ScanResult result = null;
                do{
                    ScanRequest req = new ScanRequest();
                    req.setTableName(Config.ADMINTABLENAME);
                    if(result != null){
                        req.setExclusiveStartKey(result.getLastEvaluatedKey());
                    }
                    result = amazonDynamoDBClients[0].scan(req);
                    List<Map<String, AttributeValue>> rows = result.getItems();
                    for(Map<String, AttributeValue> map : rows){
                        try{
                            if(!centreNames.contains(map.get("centre").getS())) {
                                centreNames.add(map.get("centre").getS());
                                centreEmail.add(map.get("emailId").getS());
                                centrePhone.add(map.get("phone").getS());
                                centrePassword.add(map.get("password").getS());
                            }
                        } catch (NumberFormatException e){
                            System.out.println(e.getMessage());
                        }
                    }
                } while(result.getLastEvaluatedKey() != null);

                cd.add(centreNames);
                cd.add(centreEmail);
                cd.add(centrePhone);
                cd.add(centrePassword);

                return cd;
            } catch (AmazonClientException e){
                mainBranchActivity.showSnackBar("Network connection error!!", "other");
                return null;
            }
        }
    }



    public static class GetCountValues extends AsyncTask<Void, Void, Map<String, List<Map>>>{

        ScanResult response;
        AmazonDynamoDBClient dynamoDBClient;
        Map<String, List<Map>> mapCount = new HashMap<>();
        List<Map> countValues = new ArrayList<>();
        @SuppressLint("StaticFieldLeak")
        HomePageActivity homePageActivity;
        @SuppressLint("StaticFieldLeak")
        MCQActivity mcqActivity;

        public GetCountValues(AmazonDynamoDBClient dynamoDBClient, HomePageActivity homePageActivity){
            this.dynamoDBClient = dynamoDBClient;
            this.homePageActivity = homePageActivity;
            this.mcqActivity = null;
        }

        public GetCountValues(AmazonDynamoDBClient dynamoDBClient, MCQActivity mcqActivity){
            this.dynamoDBClient = dynamoDBClient;
            this.mcqActivity = mcqActivity;
            this.homePageActivity = null;
        }

        @Override
        protected Map<String, List<Map>> doInBackground(Void... voids) {
            mapCount.clear();
            countValues.clear();
            try {
                ScanRequest request = new ScanRequest().withTableName(Config.COUNTTABLENAME);
                response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> rows = response.getItems();
                for(Map<String, AttributeValue> map : rows){
                    countValues.clear();
                    try{
                        AttributeValue email = map.get("emailId");
                        AttributeValue videoCounts = map.get("videoCounts");
                        countValues.add(videoCounts.getM());
                        AttributeValue mcqCounts = map.get("mcqCounts");
                        countValues.add(mcqCounts.getM());
                        mapCount.put(email.getS(), countValues);
                    } catch (NumberFormatException e){
                        Log.d("number_format_exception", e.getMessage());
                    }
                }
            } catch (AmazonClientException e){
                if(homePageActivity == null){
                    mcqActivity.showSnackBar("Network connection error!!", "other");
                } else {
                    homePageActivity.showSnackBar("Network connection error!!");
                }
                return null;
            }

            return mapCount;
        }
    }


    //calling this class from activity and getting values

    /*DynamoDBCRUDOperations.GetNames getNames = new DynamoDBCRUDOperations.GetNames();
        getNames.execute(dynamoDBClient);
        DynamoDBCRUDOperations.GetCentres getCentres = new DynamoDBCRUDOperations.GetCentres();
        getCentres.execute(dynamoDBClient);
        try {
            userNames = getNames.get();
            userCentres = getCentres.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Collections.sort(userNames, String.CASE_INSENSITIVE_ORDER);
        usersListAdapter = new UsersListAdapter(MainBranchActivity.this, userNames, userCentres);
        lvUsers.setAdapter(usersListAdapter);*/


    //updating mcq values

    /*private static class UpdateResultCount extends AsyncTask<DynamoDBMapper, Void, String> {

        CountValuesDO countValuesDO = new CountValuesDO();

        String fileName;
        Map<String, List<Map>> mapList = new HashMap<>();

        UpdateResultCount(Map<String, List<Map>> mapList, String fileName){
            this.mapList = mapList;
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {

            if(pbMCQ != null)
                pbMCQ.setVisibility(View.VISIBLE);

            countValuesDO.setEmailId("sagarq@brightkidmont.com");
            countValuesDO.setPhone("699");

            List<Map> countMap = mapList.get("sagarq@brightkidmont.com");

            Map<String, Integer> video_count = new HashMap<>();
            Set vkeys = countMap.get(0).keySet();
            for (Object key1 : vkeys) {
                String key = (String) key1;
                AttributeValue value = (AttributeValue) countMap.get(0).get(key);
                video_count.put(key, Integer.valueOf(value.getN()));
            }
            countValuesDO.setVideoCounts(video_count);

            Map<String, List<Integer>> mcq_count = new HashMap<>();
            Set mkeys = countMap.get(1).keySet();
            for (Object key1 : mkeys) {
                String key = (String) key1;
                AttributeValue value = (AttributeValue) countMap.get(1).get(key);
                List<Integer> list = new ArrayList<>();
                for (int i=0; i< value.getL().size(); i++){
                    list.add(Integer.valueOf(value.getL().get(i).getN()));
                }
                mcq_count.put(key, list);
            }
            if (mcq_count.get(fileName) !=null){
                List<Integer> mCount = mcq_count.get(fileName);
                mCount.add(correctAnswer);
                mcq_count.put(fileName, mCount);
            } else {
                List<Integer> mCount = new ArrayList<>();
                mCount.add(correctAnswer);
                mcq_count.put(fileName, mCount);
            }
            countValuesDO.setMcqCounts(mcq_count);
        }

        @Override
        protected String doInBackground(DynamoDBMapper...dynamoDBMappers) {
            try {
                dynamoDBMappers[0].save(countValuesDO);
                return "true";
            } catch (AmazonClientException e){
                mcqActivity.showSnackBar("Network connection error!!", "update");
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(pbMCQ != null)
                pbMCQ.setVisibility(View.GONE);
            if(result.equals("true")) {
                context.finish();
            }
        }
    }*/



    //query using only hash key
    /*private static class UpdateAnswer extends AsyncTask<String, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Updating answer, please wait.");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                String oldAnswer = "null", uWhen = "null", uName = "null", uUser = "null";
                QADetailsDO qaDetailsDOItem = new QADetailsDO();
                qaDetailsDOItem.setQuestion(strings[0]);
                DynamoDBQueryExpression<QADetailsDO> queryExpression = new DynamoDBQueryExpression<QADetailsDO>()
                        .withHashKeyValues(qaDetailsDOItem)
                        .withConsistentRead(false);
                PaginatedQueryList result = dynamoDBMapper.query(QADetailsDO.class, queryExpression);

                Gson gson = new Gson();
                for (int i = 0; i < result.size(); i++) {
                    String jsonFormOfItem = gson.toJson(result.get(i));
                    try {
                        JSONObject resultObject = new JSONObject(jsonFormOfItem);
                        oldAnswer = resultObject.getString("_answer");
                        uName = resultObject.getString("_name");
                        uUser = resultObject.getString("_user");
                        uWhen = resultObject.getString("_when");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                qaDetailsDOItem.setAnswer(oldAnswer);
                dynamoDBMapper.delete(qaDetailsDOItem);

                qaDetailsDOItem.setAnswer(strings[1]);
                qaDetailsDOItem.setName(uName);
                qaDetailsDOItem.setUser(uUser);
                qaDetailsDOItem.setWhen(uWhen);
                dynamoDBMapper.save(qaDetailsDOItem);
                return true;
            } catch (AmazonClientException e){
                qaActivity.showSnackBar("Network connection error!!", "update");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            if(result) {
                if (questionType.equals("answered")) {
                    new PrepareQA().execute();
                } else {
                    new PrepareUnansweredQA().execute();
                }
            }
        }
    }*/

}
