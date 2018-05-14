package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.BmttUsersDO;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserDetailsActivity extends AppCompatActivity {

    private static CircleImageView ivProfilePic;
    @SuppressLint("StaticFieldLeak")
    private static ConstraintLayout clUserDetails;

    private RelativeLayout editUserDetails, saveUserDetails;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvExpiryDate;
    @SuppressLint("StaticFieldLeak")
    private static TextView tvDOB;
    @SuppressLint("StaticFieldLeak")
    private static EditText etFullName, etCentre, etEmailAddress, etPassword, etPhone, etAddress;
    @SuppressLint("StaticFieldLeak")
    private static CheckBox cbPart1, cbPart2, cbPart3;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar pbUserDetails;
    private boolean pbStatus = false;

    private static DynamoDBMapper dynamoDBMapper;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    @SuppressLint("StaticFieldLeak")
    private static UserDetailsActivity userDetailsActivity;

    private static Boolean saved;
    private static String createdDate, lastName, profileURL, firstName, email, phone, notificationARN;

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

        editUserDetails = findViewById(R.id.editUserDetails);
        editUserDetails.setVisibility(View.GONE);
        saveUserDetails = findViewById(R.id.saveUserDetails);
        saveUserDetails.setVisibility(View.GONE);

        pbUserDetails = findViewById(R.id.pbUserDetails);
        pbUserDetails.setVisibility(View.GONE);

        ImageView ivPDetails = findViewById(R.id.ivPDetails);

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

        new ShowUserDetails().execute(email, phone);

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
                new UpdateUserDetails().execute(email, phone);
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
                            new ShowUserDetails().execute(email, phone);
                        } else {
                            new UpdateUserDetails().execute(email, phone);
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
        tvExpiryDate.setClickable(false);
        ivProfilePic.setClickable(false);
        etAddress.setKeyListener(null);
        editUserDetails.setVisibility(View.VISIBLE);
    }

    private void editable(){
        editUserDetails.setVisibility(View.GONE);
        etCentre.setKeyListener((KeyListener) etCentre.getTag());
        etPassword.setKeyListener((KeyListener) etCentre.getTag());
        cbPart1.setEnabled(true);
        cbPart2.setEnabled(true);
        cbPart3.setEnabled(true);
        tvExpiryDate.setClickable(true);
        ivProfilePic.setClickable(true);
        etAddress.setKeyListener((KeyListener) etAddress.getTag());
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
        notificationARN = bmttUsersDO.getNotificationARN();
        userDetailsActivity.nonEditable();
    }

    private void updateLabel(Calendar myCalendar) {
        String myFormat = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        tvExpiryDate.setText(sdf.format(myCalendar.getTime()));
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
        String fullName;

        @Override
        protected void onPreExecute() {
            if (userDetailsActivity.pbUserDetails !=null)
                userDetailsActivity.pbUserDetails.setVisibility(View.VISIBLE);
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
            bmttUsersDO.setNotificationARN(notificationARN);
            try {
                dynamoDBMapper.save(bmttUsersDO);
                fullName = firstName + " " + lastName;
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
            } else {
                userDetailsActivity.showSnackBar("Network connection error!!", "update");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pbStatus){
            if (pbUserDetails !=null && pbUserDetails.getVisibility()==View.VISIBLE)
                pbUserDetails.setVisibility(View.GONE);
        }
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
