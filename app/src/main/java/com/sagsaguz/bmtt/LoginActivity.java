package com.sagsaguz.bmtt;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
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
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.GetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesResult;
import com.amazonaws.services.sns.model.InvalidParameterException;
import com.amazonaws.services.sns.model.NotFoundException;
import com.amazonaws.services.sns.model.SetEndpointAttributesRequest;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.sagsaguz.bmtt.services.FirebaseDispatcher;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.BmttAdminsDO;
import com.sagsaguz.bmtt.utils.BmttUsersDO;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.SendEMail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmailAddress, etPassword, etEmpty;
    private TextView tvForgotPassword;
    private Button btnLogin;
    private ScrollView scrollView;
    private static ProgressBar pbLogin;
    private static ImageView ivLoadingCircle;
    private static RelativeLayout rlLogoSpace, rlLoginSpace;
    private static ConstraintLayout clLogin;

    private static Context context;
    private static LoginActivity loginActivity;
    private static AnimationSet animationSet;
    private static SharedPreferences userPreferences;

    private static View fView;
    private static AWSProvider awsProvider;
    private static AmazonDynamoDBClient dynamoDBClient;

    private static String userType = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        loginActivity = LoginActivity.this;

        etEmailAddress = findViewById(R.id.etEmailAddress);
        etPassword = findViewById(R.id.etPassword);
        etEmpty = findViewById(R.id.etEmpty);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnLogin= findViewById(R.id.btnLogin);

        scrollView = findViewById(R.id.scrollView);
        scrollView.setVerticalScrollBarEnabled(false);

        pbLogin = findViewById(R.id.pbLogin);
        pbLogin.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
        pbLogin.setVisibility(View.GONE);

        ivLoadingCircle = findViewById(R.id.ivLoadingCircle);

        rlLogoSpace = findViewById(R.id.rlLogoSpace);
        rlLoginSpace = findViewById(R.id.rlLoginSpace);

        userPreferences = getSharedPreferences("USERDETAILS", MODE_PRIVATE);

        context = LoginActivity.this;
        clLogin = findViewById(R.id.clLogin);

        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
        alpha.setDuration(1000);

        RotateAnimation rotate = new RotateAnimation(
                0, 359,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setDuration(1000);
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                translateUP(rlLogoSpace);
                translateDown(rlLoginSpace);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        animationSet = new AnimationSet(true);
        animationSet.addAnimation(alpha);
        animationSet.addAnimation(rotate);
        animationSet.setFillAfter(true);

        awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginActivity.basicSnackBar("Make sure you are connected to internet.");
                new PasswordRecovery().execute(etEmailAddress.getText().toString());
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fView = view;
                if(fView != null) {
                    login(fView);
                }
            }
        });

    }

    private void login(View view){
        etEmpty.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if(TextUtils.isEmpty(etEmailAddress.getText().toString()) ||
                TextUtils.isEmpty(etPassword.getText().toString())){
            basicSnackBar("Please fill your login details.");
        } else {
            new validateUser().execute(etEmailAddress.getText().toString().trim(), etPassword.getText().toString());
        }
    }

    private void basicSnackBar(String message){
        Snackbar snackbar = Snackbar.make(clLogin, message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        snackbar.show();
    }

    private static void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(clLogin, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (fView != null) {
                            loginActivity.login(fView);
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

    private void translateUP(View view){
        TranslateAnimation translate = new TranslateAnimation( 0, 0 , 0, -500 );
        translate.setDuration(1000);
        translate.setFillAfter( true );
        view.startAnimation(translate);
    }

    private void translateDown(View view){
        TranslateAnimation translate = new TranslateAnimation( 0, 0 , 0, 1500 );
        translate.setDuration(1000);
        translate.setFillAfter( true );
        view.startAnimation(translate);
        translate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if(userType.equals("user")) {
                    startActivity(new Intent(getBaseContext(), IntroActivity.class));
                } else if(userType.equals("admin")){
                    Intent intent = new Intent(getBaseContext(), MainBranchActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getBaseContext(), MainBranchActivity.class);
                    startActivity(intent);
                }
                finish();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private static class validateUser extends AsyncTask<String, Void, Boolean> {

        Boolean exception, expired;
        String uEmail;
        Date currentDate, expiryDate;
        SimpleDateFormat df;

        @Override
        protected void onPreExecute() {
            if (pbLogin !=null && pbLogin.getVisibility()==View.GONE)
                pbLogin.setVisibility(View.VISIBLE);
            expired = false;
            exception = false;
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
        protected Boolean doInBackground(String... strings) {

            try {
                ScanRequest request = new ScanRequest().withTableName(Config.USERSTABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> userRows = response.getItems();
                for (Map<String, AttributeValue> map : userRows) {
                    try {
                        if (map.get("emailId").getS().equals(strings[0]) || map.get("phone").getS().equals(strings[0])) {
                            if (map.get("password").getS().equals(strings[1])) {
                                uEmail = map.get("emailId").getS();
                                try {
                                    expiryDate = df.parse(map.get("expiryDate").getS());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                long remaining = expiryDate.getTime() - currentDate.getTime();
                                int remainingDays = (int) TimeUnit.DAYS.convert(remaining, TimeUnit.MILLISECONDS);
                                if (remainingDays < 0){
                                    expired = true;
                                    return false;
                                } else {
                                    userType = "user";
                                    SharedPreferences.Editor editor = userPreferences.edit();
                                    editor.putString("LOGIN", "login");
                                    editor.putString("EMAIL", map.get("emailId").getS());
                                    editor.putString("PHONE", map.get("phone").getS());
                                    editor.putString("NAME", map.get("firstName").getS() + " " + map.get("lastName").getS());
                                    editor.putString("USERTYPE", "user");
                                    editor.putString("CENTRE", map.get("centre").getS());
                                    editor.apply();
                                    return true;
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        Log.d("number_format_exception", e.getMessage());
                    }
                }

                request = new ScanRequest().withTableName(Config.ADMINTABLENAME);
                response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> adminRows = response.getItems();
                for (Map<String, AttributeValue> map : adminRows) {
                    try {
                        if (map.get("emailId").getS().equals(strings[0]) || map.get("phone").getS().equals(strings[0])) {
                            if (map.get("password").getS().equals(strings[1])) {
                                if(strings[0].equals(Config.SUPERADMIN) || strings[0].equals(Config.SAPHONE)){
                                    uEmail = map.get("emailId").getS();
                                    userType = "SuperAdmin";
                                    SharedPreferences.Editor editor = userPreferences.edit();
                                    editor.putString("LOGIN", "login");
                                    editor.putString("EMAIL", Config.SUPERADMIN);
                                    editor.putString("USERTYPE", "SuperAdmin");
                                    editor.putString("CENTRE", "All users");
                                    editor.apply();
                                    return true;
                                } else {
                                    userType = "admin";
                                    //centre = map.get("centre").getS();
                                    SharedPreferences.Editor editor = userPreferences.edit();
                                    editor.putString("LOGIN", "login");
                                    editor.putString("EMAIL", map.get("emailId").getS());
                                    editor.putString("USERTYPE", "admin");
                                    editor.putString("CENTRE", map.get("centre").getS());
                                    editor.putString("CCODE", map.get("centerCode").getS());
                                    editor.apply();
                                    return true;
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        Log.d("number_format_exception", e.getMessage());
                    }
                }
            } catch (AmazonClientException e){
                exception = true;
                showSnackBar("Network connection error!!");
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean loginResult) {
            if (pbLogin !=null && pbLogin.getVisibility()==View.VISIBLE)
                pbLogin.setVisibility(View.GONE);
            if(!exception) {
                if (!expired) {
                    if (loginResult) {
                        switch (userType) {
                            case "user":
                                new SetNotificationARN().execute(uEmail);
                                break;
                            case "SuperAdmin":
                                new SetAdminNotificationARN().execute(Config.SUPERADMIN);
                                break;
                            default:
                                ivLoadingCircle.startAnimation(animationSet);
                                break;
                        }
                    } else {
                        loginActivity.basicSnackBar("Please check your login details and try again");
                    }
                } else {
                    loginActivity.basicSnackBar("Your account is expired\nPlease contact centre for more details.");
                }
            }
        }
    }

    private static class SetNotificationARN extends AsyncTask<String, Void, Boolean>{

        BmttUsersDO bmttUsersDO = new BmttUsersDO();
        BmttUsersDO bmttUsersDOSet = new BmttUsersDO();
        AWSProvider awsProvider = new AWSProvider();
        DynamoDBMapper dynamoDBMapper;
        DynamoDBQueryExpression<BmttUsersDO> queryExpression;
        PaginatedQueryList result;

        @Override
        protected void onPreExecute() {
            if (pbLogin !=null && pbLogin.getVisibility()==View.GONE)
                pbLogin.setVisibility(View.VISIBLE);
            dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(loginActivity));
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                    .build();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                bmttUsersDO.setEmailId(strings[0]);
                queryExpression = new DynamoDBQueryExpression<BmttUsersDO>()
                        .withHashKeyValues(bmttUsersDO)
                        .withConsistentRead(false);
                result = dynamoDBMapper.query(BmttUsersDO.class, queryExpression);

                Gson gson = new Gson();
                for (int i = 0; i < result.size(); i++) {
                    String jsonFormOfItem = gson.toJson(result.get(i));
                    bmttUsersDO = gson.fromJson(jsonFormOfItem, BmttUsersDO.class);
                }
            } catch (AmazonClientException e){
                return false;
            }

            AmazonSNSClient client = new AmazonSNSClient(awsProvider.getCredentialsProvider(loginActivity));
            String endpointArn = bmttUsersDO.getNotificationARN();
            String token = FirebaseInstanceId.getInstance().getToken();
            boolean updateNeeded = false;
            boolean createNeeded = (endpointArn == null || endpointArn.equals("null"));
            if (createNeeded) {
                endpointArn = loginActivity.createEndpoint(strings[0]);
                createNeeded = false;
            }
            try {
                GetEndpointAttributesRequest geaReq = new GetEndpointAttributesRequest().withEndpointArn(endpointArn);
                GetEndpointAttributesResult geaRes = client.getEndpointAttributes(geaReq);

                updateNeeded = !geaRes.getAttributes().get("Token").equals(token)
                        || !geaRes.getAttributes().get("Enabled").equalsIgnoreCase("true");

            } catch (NotFoundException nfe) {
                createNeeded = true;
            }

            if (createNeeded) {
                endpointArn = loginActivity.createEndpoint(strings[0]);
            }

            if (updateNeeded) {
                System.out.println("Updating endpoint " + endpointArn);
                Map<String, String> attribs = new HashMap<>();
                attribs.put("Token", token);
                attribs.put("Enabled", "true");
                SetEndpointAttributesRequest saeReq = new SetEndpointAttributesRequest().withEndpointArn(endpointArn).withAttributes(attribs);
                client.setEndpointAttributes(saeReq);
                SharedPreferences.Editor editor = userPreferences.edit();
                editor.putString("ARN", endpointArn);
                editor.apply();
                return true;
            }

            try {

                bmttUsersDOSet = dynamoDBMapper.load(BmttUsersDO.class, bmttUsersDO.getEmailId(), bmttUsersDO.getPhone());
                bmttUsersDOSet.setNotificationARN(endpointArn);

                /*bmttUsersDOSet.setEmailId(bmttUsersDO.getEmailId());
                bmttUsersDOSet.setPhone(bmttUsersDO.getPhone());
                bmttUsersDOSet.setAddress(bmttUsersDO.getAddress());
                bmttUsersDOSet.setBmttPart1(bmttUsersDO.getBmttPart1());
                bmttUsersDOSet.setBmttPart2(bmttUsersDO.getBmttPart2());
                bmttUsersDOSet.setBmttPart3(bmttUsersDO.getBmttPart3());
                bmttUsersDOSet.setCentre(bmttUsersDO.getCentre());
                bmttUsersDOSet.setCreatedDate(bmttUsersDO.getCreatedDate());
                bmttUsersDOSet.setDob(bmttUsersDO.getDob());
                bmttUsersDOSet.setExpiryDate(bmttUsersDO.getExpiryDate());
                bmttUsersDOSet.setFirstName(bmttUsersDO.getFirstName());
                bmttUsersDOSet.setLastName(bmttUsersDO.getLastName());
                bmttUsersDOSet.setPassword(bmttUsersDO.getPassword());
                bmttUsersDOSet.setProfilePic(bmttUsersDO.getProfilePic());
                bmttUsersDOSet.setNotificationARN(endpointArn);*/
                SharedPreferences.Editor editor = userPreferences.edit();
                editor.putString("ARN", endpointArn);
                editor.apply();
                dynamoDBMapper.save(bmttUsersDOSet);
                return true;
            } catch (AmazonClientException e){
                loginActivity.basicSnackBar("Please check your network connection and try again");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pbLogin !=null && pbLogin.getVisibility()==View.VISIBLE)
                pbLogin.setVisibility(View.GONE);
            if (result) {
                ivLoadingCircle.startAnimation(animationSet);
            }
        }
    }

    private static class SetAdminNotificationARN extends AsyncTask<String, Void, Boolean>{

        BmttAdminsDO bmttAdminsDO = new BmttAdminsDO();
        BmttAdminsDO bmttAdminsDOSet = new BmttAdminsDO();
        AWSProvider awsProvider = new AWSProvider();
        DynamoDBMapper dynamoDBMapper;
        DynamoDBQueryExpression<BmttAdminsDO> queryExpression;
        PaginatedQueryList result;

        @Override
        protected void onPreExecute() {
            if (pbLogin !=null && pbLogin.getVisibility()==View.GONE)
                pbLogin.setVisibility(View.VISIBLE);
            dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(loginActivity));
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                    .build();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                bmttAdminsDO.setEmailId(strings[0]);
                queryExpression = new DynamoDBQueryExpression<BmttAdminsDO>()
                        .withHashKeyValues(bmttAdminsDO)
                        .withConsistentRead(false);
                result = dynamoDBMapper.query(BmttAdminsDO.class, queryExpression);

                Gson gson = new Gson();
                for (int i = 0; i < result.size(); i++) {
                    String jsonFormOfItem = gson.toJson(result.get(i));
                    bmttAdminsDO = gson.fromJson(jsonFormOfItem, BmttAdminsDO.class);
                }
            } catch (AmazonClientException e){
                return false;
            }

            AmazonSNSClient client = new AmazonSNSClient(awsProvider.getCredentialsProvider(loginActivity));
            String endpointArn = bmttAdminsDO.getNotificationARN();
            String token = FirebaseInstanceId.getInstance().getToken();
            boolean updateNeeded = false;
            boolean createNeeded = (endpointArn == null);
            if (createNeeded) {
                endpointArn = loginActivity.createEndpoint(strings[0]);
                createNeeded = false;
            }
            try {
                GetEndpointAttributesRequest geaReq = new GetEndpointAttributesRequest().withEndpointArn(endpointArn);
                GetEndpointAttributesResult geaRes = client.getEndpointAttributes(geaReq);

                updateNeeded = !geaRes.getAttributes().get("Token").equals(token)
                        || !geaRes.getAttributes().get("Enabled").equalsIgnoreCase("true");

            } catch (NotFoundException nfe) {
                createNeeded = true;
            }

            if (createNeeded) {
                endpointArn = loginActivity.createEndpoint(strings[0]);
            }

            if (updateNeeded) {
                System.out.println("Updating endpoint " + endpointArn);
                Map<String, String> attribs = new HashMap<>();
                attribs.put("Token", token);
                attribs.put("Enabled", "true");
                SetEndpointAttributesRequest saeReq = new SetEndpointAttributesRequest().withEndpointArn(endpointArn).withAttributes(attribs);
                client.setEndpointAttributes(saeReq);
                return true;
            }

            try {
                bmttAdminsDOSet = dynamoDBMapper.load(BmttAdminsDO.class, bmttAdminsDO.getEmailId(), bmttAdminsDO.getPhone());
                bmttAdminsDOSet.setNotificationARN(endpointArn);

                SharedPreferences.Editor editor = userPreferences.edit();
                editor.putString("ARN", endpointArn);
                editor.apply();
                dynamoDBMapper.save(bmttAdminsDOSet);
                return true;
            } catch (AmazonClientException e){
                loginActivity.basicSnackBar("Please check your network connection and try again");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pbLogin !=null && pbLogin.getVisibility()==View.VISIBLE)
                pbLogin.setVisibility(View.GONE);
            if (result) {
                ivLoadingCircle.startAnimation(animationSet);
            }
        }
    }

    private String createEndpoint(String customPushData) {

        String endpointArn = null;
        try {
            String platformApplicationArn = Config.PLATFORMAPPLICATONARN;
            AmazonSNSClient pushClient = new AmazonSNSClient(awsProvider.getCredentialsProvider(loginActivity));
            CreatePlatformEndpointRequest platformEndpointRequest = new CreatePlatformEndpointRequest();
            platformEndpointRequest.setCustomUserData(customPushData);
            platformEndpointRequest.setToken(FirebaseInstanceId.getInstance().getToken());
            platformEndpointRequest.setPlatformApplicationArn(platformApplicationArn);
            CreatePlatformEndpointResult cpeRes = pushClient.createPlatformEndpoint(platformEndpointRequest);
            endpointArn = cpeRes.getEndpointArn();
        } catch (InvalidParameterException ipe) {
            String message = ipe.getErrorMessage();
            System.out.println("Exception message: " + message);
            Pattern p = Pattern
                    .compile(".*Endpoint (arn:aws:sns[^ ]+) already exists " +
                            "with the same Token.*");
            Matcher m = p.matcher(message);
            if (m.matches()) {
                endpointArn = m.group(1);
            } else {
                throw ipe;
            }
        }
        return endpointArn;
    }

    private static class PasswordRecovery extends AsyncTask<String, Void, Boolean> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(loginActivity, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Sending password, please wait.");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                ScanRequest request = new ScanRequest().withTableName(Config.USERSTABLENAME);
                ScanResult response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> userRows = response.getItems();
                for (Map<String, AttributeValue> map : userRows) {
                    try {
                        if (map.get("emailId").getS().equals(strings[0])) {
                            loginActivity.sendEmail(strings[0], map.get("password").getS());
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        Log.d("number_format_exception", e.getMessage());
                    }
                }

                request = new ScanRequest().withTableName(Config.ADMINTABLENAME);
                response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> adminRows = response.getItems();
                for (Map<String, AttributeValue> map : adminRows) {
                    try {
                        if (map.get("emailId").getS().equals(strings[0])) {
                            loginActivity.sendEmail(strings[0], map.get("password").getS());
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        Log.d("number_format_exception", e.getMessage());
                    }
                }
            } catch (AmazonClientException e){
                showSnackBar("Network connection error!!");
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean loginResult) {
            progressDialog.dismiss();
            if (!loginResult){
                loginActivity.basicSnackBar("Please enter your registered email address and try again.");
            } else {
                loginActivity.basicSnackBar("Password sent to your email.");
            }
        }
    }

    private void sendEmail(String email, String password) {
        String subject = "Password recovery from BMTT";
        String message = "Your login credentials are,\nLoginId : "+email+"\nPassword : "+password;
        //Creating SendMail object
        SendEMail sm = new SendEMail(LoginActivity.this, email, subject, message);
        sm.execute();
        basicSnackBar("Email sent");
    }

}
