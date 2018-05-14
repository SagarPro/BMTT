package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.BmttUsersDO;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.CountValuesDO;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static CircleImageView ivProfilePic;
    @SuppressLint("StaticFieldLeak")
    private static ProgressBar pbProfile;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvFullName, tvCentre, tvEmailAddress, tvPassword, tvPhone, tvDOB, tvEnrolledIn, tvAddress;
    private RelativeLayout rlProfile;

    @SuppressLint("StaticFieldLeak")
    private static ImageView ivTimeCompletion, ivCourseCompletion;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvAdmissionDate, tvExpiryDate;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvCourseCompletion, tvDaysLeft;
    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout rlTimeCompletion, rlCourseCompletion;

    private static int totalVideosCount;

    private String email, phone;

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    @SuppressLint("StaticFieldLeak")
    private static ProfileActivity profileActivity;

    private static DynamoDBMapper dynamoDBMapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        profileActivity = ProfileActivity.this;
        context = ProfileActivity.this;

        Intent intent = getIntent();
        email = intent.getStringExtra("EMAIL");
        phone = intent.getStringExtra("PHONE");
        totalVideosCount = intent.getIntExtra("TOTALCOUNT", 0);

        rlProfile = findViewById(R.id.rlProfile);

        AWSProvider awsProvider = new AWSProvider();
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        /*AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
        alpha.setDuration(1000);
        rlProfile.startAnimation(alpha);*/

        pbProfile = findViewById(R.id.pbProfile);
        pbProfile.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvFullName = findViewById(R.id.tvFullName);
        tvCentre = findViewById(R.id.tvCentre);
        tvEmailAddress = findViewById(R.id.tvEmailAddress);
        tvPassword = findViewById(R.id.tvPassword);
        tvPhone = findViewById(R.id.tvPhone);
        tvDOB = findViewById(R.id.tvDOB);
        tvEnrolledIn = findViewById(R.id.tvEnrolledIn);
        tvAddress = findViewById(R.id.tvAddress);

        ivTimeCompletion = findViewById(R.id.ivTimeCompletion);
        ivTimeCompletion.setVisibility(View.INVISIBLE);
        ivCourseCompletion = findViewById(R.id.ivCourseCompletion);
        ivCourseCompletion.setVisibility(View.INVISIBLE);

        tvDaysLeft = findViewById(R.id.tvDaysLeft);
        tvAdmissionDate = findViewById(R.id.tvAdmissionDate);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        tvCourseCompletion = findViewById(R.id.tvCourseCompletion);

        rlTimeCompletion = findViewById(R.id.rlTimeCompletion);
        rlCourseCompletion = findViewById(R.id.rlCourseCompletion);

        new ShowProfile().execute(email, phone);

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(rlProfile, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new ShowProfile().execute(email, phone);
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

    private static void setMilestoneValues(CountValuesDO countValuesDO){

        Map<String, Integer> video_count = new HashMap<>();

        if(countValuesDO == null){
            Toast.makeText(context, "Not completed any chapter", Toast.LENGTH_SHORT).show();
        } else {
            video_count = countValuesDO.getVideoCounts();
        }

        int courseCompletion = ((video_count.size() * 100) / totalVideosCount);
        String cc = courseCompletion + " %";
        tvCourseCompletion.setText(cc);
        if(courseCompletion >= 50){
            tvCourseCompletion.setTextColor(context.getResources().getColor(R.color.colorAccent));
        }

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
            sDate = sdf.parse(tvAdmissionDate.getText().toString());
            eDate = sdf.parse(tvExpiryDate.getText().toString());
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

        if((numOfDays/2) >= remainingDays){
            tvDaysLeft.setTextColor(context.getResources().getColor(R.color.colorAccent));
        }

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


    }

    private static void setProfileDetails(BmttUsersDO bmttUsersDO){
        Picasso.with(context).load(bmttUsersDO.getProfilePic()).into(ivProfilePic);
        String fullName = bmttUsersDO.getFirstName() + " " + bmttUsersDO.getLastName();
        tvFullName.setText(fullName);
        tvCentre.setText(bmttUsersDO.getCentre());
        tvEmailAddress.setText(bmttUsersDO.getEmailId());
        tvPassword.setText(bmttUsersDO.getPassword());
        tvPhone.setText(bmttUsersDO.getPhone());
        tvDOB.setText(bmttUsersDO.getDob());
        String enrolledIn = "";
        if(bmttUsersDO.getBmttPart1())
            enrolledIn = "BMTT - Part 1";
        if(bmttUsersDO.getBmttPart2())
            enrolledIn = enrolledIn + "\nBMTT - Part 2";
        if(bmttUsersDO.getBmttPart3())
            enrolledIn = enrolledIn + "\nBMTT - Part 3";
        if(enrolledIn.equals(""))
            enrolledIn = "not enrolled in any parts";
        tvEnrolledIn.setText(enrolledIn);
        tvAddress.setText(bmttUsersDO.getAddress());
        tvAdmissionDate.setText(bmttUsersDO.getCreatedDate());
        tvExpiryDate.setText(bmttUsersDO.getExpiryDate());
        new GetVideosCount().execute(tvEmailAddress.getText().toString(), tvPhone.getText().toString());
    }


    private static class ShowProfile extends AsyncTask<String, Void, BmttUsersDO> {

        BmttUsersDO bmttUsersDO = new BmttUsersDO();

        @Override
        protected void onPreExecute() {
            if (pbProfile !=null)
                pbProfile.setVisibility(View.VISIBLE);
        }

        @Override
        protected BmttUsersDO doInBackground(String... strings) {
            try {
                bmttUsersDO = dynamoDBMapper.load(BmttUsersDO.class, strings[0], strings[1]);
                return bmttUsersDO;
            } catch (AmazonClientException e){
                profileActivity.showSnackBar("Network connection error!!");
                return null;
            }
        }

        @Override
        protected void onPostExecute(BmttUsersDO bmttUsersDO) {
            if (pbProfile !=null)
                pbProfile.setVisibility(View.GONE);
            if(bmttUsersDO != null)
                setProfileDetails(bmttUsersDO);
        }
    }

    private static class GetVideosCount extends AsyncTask<String, Void, CountValuesDO> {

        CountValuesDO countValuesDO = new CountValuesDO();

        @Override
        protected void onPreExecute() {
            if (pbProfile !=null)
                pbProfile.setVisibility(View.VISIBLE);
        }

        @Override
        protected CountValuesDO doInBackground(String... strings) {
            try {
                countValuesDO = dynamoDBMapper.load(CountValuesDO.class, strings[0], strings[1]);
                return countValuesDO;
            } catch (AmazonClientException e){
                profileActivity.showSnackBar("Network connection error!!");
                return null;
            }
        }

        @Override
        protected void onPostExecute(CountValuesDO countValuesDO) {
            if (pbProfile !=null)
                pbProfile.setVisibility(View.GONE);
            setMilestoneValues(countValuesDO);
        }
    }

}
