package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.bumptech.glide.Glide;
import com.payumoney.core.PayUmoneyConfig;
import com.payumoney.core.PayUmoneyConstants;
import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;
import com.payumoney.sdkui.ui.utils.ResultModel;
import com.sagsaguz.bmtt.adapter.FileSub_StuAdapter;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.AmountDO;
import com.sagsaguz.bmtt.utils.BmttAdminsDO;
import com.sagsaguz.bmtt.utils.BmttUsersDO;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.CountValuesDO;
import com.sagsaguz.bmtt.utils.FileSubmissionDO;
import com.sagsaguz.bmtt.utils.NotificationsDO;
import com.sagsaguz.bmtt.utils.PaymentDO;
import com.sagsaguz.bmtt.utils.SendEMail;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private static int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_SEND_SMS = 2 ;
    private static final int REQUEST_STORAGE = 3;

    private static CircleImageView ivProfilePic;
    @SuppressLint("StaticFieldLeak")
    private static ProgressBar pbProfile;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvFullName, tvCentre, tvEmailAddress, tvPassword, tvPhone, tvDOB, tvEnrolledIn1, tvEnrolledIn2, tvEnrolledIn3, tvAddress, tvUserId;
    private RelativeLayout rlProfile;

    private CheckBox cbSubmitted1, cbSubmitted2, cbSubmitted3, cbSubmitted4;
    private CheckBox cbReceived1, cbReceived2, cbReceived3, cbReceived4;
    private CheckBox cbEvaluated1, cbEvaluated2, cbEvaluated3, cbEvaluated4;

    private TextView tvPayment1, tvPayment2, tvPayment3, tvTotal, tvTotalPaid;
    private Button btnPay1, btnPay2, btnPay3;
    private RelativeLayout rlPayment2, rlPayment3;
    private String pay1, pay2, pay3, launchPay;

    private Map<String, String> amount, status;
    private List<String> date;
    private String payDate;
    int payingAmt = 0;

    String filePath = "null";
    static String profilePicPath;
    ProgressDialog dialog;

    @SuppressLint("StaticFieldLeak")
    private static ImageView ivTimeCompletion, ivCourseCompletion;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvAdmissionDate, tvExpiryDate;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvCourseCompletion, tvDaysLeft;
    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout rlTimeCompletion, rlCourseCompletion;
    private ImageView ivMarksSheet;

    private static int totalVideosCount;
    private int totalPaid = 0;

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
        totalVideosCount = intent.getIntExtra("TOTALCOUNT", 138);

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
        tvUserId = findViewById(R.id.tvUserId);
        tvFullName = findViewById(R.id.tvFullName);
        tvCentre = findViewById(R.id.tvCentre);
        tvEmailAddress = findViewById(R.id.tvEmailAddress);
        tvPassword = findViewById(R.id.tvPassword);
        tvPhone = findViewById(R.id.tvPhone);
        tvDOB = findViewById(R.id.tvDOB);
        tvEnrolledIn1 = findViewById(R.id.tvEnrolledIn1);
        tvEnrolledIn2 = findViewById(R.id.tvEnrolledIn2);
        tvEnrolledIn3 = findViewById(R.id.tvEnrolledIn3);
        tvAddress = findViewById(R.id.tvAddress);

        ivMarksSheet = findViewById(R.id.ivMarksSheet);
        ivMarksSheet.setOnClickListener(this);

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

        //lvFileSub = findViewById(R.id.lvFileSub);

        cbSubmitted1 = findViewById(R.id.cbSubmitted1);
        cbReceived1 = findViewById(R.id.cbReceived1);
        cbEvaluated1 = findViewById(R.id.cbEvaluated1);

        cbSubmitted2 = findViewById(R.id.cbSubmitted2);
        cbReceived2 = findViewById(R.id.cbReceived2);
        cbEvaluated2 = findViewById(R.id.cbEvaluated2);

        cbSubmitted3 = findViewById(R.id.cbSubmitted3);
        cbReceived3 = findViewById(R.id.cbReceived3);
        cbEvaluated3 = findViewById(R.id.cbEvaluated3);

        cbSubmitted4 = findViewById(R.id.cbSubmitted4);
        cbReceived4 = findViewById(R.id.cbReceived4);
        cbEvaluated4 = findViewById(R.id.cbEvaluated4);

        tvPayment1 = findViewById(R.id.tvPayment1);
        tvPayment2 = findViewById(R.id.tvPayment2);
        tvPayment3 = findViewById(R.id.tvPayment3);

        tvTotal = findViewById(R.id.tvTotal);
        tvTotalPaid = findViewById(R.id.tvTotalPaid);

        btnPay1 = findViewById(R.id.btnPay1);
        btnPay1.setOnClickListener(this);
        btnPay2 = findViewById(R.id.btnPay2);
        btnPay2.setOnClickListener(this);
        btnPay3 = findViewById(R.id.btnPay3);
        btnPay3.setOnClickListener(this);

        rlPayment2 = findViewById(R.id.rlPayment2);
        rlPayment3 = findViewById(R.id.rlPayment3);

        new PaymentDetails().execute(email, phone);

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storagePermissionCheck();
            }
        });

        cbSubmitted1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UpdateFileSub().execute(1);
            }
        });

        cbSubmitted2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UpdateFileSub().execute(2);
            }
        });

        cbSubmitted3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UpdateFileSub().execute(3);
            }
        });

        cbSubmitted4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UpdateFileSub().execute(4);
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

    public void paySnackBar(String message){
        Snackbar snackbar = Snackbar.make(rlProfile, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new UpdatePayStatus().execute(payDate);
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

        totalVideosCount = 138;
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
        if(remainingDays < 0) {
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

    private void setUpPayment(PaymentDO payment){

        amount = payment.getInstallments();
        status = payment.getPayment();

        date = new ArrayList<>(amount.keySet());
        List<Date> dateList = new ArrayList<>();
        for (int i=0; i<date.size(); i++){
            dateList.add(stringToDate(date.get(i)));
        }
        Collections.sort(dateList, new Comparator<Date>() {
            public int compare(Date date1, Date date2) {
                return date1.compareTo(date2);
            }
        });
        date.clear();
        for (int i=0; i<dateList.size(); i++){
            date.add(dateToString(dateList.get(i)));
        }

        if (amount.size() == 1){
            rlPayment2.setVisibility(View.GONE);
            rlPayment3.setVisibility(View.GONE);
            tvPayment1.setText(date.get(0) + " - Rs. " + amount.get(date.get(0)));
            pay1 = amount.get(date.get(0));
            tvTotal.setText("Rs. " + pay1);
            if (status.get(date.get(0)).equals("paid")){
                btnPay1.setText("Paid");
                btnPay1.setBackgroundColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay1));
            }
            tvTotalPaid.setText("Rs. " + totalPaid);
        } else if (amount.size() == 2){
            rlPayment3.setVisibility(View.GONE);
            tvPayment1.setText(date.get(0) + " - Rs. " + amount.get(date.get(0)));
            tvPayment2.setText(date.get(1) + " - Rs. " + amount.get(date.get(1)));
            pay1 = amount.get(date.get(0));
            pay2 = amount.get(date.get(1));
            tvTotal.setText("Rs. " + (Integer.parseInt(pay1) + Integer.parseInt(pay2)));
            if (status.get(date.get(0)).equals("paid")){
                btnPay1.setText("Paid");
                btnPay1.setBackgroundColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay1));
            }
            if (status.get(date.get(1)).equals("paid")){
                btnPay2.setText("Paid");
                btnPay2.setBackgroundColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay2));
            }
            tvTotalPaid.setText("Rs. " + totalPaid);
        } else {
            tvPayment1.setText(date.get(0) + " - Rs. " + amount.get(date.get(0)));
            tvPayment2.setText(date.get(1) + " - Rs. " + amount.get(date.get(1)));
            tvPayment3.setText(date.get(2) + " - Rs. " + amount.get(date.get(2)));
            pay1 = amount.get(date.get(0));
            pay2 = amount.get(date.get(1));
            pay3 = amount.get(date.get(2));
            tvTotal.setText("Rs. " + (Integer.parseInt(pay1) + Integer.parseInt(pay2) + Integer.parseInt(pay3)));
            if (status.get(date.get(0)).equals("paid")){
                btnPay1.setText("Paid");
                btnPay1.setBackgroundColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay1));
            }
            if (status.get(date.get(1)).equals("paid")){
                btnPay2.setText("Paid");
                btnPay2.setBackgroundColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay2));
            }
            if (status.get(date.get(2)).equals("paid")){
                btnPay3.setText("Paid");
                btnPay3.setBackgroundColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay3));
            }
            tvTotalPaid.setText("Rs. " + totalPaid);
        }

        new LoadFileSub().execute(email, phone);
    }

    private void fileSub(FileSubmissionDO fileSubmissionDO){

        for (int i=0; i<4; i++){
            switch (i){
                case 0:
                    switch (fileSubmissionDO.getFile1()){
                        case "submitted":
                            cbSubmitted1.setChecked(true);
                            cbReceived1.setChecked(false);
                            cbEvaluated1.setChecked(false);
                            break;
                        case "received":
                            cbSubmitted1.setChecked(true);
                            cbReceived1.setChecked(true);
                            cbEvaluated1.setChecked(false);
                            break;
                        case "evaluated":
                            cbSubmitted1.setChecked(true);
                            cbReceived1.setChecked(true);
                            cbEvaluated1.setChecked(true);
                            break;
                        case "not":
                            cbSubmitted1.setChecked(false);
                            cbReceived1.setChecked(false);
                            cbEvaluated1.setChecked(false);
                            break;
                    }
                    break;
                case 1:
                    switch (fileSubmissionDO.getFile2()){
                        case "submitted":
                            cbSubmitted2.setChecked(true);
                            cbReceived2.setChecked(false);
                            cbEvaluated2.setChecked(false);
                            break;
                        case "received":
                            cbSubmitted2.setChecked(true);
                            cbReceived2.setChecked(true);
                            cbEvaluated2.setChecked(false);
                            break;
                        case "evaluated":
                            cbSubmitted2.setChecked(true);
                            cbReceived2.setChecked(true);
                            cbEvaluated2.setChecked(true);
                            break;
                        case "not":
                            cbSubmitted2.setChecked(false);
                            cbReceived2.setChecked(false);
                            cbEvaluated2.setChecked(false);
                            break;
                    }
                    break;
                case 2:
                    switch (fileSubmissionDO.getFile3()){
                        case "submitted":
                            cbSubmitted3.setChecked(true);
                            cbReceived3.setChecked(false);
                            cbEvaluated3.setChecked(false);
                            break;
                        case "received":
                            cbSubmitted3.setChecked(true);
                            cbReceived3.setChecked(true);
                            cbEvaluated3.setChecked(false);
                            break;
                        case "evaluated":
                            cbSubmitted3.setChecked(true);
                            cbReceived3.setChecked(true);
                            cbEvaluated3.setChecked(true);
                            break;
                        case "not":
                            cbSubmitted3.setChecked(false);
                            cbReceived3.setChecked(false);
                            cbEvaluated3.setChecked(false);
                            break;
                    }
                    break;
                case 3:
                    switch (fileSubmissionDO.getFile4()){
                        case "submitted":
                            cbSubmitted4.setChecked(true);
                            cbReceived4.setChecked(false);
                            cbEvaluated4.setChecked(false);
                            break;
                        case "received":
                            cbSubmitted4.setChecked(true);
                            cbReceived4.setChecked(true);
                            cbEvaluated4.setChecked(false);
                            break;
                        case "evaluated":
                            cbSubmitted4.setChecked(true);
                            cbReceived4.setChecked(true);
                            cbEvaluated4.setChecked(true);
                            break;
                        case "not":
                            cbSubmitted4.setChecked(false);
                            cbReceived4.setChecked(false);
                            cbEvaluated4.setChecked(false);
                            break;
                    }
                    break;
            }

        }

        new ShowProfile().execute(email, phone);
    }

    private Date stringToDate(String date){
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
        try {
            return format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String dateToString(Date date){
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
        return format.format(date);
    }

    private String currentDate(){
        Calendar myCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        return df.format(myCalendar.getTime());
    }

    private static void setProfileDetails(BmttUsersDO bmttUsersDO){
        //Picasso.with(context).load(bmttUsersDO.getProfilePic()).into(ivProfilePic);
        profilePicPath = bmttUsersDO.getProfilePic();
        Glide.with(profileActivity).load(bmttUsersDO.getProfilePic()).into(ivProfilePic);
        String fullName = bmttUsersDO.getFirstName() + " " + bmttUsersDO.getLastName();
        tvFullName.setText(fullName);
        tvUserId.setText(bmttUsersDO.getUserId());
        tvCentre.setText(bmttUsersDO.getCentre());
        tvEmailAddress.setText(bmttUsersDO.getEmailId());
        tvPassword.setText(bmttUsersDO.getPassword());
        tvPhone.setText(bmttUsersDO.getPhone());
        tvDOB.setText(bmttUsersDO.getDob());
        //String enrolledIn = "";
        if(bmttUsersDO.getBmttPart1())
            tvEnrolledIn1.setTextColor(profileActivity.getResources().getColor(R.color.green));
            //enrolledIn = "BMTT - Part 1";
        if(bmttUsersDO.getBmttPart2())
            tvEnrolledIn2.setTextColor(profileActivity.getResources().getColor(R.color.green));
            //enrolledIn = enrolledIn + "\nBMTT - Part 2";
        if(bmttUsersDO.getBmttPart3())
            tvEnrolledIn3.setTextColor(profileActivity.getResources().getColor(R.color.green));
            //enrolledIn = enrolledIn + "\nBMTT - Part 3";
        //if(enrolledIn.equals(""))
            //enrolledIn = "not enrolled in any parts";
        //tvEnrolledIn.setText(enrolledIn);
        tvAddress.setText(bmttUsersDO.getAddress());
        tvAdmissionDate.setText(bmttUsersDO.getCreatedDate());
        tvExpiryDate.setText(bmttUsersDO.getExpiryDate());
        new GetVideosCount().execute(tvEmailAddress.getText().toString(), tvPhone.getText().toString());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivMarksSheet:
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.marks_sheet_dialog, (ViewGroup) findViewById(R.id.rlMarksSheet));
                PopupWindow pw = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                ImageView ivMarksSheet = layout.findViewById(R.id.ivMarksSheet);
                ivMarksSheet.setImageResource(R.drawable.marks_sheet);
                pw.showAtLocation(ivMarksSheet, Gravity.CENTER, 0, 0);

                PhotoViewAttacher pAttacher;
                pAttacher = new PhotoViewAttacher(ivMarksSheet);
                pAttacher.update();
                break;
            case R.id.btnPay1:
                launchPay = "pay1";
                smsPermissionCheck();
                break;
            case R.id.btnPay2:
                launchPay = "pay2";
                smsPermissionCheck();
                break;
            case R.id.btnPay3:
                launchPay = "pay3";
                smsPermissionCheck();
                break;
        }
    }

    private void launchPayment(){
        switch (launchPay){
            case "pay1":
                payDate = date.get(0);
                if (!btnPay1.getText().equals("Paid"))
                    //pay1 = pay1;
                    launchPayUMoneyFlow(amount.get(date.get(0)));
                else Toast.makeText(context, "This amount is paid", Toast.LENGTH_SHORT).show();
                break;
            case "pay2":
                payDate = date.get(1);
                if (!btnPay2.getText().equals("Paid"))
                    //pay1 = pay1;
                    launchPayUMoneyFlow(amount.get(date.get(1)));
                else Toast.makeText(context, "This amount is paid", Toast.LENGTH_SHORT).show();
                break;
            case "pay3":
                payDate = date.get(2);
                if (!btnPay3.getText().equals("Paid"))
                    //pay1 = pay1;
                    launchPayUMoneyFlow(amount.get(date.get(2)));
                else Toast.makeText(context, "This amount is paid", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void launchPayUMoneyFlow(String pay) {

        payingAmt = Integer.parseInt(pay);

        PayUmoneyConfig payUmoneyConfig = PayUmoneyConfig.getInstance();

        //Use this to set your custom text on result screen button
        payUmoneyConfig.setDoneButtonText("Done Button");

        //Use this to set your custom title for the activity
        payUmoneyConfig.setPayUmoneyActivityTitle("Payment");

        PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();

        double amount = 0;
        try {
            amount = Double.parseDouble(pay);

        } catch (Exception e) {
            e.printStackTrace();
        }
        String txnId = System.currentTimeMillis() + "";
        String phone = tvPhone.getText().toString();
        String productName = "BMTT";
        String firstName = tvFullName.getText().toString();
        String email = tvEmailAddress.getText().toString();
        String udf1 = "";
        String udf2 = "";
        String udf3 = "";
        String udf4 = "";
        String udf5 = "";
        String udf6 = "";
        String udf7 = "";
        String udf8 = "";
        String udf9 = "";
        String udf10 = "";

        builder.setAmount(amount)
                .setTxnId(txnId)
                .setPhone(phone)
                .setProductName(productName)
                .setFirstName(firstName)
                .setEmail(email)
                .setsUrl("https://www.payumoney.com/mobileapp/payumoney/success.php")
                .setfUrl("https://www.payumoney.com/mobileapp/payumoney/failure.php")
                .setUdf1(udf1)
                .setUdf2(udf2)
                .setUdf3(udf3)
                .setUdf4(udf4)
                .setUdf5(udf5)
                .setUdf6(udf6)
                .setUdf7(udf7)
                .setUdf8(udf8)
                .setUdf9(udf9)
                .setUdf10(udf10)
                /*.setIsDebug(true)
                .setKey("rjQUPktU")
                .setMerchantId("4934580");*/
                .setIsDebug(false)
                .setKey("7oYrm7US")
                .setMerchantId("5761531");

        try {
            PayUmoneySdkInitializer.PaymentParam mPaymentParams = builder.build();
            mPaymentParams = calculateServerSideHashAndInitiatePayment1(mPaymentParams);
            PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams, ProfileActivity.this, R.style.AppTheme_default, false);

        } catch (Exception e) {
            // some exception occurred
            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private PayUmoneySdkInitializer.PaymentParam calculateServerSideHashAndInitiatePayment1(final PayUmoneySdkInitializer.PaymentParam paymentParam) {

        StringBuilder stringBuilder = new StringBuilder();
        HashMap<String, String> params = paymentParam.getParams();
        stringBuilder.append(params.get(PayUmoneyConstants.KEY)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.TXNID)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.AMOUNT)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.PRODUCT_INFO)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.FIRSTNAME)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.EMAIL)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF1)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF2)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF3)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF4)).append("|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF5)).append("||||||");
        //stringBuilder.append("e5iIg1jwi8");
        stringBuilder.append("zbk4YZzVY9");

        String hash = hashCal(stringBuilder.toString());
        paymentParam.setMerchantHash(hash);

        return paymentParam;
    }

    private String hashCal(String str) {
        byte[] hashseq = str.getBytes();
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-512");
            algorithm.reset();
            algorithm.update(hashseq);
            byte messageDigest[] = algorithm.digest();
            for (byte aMessageDigest : messageDigest) {
                String hex = Integer.toHexString(0xFF & aMessageDigest);
                if (hex.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException ignored) {
            Toast.makeText(ProfileActivity.this, "Exception : " + ignored.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return hexString.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {
            TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager
                    .INTENT_EXTRA_TRANSACTION_RESPONSE);

            ResultModel resultModel = data.getParcelableExtra(PayUmoneyFlowManager.ARG_RESULT);

            // Check which object is non-null
            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {
                if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    new UpdatePayStatus().execute(payDate);
                } else {
                    Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show();
                }

            } else if (resultModel != null && resultModel.getError() != null) {
                Toast.makeText(this, "Error : " + resultModel.getError().getTransactionResponse(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Both Objects are Null", Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

                    Uri URI = data.getData();
                    String[] FILE = { MediaStore.Images.Media.DATA };

                    Cursor cursor = null;
                    if (URI != null) {
                        cursor = getContentResolver().query(URI, FILE, null, null, null);
                    }
                    String imageDecode = null;
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int  columnIndex = cursor.getColumnIndex(FILE[0]);
                        imageDecode = cursor.getString(columnIndex);
                        cursor.close();
                    }
                    filePath = imageDecode;
                    ivProfilePic.setImageBitmap(BitmapFactory.decodeFile(imageDecode));
                    uploadProfilePic();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Please try again", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void storagePermissionCheck(){
        int permissionCheck = ContextCompat.checkSelfPermission(ProfileActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCheck == 0) {
            pickProfilePic();
        } else {
            final Dialog dialog2 = new Dialog(ProfileActivity.this);
            dialog2.setContentView(R.layout.permission_dialog);

            TextView dialog_message = dialog2.findViewById(R.id.dialog_message);
            dialog_message.setText("This app needs storage permission for uploading user image. You can allow permissions manually by clicking on settings below.");
            TextView pCancel = dialog2.findViewById(R.id.pCancel);
            TextView pSettings = dialog2.findViewById(R.id.pSettings);
            TextView pOk = dialog2.findViewById(R.id.pOk);
            pCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog2.dismiss();
                }
            });
            pSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog2.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            pOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog2.dismiss();
                    ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                }
            });
            dialog2.show();
        }
    }

    private void pickProfilePic(){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchPayment();
                }
                break;
            }
            case REQUEST_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickProfilePic();
                }
                break;
            }
        }
    }

    private void uploadProfilePic(){

        dialog = new ProgressDialog(ProfileActivity.this, R.style.MyAlertDialogStyle);
        dialog.setMessage("Updating, please wait.");
        dialog.show();

        try {
            BasicAWSCredentials credentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
            AmazonS3Client s3 = new AmazonS3Client(credentials);
            TransferUtility transferUtility = new TransferUtility(s3, ProfileActivity.this);

            String code = profilePicPath.substring(profilePicPath.length()-7, profilePicPath.length()-4);
            int n = (Integer.parseInt(code)+1);
            StringBuilder extras = new StringBuilder();
            for (int i=0; i<3-String.valueOf(n).length(); i++){
                extras.append("0");
            }
            String newPath = profilePicPath.substring(profilePicPath.length()-19, profilePicPath.length()-7);
            newPath = newPath + extras + n;

            File file = new File(filePath);
            String fileName = newPath + ".jpg";
            final TransferObserver observer = transferUtility.upload(
                    Config.BUCKETNAME + "/profilePics",
                    fileName,
                    file,
                    CannedAccessControlList.PublicRead
            );

            final String finalNewPath = newPath;
            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED.equals(observer.getState())) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        new UpdateProfile().execute(finalNewPath);
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                }

                @Override
                public void onError(int id, Exception ex) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.d("profilePicUpload Failed", ex.getMessage());
                }
            });
        } catch (AmazonClientException e){
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Toast.makeText(context, "Profile Pic Uploading Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void smsPermissionCheck(){
        int permissionCheck = ContextCompat.checkSelfPermission(ProfileActivity.this, android.Manifest.permission.SEND_SMS);
        if(permissionCheck == 0) {
            launchPayment();
        } else {
            final Dialog dialog2 = new Dialog(ProfileActivity.this);
            dialog2.setContentView(R.layout.permission_dialog);

            TextView pCancel = dialog2.findViewById(R.id.pCancel);
            TextView pSettings = dialog2.findViewById(R.id.pSettings);
            TextView pOk = dialog2.findViewById(R.id.pOk);
            pCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog2.dismiss();
                }
            });
            pSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog2.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            pOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog2.dismiss();
                    ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{android.Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS);
                }
            });
            dialog2.show();
        }
    }

    private void sendSMS(String message){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, message, null, null);
        } catch (Exception ex) {
            Toast.makeText(context, "SMS sending failed", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    /*private void sendEmail(String message) {
        String subject = "Approved as a BMTT Student.";
        //Creating SendMail object
        SendEMail sm = new SendEMail(ProfileActivity.this, email, subject, message);
        sm.execute();
    }*/


    private class UpdateProfile extends AsyncTask<String, Void, Boolean>{

        BmttUsersDO bmttUsersDO = new BmttUsersDO();

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ProfileActivity.this, R.style.MyAlertDialogStyle);
            dialog.setMessage("Updating Details. Please Wait...");
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            try {

                AWSProvider awsProvider = new AWSProvider();
                AmazonS3 s3client = new AmazonS3Client(awsProvider.getCredentialsProvider(getBaseContext()));

                String oldPath = profilePicPath.substring(profilePicPath.length()-19);

                s3client.deleteObject(new DeleteObjectRequest(Config.BUCKETNAME, "profilePics/"+oldPath));

                bmttUsersDO.setEmailId(email);
                bmttUsersDO.setPhone(phone);

                bmttUsersDO = dynamoDBMapper.load(bmttUsersDO);

                String profilePicPath = "https://s3.amazonaws.com/brightkidmont/profilePics/"+strings[0]+".jpg";

                bmttUsersDO.setProfilePic(profilePicPath);

                dynamoDBMapper.save(bmttUsersDO);
                return true;
            } catch (AmazonClientException e){
                paySnackBar("Network connection error!!");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (result) {
                Toast.makeText(context, "SuccessFully Updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Profile Pic Uploading Failed", Toast.LENGTH_SHORT).show();
            }
        }

    }


    private class UpdatePayStatus extends AsyncTask<String, Void, Boolean>{

        PaymentDO paymentDO = new PaymentDO();
        String pay, dateTime, admDT;
        private ProgressDialog dialog;

        String message = tvFullName.getText().toString() +", bearing SRN "+tvUserId.getText().toString()+" has paid " +amount.get(payDate)+" Rs For BMTT.";

        String year, month, today, thisYear;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ProfileActivity.this, R.style.MyAlertDialogStyle);
            dialog.setMessage("Updating Payment Details. Please Wait...");
            dialog.show();
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a");
            dateTime = sdf.format(c.getTime());

            Date d = c.getTime();

            SimpleDateFormat yearf = new SimpleDateFormat("yy");
            SimpleDateFormat monthf = new SimpleDateFormat("MM");
            SimpleDateFormat todayf = new SimpleDateFormat("dd");
            SimpleDateFormat thisYearf = new SimpleDateFormat("yyyy");

            year = yearf.format(d);
            month = monthf.format(d);
            today = todayf.format(d);
            thisYear = thisYearf.format(d);

            SimpleDateFormat admf = new SimpleDateFormat("MMM dd, yyyy");
            admDT = admf.format(d);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            //pay = amount.get(strings[0]);
            //amount.remove(strings[0]);
            //status.remove(strings[0]);
            //amount.put(currentDate(), pay);
            status.put(strings[0], "paid");
            paymentDO.setEmailId(email);
            paymentDO.setPhone(phone);
            paymentDO.setInstallments(amount);
            paymentDO.setPayment(status);
            try {

                BmttAdminsDO bmttAdminsDO = dynamoDBMapper.load(BmttAdminsDO.class, Config.SUPERADMIN, Config.SAPHONE);
                String notificationARN = bmttAdminsDO.getNotificationARN();

                //List<String> user = new ArrayList<>();
                //user.add(tvEmailAddress.getText().toString());

                /*if(dateTime != null) {
                    NotificationsDO notificationsDO = new NotificationsDO();
                    notificationsDO.setCentre("All users");
                    notificationsDO.setWhen(dateTime);
                    notificationsDO.setMessage(message);
                    notificationsDO.setWho(user);
                    dynamoDBMapper.save(notificationsDO);
                } else {
                    return false;
                }*/

                AWSCredentials awsCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
                AmazonSNSClient snsClient = new AmazonSNSClient(awsCredentials);

                PublishRequest publishRequest = new PublishRequest();
                publishRequest.setMessage(6 + notificationARN + "$" + message);
                publishRequest.setSubject("BMTT User Acceptance");
                publishRequest.withTargetArn(notificationARN);
                snsClient.publish(publishRequest);

                dynamoDBMapper.save(paymentDO);

                AmountDO amountDO = dynamoDBMapper.load(AmountDO.class, Config.SUPERADMIN, thisYear);
                if (amountDO != null){
                    amountDO.setEmail(Config.SUPERADMIN);
                    amountDO.setYear(thisYear);
                    String todayVal = amountDO.getToday();
                    if (today.equals(todayVal.substring(0,2).trim())){
                        int amt = Integer.parseInt(todayVal.substring(3)) + payingAmt;
                        amountDO.setToday(todayVal.substring(0,2) + " " + String.valueOf(amt));
                    } else {
                        amountDO.setToday(today + " " + payingAmt);
                    }
                    String monthVal = amountDO.getMonth();
                    if (month.equals(monthVal.substring(0,2).trim())){
                        int amt = Integer.parseInt(monthVal.substring(3)) + payingAmt;
                        amountDO.setMonth(monthVal.substring(0,2) + " " + String.valueOf(amt));
                    } else {
                        amountDO.setMonth(month + " " + payingAmt);
                    }
                    String annualVal = amountDO.getAnnual();
                    if (year.equals(annualVal.substring(0,2).trim())){
                        int amt = Integer.parseInt(annualVal.substring(3)) + payingAmt;
                        amountDO.setAnnual(annualVal.substring(0,2) + " " + String.valueOf(amt));
                    } else {
                        amountDO.setAnnual(year + " " + payingAmt);
                    }
                } else {
                    amountDO = new AmountDO();
                    amountDO.setEmail(Config.SUPERADMIN);
                    amountDO.setYear(thisYear);
                    amountDO.setToday(today + " " + payingAmt);
                    amountDO.setMonth(month + " " + payingAmt);
                    amountDO.setAnnual(year + " " + payingAmt);
                }

                dynamoDBMapper.save(amountDO);

                BmttUsersDO bmttUsersDO = dynamoDBMapper.load(BmttUsersDO.class, email, phone);
                if (bmttUsersDO.getAdmissionDone().equals("pending")){
                    bmttUsersDO.setAdmissionDone("paid " + admDT);
                }

                dynamoDBMapper.save(bmttUsersDO);

                return true;
            } catch (AmazonClientException e){
                paySnackBar("Network connection error!!");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (result) {
                //sendEmail(message);
                //sendSMS(message);
                setUpPayment(paymentDO);
            }
        }

    }


    private class PaymentDetails extends AsyncTask<String, Void, Boolean> {

        PaymentDO paymentDO = new PaymentDO();

        @Override
        protected void onPreExecute() {
            if (pbProfile !=null)
                pbProfile.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                paymentDO = dynamoDBMapper.load(PaymentDO.class, strings[0], strings[1]);
                return true;
            } catch (AmazonClientException e){
                profileActivity.showSnackBar("Network connection error!!");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pbProfile !=null)
                pbProfile.setVisibility(View.GONE);
            if (result) {
                setUpPayment(paymentDO);
            }
        }
    }

    private class LoadFileSub extends AsyncTask<String, Void, FileSubmissionDO>{

        @Override
        protected void onPreExecute() {
            if (pbProfile !=null)
                pbProfile.setVisibility(View.VISIBLE);
        }

        @Override
        protected FileSubmissionDO doInBackground(String... strings) {
            try {
                return dynamoDBMapper.load(FileSubmissionDO.class, strings[0], strings[1]);
            } catch (AmazonClientException e){
                profileActivity.showSnackBar("Network connection error!!");
                return null;
            }
        }

        @Override
        protected void onPostExecute(FileSubmissionDO fileSubmissionDO) {
            if (pbProfile !=null)
                pbProfile.setVisibility(View.GONE);
            if(fileSubmissionDO != null)
                fileSub(fileSubmissionDO);
        }

    }

    private class UpdateFileSub extends AsyncTask<Integer, Void, Boolean>{

        ProgressDialog progressDialog;
        FileSubmissionDO fileSubmissionDO = new FileSubmissionDO();

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ProfileActivity.this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Updating File Submission, please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            try {
                fileSubmissionDO = dynamoDBMapper.load(FileSubmissionDO.class, email, phone);
                Boolean submitted = false;
                switch (integers[0]){
                    case 1:
                        if (fileSubmissionDO.getFile1().equals("not")) {
                            fileSubmissionDO.setFile1("submitted");
                            submitted = true;
                        } else if (fileSubmissionDO.getFile1().equals("submitted"))
                            fileSubmissionDO.setFile1("not");
                        break;
                    case 2:
                        if (fileSubmissionDO.getFile2().equals("not")) {
                            fileSubmissionDO.setFile2("submitted");
                            submitted = true;
                        } else if (fileSubmissionDO.getFile2().equals("submitted"))
                            fileSubmissionDO.setFile2("not");
                        break;
                    case 3:
                        if (fileSubmissionDO.getFile3().equals("not")) {
                            fileSubmissionDO.setFile3("submitted");
                            submitted = true;
                        } else if (fileSubmissionDO.getFile3().equals("submitted"))
                            fileSubmissionDO.setFile3("not");
                        break;
                    case 4:
                        if (fileSubmissionDO.getFile4().equals("not")) {
                            fileSubmissionDO.setFile4("submitted");
                            submitted = true;
                        } else if (fileSubmissionDO.getFile4().equals("submitted"))
                            fileSubmissionDO.setFile4("not");
                        break;
                }
                dynamoDBMapper.save(fileSubmissionDO);

                if (submitted){
                    BmttAdminsDO bmttAdminsDO = dynamoDBMapper.load(BmttAdminsDO.class, Config.SUPERADMIN, Config.SAPHONE);

                    AWSCredentials awsCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
                    AmazonSNSClient snsClient = new AmazonSNSClient(awsCredentials);

                    String notificationArn = bmttAdminsDO.getNotificationARN();
                    PublishRequest publishRequest = new PublishRequest();
                    publishRequest.setMessage(0 + notificationArn + "$" +tvFullName.getText().toString()+" submitted files.");
                    publishRequest.setSubject("BMTT Notification");
                    publishRequest.withTargetArn(notificationArn);
                    snsClient.publish(publishRequest);
                }

                return true;
            } catch (AmazonClientException e){
                profileActivity.showSnackBar("Network connection error!!");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            if (aBoolean && fileSubmissionDO!= null)
                fileSub(fileSubmissionDO);
        }
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
