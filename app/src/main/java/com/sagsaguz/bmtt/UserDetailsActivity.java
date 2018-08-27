package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.AmountDO;
import com.sagsaguz.bmtt.utils.BmttAdminsDO;
import com.sagsaguz.bmtt.utils.BmttUsersDO;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.FileSubmissionDO;
import com.sagsaguz.bmtt.utils.NotificationsDO;
import com.sagsaguz.bmtt.utils.PaymentDO;
import com.sagsaguz.bmtt.utils.PaymentInfoDO;
import com.sagsaguz.bmtt.utils.SendEMail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_SEND_SMS = 2 ;

    private static CircleImageView ivProfilePic;
    @SuppressLint("StaticFieldLeak")
    private static ConstraintLayout clUserDetails;

    private RelativeLayout editUserDetails, saveUserDetails, rlEditOptions;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvExpiryDate, tvUserId;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvDOB;
    @SuppressLint("StaticFieldLeak")
    private static EditText etFullName, etCentre, etEmailAddress, etPassword, etPhone, etAddress;
    @SuppressLint("StaticFieldLeak")
    private static CheckBox cbPart1, cbPart2, cbPart3;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar pbUserDetails;
    private boolean pbStatus = false, docStatus = false;
    @SuppressLint("StaticFieldLeak")
    private static SwitchCompat docSubmission;

    private CheckBox cbSubmitted1, cbSubmitted2, cbSubmitted3, cbSubmitted4;
    private CheckBox cbReceived1, cbReceived2, cbReceived3, cbReceived4;
    private CheckBox cbEvaluated1, cbEvaluated2, cbEvaluated3, cbEvaluated4;

    private TextView tvPayment1, tvPayment2, tvPayment3, tvTotal, tvTotalPaid, tvPay1, tvPay2, tvPay3;
    private RelativeLayout rlPayment2, rlPayment3;
    private String pay1, pay2, pay3;
    private Boolean bmtt1 = false, bmtt2 = false, bmtt3 = false;

    private ImageView ivPaymentInfo;

    private static DynamoDBMapper dynamoDBMapper;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    @SuppressLint("StaticFieldLeak")
    private static UserDetailsActivity userDetailsActivity;

    private static Boolean saved;
    private static String createdDate, lastName, profileURL, firstName, email, phone, notificationARN, admissionDone;
    private int totalPaid = 0;
    private String userType;

    private PaymentInfoDO paymentInfoDO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_details_layout);

        userDetailsActivity = UserDetailsActivity.this;
        context = UserDetailsActivity.this;

        clUserDetails = findViewById(R.id.clUserDetails);

        ivProfilePic = findViewById(R.id.ivProfilePic);
        etFullName = findViewById(R.id.etFullName);
        etCentre = findViewById(R.id.etCentre);

        etEmailAddress = findViewById(R.id.etEmailAddress);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);

        cbPart1 = findViewById(R.id.cbPart1);
        cbPart2 = findViewById(R.id.cbPart2);
        cbPart3 = findViewById(R.id.cbPart3);

        tvDOB = findViewById(R.id.tvDOB);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        TextView tvMilestone = findViewById(R.id.tvMilestone);
        etAddress = findViewById(R.id.etAddress);

        ivPaymentInfo = findViewById(R.id.ivPaymentInfo);
        //ivPaymentInfo.setVisibility(View.GONE);

        cbSubmitted1 = findViewById(R.id.cbSubmitted1);
        cbReceived1 = findViewById(R.id.cbReceived1);
        cbReceived1.setEnabled(false);
        cbEvaluated1 = findViewById(R.id.cbEvaluated1);
        cbEvaluated1.setEnabled(false);

        cbSubmitted2 = findViewById(R.id.cbSubmitted2);
        cbReceived2 = findViewById(R.id.cbReceived2);
        cbReceived2.setEnabled(false);
        cbEvaluated2 = findViewById(R.id.cbEvaluated2);
        cbEvaluated2.setEnabled(false);

        cbSubmitted3 = findViewById(R.id.cbSubmitted3);
        cbReceived3 = findViewById(R.id.cbReceived3);
        cbReceived3.setEnabled(false);
        cbEvaluated3 = findViewById(R.id.cbEvaluated3);
        cbEvaluated3.setEnabled(false);

        cbSubmitted4 = findViewById(R.id.cbSubmitted4);
        cbReceived4 = findViewById(R.id.cbReceived4);
        cbReceived4.setEnabled(false);
        cbEvaluated4 = findViewById(R.id.cbEvaluated4);
        cbEvaluated4.setEnabled(false);

        rlEditOptions = findViewById(R.id.rlEditOptions);

        SharedPreferences adminPreferences = getSharedPreferences("USERDETAILS", MODE_PRIVATE);
        userType = adminPreferences.getString("USERTYPE", "super");
        /*if (userType.equals("admin"))
            ivPaymentInfo.setVisibility(View.VISIBLE);*/

        editUserDetails = findViewById(R.id.editUserDetails);
        editUserDetails.setVisibility(View.GONE);
        saveUserDetails = findViewById(R.id.saveUserDetails);
        saveUserDetails.setVisibility(View.GONE);

        tvUserId = findViewById(R.id.tvUserId);

        pbUserDetails = findViewById(R.id.pbUserDetails);
        pbUserDetails.setVisibility(View.GONE);

        ImageView ivPDetails = findViewById(R.id.ivPDetails);
        ivPDetails.setVisibility(View.GONE);

        docSubmission = findViewById(R.id.sDocument);

        tvPayment1 = findViewById(R.id.tvPayment1);
        tvPayment2 = findViewById(R.id.tvPayment2);
        tvPayment3 = findViewById(R.id.tvPayment3);

        tvPay1 = findViewById(R.id.tvPay1);
        tvPay2 = findViewById(R.id.tvPay2);
        tvPay3 = findViewById(R.id.tvPay3);

        tvTotal = findViewById(R.id.tvTotal);
        tvTotalPaid = findViewById(R.id.tvTotalPaid);

        rlPayment2 = findViewById(R.id.rlPayment2);
        rlPayment3 = findViewById(R.id.rlPayment3);

        etFullName.setKeyListener(null);
        etCentre.setTag(etCentre.getKeyListener());
        etPassword.setTag(etPassword.getKeyListener());
        etPhone.setKeyListener(null);
        etEmailAddress.setKeyListener(null);
        etAddress.setTag(etAddress.getKeyListener());

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

        Bundle bundle = getIntent ().getExtras ();
        if (bundle != null) {
            Bitmap image = bundle.getParcelable ("IMAGE");
            ivProfilePic.setImageBitmap (image);
        }

        //new ShowUserDetails().execute(email, phone);
        //new PaymentDetails().execute(email, phone);
        new PaymentInfo().execute(email, phone);

        final Calendar myCalendar = Calendar.getInstance();
        tvExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel(myCalendar);
                    }
                };
                new DatePickerDialog(UserDetailsActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        editUserDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editable();
            }
        });

        saveUserDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nonEditable();
                smsPermissionCheck();
            }
        });

        tvMilestone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pbStatus = true;
                Intent milestoneIntent = new Intent(getBaseContext(), MilestoneActivity.class);
                milestoneIntent.putExtra("EMAIL", etEmailAddress.getText().toString());
                milestoneIntent.putExtra("PHONE", etPhone.getText().toString());
                milestoneIntent.putExtra("CREATED", createdDate);
                milestoneIntent.putExtra("EXPIRY", tvExpiryDate.getText().toString());
                milestoneIntent.putExtra("BMTT1", cbPart1.isChecked());
                milestoneIntent.putExtra("BMTT2", cbPart2.isChecked());
                milestoneIntent.putExtra("BMTT3", cbPart3.isChecked());
                startActivity(milestoneIntent);
            }
        });

        ivPDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pbStatus = true;
                Intent intent = new Intent(getBaseContext(), PracticalResultsActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("PHONE", phone);
                intent.putExtra("USERTYPE", "admin");
                startActivity(intent);
            }
        });

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ImageView activities = findViewById(R.id.activities);
        activities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(userDetailsActivity, ActivitiesActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("PHONE", phone);
                startActivity(intent);
            }
        });

        ivPaymentInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentInfo();
            }
        });

        tvPay1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tvPay1.getText().toString().equals("pending")){
                    updatePaymentInfo(tvPayment1.getText().toString().substring(0,12), "pay1");
                }
            }
        });

        tvPay2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tvPay2.getText().toString().equals("pending")){
                    updatePaymentInfo(tvPayment2.getText().toString().substring(0,12), "pay2");
                }
            }
        });

        tvPay3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tvPay3.getText().toString().equals("pending")){
                    updatePaymentInfo(tvPayment3.getText().toString().substring(0,12), "pay3");
                }
            }
        });

        if (userType.equals("admin")){
            tvPay1.setOnClickListener(null);
            tvPay2.setOnClickListener(null);
            tvPay3.setOnClickListener(null);
        }

    }

    public void basicSnackBar(String message){
        Snackbar snackbar = Snackbar.make(clUserDetails, message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        snackbar.show();
    }

    public void showSnackBar(String message, final String type){
        Snackbar snackbar = Snackbar.make(clUserDetails, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(type.equals("showuser")){
                            //new ShowUserDetails().execute(email, phone);
                            //new PaymentDetails().execute(email, phone);
                            new PaymentInfo().execute(email, phone);
                        } else {
                            smsPermissionCheck();
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

    private void paymentInfo(){

        final Dialog dialog = new Dialog(UserDetailsActivity.this);
        dialog.setContentView(R.layout.payment_info_dialog);

        RelativeLayout rlAdmin = dialog.findViewById(R.id.rlAdmin);
        RelativeLayout rlSAdmin = dialog.findViewById(R.id.rlSAdmin);
        //rlSAdmin.setVisibility(View.GONE);

        final EditText etPaymentInfo = dialog.findViewById(R.id.etPaymentInfo);
        TextView tvPaymentInfo = dialog.findViewById(R.id.tvPaymentInfo);

        Button btnCancel1 = dialog.findViewById(R.id.btnCancel1);
        Button btnCancel2 = dialog.findViewById(R.id.btnCancel2);

        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);
        Button btnAccept = dialog.findViewById(R.id.btnAccept);

        if (userType.equals("admin"))
            rlSAdmin.setVisibility(View.GONE);
        else {
            rlAdmin.setVisibility(View.GONE);
            if (paymentInfoDO != null)
                tvPaymentInfo.setText(paymentInfoDO.getMessage());
        }

        btnCancel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(etPaymentInfo.getText().toString().trim())) {
                    new Thread(new Runnable() {
                        public void run() {
                            PaymentInfoDO paymentInfoDO = new PaymentInfoDO();
                            paymentInfoDO.setEmailId(email);
                            paymentInfoDO.setPhone(phone);
                            paymentInfoDO.setMessage(etPaymentInfo.getText().toString().trim());

                            dynamoDBMapper.save(paymentInfoDO);

                            BmttAdminsDO bmttAdminsDO = dynamoDBMapper.load(BmttAdminsDO.class, Config.SUPERADMIN, Config.SAPHONE);

                            AWSCredentials awsCredentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
                            AmazonSNSClient snsClient = new AmazonSNSClient(awsCredentials);

                            String notificationArn = bmttAdminsDO.getNotificationARN();
                            PublishRequest publishRequest = new PublishRequest();
                            publishRequest.setMessage(6 + notificationArn + "$" + firstName + " " + lastName + "'s payment details are updated.");
                            publishRequest.setSubject("BMTT Notification");
                            publishRequest.withTargetArn(notificationArn);
                            snsClient.publish(publishRequest);
                        }
                    }).start();
                    Toast.makeText(UserDetailsActivity.this, "Updated Payment Info.", Toast.LENGTH_SHORT).show();
                    ivPaymentInfo.setVisibility(View.GONE);
                    dialog.dismiss();
                }
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    public void run() {
                        PaymentInfoDO paymentInfoDO = new PaymentInfoDO();
                        paymentInfoDO.setEmailId(email);
                        paymentInfoDO.setPhone(phone);

                        dynamoDBMapper.delete(paymentInfoDO);
                    }
                }).start();
                Toast.makeText(UserDetailsActivity.this, "Update the payment details in payment section.", Toast.LENGTH_SHORT).show();
                ivPaymentInfo.setVisibility(View.GONE);
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void updatePaymentInfo(final String payDate, final String payType){
        final Dialog dialog = new Dialog(UserDetailsActivity.this);
        dialog.setContentView(R.layout.custom_dialog);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        tvTitle.setText("Payment Update");
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        tvMessage.setText("Are you sure, you want to make changes to this particular payment details?...");

        Button btnYes = dialog.findViewById(R.id.btnRemove);
        btnYes.setText("Yes");
        Button btnNo = dialog.findViewById(R.id.btnCancel);
        btnNo.setText("No");

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                new UpdatePayment().execute(payDate, payType);
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void nonEditable(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(clUserDetails.getWindowToken(), 0);
        }
        saveUserDetails.setVisibility(View.GONE);
        etCentre.setKeyListener(null);
        etPassword.setKeyListener(null);
        cbPart1.setEnabled(false);
        cbPart2.setEnabled(false);
        cbPart3.setEnabled(false);
        docSubmission.setEnabled(false);
        tvExpiryDate.setClickable(false);
        ivProfilePic.setClickable(false);
        etAddress.setKeyListener(null);
        /*if (!userType.equals("admin")){
        }*/
        cbReceived1.setEnabled(false);
        cbEvaluated1.setEnabled(false);
        cbReceived2.setEnabled(false);
        cbEvaluated2.setEnabled(false);
        cbReceived3.setEnabled(false);
        cbEvaluated3.setEnabled(false);
        cbReceived4.setEnabled(false);
        cbEvaluated4.setEnabled(false);
        editUserDetails.setVisibility(View.VISIBLE);
    }

    private void editable() {
        editUserDetails.setVisibility(View.GONE);
        etPassword.setKeyListener((KeyListener) etCentre.getTag());
        ivProfilePic.setClickable(true);
        etAddress.setKeyListener((KeyListener) etAddress.getTag());
        if (!userType.equals("admin")){
            etCentre.setKeyListener((KeyListener) etCentre.getTag());
            cbPart1.setEnabled(true);
            cbPart2.setEnabled(true);
            cbPart3.setEnabled(true);
            docSubmission.setEnabled(true);
            tvExpiryDate.setClickable(true);

            cbReceived1.setEnabled(true);
            cbEvaluated1.setEnabled(true);
            cbReceived2.setEnabled(true);
            cbEvaluated2.setEnabled(true);
            cbReceived3.setEnabled(true);
            cbEvaluated3.setEnabled(true);
            cbReceived4.setEnabled(true);
            cbEvaluated4.setEnabled(true);
        }
        saveUserDetails.setVisibility(View.VISIBLE);
    }

    private static void setUserDetails(BmttUsersDO bmttUsersDO){
        profileURL = bmttUsersDO.getProfilePic();
        //Picasso.with(context).load(profileURL).into(ivProfilePic);
        String fullName = bmttUsersDO.getFirstName() + " " + bmttUsersDO.getLastName();
        etFullName.setText(fullName);
        etCentre.setText(bmttUsersDO.getCentre());
        etEmailAddress.setText(bmttUsersDO.getEmailId());
        etPassword.setText(bmttUsersDO.getPassword());
        etPhone.setText(bmttUsersDO.getPhone());
        tvDOB.setText(bmttUsersDO.getDob());
        cbPart1.setChecked(bmttUsersDO.getBmttPart1());
        cbPart2.setChecked(bmttUsersDO.getBmttPart2());
        cbPart3.setChecked(bmttUsersDO.getBmttPart3());
        tvExpiryDate.setText(bmttUsersDO.getExpiryDate());
        createdDate = bmttUsersDO.getCreatedDate();
        lastName = bmttUsersDO.getLastName();
        firstName = bmttUsersDO.getFirstName();
        etAddress.setText(bmttUsersDO.getAddress());
        tvUserId.setText(bmttUsersDO.getUserId());
        notificationARN = bmttUsersDO.getNotificationARN();
        docSubmission.setChecked(bmttUsersDO.getDocSubmission());
        userDetailsActivity.docStatus = bmttUsersDO.getDocSubmission();
        userDetailsActivity.nonEditable();
        userDetailsActivity.bmtt1 = bmttUsersDO.getBmttPart1();
        userDetailsActivity.bmtt2 = bmttUsersDO.getBmttPart2();
        userDetailsActivity.bmtt3 = bmttUsersDO.getBmttPart3();
        admissionDone = bmttUsersDO.getAdmissionDone();

    }

    private void updateLabel(Calendar myCalendar) {
        String myFormat = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        tvExpiryDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void setUpPayment(PaymentDO payment){

        Map<String, String> amount = payment.getInstallments();
        Map<String, String> status = payment.getPayment();

        List<String> date = new ArrayList<>(amount.keySet());
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
            tvPayment1.setText(date.get(0) + " - " + amount.get(date.get(0)) + " Rs");
            pay1 = amount.get(date.get(0));
            tvTotal.setText(pay1 + " Rs");
            tvPay1.setText(status.get(date.get(0)));
            if (status.get(date.get(0)).equals("paid")) {
                tvPay1.setTextColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay1));
            }
            tvTotalPaid.setText(totalPaid + " Rs");
        } else if (amount.size() == 2){
            rlPayment3.setVisibility(View.GONE);
            tvPayment1.setText(date.get(0) + " - " + amount.get(date.get(0)) + " Rs");
            tvPayment2.setText(date.get(1) + " - " + amount.get(date.get(1)) + " Rs");
            pay1 = amount.get(date.get(0));
            pay2 = amount.get(date.get(1));
            tvTotal.setText((Integer.parseInt(pay1) + Integer.parseInt(pay2)) + " Rs");
            tvPay1.setText(status.get(date.get(0)));
            tvPay2.setText(status.get(date.get(1)));
            if (status.get(date.get(0)).equals("paid")) {
                tvPay1.setTextColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay1));
            }
            if (status.get(date.get(1)).equals("paid")) {
                tvPay2.setTextColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay2));
            }
            tvTotalPaid.setText(totalPaid + " Rs");
        } else {
            tvPayment1.setText(date.get(0) + " - " + amount.get(date.get(0)) + " Rs");
            tvPayment2.setText(date.get(1) + " - " + amount.get(date.get(1)) + " Rs");
            tvPayment3.setText(date.get(2) + " - " + amount.get(date.get(2)) + " Rs");
            pay1 = amount.get(date.get(0));
            pay2 = amount.get(date.get(1));
            pay3 = amount.get(date.get(2));
            tvTotal.setText((Integer.parseInt(pay1) + Integer.parseInt(pay2) + Integer.parseInt(pay3)) + " Rs");
            tvPay1.setText(status.get(date.get(0)));
            tvPay2.setText(status.get(date.get(1)));
            tvPay3.setText(status.get(date.get(2)));
            if (status.get(date.get(0)).equals("paid")) {
                tvPay1.setTextColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay1));
            }
            if (status.get(date.get(1)).equals("paid")) {
                tvPay2.setTextColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay2));
            }
            if (status.get(date.get(2)).equals("paid")) {
                tvPay3.setTextColor(getResources().getColor(R.color.green));
                totalPaid = totalPaid + (Integer.parseInt(pay3));
            }
            tvTotalPaid.setText(totalPaid + " Rs");
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

        new ShowUserDetails().execute(email, phone);
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

    private void smsPermissionCheck(){
        int permissionCheck = ContextCompat.checkSelfPermission(UserDetailsActivity.this, android.Manifest.permission.SEND_SMS);
        if(permissionCheck == 0) {
            new UpdateUserDetails().execute(email, phone);
        } else {
            final Dialog dialog2 = new Dialog(UserDetailsActivity.this);
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
                    ActivityCompat.requestPermissions(UserDetailsActivity.this, new String[]{android.Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS);
                }
            });
            dialog2.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new UpdateUserDetails().execute(email, phone);
                }
                break;
            }
        }
    }

    private void sendSMS(String message){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(etPhone.getText().toString(), null, message, null, null);
        } catch (Exception ex) {
            basicSnackBar("SMS sending failed");
            ex.printStackTrace();
        }
    }

    private void sendEmail(String subject, String message) {
        String email = etEmailAddress.getText().toString();
        String pas = etPassword.getText().toString();
        //Creating SendMail object
        SendEMail sm = new SendEMail(UserDetailsActivity.this, email, subject, message);
        sm.execute();
    }


    private class PaymentInfo extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            if (pbUserDetails !=null)
                pbUserDetails.setVisibility(View.VISIBLE);
            paymentInfoDO = new PaymentInfoDO();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                paymentInfoDO = dynamoDBMapper.load(PaymentInfoDO.class, strings[0], strings[1]);
                return true;
            } catch (AmazonClientException e){
                userDetailsActivity.showSnackBar("Network connection error!!", "showuser");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pbUserDetails !=null)
                pbUserDetails.setVisibility(View.GONE);
            if (result) {
                if (paymentInfoDO == null){
                    if (userType.equals("admin"))
                        ivPaymentInfo.setVisibility(View.VISIBLE);
                    else
                        ivPaymentInfo.setVisibility(View.GONE);
                } else {
                    if (userType.equals("admin"))
                        ivPaymentInfo.setVisibility(View.GONE);
                    else
                        ivPaymentInfo.setVisibility(View.VISIBLE);
                }
                new PaymentDetails().execute(email, phone);
            }
        }
    }

    private class UpdatePayment extends AsyncTask<String, Void, Boolean> {

        PaymentDO paymentDO = new PaymentDO();
        ProgressDialog progressDialog;
        String payType;
        int paidAmount, payingAmt = 0;
        String year, month, today, thisYear, dateTime, admDT;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(userDetailsActivity, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Updating payment details, please wait.");
            progressDialog.setCancelable(false);
            progressDialog.show();
            paidAmount = Integer.parseInt(tvTotalPaid.getText().toString().substring(0, tvTotalPaid.length()-3));

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
            payType = strings[1];
            try {
                paymentDO = dynamoDBMapper.load(PaymentDO.class, email, phone);
                Map<String, String> amount = paymentDO.getInstallments();
                Map<String, String> status = paymentDO.getPayment();
                List<String> date = new ArrayList<>(status.keySet());
                for (int i=0; i<date.size(); i++){
                    if (date.get(i).equals(strings[0])) {
                        status.put(strings[0], "paid");
                        payingAmt = Integer.parseInt(amount.get(strings[0]));
                        paidAmount = paidAmount + payingAmt;
                    }
                }
                paymentDO.setPayment(status);
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
                userDetailsActivity.basicSnackBar("Network connection error!!. Please try again");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            if (result) {
                switch (payType){
                    case "pay1":
                        tvPay1.setText("paid");
                        tvPay1.setTextColor(getResources().getColor(R.color.green));
                        break;
                    case "pay2":
                        tvPay2.setText("paid");
                        tvPay2.setTextColor(getResources().getColor(R.color.green));
                        break;
                    case "pay3":
                        tvPay3.setText("paid");
                        tvPay3.setTextColor(getResources().getColor(R.color.green));
                        break;
                }
                tvTotalPaid.setText(paidAmount + " Rs");
            }

        }
    }


    @SuppressLint("StaticFieldLeak")
    private class PaymentDetails extends AsyncTask<String, Void, Boolean> {

        PaymentDO paymentDO = new PaymentDO();

        @Override
        protected void onPreExecute() {
            if (pbUserDetails !=null)
                pbUserDetails.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                paymentDO = dynamoDBMapper.load(PaymentDO.class, strings[0], strings[1]);
                return true;
            } catch (AmazonClientException e){
                userDetailsActivity.showSnackBar("Network connection error!!", "showuser");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pbUserDetails !=null)
                pbUserDetails.setVisibility(View.GONE);
            if (result) {
                setUpPayment(paymentDO);
            }

        }
    }


    private static class ShowUserDetails extends AsyncTask<String, Void, BmttUsersDO> {

        BmttUsersDO bmttUsersDO = new BmttUsersDO();

        @Override
        protected void onPreExecute() {
            if (userDetailsActivity.pbUserDetails !=null)
                userDetailsActivity.pbUserDetails.setVisibility(View.VISIBLE);
        }

        @Override
        protected BmttUsersDO doInBackground(String... strings) {
            try {
                bmttUsersDO = dynamoDBMapper.load(BmttUsersDO.class, strings[0], strings[1]);
                return bmttUsersDO;
            } catch (AmazonClientException e){
                userDetailsActivity.showSnackBar("Network connection error!!", "showuser");
                return null;
            }
        }

        @Override
        protected void onPostExecute(BmttUsersDO bmttUsersDO) {
            if (userDetailsActivity.pbUserDetails !=null)
                userDetailsActivity.pbUserDetails.setVisibility(View.GONE);
            if(bmttUsersDO != null) {
                setUserDetails(bmttUsersDO);
                saved = false;
            }
        }
    }


    private static class UpdateUserDetails extends AsyncTask<String, Void, Boolean>{

        BmttUsersDO bmttUsersDO = new BmttUsersDO();
        String fullName, dateTime, message, subject;
        Boolean changed = false;
        FileSubmissionDO fileSubmissionDO = new FileSubmissionDO();
        int category;

        @Override
        protected void onPreExecute() {
            if (userDetailsActivity.pbUserDetails !=null)
                userDetailsActivity.pbUserDetails.setVisibility(View.VISIBLE);
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a");
            dateTime = sdf.format(c.getTime());
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            bmttUsersDO.setEmailId(strings[0]);
            bmttUsersDO.setFirstName(firstName);
            bmttUsersDO.setLastName(lastName);
            bmttUsersDO.setProfilePic(profileURL);
            bmttUsersDO.setCentre(etCentre.getText().toString());
            bmttUsersDO.setPassword(etPassword.getText().toString());
            bmttUsersDO.setPhone(strings[1]);
            bmttUsersDO.setDob(tvDOB.getText().toString());
            bmttUsersDO.setBmttPart1(cbPart1.isChecked());
            bmttUsersDO.setBmttPart2(cbPart2.isChecked());
            bmttUsersDO.setBmttPart3(cbPart3.isChecked());
            bmttUsersDO.setCreatedDate(createdDate);
            bmttUsersDO.setExpiryDate(tvExpiryDate.getText().toString());
            bmttUsersDO.setAddress(etAddress.getText().toString());
            bmttUsersDO.setUserId(tvUserId.getText().toString());
            bmttUsersDO.setNotificationARN(notificationARN);
            bmttUsersDO.setDocSubmission(docSubmission.isChecked());
            bmttUsersDO.setAdmissionDone(admissionDone);

            try {

                if (!userDetailsActivity.bmtt1 && cbPart1.isChecked()){
                    subject = "Approved as a BMTT Student. Access granted to TERM 1, part-1";
                    message = firstName + " " + lastName + ", Congratulations!! Start an exciting journey of Learn to Teach and Teach to Learn with BMTT.  Your student registration number is " + tvUserId.getText().toString()+"\n Now you can access the videos of TERM 1, part-1.\n" +
                            "A gentle reminder to submit the  copies  of enrolment documents and duly filled in application form required mandatorily for continuing the BMTT course. Please submit it as soon as possible.\nThank you.";
                    userDetailsActivity.bmtt1 = true;
                    changed = true;
                    category = 1;
                }
                if (!userDetailsActivity.bmtt2 && cbPart2.isChecked()){
                    subject = "Access granted to TERM 1, part-1 & part-2";
                    message = "Dear "+ firstName + " " + lastName +", thank you for the successful payment. Now you can access the videos of TERM 1, part-1 & part-2";
                    userDetailsActivity.bmtt2 = true;
                    changed = true;
                    category = 2;
                }
                if (!userDetailsActivity.bmtt3 && cbPart3.isChecked()){
                    subject = "Access granted to TERM 1, part-1 & part-2 and TERM 2";
                    message = "Dear "+ firstName + " " + lastName +", thank you for the successful payment. Now you can access the videos of TERM 1, part-1 & part-2 and TERM 2";
                    userDetailsActivity.bmtt3 = true;
                    changed = true;
                    category = 3;
                }
                if (userDetailsActivity.bmtt1 && !cbPart1.isChecked() ||
                        userDetailsActivity.bmtt2 && !cbPart2.isChecked() ||
                        userDetailsActivity.bmtt3 && !cbPart3.isChecked()){
                    subject = "Access restricted to BMTT course.";
                    message = "Dear "+ firstName + " " + lastName +", we regret to inform you, despite several reminders, you have not paid the amount due now for the BMTT course. Your enrollment is cancelled. Please contact Head Office For more details.\nThank you.";
                    userDetailsActivity.bmtt3 = true;
                    changed = true;
                    category = 4;
                }
                if (!userDetailsActivity.docStatus && docSubmission.isChecked()){
                    subject = "Successfully received all of your documents.";
                    message = "Dear "+ firstName + " " + lastName +", Good Job! Your BMTT enrollment document copies received. Our coordinator will advise you on further requirements if any after checking the document copies.";
                    userDetailsActivity.docStatus = true;
                    changed = true;
                    category = 5;
                }

                if (changed){
                    //user.add(strings[0]);

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
                    publishRequest.setMessage(category + notificationARN + "$" + message);
                    publishRequest.setSubject("BMTT User Acceptance");
                    publishRequest.withTargetArn(notificationARN);
                    snsClient.publish(publishRequest);

                    //userDetailsActivity.sendSMS(message);
                    userDetailsActivity.sendEmail(subject, message);
                }

                dynamoDBMapper.save(bmttUsersDO);
                fullName = firstName + " " + lastName;

                fileSubmissionDO = dynamoDBMapper.load(FileSubmissionDO.class, email, phone);
                if (userDetailsActivity.cbReceived1.isChecked())
                    fileSubmissionDO.setFile1("received");
                if (userDetailsActivity.cbEvaluated1.isChecked())
                    fileSubmissionDO.setFile1("evaluated");
                if (userDetailsActivity.cbReceived2.isChecked())
                    fileSubmissionDO.setFile2("received");
                if (userDetailsActivity.cbEvaluated2.isChecked())
                    fileSubmissionDO.setFile2("evaluated");
                if (userDetailsActivity.cbReceived3.isChecked())
                    fileSubmissionDO.setFile3("received");
                if (userDetailsActivity.cbEvaluated3.isChecked())
                    fileSubmissionDO.setFile3("evaluated");
                if (userDetailsActivity.cbReceived4.isChecked())
                    fileSubmissionDO.setFile4("received");
                if (userDetailsActivity.cbEvaluated4.isChecked())
                    fileSubmissionDO.setFile4("evaluated");

                dynamoDBMapper.save(fileSubmissionDO);

                return true;
            } catch (AmazonClientException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (userDetailsActivity.pbUserDetails !=null)
                userDetailsActivity.pbUserDetails.setVisibility(View.GONE);
            if(result){
                saved = true;
                userDetailsActivity.basicSnackBar("Successfully update "+fullName+" details.");
                userDetailsActivity.fileSub(fileSubmissionDO);
            } else {
                userDetailsActivity.showSnackBar("Network connection error!!", "update");
            }
        }
    }

    private class LoadFileSub extends AsyncTask<String, Void, FileSubmissionDO>{

        @Override
        protected void onPreExecute() {
            if (pbUserDetails !=null)
                pbUserDetails.setVisibility(View.VISIBLE);
        }

        @Override
        protected FileSubmissionDO doInBackground(String... strings) {
            try {
                return dynamoDBMapper.load(FileSubmissionDO.class, strings[0], strings[1]);
            } catch (AmazonClientException e){
                basicSnackBar("Network connection error!!");
                return null;
            }
        }

        @Override
        protected void onPostExecute(FileSubmissionDO fileSubmissionDO) {
            if (pbUserDetails !=null)
                pbUserDetails.setVisibility(View.GONE);
            if(fileSubmissionDO != null)
                fileSub(fileSubmissionDO);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        /*if (pbStatus){
            if (pbUserDetails !=null && pbUserDetails.getVisibility()==View.VISIBLE)
                pbUserDetails.setVisibility(View.GONE);
        }*/
        if (pbUserDetails !=null && pbUserDetails.getVisibility()==View.VISIBLE)
            pbUserDetails.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(saved != null) {
            if (saved) {
                MainBranchActivity.mainBranchActivity.recreate();
                finish();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
