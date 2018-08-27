package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.CountValuesDO;
import com.sagsaguz.bmtt.utils.DynamoDBCRUDOperations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class MCQActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private static TextView tvQuestion;
    @SuppressLint("StaticFieldLeak")
    private static RadioButton rbOpt1, rbOpt2, rbOpt3, rbOpt4;
    @SuppressLint("StaticFieldLeak")
    private static RadioGroup rgOpt;
    @SuppressLint("StaticFieldLeak")
    private static ProgressBar pbMCQ;
    private RelativeLayout rlMCQ;

    @SuppressLint("StaticFieldLeak")
    private static MCQActivity mcqActivity;

    @SuppressLint("StaticFieldLeak")
    private static Button btnCheck, btnNext;

    private static AmazonS3 s3client;

    private static List<String> question = new ArrayList<>();
    private static List<String> opt1 = new ArrayList<>();
    private static List<String> opt2 = new ArrayList<>();
    private static List<String> opt3 = new ArrayList<>();
    private static List<String> opt4 = new ArrayList<>();
    private static List<String> answer = new ArrayList<>();

    private static List<Integer> rbId = new ArrayList<>();
    private static int questionNumber = 0;
    private static int selectedOpt, correctOpt, correctAnswer = 0;
    @SuppressLint("StaticFieldLeak")
    private static Activity context;
    private String subkey, email, phone;
    private DynamoDBMapper dynamoDBMapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mcq_layout);

        mcqActivity = MCQActivity.this;
        context = MCQActivity.this;

        rlMCQ = findViewById(R.id.rlMCQ);

        tvQuestion = findViewById(R.id.tvQuestion);

        rgOpt = findViewById(R.id.rgOpt);

        rbOpt1 = findViewById(R.id.rbOpt1);
        rbId.add(R.id.rbOpt1);
        rbOpt2 = findViewById(R.id.rbOpt2);
        rbId.add(R.id.rbOpt2);
        rbOpt3 = findViewById(R.id.rbOpt3);
        rbId.add(R.id.rbOpt3);
        rbOpt4 = findViewById(R.id.rbOpt4);
        rbId.add(R.id.rbOpt4);

        pbMCQ = findViewById(R.id.pbMCQ);
        pbMCQ.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
        pbMCQ.setVisibility(View.GONE);

        btnCheck = findViewById(R.id.btnCheck);
        btnNext = findViewById(R.id.btnNext);

        Intent intent = getIntent();
        String key = intent.getStringExtra("FILENAME");
        subkey = key.substring(key.indexOf("_")+1);
        subkey = subkey + ".csv";
        email = intent.getStringExtra("EMAIL");
        phone = intent.getStringExtra("PHONE");
        correctAnswer = 0;

        new readCSVFile().execute(subkey);

        AWSProvider awsProvider = new AWSProvider();
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        rgOpt.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                selectedOpt = radioGroup.getCheckedRadioButtonId();
                setRBColor(selectedOpt);
                if(selectedOpt==-1) {
                    btnCheck.setClickable(false);
                } else {
                    btnCheck.setClickable(true);
                }
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResultRBColor(selectedOpt, correctOpt);
                if(questionNumber<10)
                    questionNumber++;
                btnCheck.setVisibility(View.GONE);
                btnNext.setVisibility(View.VISIBLE);
                nonClickableRB();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(questionNumber<10) {
                    setQuestion(questionNumber);
                    btnNext.setVisibility(View.GONE);
                    btnCheck.setVisibility(View.VISIBLE);
                    btnCheck.setClickable(false);
                    setDefaultRBColor();
                } else {
                    questionNumber = 0;
                    btnNext.setClickable(false);
                    new GetCountValues(dynamoDBMapper).execute(email, phone);
                }
            }
        });

        btnCheck.setClickable(false);
        nonClickableRB();

    }

    public void showSnackBar(String message, final String type){
        Snackbar snackbar = Snackbar.make(rlMCQ, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(type.equals("csv")) {
                            new readCSVFile().execute(subkey);
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

    private static void setQuestion(int qNumber){
        tvQuestion.setText(question.get(qNumber));
        rbOpt1.setText(opt1.get(qNumber));
        rbOpt2.setText(opt2.get(qNumber));
        rbOpt3.setText(opt3.get(qNumber));
        rbOpt4.setText(opt4.get(qNumber));
        for(int i=0; i<4; i++){
            RadioButton radioButton = context.findViewById(rbId.get(i));
            if(radioButton.getText().equals(answer.get(qNumber)))
                correctOpt = rbId.get(i);
        }
        clickableRB();
    }

    private void setRBColor(int selectedRBId){
        for (int i=0; i<4; i++){
            if(rbId.get(i) == selectedRBId){
                RadioButton selectedRadioButton = findViewById(rbId.get(i));
                selectedRadioButton.setBackgroundColor(getResources().getColor(R.color.orange));
                selectedRadioButton.setTextColor(getResources().getColor(R.color.colorAccent));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    selectedRadioButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(MCQActivity.this, R.color.colorAccent)));
                }
                selectedRadioButton.setHighlightColor(getResources().getColor(R.color.colorAccent));
            } else {
                RadioButton selectedRadioButton = findViewById(rbId.get(i));
                selectedRadioButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                selectedRadioButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    selectedRadioButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(MCQActivity.this, R.color.colorPrimary)));
                }
                selectedRadioButton.setHighlightColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    }

    private void setResultRBColor(int selected, int correct){
        if(selected == correct){
            correctAnswer++;
            RadioButton radioButton = findViewById(selected);
            radioButton.setBackgroundColor(getResources().getColor(R.color.green));
            radioButton.setTextColor(getResources().getColor(R.color.colorAccent));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                radioButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(MCQActivity.this, R.color.colorAccent)));
            }
            radioButton.setHighlightColor(getResources().getColor(R.color.colorAccent));
        } else {
            RadioButton radioButtonS = findViewById(selected);
            radioButtonS.setBackgroundColor(getResources().getColor(R.color.red));
            radioButtonS.setTextColor(getResources().getColor(R.color.colorAccent));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                radioButtonS.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(MCQActivity.this, R.color.colorAccent)));
            }
            radioButtonS.setHighlightColor(getResources().getColor(R.color.colorAccent));

            RadioButton radioButtonC = findViewById(correct);
            radioButtonC.setBackgroundColor(getResources().getColor(R.color.green));
            radioButtonC.setTextColor(getResources().getColor(R.color.colorAccent));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                radioButtonC.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(MCQActivity.this, R.color.colorAccent)));
            }
            radioButtonC.setHighlightColor(getResources().getColor(R.color.colorAccent));
        }
    }

    private void setDefaultRBColor(){
        rgOpt.clearCheck();
        for (int i=0; i<4; i++){
            RadioButton radioButton = findViewById(rbId.get(i));
            radioButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            radioButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                radioButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(MCQActivity.this, R.color.colorPrimary)));
            }
            radioButton.setHighlightColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    private void nonClickableRB(){
        rbOpt1.setClickable(false);
        rbOpt2.setClickable(false);
        rbOpt3.setClickable(false);
        rbOpt4.setClickable(false);
    }

    private static void clickableRB(){
        rbOpt1.setClickable(true);
        rbOpt2.setClickable(true);
        rbOpt3.setClickable(true);
        rbOpt4.setClickable(true);
    }


    private static class readCSVFile extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            if(pbMCQ != null)
                pbMCQ.setVisibility(View.VISIBLE);
            question.clear();
            opt1.clear();
            opt2.clear();
            opt3.clear();
            opt4.clear();
            answer.clear();
            AWSCredentials myCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
            s3client = new AmazonS3Client(myCredentials);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                ObjectListing listing = s3client.listObjects( Config.BUCKETNAME, "mcq/"+strings[0] );
                List<S3ObjectSummary> summaries = listing.getObjectSummaries();
                if(summaries.size() == 0){
                    return "false";
                } else {
                    S3Object s3object = s3client.getObject(new GetObjectRequest(Config.BUCKETNAME, summaries.get(0).getKey()));
                    System.out.println(s3object.getObjectMetadata().getContentType());
                    System.out.println(s3object.getObjectMetadata().getContentLength());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(s3object.getObjectContent()));
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            line = line.replace("\"", "");
                            System.out.println(line);
                            if (line.contains("$")) {
                                StringTokenizer tokens = new StringTokenizer(line, "$");
                                question.add(tokens.nextToken());
                                opt1.add(tokens.nextToken());
                                opt2.add(tokens.nextToken());
                                opt3.add(tokens.nextToken());
                                opt4.add(tokens.nextToken());
                                answer.add(tokens.nextToken());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return "true";
                }
            } catch (AmazonClientException e){
                mcqActivity.showSnackBar("Network connection error!!", "csv");
                return "error";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            if(pbMCQ != null)
                pbMCQ.setVisibility(View.GONE);
            if(result.equals("true")) {
                setQuestion(questionNumber);
            } else if (result.equals("false")){
                context.finish();
            }
        }
    }


    public static class UpdateMCQResult extends AsyncTask<DynamoDBMapper, Void, Boolean> {

        CountValuesDO countValuesDO = new CountValuesDO();
        String fileName;

        UpdateMCQResult(CountValuesDO countValuesDO, String fileName){
            this.fileName = fileName;
            this.countValuesDO = countValuesDO;
        }

        @Override
        protected void onPreExecute() {
            if(pbMCQ != null)
                pbMCQ.setVisibility(View.VISIBLE);

            countValuesDO.setEmailId(mcqActivity.email);
            countValuesDO.setPhone(mcqActivity.phone);

            countValuesDO.setVideoCounts(countValuesDO.getVideoCounts());

            Map<String, List<Integer>> mcq = countValuesDO.getMcqCounts();
            Map<String, List<Integer>> mcq_count = new HashMap<>();
            Set mkeys = mcq.keySet();
            for (Object key1 : mkeys) {
                String key = (String) key1;
                List<Integer> list = new ArrayList<>();
                list.addAll(mcq.get(key));
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
        protected Boolean doInBackground(DynamoDBMapper...dynamoDBMappers) {
            try {
                dynamoDBMappers[0].save(countValuesDO);
                return true;
            } catch (AmazonClientException e){
                mcqActivity.showSnackBar("Network connection error!!", "update");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(pbMCQ != null)
                pbMCQ.setVisibility(View.GONE);
            if(result) {
                correctAnswer = 0;
                mcqActivity.finish();
            }
        }
    }


    public static class GetCountValues extends AsyncTask<String, Void, Boolean>{

        DynamoDBMapper dynamoDBMapper;
        CountValuesDO countValuesDO = new CountValuesDO();

        GetCountValues(DynamoDBMapper dynamoDBMapper){
            this.dynamoDBMapper = dynamoDBMapper;
        }

        @Override
        protected void onPreExecute() {
            if(pbMCQ != null){
                pbMCQ.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                countValuesDO = dynamoDBMapper.load(CountValuesDO.class, strings[0], strings[1]);
                return true;
            } catch (AmazonClientException e){
                mcqActivity.showSnackBar("Network connection error!!", "count");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(pbMCQ != null){
                pbMCQ.setVisibility(View.GONE);
            }
            if (result) {
                String fileName = mcqActivity.subkey.replace("_", " ");
                fileName = fileName.substring(0,fileName.indexOf("."));
                new UpdateMCQResult(countValuesDO, fileName).execute(dynamoDBMapper);
            }
        }
    }

}
