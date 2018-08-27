package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;
import com.payumoney.sdkui.ui.utils.ResultModel;
import com.sagsaguz.bmtt.adapter.SpinnerAdapter;
import com.sagsaguz.bmtt.payu.PayU;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.ActivitiesDO;
import com.sagsaguz.bmtt.utils.BmttAdminsDO;
import com.sagsaguz.bmtt.utils.BmttUsersDO;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.FileSubmissionDO;
import com.sagsaguz.bmtt.utils.PaymentDO;
import com.sagsaguz.bmtt.utils.SendEMail;
import com.sagsaguz.bmtt.utils.UserModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.sagsaguz.bmtt.MainBranchActivity.mainBranchActivity;

public class AddUserActivity extends AppCompatActivity implements View.OnClickListener{

    private static int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_SEND_SMS = 2 ;
    private static final int REQUEST_STORAGE = 3;

    private int total = 0;

    @SuppressLint("StaticFieldLeak")
    private static ScrollView scrollView;
    private CircleImageView ivProfilePic;
    private ImageView action_down, action_up;
    private RelativeLayout rlUserDetails;
    private EditText etFirstName, etLastName, etAddress,  etPhone, etEmail, etPassword;
    private FloatingActionButton add_installment1, add_installment2, sub_installment2, sub_installment3;
    private EditText installment1, installment2, installment3;
    private TextView date2, date3, tvTotal, etExpiryDate, etDOB;
    private RelativeLayout rl_installment1, rl_installment2, rl_installment3;
    private Spinner centreSpinner;
    private CheckBox cbPart1, cbPart2, cbPart3;
    private Button saveUser;
    @SuppressLint("StaticFieldLeak")
    private static ProgressBar pbAddUser;
    private ProgressDialog progressDialog;

    private RelativeLayout rlAddUser;

    Calendar myCalendar;
    DynamoDBMapper dynamoDBMapper;
    AmazonDynamoDBClient dynamoDBClient;

    private List<String> centreList = new ArrayList<>();
    private Map<String, String> centreCode = new HashMap<>();
    private ArrayAdapter<String> myAdapter;

    String filePath = "null";

    @SuppressLint("StaticFieldLeak")
    private static AddUserActivity addUserActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user_layout);

        addUserActivity = AddUserActivity.this;

        rlAddUser = findViewById(R.id.rlAddUser);

        progressDialog = new ProgressDialog(AddUserActivity.this, R.style.MyAlertDialogStyle);
        progressDialog.setMessage("Uploading, please wait.");
        progressDialog.setCancelable(true);

        scrollView = findViewById(R.id.scrollView);
        action_down = findViewById(R.id.action_down);
        action_down.setVisibility(View.GONE);
        action_up = findViewById(R.id.action_up);
        action_up.setVisibility(View.GONE);
        
        rlUserDetails = findViewById(R.id.rlUserDetails);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        ivProfilePic.setImageDrawable(getResources().getDrawable(R.drawable.bmtt_logo));
        /*Uri path = Uri.parse("res:///" + R.drawable.bmtt_logo);
        filePath = String.valueOf(path);*/
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbPart1 = findViewById(R.id.cbPart1);
        cbPart2 = findViewById(R.id.cbPart2);
        cbPart3 = findViewById(R.id.cbPart3);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etDOB = findViewById(R.id.etDOB);

        rl_installment1 = findViewById(R.id.rl_installment1);
        rl_installment2 = findViewById(R.id.rl_installment2);
        rl_installment2.setVisibility(View.GONE);
        rl_installment3 = findViewById(R.id.rl_installment3);
        rl_installment3.setVisibility(View.GONE);

        add_installment1 = findViewById(R.id.add_installment1);
        add_installment2 = findViewById(R.id.add_installment2);
        add_installment2.setVisibility(View.GONE);
        sub_installment2 = findViewById(R.id.sub_installment2);
        sub_installment3 = findViewById(R.id.sub_installment3);

        installment1 = findViewById(R.id.installment1);
        installment2 = findViewById(R.id.installment2);
        installment3 = findViewById(R.id.installment3);

        date2 = findViewById(R.id.date2);
        date3 = findViewById(R.id.date3);
        tvTotal = findViewById(R.id.tvTotal);

        saveUser = findViewById(R.id.saveUser);

        pbAddUser = findViewById(R.id.pbAddUser);
        pbAddUser.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
        pbAddUser.setVisibility(View.GONE);

        action_down.setOnClickListener(this);
        action_up.setOnClickListener(this);
        saveUser.setOnClickListener(this);
        //ivProfilePic.setOnClickListener(this);

        myCalendar = Calendar.getInstance();

        //Translating relative layout from bottom to position
        TranslateAnimation translate = new TranslateAnimation( 0, 0 , 3000, 0 );
        translate.setDuration(500);
        translate.setFillAfter( true );
        rlUserDetails.startAnimation(translate);
        translate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                action_down.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        add_installment1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_installment1.setVisibility(View.GONE);
                rl_installment2.setVisibility(View.VISIBLE);
                add_installment2.setVisibility(View.VISIBLE);
            }
        });

        add_installment2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_installment2.setVisibility(View.GONE);
                rl_installment3.setVisibility(View.VISIBLE);
                sub_installment2.setVisibility(View.INVISIBLE);
            }
        });

        sub_installment2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_installment2.setVisibility(View.GONE);
                rl_installment2.setVisibility(View.GONE);
                add_installment1.setVisibility(View.GONE);
                add_installment1.setVisibility(View.VISIBLE);
                if (!installment2.getText().toString().equals(""))
                    total = total - Integer.parseInt(installment2.getText().toString());
                installment2.setText("");
            }
        });

        sub_installment3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_installment3.setVisibility(View.GONE);
                add_installment2.setVisibility(View.VISIBLE);
                sub_installment2.setVisibility(View.VISIBLE);
                // not used, because total variable is not same as tvTotal
                //total = total - Integer.parseInt(installment3.getText().toString());
                installment3.setText("");
            }
        });

        date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String myFormat = "MMM dd, yyyy";
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                        date2.setText(sdf.format(myCalendar.getTime()));
                    }
                };
                new DatePickerDialog(AddUserActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        date3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String myFormat = "MMM dd, yyyy";
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                        date3.setText(sdf.format(myCalendar.getTime()));
                    }
                };
                new DatePickerDialog(AddUserActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        installment1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (installment1.getText().toString().equals("")){
                    total = Integer.parseInt(tvTotal.getText().toString());
                } else {
                    total = Integer.parseInt(tvTotal.getText().toString()) - Integer.parseInt(installment1.getText().toString());
                }
                return false;
            }
        });

        installment1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (installment1.getText().toString().equals(""))
                    total = Integer.parseInt(tvTotal.getText().toString());
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (installment1.getText().toString().equals("")){
                    tvTotal.setText("" + total);
                } else {
                    tvTotal.setText("" + (total + Integer.parseInt(charSequence.toString())));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        installment2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (installment2.getText().toString().equals("")){
                    total = Integer.parseInt(tvTotal.getText().toString());
                } else {
                    total = Integer.parseInt(tvTotal.getText().toString()) - Integer.parseInt(installment2.getText().toString());
                }
                return false;
            }
        });

        installment2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (installment2.getText().toString().equals(""))
                    total = Integer.parseInt(tvTotal.getText().toString());
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (installment2.getText().toString().equals("")){
                    tvTotal.setText("" + total);
                } else {
                    tvTotal.setText("" + (total + Integer.parseInt(charSequence.toString())));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        installment3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (installment3.getText().toString().equals("")){
                    total = Integer.parseInt(tvTotal.getText().toString());
                } else {
                    total = Integer.parseInt(tvTotal.getText().toString()) - Integer.parseInt(installment3.getText().toString());
                }
                return false;
            }
        });

        installment3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (installment3.getText().toString().equals(""))
                    total = Integer.parseInt(tvTotal.getText().toString());
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (installment3.getText().toString().equals("")){
                    tvTotal.setText("" + total);
                } else {
                    tvTotal.setText("" + (total + Integer.parseInt(charSequence.toString())));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        etExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel();
                    }
                };
                new DatePickerDialog(AddUserActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        etDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel2();
                    }
                };
                new DatePickerDialog(AddUserActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scrollView != null) {
                    if (scrollView.getScrollY()==0) {
                        action_up.setVisibility(View.GONE);
                    } else if (scrollView.getChildAt(0).getBottom() <= (scrollView.getHeight() + scrollView.getScrollY())) {
                        action_down.setVisibility(View.GONE);
                    } else {
                        action_down.setVisibility(View.VISIBLE);
                        action_up.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        centreSpinner = findViewById(R.id.centreSpinner);
        centreSpinner.setBackgroundColor(getResources().getColor(R.color.shadow));
        myAdapter = new SpinnerAdapter(this, R.layout.centre_spinner_item, centreList);
        centreSpinner.setAdapter(myAdapter);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.bmtt_logo);

        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "BMTT");

        boolean doSave = true;
        if (!dir.exists()) {
            doSave = dir.mkdirs();
        }

        if (doSave) {
            saveBitmapToFile(dir,"bmtt_logo.png",bm,Bitmap.CompressFormat.PNG,100);
        } else {
            Log.e("app","Couldn't create target directory.");
        }

        filePath = Environment.getExternalStorageDirectory() + "/BMTT/bmtt_logo.png";

        String userType = getIntent().getStringExtra("UTYPE");
        if (userType.equals("superAdmin")){
            new GetCentresList(dynamoDBClient, dynamoDBMapper).execute();
        } else {
            centreList.add(userType);
            centreCode.put(userType, getIntent().getStringExtra("CC"));
            myAdapter.notifyDataSetChanged();
        }
    }

    private boolean saveBitmapToFile(File dir, String fileName, Bitmap bm,
                                     Bitmap.CompressFormat format, int quality) {

        File imageFile = new File(dir,fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(format,quality,fos);

            fos.close();

            return true;
        }
        catch (IOException e) {
            Log.e("app",e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    public void basicSnackBar(String message){
        Snackbar snackbar = Snackbar.make(rlAddUser, message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(addUserActivity, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(addUserActivity, R.color.colorAccent));
        snackbar.show();
    }

    public void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(rlAddUser, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        smsPermissionCheck();
                    }
                });
        snackbar.setActionTextColor(ContextCompat.getColor(addUserActivity, R.color.colorAccent));

        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(addUserActivity, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(addUserActivity, R.color.colorAccent));
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    private void pickProfilePic(){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
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

            }
        } catch (Exception e) {
            Toast.makeText(this, "Please try again", Toast.LENGTH_LONG).show();
        }

    }

    private void uploadProfilePic(){

        progressDialog.show();
        basicSnackBar("Make sure you are connected to internet");
        try {
            BasicAWSCredentials credentials = new BasicAWSCredentials(Config.ACCESSKEY, Config.SECRETKEY);
            AmazonS3Client s3 = new AmazonS3Client(credentials);
            TransferUtility transferUtility = new TransferUtility(s3, AddUserActivity.this);

            File file = new File(filePath);
            String fileName = etPhone.getText().toString() + "_p001.jpg";
            final TransferObserver observer = transferUtility.upload(
                    Config.BUCKETNAME + "/profilePics",
                    fileName,
                    file,
                    CannedAccessControlList.PublicRead
            );

            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED.equals(observer.getState())) {
                        sendEmail();
                        sendSMS();
                        setUpPayment();
                        //createUser();
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                }

                @Override
                public void onError(int id, Exception ex) {
                    progressDialog.dismiss();
                    Log.d("profilePicUpload Failed", ex.getMessage());
                }
            });
        } catch (AmazonClientException e){
            showSnackBar("Network connection error!!");
            progressDialog.dismiss();
        }
    }

    private boolean isValidEmail(){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        Pattern patternEmail = Pattern.compile(emailPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcherEmail = patternEmail.matcher(etEmail.getText().toString());
        return matcherEmail.find();
    }

    private String currentDate(){
        myCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        return df.format(myCalendar.getTime());
    }

    private boolean isValidDate(String pDateString) throws ParseException {
        Date date = new SimpleDateFormat("MMM dd, yyyy").parse(pDateString);
        return new Date().before(date);
    }

    private void updateLabel() {
        String myFormat = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etExpiryDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void updateLabel2() {
        String myFormat = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDOB.setText(sdf.format(myCalendar.getTime()));
    }

    private boolean validateUserDetails(){
        if (TextUtils.isEmpty(etFirstName.getText().toString()) ||
                TextUtils.isEmpty(etLastName.getText().toString()) ||
                TextUtils.isEmpty(etDOB.getText().toString()) ||
                centreSpinner.getSelectedItem().toString().equals("centre *") ||
                TextUtils.isEmpty(etAddress.getText().toString()) ||
                TextUtils.isEmpty(etPhone.getText().toString()) ||
                TextUtils.isEmpty(etEmail.getText().toString()) ||
                TextUtils.isEmpty(etPassword.getText().toString()) ||
                /*(!cbPart1.isChecked() &&
                !cbPart2.isChecked() &&
                !cbPart3.isChecked()) ||*/
                TextUtils.isEmpty(installment1.getText().toString())||
                TextUtils.isEmpty(etExpiryDate.getText().toString()) ||
                ivProfilePic.getDrawable() == null){
            basicSnackBar("Please fill all the required fields.");
            return false;
        } else if(etFirstName.getText().toString().contains(" ")){
            basicSnackBar("Don't include spaces in first name");
            return false;
        } else if(isValidEmail()){
                try {
                    if(!isValidDate(etExpiryDate.getText().toString())){
                        basicSnackBar("Please select valid date.");
                        return false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
        } else {
            basicSnackBar("Please enter valid email id.");
            return false;
        }
        return true;
    }

    private void setUpPayment(){

        PaymentDO paymentDO = new PaymentDO();
        paymentDO.setEmailId(etEmail.getText().toString());
        paymentDO.setPhone(etPhone.getText().toString());
        Map<String, String> installments = new HashMap<>();
        Map<String, String> payment = new HashMap<>();
        installments.put(currentDate(), installment1.getText().toString());
        payment.put(currentDate(), "pending");
        if (rl_installment2.getVisibility() == View.VISIBLE){
            installments.put(date2.getText().toString(), installment2.getText().toString());
            payment.put(date2.getText().toString(), "pending");
        }
        if (rl_installment3.getVisibility() == View.VISIBLE){
            installments.put(date3.getText().toString(), installment3.getText().toString());
            payment.put(date3.getText().toString(), "pending");
        }
        paymentDO.setInstallments(installments);
        paymentDO.setPayment(payment);

        new PaymentDetails(paymentDO, dynamoDBClient).execute(dynamoDBMapper);

    }

    private void createUser(){

        UserModel userModel = new UserModel();
        userModel.setEmailId(etEmail.getText().toString().trim());
        userModel.setFirstName(etFirstName.getText().toString().trim());
        userModel.setLastName(etLastName.getText().toString().trim());
        userModel.setCentre(centreSpinner.getSelectedItem().toString());
        userModel.setAddress(etAddress.getText().toString());
        userModel.setPhone(etPhone.getText().toString());
        userModel.setPassword(etPassword.getText().toString());
        userModel.setBmttPart1(false);
        userModel.setBmttPart2(false);
        userModel.setBmttPart3(false);
        /*if(cbPart1.isChecked()){
            userModel.setBmttPart1(true);
        } else {
            userModel.setBmttPart1(false);
        }
        if(cbPart2.isChecked()){
            userModel.setBmttPart2(true);
        } else {
            userModel.setBmttPart2(false);
        }
        if(cbPart3.isChecked()){
            userModel.setBmttPart3(true);
        } else {
            userModel.setBmttPart3(false);
        }*/
        userModel.setCreatedDate(currentDate());
        userModel.setDob(etDOB.getText().toString());
        userModel.setExpiryDate(etExpiryDate.getText().toString());
        String profilePicPath = "https://s3.amazonaws.com/brightkidmont/profilePics/"+etPhone.getText().toString()+"_p001.jpg";
        userModel.setProfilePic(profilePicPath);

        progressDialog.dismiss();
        //dynamoDBCRUDOperations.createItem(dynamoDBMapper, userModel);
        new AddUser(userModel, dynamoDBClient).execute(dynamoDBMapper);
    }

    private void smsPermissionCheck(){
        int permissionCheck = ContextCompat.checkSelfPermission(AddUserActivity.this, android.Manifest.permission.SEND_SMS);
        if(permissionCheck == 0) {
            uploadProfilePic();
        } else {
            final Dialog dialog2 = new Dialog(AddUserActivity.this);
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
                    ActivityCompat.requestPermissions(AddUserActivity.this, new String[]{android.Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS);
                }
            });
            dialog2.show();
        }
    }

    private void storagePermissionCheck(){
        int permissionCheck = ContextCompat.checkSelfPermission(AddUserActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCheck == 0) {
            pickProfilePic();
        } else {
            final Dialog dialog2 = new Dialog(AddUserActivity.this);
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
                    //ActivityCompat.requestPermissions(DrawingPadActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                    ActivityCompat.requestPermissions(AddUserActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                }
            });
            dialog2.show();
        }
    }

    private void sendSMS(){
        String message = "Welcome to BMTT.\nYour login credentials are,\nLoginId : "+etEmail.getText().toString()+"\nPassword : "+etPassword.getText().toString();
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(etPhone.getText().toString(), null, message, null, null);
        } catch (Exception ex) {
            basicSnackBar("SMS sending failed");
            ex.printStackTrace();
        }
        /*Uri sms_uri = Uri.parse("smsto:+" + etPhone.getText().toString());
        Intent sms_intent = new Intent(Intent.ACTION_SENDTO, sms_uri);
        sms_intent.putExtra("sms_body", message);
        startActivity(sms_intent);*/
    }

    private void sendEmail() {
        String email = etEmail.getText().toString();
        String pas = etPassword.getText().toString();
        String subject = "BMTT app - Login credentials";
        String message = "Hi "+ etFirstName.getText().toString() +" "+ etLastName.getText().toString() + ",\n\n"+getString(R.string.login_message)+"\n\n"+
                " Here is your login credentials:\n\nLogin Id: "+email+" / "+etPhone.getText().toString()+"\n"+
                "Password: "+pas+"\n\nIf you face any login issues, please feel free to get in touch at bmtt@brightkidmont.com.\n\n"+
                "Kind regards,\nTeam BMTT";
        //String message = "Welcome to BMTT.\nYour login credentials are,\nLoginId : "+email+"\nPassword : "+pas;
        //Creating SendMail object
        SendEMail sm = new SendEMail(AddUserActivity.this, email, subject, message);
        sm.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    uploadProfilePic();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.action_down:
                scrollView.fullScroll(View.FOCUS_DOWN);
                break;
            case R.id.action_up:
                scrollView.fullScroll(View.FOCUS_UP);
                break;
            case R.id.etExpiryDate:
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel();
                    }
                };
                new DatePickerDialog(AddUserActivity.this,date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.saveUser:
                if(validateUserDetails()){
                    smsPermissionCheck();
                }
                break;
            case R.id.ivProfilePic:
                storagePermissionCheck();
                break;
        }
    }

    private class PaymentDetails extends AsyncTask<DynamoDBMapper, Void, String>{

        PaymentDO paymentDO = new PaymentDO();
        AmazonDynamoDBClient dynamoDBClient;

        PaymentDetails(PaymentDO paymentDO, AmazonDynamoDBClient dynamoDBClient){
            this.paymentDO = paymentDO;
            this.dynamoDBClient = dynamoDBClient;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(DynamoDBMapper... dynamoDBMappers) {
            try {
                ScanResult result = null;
                do {
                    ScanRequest req = new ScanRequest();
                    req.setTableName(Config.PAYMENTTABLENAME);
                    if (result != null) {
                        req.setExclusiveStartKey(result.getLastEvaluatedKey());
                    }
                    result = dynamoDBClient.scan(req);
                    List<Map<String, AttributeValue>> rows = result.getItems();
                    for (Map<String, AttributeValue> map : rows) {
                        try {
                            if (map.get("emailId").getS().equals(paymentDO.getEmailId()) && map.get("phone").getS().equals(paymentDO.getPhone())) {
                                return "failed";
                            }
                        } catch (NumberFormatException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } while (result.getLastEvaluatedKey() != null);

                dynamoDBMappers[0].save(paymentDO);

                return "added";
            } catch (AmazonClientException e){
                addUserActivity.showSnackBar("Network connection error!!");
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String str) {
            progressDialog.dismiss();
            if(str.equals("added")) {
                createUser();
            } else if(str.equals("failed")) {
                addUserActivity.basicSnackBar("User with this email id already exists.");
            }
        }
    }

    private static class AddUser extends AsyncTask<DynamoDBMapper, Void, String> {

        BmttUsersDO bmttUsersDO = new BmttUsersDO();
        UserModel userModel = new UserModel();
        AmazonDynamoDBClient dynamoDBClient;
        int userId = 0;
        String lUserId;

        AddUser(UserModel userModel, AmazonDynamoDBClient dynamoDBClient){
            this.userModel = userModel;
            this.dynamoDBClient = dynamoDBClient;
        }

        @Override
        protected void onPreExecute() {
            addUserActivity.progressDialog.show();
            bmttUsersDO.setEmailId(userModel.getEmailId());
            bmttUsersDO.setFirstName(userModel.getFirstName());
            bmttUsersDO.setLastName(userModel.getLastName());
            bmttUsersDO.setCentre(userModel.getCentre());
            bmttUsersDO.setAddress(userModel.getAddress());
            bmttUsersDO.setPhone(userModel.getPhone());
            bmttUsersDO.setPassword(userModel.getPassword());
            bmttUsersDO.setBmttPart1(userModel.getBmttPart1());
            bmttUsersDO.setBmttPart2(userModel.getBmttPart2());
            bmttUsersDO.setBmttPart3(userModel.getBmttPart3());
            bmttUsersDO.setCreatedDate(userModel.getCreatedDate());
            bmttUsersDO.setExpiryDate(userModel.getExpiryDate());
            bmttUsersDO.setDob(userModel.getDob());
            bmttUsersDO.setProfilePic(userModel.getProfilePic());
            bmttUsersDO.setNotificationARN("null");
            bmttUsersDO.setDocSubmission(false);
            bmttUsersDO.setAdmissionDone("pending");

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdfmm = new SimpleDateFormat("MM");
            SimpleDateFormat sdfyy = new SimpleDateFormat("yy");
            String codedDate = sdfyy.format(calendar.getTime()) + sdfmm.format(calendar.getTime());

            lUserId = addUserActivity.centreCode.get(userModel.getCentre());
        }

        @Override
        protected String doInBackground(DynamoDBMapper...dynamoDBMappers) {
            try {
                ScanResult result = null;
                do {
                    ScanRequest req = new ScanRequest();
                    req.setTableName(Config.USERSTABLENAME);
                    if (result != null) {
                        req.setExclusiveStartKey(result.getLastEvaluatedKey());
                    }
                    result = dynamoDBClient.scan(req);
                    List<Map<String, AttributeValue>> rows = result.getItems();
                    for (Map<String, AttributeValue> map : rows) {
                        try {
                            if (map.get("emailId").getS().equals(userModel.getEmailId()) && map.get("phone").getS().equals(userModel.getPhone())) {
                                return "failed";
                            }
                            String stringId = map.get("userId").getS();
                            stringId = stringId.substring(5);
                            if (stringId.length() > 5)
                                return "userIdError";
                            if (userId < Integer.parseInt(stringId))
                                userId = Integer.parseInt(stringId);
                            /*String stringId = map.get("userId").getS();
                            stringId = stringId.substring(stringId.length()-2);
                            if (map.get("userId").getS().contains(lUserId) && userId < Integer.parseInt(stringId))
                                userId = Integer.parseInt(stringId);
                            if (stringId.length() > 2)
                                return "userIdError";*/
                        } catch (NumberFormatException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } while (result.getLastEvaluatedKey() != null);

                userId++;
                StringBuilder extras = new StringBuilder();
                for (int i=0; i<5-String.valueOf(userId).length(); i++){
                    extras.append("0");
                }

                bmttUsersDO.setUserId(lUserId + extras + String.valueOf(userId));

                dynamoDBMappers[0].save(bmttUsersDO);

                FileSubmissionDO fileSubmissionDO = new FileSubmissionDO();
                fileSubmissionDO.setEmailID(userModel.getEmailId());
                fileSubmissionDO.setPhone(userModel.getPhone());
                fileSubmissionDO.setFile1("not");
                fileSubmissionDO.setFile2("not");
                fileSubmissionDO.setFile3("not");
                fileSubmissionDO.setFile4("not");
                dynamoDBMappers[0].save(fileSubmissionDO);

                ActivitiesDO activitiesDO = new ActivitiesDO();
                activitiesDO.setEmailID(userModel.getEmailId());
                activitiesDO.setPhone(userModel.getPhone());
                List<String> doneList = new ArrayList<>();
                List<String> attendedList = new ArrayList<>();
                activitiesDO.setDone(doneList);
                activitiesDO.setAttended(attendedList);
                dynamoDBMappers[0].save(activitiesDO);

                return "added";
            } catch (AmazonClientException e){
                addUserActivity.showSnackBar("Network connection error!!");
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String str) {
            addUserActivity.progressDialog.dismiss();
            switch (str) {
                case "added":
                    mainBranchActivity.finish();
                    addUserActivity.startActivity(new Intent(addUserActivity, MainBranchActivity.class));
                    addUserActivity.finish();
                    break;
                case "failed":
                    addUserActivity.basicSnackBar("User with this email id already exists.");
                    break;
                case "userIdError":
                    addUserActivity.basicSnackBar("Cannot register user, please contact Head Office.");
                    break;
            }
        }
    }


    private static class GetCentresList extends AsyncTask<Void, Void, Boolean>{

        AmazonDynamoDBClient dynamoDBClient;
        DynamoDBMapper dynamoDBMapper;

        GetCentresList(AmazonDynamoDBClient dynamoDBClient, DynamoDBMapper dynamoDBMapper){
            this.dynamoDBClient = dynamoDBClient;
            this.dynamoDBMapper = dynamoDBMapper;
        }

        @Override
        protected void onPreExecute() {
            addUserActivity.centreList.clear();
            addUserActivity.centreCode.clear();
            //addUserActivity.centreList.add("centre *");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {

                ScanResult result = null;
                do {
                    ScanRequest req = new ScanRequest();
                    req.setTableName(Config.ADMINTABLENAME);
                    if (result != null) {
                        req.setExclusiveStartKey(result.getLastEvaluatedKey());
                    }
                    result = dynamoDBClient.scan(req);
                    List<Map<String, AttributeValue>> rows = result.getItems();
                    for (Map<String, AttributeValue> map : rows) {
                        try {
                            if (!map.get("centre").getS().equals("All users")) {
                                addUserActivity.centreList.add(map.get("centre").getS());
                                addUserActivity.centreCode.put(map.get("centre").getS(), map.get("centerCode").getS());
                            }
                        } catch (NumberFormatException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } while (result.getLastEvaluatedKey() != null);

                return true;
            } catch (AmazonClientException e){
                addUserActivity.basicSnackBar("Network connection error!!");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result){
                new GetCentresList(dynamoDBClient, dynamoDBMapper).execute();
            } else {
                addUserActivity.myAdapter.notifyDataSetChanged();
            }
        }
    }

}
