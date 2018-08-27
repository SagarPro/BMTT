package com.sagsaguz.bmtt;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
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
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.google.gson.Gson;
import com.sagsaguz.bmtt.adapter.ExpandableListAdapter;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.BmttAdminsDO;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.QADetailsDO;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sagsaguz.bmtt.NotificationActivity.notificationActivity;

public class QAActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener {

    private static ExpandableListView expLv;
    private static ProgressBar pbPrepareQA;
    private static FloatingActionButton fb_askQuestion;
    private static ExpandableListAdapter listAdapter;

    private RelativeLayout rlQA, rlQABottom;

    private static AmazonDynamoDBClient dynamoDBClient;
    private static DynamoDBMapper dynamoDBMapper;

    private static int itemsCount = 0;
    private static Dialog dialog;
    private static Context context;
    private static QAActivity qaActivity;
    private static String questionType = "answered", userType = "user", menuType = "unselected";

    private static String email, name;

    private static MenuItem unansweredQuestions, allQuestions, userQuestions;

    private ProgressDialog progressDialog;
    private EditText etQASearch;

    private static List<String> listDataHeader = new ArrayList<String>();
    private static List<String> filterListDataHeader = new ArrayList<String>();
    private static HashMap<String, String> listSubHeader = new HashMap<String, String>();
    private static HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qa_layout);

        qaActivity = QAActivity.this;

        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        rlQA = findViewById(R.id.rlQA);
        rlQABottom = findViewById(R.id.rlQABottom);

        expLv = findViewById(R.id.expLv);

        etQASearch = findViewById(R.id.etQASearch);

        pbPrepareQA = findViewById(R.id.pbPrepareQA);
        pbPrepareQA.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.green), android.graphics.PorterDuff.Mode.MULTIPLY);
        //pbPrepareQA.setVisibility(View.VISIBLE);
        fb_askQuestion = findViewById(R.id.fb_askQuestion);
        fb_askQuestion.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(QAActivity.this, R.style.MyAlertDialogStyle);

        context = QAActivity.this;

        Intent intent = getIntent();
        userType = intent.getStringExtra("USERTYPE");
        if(userType.equals("user")){
            email = intent.getStringExtra("EMAIL");
            name = intent.getStringExtra("NAME");
            new PrepareUnansweredQA().execute();
        } else {
            new PrepareQA().execute();
        }

        listAdapter = new ExpandableListAdapter(QAActivity.this, userType, listDataHeader, listSubHeader, listDataChild);
        expLv.setAdapter(listAdapter);

        expLv.setOnItemLongClickListener(this);

        expLv.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousItem = -1;
            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousItem)
                    expLv.collapseGroup(previousItem);
                previousItem = groupPosition;
            }
        });

        /*expLv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                fb_askQuestion.show();
                fabAnim();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                fb_askQuestion.hide();
                fabAnim();
            }
        });*/

        fb_askQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postQuestionDialog();
            }
        });

        etQASearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String text = etQASearch.getText().toString().toLowerCase();
                List<String> newList = new ArrayList<>();
                listDataHeader.clear();
                newList.addAll(filterListDataHeader);
                for (String str : newList){
                    String name = str.toLowerCase();
                    if(name.contains(text)) {
                        listDataHeader.add(str);
                    }
                }
                listAdapter.notifyDataSetChanged();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }

    public void basicSnackBar(String message){
        Snackbar snackbar = Snackbar.make(rlQA, message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        snackbar.show();
    }

    public void showSnackBar(String message, final String type){
        final Snackbar snackbar = Snackbar.make(rlQA, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(type.equals("unanswered")){
                            new PrepareUnansweredQA().execute();
                        } else if (type.equals("prepare")){
                            new PrepareQA().execute();
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

    private void onGroupLongClick(final int position){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        tvMessage.setText("Are you sure, you want to remove\n"+listDataHeader.get(position));
        Button btnRemove = dialog.findViewById(R.id.btnRemove);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DeleteQuestion().execute(position);
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

    private void postQuestionDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.post_question_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final EditText etQuestion = dialog.findViewById(R.id.etQuestion);
        Button btnPost = dialog.findViewById(R.id.btnPost);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        Date c = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        final String currentDate = df.format(c);

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(etQuestion.getText().toString())){
                    basicSnackBar("Please enter your question.");
                } else {
                    new SendNotification().execute(etQuestion.getText().toString(), currentDate);
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

    public static void updateAnswer(String question, String newAnswer){
        new UpdateAnswer().execute(question, newAnswer);
    }

    private static Drawable buildCounterDrawable(int count, int backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.new_questions_notify, null);
        view.setBackgroundResource(backgroundImageId);

        if (count == 0) {
            View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count);
            textView.setText("" + count);
        }

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(context.getResources(), bitmap);
    }

    private static void checkQuestionType(){
        if(questionType.equals("answered")){
            allQuestions.setVisible(false);
        } else {
            allQuestions.setVisible(true);
        }
    }

    private static void checkUserType(){
        if(userType.equals("admin")){
            unansweredQuestions.setVisible(true);
            fb_askQuestion.setVisibility(View.GONE);
            //expLv.setOnItemLongClickListener(null);
        } else {
            //expLv.setOnItemLongClickListener(null);
            userQuestions.setVisible(false);
            allQuestions.setVisible(false);
            unansweredQuestions.setVisible(false);
            fb_askQuestion.setVisibility(View.VISIBLE);
        }
    }

    private static void checkMenuType(){
        if(menuType.equals("unselected")){
            checkUserType();
        } else {
            /*if(userType.equals("admin")){
                expLv.setOnItemLongClickListener(null);
            } else {
                expLv.setOnItemLongClickListener((AdapterView.OnItemLongClickListener) context);
            }*/
            unansweredQuestions.setVisible(false);
            userQuestions.setVisible(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.qa_menu, menu);
        userQuestions = menu.findItem(R.id.user_questions);
        allQuestions = menu.findItem(R.id.all_questions);
        unansweredQuestions = menu.findItem(R.id.new_questions);
        userQuestions.setVisible(false);
        allQuestions.setVisible(false);
        unansweredQuestions.setVisible(false);
        checkQuestionType();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.new_questions) {
            questionType = "unanswered";
            menuType = "selected";
            //checkUserType();
            unansweredQuestions.setVisible(false);
            rlQABottom.setVisibility(View.GONE);
            new PrepareUnansweredQA().execute();
            return true;
        }

        if (id == R.id.user_questions) {
            questionType = "unanswered";
            menuType = "selected";
            userQuestions.setVisible(false);
            rlQABottom.setVisibility(View.GONE);
            new PrepareUnansweredQA().execute();
            return true;
        }

        if (id == R.id.all_questions) {
            questionType = "answered";
            menuType = "unselected";
            checkQuestionType();
            rlQABottom.setVisibility(View.VISIBLE);
            new PrepareQA().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        long packedPosition = expLv.getExpandableListPosition(position);

        int itemType = ExpandableListView.getPackedPositionType(packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

        if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            onGroupLongClick(groupPosition);
        }
        return true;
    }


    private static class PrepareUnansweredQA extends AsyncTask<Void, Void, Boolean> {

        ScanResult response;
        int i;

        @Override
        protected void onPreExecute() {
            i = 0;
            listDataHeader.clear();
            listSubHeader.clear();
            listDataChild.clear();
            if (pbPrepareQA !=null)
                pbPrepareQA.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ScanRequest request = new ScanRequest().withTableName(Config.QATABLENAME);
                response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> rows = response.getItems();
                for(Map<String, AttributeValue> map : rows){
                    try{
                        AttributeValue question = map.get("question");
                        AttributeValue answer = map.get("answer");
                        AttributeValue name = map.get("name");
                        AttributeValue user = map.get("user");
                        AttributeValue when = map.get("when");
                        if(userType.equals("admin")) {
                            if (answer.getS().equals("unanswered")) {
                                listDataHeader.add(question.getS());
                                listSubHeader.put(listDataHeader.get(i), "by " + name.getS() + ", on " + when.getS());
                                List<String> answerList = new ArrayList<>();
                                answerList.add(answer.getS());
                                listDataChild.put(listDataHeader.get(i), answerList);
                                i++;
                            }
                        } else {
                            if (user.getS().equals(email)) {
                                listDataHeader.add(question.getS());
                                listSubHeader.put(listDataHeader.get(i), "by " + name.getS() + ", on " + when.getS());
                                List<String> answerList = new ArrayList<>();
                                answerList.add(answer.getS());
                                listDataChild.put(listDataHeader.get(i), answerList);
                                i++;
                            }
                        }
                    } catch (NumberFormatException e){
                        Log.d("number_format_exception", e.getMessage());
                    }
                }
                return true;
            } catch (AmazonClientException e){
                qaActivity.showSnackBar("Network connection error!!", "unanswered");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pbPrepareQA !=null)
                pbPrepareQA.setVisibility(View.GONE);
            if(result) {
                Collections.sort(listDataHeader, String.CASE_INSENSITIVE_ORDER);
                listAdapter.notifyDataSetChanged();
                filterListDataHeader.clear();
                filterListDataHeader.addAll(listDataHeader);
                if(!userType.equals("user")){
                    unansweredQuestions.setIcon(buildCounterDrawable(i, R.drawable.icon_user_questions));
                }
                checkMenuType();
                checkQuestionType();
            }
        }
    }



    private static class PrepareQA extends AsyncTask<Void, Void, Boolean> {

        ScanResult response;
        int i = 0, unansweredCount;

        @Override
        protected void onPreExecute() {
            unansweredCount = 0;
            listDataHeader.clear();
            listSubHeader.clear();
            listDataChild.clear();
            if (pbPrepareQA !=null)
                pbPrepareQA.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ScanRequest request = new ScanRequest().withTableName(Config.QATABLENAME);
                response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> rows = response.getItems();
                for(Map<String, AttributeValue> map : rows){
                    try{
                        AttributeValue question = map.get("question");
                        AttributeValue answer = map.get("answer");
                        AttributeValue name = map.get("name");
                        AttributeValue when = map.get("when");
                        if(!answer.getS().equals("unanswered")) {
                            listDataHeader.add(question.getS());
                            listSubHeader.put(listDataHeader.get(i), "by " + name.getS() + ", on " + when.getS());
                            List<String> answerList = new ArrayList<>();
                            answerList.add(answer.getS());
                            listDataChild.put(listDataHeader.get(i), answerList);
                            i++;
                        } else {
                            unansweredCount++;
                        }
                    } catch (NumberFormatException e){
                        Log.d("number_format_exception", e.getMessage());
                    }
                }
                return true;
            } catch (AmazonClientException e){
                qaActivity.showSnackBar("Network connection error!!", "prepare");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pbPrepareQA !=null)
                pbPrepareQA.setVisibility(View.GONE);
            if(result) {
                Collections.sort(listDataHeader, String.CASE_INSENSITIVE_ORDER);
                listAdapter.notifyDataSetChanged();
                filterListDataHeader.clear();
                filterListDataHeader.addAll(listDataHeader);
                unansweredQuestions.setIcon(buildCounterDrawable(unansweredCount, R.drawable.icon_user_questions));
                checkMenuType();
                checkQuestionType();
            }
        }
    }



    private static class PostQuestion extends AsyncTask<String, Void, Integer>{

        @Override
        protected void onPreExecute() {
            qaActivity.progressDialog.setMessage("Posting, please wait.");
            qaActivity.progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... strings) {

            try {
                ScanRequest request = new ScanRequest().withTableName(Config.QATABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                itemsCount = response.getCount();

                QADetailsDO qaDetailsDOItem = new QADetailsDO();

                qaDetailsDOItem.setQuestion(strings[0]);
                qaDetailsDOItem.setAnswer("unanswered");
                qaDetailsDOItem.setName(name);
                qaDetailsDOItem.setUser(email);
                qaDetailsDOItem.setWhen(strings[1]);

                dynamoDBMapper.save(qaDetailsDOItem);

                response = dynamoDBClient.scan(request);
                return response.getCount();
            } catch (AmazonClientException e){
                qaActivity.showSnackBar("Network connection error!!", "post");
                return itemsCount;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            qaActivity.progressDialog.dismiss();
            dialog.dismiss();
            if(itemsCount < integer){
                qaActivity.basicSnackBar("Successfully posted your question");
                if(questionType.equals("answered")){
                    if(userType.equals("admin")){
                        new PrepareQA().execute();
                    } else {
                        new PrepareUnansweredQA().execute();
                    }
                } else {
                    new PrepareUnansweredQA().execute();
                }
            }
        }
    }


    private static class DeleteQuestion extends AsyncTask<Integer, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            qaActivity.progressDialog.setMessage("Removing question, please wait.");
            qaActivity.progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {

            try {
                QADetailsDO qaDetailsDOItem = new QADetailsDO();
                qaDetailsDOItem.setQuestion(listDataHeader.get(integers[0]));
                DynamoDBQueryExpression<QADetailsDO> queryExpression = new DynamoDBQueryExpression<QADetailsDO>()
                        .withHashKeyValues(qaDetailsDOItem)
                        .withConsistentRead(false);
                PaginatedQueryList result = dynamoDBMapper.query(QADetailsDO.class, queryExpression);

                Gson gson = new Gson();
                for (int i = 0; i < result.size(); i++) {
                    String jsonFormOfItem = gson.toJson(result.get(i));
                    try {
                        JSONObject resultObject = new JSONObject(jsonFormOfItem);
                        qaDetailsDOItem.setUser(resultObject.getString("_user"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                dynamoDBMapper.delete(qaDetailsDOItem);
                return true;
            } catch (AmazonClientException e){
                qaActivity.showSnackBar("Network connection error!!", "update");
                return false;
            }

            /*try {
                ScanRequest request = new ScanRequest().withTableName(Config.QATABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                itemsCount = response.getCount();

                QADetailsDO qaDetailsDOItem = new QADetailsDO();

                String key = listDataHeader.get(integers[0]);

                qaDetailsDOItem.setQuestion(key);
                qaDetailsDOItem.setUser(email);

                dynamoDBMapper.delete(qaDetailsDOItem);

                response = dynamoDBClient.scan(request);
                return response.getCount();
            } catch (AmazonClientException e){
                qaActivity.showSnackBar("Network connection error!!", "delete");
                return itemsCount;
            }*/
        }

        @Override
        protected void onPostExecute(Boolean result) {
            qaActivity.progressDialog.dismiss();
            dialog.dismiss();
            if(result){
                if(questionType.equals("answered")){
                    if(userType.equals("admin")){
                        new PrepareQA().execute();
                    } else {
                        new PrepareUnansweredQA().execute();
                    }
                } else {
                    new PrepareUnansweredQA().execute();
                }
            }
        }
    }



    private static class UpdateAnswer extends AsyncTask<String, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            qaActivity.progressDialog.setMessage("Updating answer, please wait.");
            qaActivity.progressDialog.show();
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

                qaDetailsDOItem.setUser(uName);
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
            qaActivity.progressDialog.dismiss();
            if(result) {
                if (questionType.equals("answered")) {
                    if(userType.equals("admin")){
                        new PrepareQA().execute();
                    } else {
                        new PrepareUnansweredQA().execute();
                    }
                } else {
                    new PrepareUnansweredQA().execute();
                }
            }
        }
    }

    private static class SendNotification extends AsyncTask<String, Void, Boolean>{

        String question, date;

        @Override
        protected void onPreExecute() {
            qaActivity.progressDialog.setMessage("Posting, please wait.");
            qaActivity.progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            BmttAdminsDO bmttAdminsDO;

            question = strings[0];
            date = strings[1];
            try {
                bmttAdminsDO = dynamoDBMapper.load(BmttAdminsDO.class, Config.SUPERADMIN, Config.SAPHONE);

                AWSCredentials awsCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
                AmazonSNSClient snsClient = new AmazonSNSClient(awsCredentials);

                String notificationArn = bmttAdminsDO.getNotificationARN();
                PublishRequest publishRequest = new PublishRequest();
                publishRequest.setMessage(7 + notificationArn + "$" +name + " asked a question.");
                publishRequest.setSubject("BMTT Notification");
                publishRequest.withTargetArn(notificationArn);
                snsClient.publish(publishRequest);

                return true;
            } catch (AmazonClientException e){
                qaActivity.showSnackBar("Network connection error!!!", "send");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            qaActivity.progressDialog.dismiss();
            if (result) {
                new PostQuestion().execute(question, date);
            }
        }
    }

}
