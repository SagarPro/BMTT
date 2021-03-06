package com.sagsaguz.bmtt;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.sagsaguz.bmtt.adapter.CentresListAdapter;
import com.sagsaguz.bmtt.adapter.UsersListAdapter;
import com.sagsaguz.bmtt.services.FirebaseDispatcher;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.ActivitiesDO;
import com.sagsaguz.bmtt.utils.AmountDO;
import com.sagsaguz.bmtt.utils.BmttAdminsDO;
import com.sagsaguz.bmtt.utils.BmttUsersDO;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.DynamoDBCRUDOperations;
import com.sagsaguz.bmtt.utils.FileSubmissionDO;
import com.sagsaguz.bmtt.utils.PaymentDO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainBranchActivity extends AppCompatActivity{

    @SuppressLint("StaticFieldLeak")
    private static ProgressBar pbMainBranch;

    private ConstraintLayout clMainBranch;
    private ImageView ivLogout, ivAdd, ivQA, ivNotifications, ivWebinar, ivBill, ivAttachment;
    private RelativeLayout rlQA;
    private LinearLayout llBottom;
    @SuppressLint("StaticFieldLeak")
    public static View qaIndicator;
    private View view1, view2, view3, view4, view5;

    @SuppressLint("StaticFieldLeak")
    private static TextView tvMessage;

    private static List<String> userNames = new ArrayList<>();
    private static List<String> uNames = new ArrayList<>();
    private static HashMap<String, String> userCentres = new HashMap<String, String>();
    private static HashMap<String, String> userEmailAddress = new HashMap<String, String>();
    private static HashMap<String, String> userPhone = new HashMap<String, String>();
    private static HashMap<String, String> userPics = new HashMap<String, String>();
    private static HashMap<String, String> userId = new HashMap<String, String>();

    private ListView lvUsers;
    @SuppressLint("StaticFieldLeak")
    private static UsersListAdapter usersListAdapter;

    private static AmazonDynamoDBClient dynamoDBClient;
    private AmazonS3 s3client;

    @SuppressLint("StaticFieldLeak")
    public static MainBranchActivity mainBranchActivity;
    private static Dialog dialog;
    private static String adminType = "SuperAdmin";

    private static ProgressDialog progressDialog;

    private SharedPreferences adminPreferences;
    private SharedPreferences qaPref;
    private String email, login, centre, userType;

    FirebaseJobDispatcher firebaseJobDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_branch_layout);

        mainBranchActivity = MainBranchActivity.this;

        firebaseJobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job job = firebaseJobDispatcher.newJobBuilder()
                .setService(FirebaseDispatcher.class)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTag("1")
                .setTrigger(Trigger.executionWindow(300,480))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setReplaceCurrent(false)
                .setConstraints(Constraint.ON_ANY_NETWORK).build();

        firebaseJobDispatcher.mustSchedule(job);

        /*getSupportActionBar().setTitle("BMTT");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/

        clMainBranch = findViewById(R.id.clMainBranch);

        lvUsers = findViewById(R.id.lvUsers);
        tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setVisibility(View.GONE);

        ivLogout = findViewById(R.id.ivLogout);
        ivAdd = findViewById(R.id.ivAdd);
        ivQA = findViewById(R.id.ivQA);
        ivWebinar = findViewById(R.id.ivWebinar);
        ivBill = findViewById(R.id.ivBill);
        ivAttachment = findViewById(R.id.ivAttachment);
        ivNotifications = findViewById(R.id.ivNotifications);

        llBottom = findViewById(R.id.llBottom);
        llBottom.setVisibility(View.INVISIBLE);

        rlQA = findViewById(R.id.rlQA);

        SharedPreferences autoStart = getSharedPreferences("AUTOSTART", MODE_PRIVATE);
        Boolean enable = autoStart.getBoolean("ENABLE", false);
        if (!enable){
            final Intent intent = new Intent();
            if ("xiaomi".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if ("oppo".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if ("vivo".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
                intent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.MainGuideActivity."));
            }
            if (intent.getExtras() != null){
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.custom_dialog);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                TextView tvTitle = dialog.findViewById(R.id.tvTitle);
                tvTitle.setText("Auto Start permission");
                TextView tvMessage = dialog.findViewById(R.id.tvMessage);
                tvMessage.setText("Click OK to enable auto start permission for receiving notifications or you can allow it later manually.");
                Button btnOk = dialog.findViewById(R.id.btnRemove);
                btnOk.setText("OK");
                Button btnCancel = dialog.findViewById(R.id.btnCancel);

                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                startActivity(intent);

                SharedPreferences.Editor editor = autoStart.edit();
                editor.putBoolean("ENABLE", true);
                editor.apply();

                dialog.show();
            }
        }

        qaIndicator = findViewById(R.id.qaIndicator);
        qaPref = getSharedPreferences("QAINDICATOR", MODE_PRIVATE);
        if (qaPref.getBoolean("QINDICATOR", false)){
            qaIndicator.setVisibility(View.VISIBLE);
        } else {
            qaIndicator.setVisibility(View.GONE);
        }

        view1 = findViewById(R.id.view1);
        view2 = findViewById(R.id.view2);
        view3 = findViewById(R.id.view3);
        view4 = findViewById(R.id.view4);
        view5 = findViewById(R.id.view5);

        adminPreferences = getSharedPreferences("USERDETAILS", MODE_PRIVATE);
        userType = adminPreferences.getString("USERTYPE", "SuperAdmin");
        if(userType.equals("admin")){
            //ivAdd.setVisibility(View.GONE);
            ivQA.setVisibility(View.GONE);
            rlQA.setVisibility(View.GONE);
            ivWebinar.setVisibility(View.GONE);
            ivBill.setVisibility(View.GONE);
            ivAttachment.setVisibility(View.GONE);
            //view1.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
            view3.setVisibility(View.GONE);
            view4.setVisibility(View.GONE);
            view5.setVisibility(View.GONE);
            email = adminPreferences.getString("EMAIL", null);
            login = adminPreferences.getString("LOGIN", null);
        } else {
            login = adminPreferences.getString("LOGIN", null);
        }
        centre = adminPreferences.getString("CENTRE", null);
        adminType = userType;

        progressDialog = new ProgressDialog(MainBranchActivity.this, R.style.MyAlertDialogStyle);

        pbMainBranch = findViewById(R.id.pbMainBranch);
        pbMainBranch.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);

        AWSProvider awsProvider = new AWSProvider();
        dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        s3client = new AmazonS3Client(awsProvider.getCredentialsProvider(getBaseContext()));

        usersListAdapter = new UsersListAdapter(MainBranchActivity.this, userNames, userCentres, userPics, userId);
        lvUsers.setAdapter(usersListAdapter);

        if(adminType.equals("SuperAdmin")){
            new ShowAllUsers().execute();
        } else {
            new ShowUserByCentre().execute(centre);
        }

        /*lvUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

                dialog = new Dialog(mainBranchActivity);
                dialog.setContentView(R.layout.custom_dialog);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                TextView tvTitle = dialog.findViewById(R.id.tvTitle);
                tvTitle.setText("Delete user");
                TextView tvMessage = dialog.findViewById(R.id.tvMessage);
                tvMessage.setText("Are you sure, you want to delete\n"+userNames.get(i));
                Button btnDelete = dialog.findViewById(R.id.btnRemove);
                btnDelete.setText("Delete");
                Button btnCancel = dialog.findViewById(R.id.btnCancel);

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                                .dynamoDBClient(dynamoDBClient)
                                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                                .build();
                        new DeleteUser(dynamoDBMapper).execute(i);
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
            }
        });*/

        ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = adminPreferences.edit();
                editor.putString("LOGIN", "logout");
                editor.apply();
                startActivity(new Intent(getBaseContext(), LoginActivity.class));
                finish();
            }
        });

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storagePermissionCheck();
            }
        });

        ivQA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qaIndicator.setVisibility(View.GONE);
                SharedPreferences.Editor editor = qaPref.edit();
                editor.putBoolean("QINDICATOR", false);
                editor.apply();
                Intent intent = new Intent(getBaseContext(), QAActivity.class);
                intent.putExtra("USERTYPE", "admin");
                startActivity(intent);
            }
        });

        ivWebinar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), WebinarActivity.class);
                intent.putExtra("USERTYPE", "admin");
                startActivity(intent);
            }
        });

        ivBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadAmount().execute();
            }
        });

        ivAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AttachmentsActivity.class);
                intent.putExtra("USERTYPE", "admin");
                startActivity(intent);
            }
        });

        ivNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), NotificationActivity.class);
                intent.putExtra("USERTYPE", userType);
                intent.putExtra("EMAIL", email);
                intent.putExtra("CENTRE", centre);
                startActivity(intent);
            }
        });

    }

    public String getUserEmail(int position){
        return userEmailAddress.get(userNames.get(position));
    }

    public String getUserPhone(int position){
        return userPhone.get(userNames.get(position));
    }


    public void showSnackBar(String message, final String type){
        Snackbar snackbar = Snackbar.make(clMainBranch, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(type.equals("main")){
                            new ShowAllUsers().execute();
                        } else if(type.equals("centre")){
                            if(centre != null)
                                new ShowUserByCentre().execute(centre);
                        }
                    }
                });
        snackbar.setActionTextColor(ContextCompat.getColor(mainBranchActivity, R.color.colorAccent));

        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(mainBranchActivity, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(mainBranchActivity, R.color.colorAccent));
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    private void storagePermissionCheck(){
        int permissionCheck = ContextCompat.checkSelfPermission(mainBranchActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck == 0) {
            if (userType.equals("admin")){
                Intent intent = new Intent(MainBranchActivity.this, AddUserActivity.class);
                intent.putExtra("UTYPE", centre);
                intent.putExtra("CC", adminPreferences.getString("CCODE", null));
                startActivity(intent);
            } else {
                showAddDialog();
            }
        } else {
            final Dialog dialog2 = new Dialog(mainBranchActivity);
            dialog2.setContentView(R.layout.permission_dialog);

            TextView dialog_message = dialog2.findViewById(R.id.dialog_message);
            dialog_message.setText("This app needs storage permission to continue to use its features.");
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
                    ActivityCompat.requestPermissions(mainBranchActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                }
            });
            dialog2.show();
        }
    }


    private void showAddDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.centres_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final List<String> userType = new ArrayList<>();
        userType.add("Admin");
        userType.add("Student");

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        tvTitle.setText("Select UserType");
        ListView lvAddUser = dialog.findViewById(R.id.lvCentres);

        CentresListAdapter centresListAdapter = new CentresListAdapter(MainBranchActivity.this, userType);
        lvAddUser.setAdapter(centresListAdapter);

        lvAddUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                if(userType.get(position).equals("Admin")){
                    showAddAdminDialog();
                } else {
                    Intent intent = new Intent(MainBranchActivity.this, AddUserActivity.class);
                    intent.putExtra("UTYPE", "superAdmin");
                    startActivity(intent);
                }
            }
        });

        dialog.show();
    }

    private void showAddAdminDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_admin_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final EditText etAdminEmail = dialog.findViewById(R.id.etAdminEmail);
        final EditText etAdminPhone = dialog.findViewById(R.id.etAdminPhone);
        final EditText etAdminCentre = dialog.findViewById(R.id.etAdminCentre);
        final EditText etAdminPassword = dialog.findViewById(R.id.etAdminPassword);
        final EditText etCenterCode = dialog.findViewById(R.id.etCenterCode);
        final EditText etLocation = dialog.findViewById(R.id.etLocation);

        Button btnAdd = dialog.findViewById(R.id.btnAdd);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                        .dynamoDBClient(dynamoDBClient)
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .build();

                if(TextUtils.isEmpty(etAdminEmail.getText().toString()) ||
                        TextUtils.isEmpty(etAdminPhone.getText().toString()) ||
                        TextUtils.isEmpty(etAdminCentre.getText().toString()) ||
                        TextUtils.isEmpty(etAdminPassword.getText().toString()) ||
                        TextUtils.isEmpty(etCenterCode.getText().toString()) ||
                        TextUtils.isEmpty(etLocation.getText().toString())){
                    Toast.makeText(mainBranchActivity, "Please enter valid details for all fields.", Toast.LENGTH_SHORT).show();
                } else {
                    new AddAdmin(dynamoDBClient, dynamoDBMapper).execute(etAdminEmail.getText().toString().trim(),
                            etAdminPhone.getText().toString().trim(), etAdminCentre.getText().toString().trim(),
                            etAdminPassword.getText().toString().trim(), etCenterCode.getText().toString().trim(),
                            etLocation.getText().toString().trim());
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showCentresDialog(List<List<String>> centreDetails){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.centres_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ListView lvCentres = dialog.findViewById(R.id.lvCentres);
        final List<String> centreNames = new ArrayList<>();

        centreNames.addAll(centreDetails.get(0));

        Collections.sort(centreNames, String.CASE_INSENSITIVE_ORDER);
        CentresListAdapter centresListAdapter = new CentresListAdapter(MainBranchActivity.this, centreNames);
        lvCentres.setAdapter(centresListAdapter);

        lvCentres.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (centreNames.get(position).equals("All users")) {
                    new ShowAllUsers().execute();
                } else {
                    new ShowUserByCentre().execute(centreNames.get(position));
                }
            }
        });

        final List<List<String>> finalCd = centreDetails;
        lvCentres.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog.dismiss();
                for (int v = 0; v < centreNames.size(); v++) {
                    if (finalCd.get(0).get(v).equals(centreNames.get(i))) {
                        showCentreDetailsDialog(finalCd.get(0).get(v),
                                finalCd.get(1).get(v),
                                finalCd.get(2).get(v),
                                finalCd.get(3).get(v),
                                finalCd.get(4).get(v),
                                finalCd.get(5).get(v));
                        break;
                    }
                }
                return true;
            }
        });
        dialog.show();
    }

    private void showCentreDetailsDialog(String cName, String cEmail, String cPhone, String cPassword, String cCode, String cLocation){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.centre_details_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final TextView tvCName = dialog.findViewById(R.id.tvCName);
        tvCName.setText(cName);
        final TextView tvCCode = dialog.findViewById(R.id.tvCCode);
        tvCCode.setText(cCode);
        final TextView tvCEmail = dialog.findViewById(R.id.tvCEmail);
        tvCEmail.setText(cEmail);
        final TextView tvCPhone = dialog.findViewById(R.id.tvCPhone);
        tvCPhone.setText(cPhone);
        final EditText etCPassword = dialog.findViewById(R.id.etCPassword);
        etCPassword.setText(cPassword);
        etCPassword.setTag(etCPassword.getKeyListener());
        etCPassword.setKeyListener(null);
        final EditText etCLocation = dialog.findViewById(R.id.etCLocation);
        etCLocation.setText(cLocation);
        etCLocation.setTag(etCLocation.getKeyListener());
        etCLocation.setKeyListener(null);

        Button btnCancel = dialog.findViewById(R.id.btnCCancel);
        final Button btnEdit = dialog.findViewById(R.id.btnCEdit);
        final Button btnSave = dialog.findViewById(R.id.btnCSave);
        Button btnDelete = dialog.findViewById(R.id.btnCDelete);
        btnSave.setVisibility(View.GONE);

        if (cName.equals("All users"))
            btnDelete.setVisibility(View.GONE);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEdit.setVisibility(View.GONE);
                etCPassword.setKeyListener((KeyListener) etCPassword.getTag());
                etCLocation.setKeyListener((KeyListener) etCLocation.getTag());
                btnSave.setVisibility(View.VISIBLE);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSave.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
                etCPassword.setKeyListener(null);
                etCLocation.setKeyListener(null);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                                .dynamoDBClient(dynamoDBClient)
                                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                                .build();
                        BmttAdminsDO bmttAdminsDO = new BmttAdminsDO();
                        bmttAdminsDO.setCentre(tvCName.getText().toString());
                        bmttAdminsDO.setCenterCode(tvCCode.getText().toString());
                        bmttAdminsDO.setEmailId(tvCEmail.getText().toString());
                        bmttAdminsDO.setPhone(tvCPhone.getText().toString());
                        bmttAdminsDO.setPassword(etCPassword.getText().toString());
                        bmttAdminsDO.setLocation(etCLocation.getText().toString());
                        dynamoDBMapper.save(bmttAdminsDO);
                    }
                }).start();
                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog1 = new Dialog(MainBranchActivity.this);
                dialog1.setContentView(R.layout.custom_dialog);
                dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog1.setCancelable(true);

                TextView tvTitle = dialog1.findViewById(R.id.tvTitle);
                tvTitle.setText("Center Deletion");
                TextView tvMessage = dialog1.findViewById(R.id.tvMessage);
                tvMessage.setText("Are you sure, you want to delete\n"+tvCName.getText().toString());
                Button btnDelete = dialog1.findViewById(R.id.btnRemove);
                btnDelete.setText("Delete");
                Button btnCancel = dialog1.findViewById(R.id.btnCancel);

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                                        .dynamoDBClient(dynamoDBClient)
                                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                                        .build();
                                BmttAdminsDO bmttAdminsDO = new BmttAdminsDO();
                                bmttAdminsDO.setEmailId(tvCEmail.getText().toString());
                                bmttAdminsDO.setPhone(tvCPhone.getText().toString());
                                dynamoDBMapper.delete(bmttAdminsDO);
                            }
                        }).start();
                        dialog1.dismiss();
                        dialog.dismiss();
                        Toast.makeText(MainBranchActivity.this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog1.dismiss();
                    }
                });

                dialog1.show();

            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_branch_menu, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_user));
        final EditText etSearchView = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = etSearchView.getText().toString().toLowerCase();
                List<String> newList = new ArrayList<>();
                userNames.clear();
                newList.addAll(uNames);
                for (String str : newList){
                    String name = str.toLowerCase();
                    String filteredName = name.substring(0, name.indexOf("_"));
                    if(filteredName.contains(newText)) {
                        userNames.add(str);
                    }
                }
                usersListAdapter.notifyDataSetChanged();
                return true;
            }
        });
        MenuItem showCentres = menu.findItem(R.id.show_centres);
        if(adminType.equals("SuperAdmin")){
            showCentres.setVisible(true);
        } else {
            showCentres.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.show_centres) {
            new GetCentreDetails().execute(dynamoDBClient);
            return true;
        }

        if (id == R.id.admissions){
            startActivity(new Intent(mainBranchActivity, AdmissionEnquiriesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteUser(final int i){

        if (!userType.equals("admin")) {

            dialog = new Dialog(mainBranchActivity);
            dialog.setContentView(R.layout.custom_dialog);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            TextView tvTitle = dialog.findViewById(R.id.tvTitle);
            tvTitle.setText("Delete user");
            TextView tvMessage = dialog.findViewById(R.id.tvMessage);
            tvMessage.setText("Are you sure, you want to delete\n" + userNames.get(i));
            Button btnDelete = dialog.findViewById(R.id.btnRemove);
            btnDelete.setText("Delete");
            Button btnCancel = dialog.findViewById(R.id.btnCancel);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                            .dynamoDBClient(dynamoDBClient)
                            .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                            .build();
                    new DeleteUser(dynamoDBMapper).execute(i);
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } else {
            Toast.makeText(mainBranchActivity, "Please contact head office for user deletion.", Toast.LENGTH_LONG).show();
        }

    }


    private static class ShowAllUsers extends AsyncTask<Void, Void, Boolean>{

        ScanResult response;

        @Override
        protected void onPreExecute() {
            userNames.clear();
            userPhone.clear();
            userCentres.clear();
            userEmailAddress.clear();
            userPics.clear();
            userId.clear();
            if (pbMainBranch !=null)
                pbMainBranch.setVisibility(View.VISIBLE);
            if (dialog!=null && dialog.isShowing()){
                dialog.dismiss();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                ScanRequest request = new ScanRequest().withTableName(Config.USERSTABLENAME);
                response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> rows = response.getItems();
                for(Map<String, AttributeValue> map : rows){
                    try{
                        AttributeValue firstName = map.get("firstName");
                        AttributeValue lastName = map.get("lastName");
                        String name = firstName.getS() + " " + lastName.getS() + "_"+map.get("emailId").getS();
                        userNames.add(name);
                        userPhone.put(name, map.get("phone").getS());
                        userCentres.put(name, map.get("centre").getS());
                        userEmailAddress.put(name, map.get("emailId").getS());
                        userPics.put(name, map.get("profilePic").getS());
                        userId.put(name, map.get("userId").getS());
                    } catch (NumberFormatException e){
                        Log.d("number_format_exception", e.getMessage());
                    }
                }
                return true;
            } catch (AmazonClientException e){
                mainBranchActivity.showSnackBar("Network connection error!!", "main");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pbMainBranch !=null)
                pbMainBranch.setVisibility(View.GONE);
            if(result) {
                Collections.sort(userNames, String.CASE_INSENSITIVE_ORDER);
                usersListAdapter.notifyDataSetChanged();
                uNames.clear();
                uNames.addAll(userNames);
                if (userNames.size() == 0) {
                    tvMessage.setVisibility(View.VISIBLE);
                } else {
                    tvMessage.setVisibility(View.GONE);
                }
                mainBranchActivity.llBottom.setVisibility(View.VISIBLE);
            }
        }
    }



    private static class ShowUserByCentre extends AsyncTask<String, Void, Boolean>{

        ScanResult response;

        @Override
        protected void onPreExecute() {
            userNames.clear();
            userCentres.clear();
            userEmailAddress.clear();
            userPics.clear();
            userId.clear();
            if (pbMainBranch !=null)
                pbMainBranch.setVisibility(View.VISIBLE);
            if (dialog!=null && dialog.isShowing()){
                dialog.dismiss();
            }
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                ScanRequest request = new ScanRequest().withTableName(Config.USERSTABLENAME);
                response = dynamoDBClient.scan(request);
                List<Map<String, AttributeValue>> rows = response.getItems();
                for (Map<String, AttributeValue> map : rows) {
                    try {
                        if (map.get("centre").getS().equals(strings[0])) {
                            AttributeValue firstName = map.get("firstName");
                            AttributeValue lastName = map.get("lastName");
                            String name = firstName.getS() + " " + lastName.getS() + "_"+map.get("emailId").getS();
                            userNames.add(name);
                            userPhone.put(name, map.get("phone").getS());
                            userCentres.put(name, map.get("centre").getS());
                            userEmailAddress.put(name, map.get("emailId").getS());
                            userPics.put(name, map.get("profilePic").getS());
                            userId.put(name, map.get("userId").getS());
                        }
                    } catch (NumberFormatException e) {
                        Log.d("number_format_exception", e.getMessage());
                    }
                }
                return true;
            } catch (AmazonClientException e){
                mainBranchActivity.showSnackBar("Network connection error!!", "centre");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pbMainBranch !=null)
                pbMainBranch.setVisibility(View.GONE);
            if (result) {
                Collections.sort(userNames, String.CASE_INSENSITIVE_ORDER);
                usersListAdapter.notifyDataSetChanged();
                uNames.clear();
                uNames.addAll(userNames);
                if (userNames.size() == 0) {
                    tvMessage.setVisibility(View.VISIBLE);
                } else {
                    tvMessage.setVisibility(View.GONE);
                }
                mainBranchActivity.llBottom.setVisibility(View.VISIBLE);
            }
        }
    }


    private static class AddAdmin extends AsyncTask<String, Void, String> {

        BmttAdminsDO bmttAdminsDO = new BmttAdminsDO();
        AmazonDynamoDBClient dynamoDBClient;
        DynamoDBMapper dynamoDBMapper;

        AddAdmin(AmazonDynamoDBClient dynamoDBClient, DynamoDBMapper dynamoDBMapper){
            this.dynamoDBClient = dynamoDBClient;
            this.dynamoDBMapper = dynamoDBMapper;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Adding admin, please wait.");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                String email = strings[0],
                        phone = strings[1],
                        centre = strings[2],
                        password = strings[3],
                        centerCode = strings[4],
                        location = strings[5];

                centerCode = centerCode.toUpperCase();

                ScanResult result = null;
                do{
                    ScanRequest req = new ScanRequest();
                    req.setTableName(Config.ADMINTABLENAME);
                    if(result != null){
                        req.setExclusiveStartKey(result.getLastEvaluatedKey());
                    }
                    result = dynamoDBClient.scan(req);
                    List<Map<String, AttributeValue>> rows = result.getItems();
                    for(Map<String, AttributeValue> map : rows){
                        try{
                            if (map.get("emailId").getS().equals(email)) {
                                return "exist";
                            }
                            if (map.get("centerCode").getS().equals(centerCode)){
                                return "cexists";
                            }
                        } catch (NumberFormatException e){
                            System.out.println(e.getMessage());
                        }
                    }
                } while(result.getLastEvaluatedKey() != null);

                bmttAdminsDO.setEmailId(email);
                bmttAdminsDO.setPhone(phone);
                bmttAdminsDO.setCentre(centre);
                bmttAdminsDO.setPassword(password);
                bmttAdminsDO.setCenterCode(centerCode);
                bmttAdminsDO.setLocation(location);

                dynamoDBMapper.save(bmttAdminsDO);

                return "added";
            } catch (AmazonClientException e){
                mainBranchActivity.showSnackBar("Network connection error!!", "add");
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String str) {
            progressDialog.dismiss();
            if(pbMainBranch!=null)
                pbMainBranch.setVisibility(View.GONE);
            switch (str) {
                case "exist":
                    Toast.makeText(mainBranchActivity, "Admin with this email address is already exists.", Toast.LENGTH_LONG).show();
                    break;
                case "added":
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    Toast.makeText(mainBranchActivity, "Successfully added ", Toast.LENGTH_SHORT).show();
                    break;
                case "cexists":
                    Toast.makeText(mainBranchActivity, "Admin with this center code is already exists.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    break;
            }
        }
    }


    private static class GetCentreDetails extends AsyncTask<AmazonDynamoDBClient, Void, List<List<String>>>{
        List<String> centreNames = new ArrayList<>();
        List<String> centreEmail = new ArrayList<>();
        List<String> centrePhone = new ArrayList<>();
        List<String> centrePassword = new ArrayList<>();
        List<String> centreCode = new ArrayList<>();
        List<String> centreLocation = new ArrayList<>();
        List<List<String>> cd = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            if(pbMainBranch!=null)
                pbMainBranch.setVisibility(View.VISIBLE);
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
                                centreCode.add(map.get("centerCode").getS());
                                centreLocation.add(map.get("location").getS());
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
                cd.add(centreCode);
                cd.add(centreLocation);

                return cd;
            } catch (AmazonClientException e){
                mainBranchActivity.showSnackBar("Network connection error!!", "other");
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<List<String>> lists) {
            if(pbMainBranch!=null)
                pbMainBranch.setVisibility(View.GONE);
            if(lists != null){
                mainBranchActivity.showCentresDialog(lists);
            }
        }
    }


    private static class DeleteUser extends AsyncTask<Integer, Void, Boolean>{

        DynamoDBMapper dynamoDBMapper;
        int position;

        DeleteUser(DynamoDBMapper dynamoDBMapper){
            this.dynamoDBMapper = dynamoDBMapper;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Deleting user, please wait.");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            try {

                mainBranchActivity.s3client.deleteObject(new DeleteObjectRequest(Config.BUCKETNAME, "profilePics/"+userPhone.get(userNames.get(integers[0]))+".jpg"));

                BmttUsersDO bmttUsersDO = new BmttUsersDO();
                bmttUsersDO.setEmailId(userEmailAddress.get(userNames.get(integers[0])));
                bmttUsersDO.setPhone(userPhone.get(userNames.get(integers[0])));

                PaymentDO paymentDO = new PaymentDO();
                paymentDO.setEmailId(userEmailAddress.get(userNames.get(integers[0])));
                paymentDO.setPhone(userPhone.get(userNames.get(integers[0])));

                ActivitiesDO activitiesDO = new ActivitiesDO();
                activitiesDO.setEmailID(userEmailAddress.get(userNames.get(integers[0])));
                activitiesDO.setPhone(userPhone.get(userNames.get(integers[0])));

                FileSubmissionDO fileSubmissionDO = new FileSubmissionDO();
                fileSubmissionDO.setEmailID(userEmailAddress.get(userNames.get(integers[0])));
                fileSubmissionDO.setPhone(userPhone.get(userNames.get(integers[0])));

                position = integers[0];

                dynamoDBMapper.delete(bmttUsersDO);
                dynamoDBMapper.delete(paymentDO);
                dynamoDBMapper.delete(activitiesDO);
                dynamoDBMapper.delete(fileSubmissionDO);

                return true;
            } catch (AmazonClientException e){
                mainBranchActivity.showSnackBar("Network connection error!!", "delete");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            if(dialog != null && dialog.isShowing())
                dialog.dismiss();
            if(result){
                userCentres.remove(userNames.get(position));
                userPics.remove(userNames.get(position));
                userId.remove(userNames.get(position));
                userNames.remove(position);
                Collections.sort(userNames, String.CASE_INSENSITIVE_ORDER);
                usersListAdapter.notifyDataSetChanged();
            }
        }
    }

    private static class LoadAmount extends AsyncTask<Void, Void, AmountDO>{

        AmountDO amountDO = new AmountDO();
        @SuppressLint("StaticFieldLeak")
        TextView tvToday, tvMonth, tvYear, tvMonthYear, tvTodayAdm, tvMonthAdm;

        ProgressDialog progressDialog;
        String year, month, today, monthYear, thisYear, monthc;
        int todayAdm = 0, monthAdm = 0;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mainBranchActivity, R.style.MyAlertDialogStyle);
            dialog = new Dialog(mainBranchActivity);
            dialog.setContentView(R.layout.amount_dialog);
            tvMonthYear = dialog.findViewById(R.id.tvMonthYear);
            tvToday = dialog.findViewById(R.id.tvToday);
            tvMonth = dialog.findViewById(R.id.tvMonth);
            tvYear = dialog.findViewById(R.id.tvYear);
            tvTodayAdm = dialog.findViewById(R.id.tvTodayAdm);
            tvMonthAdm = dialog.findViewById(R.id.tvMonthAdm);
            dialog.show();
            progressDialog.setMessage("Calculating, please wait...");
            progressDialog.show();

            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat yearf = new SimpleDateFormat("yy");
            SimpleDateFormat monthff = new SimpleDateFormat("MMM");
            SimpleDateFormat monthf = new SimpleDateFormat("MM");
            SimpleDateFormat todayf = new SimpleDateFormat("dd");
            SimpleDateFormat monthYearf = new SimpleDateFormat("MMMM yyyy");
            SimpleDateFormat thisYearf = new SimpleDateFormat("yyyy");

            year = yearf.format(c);
            month = monthf.format(c);
            today = todayf.format(c);
            monthYear = monthYearf.format(c);
            thisYear = thisYearf.format(c);
            monthc = monthff.format(c);

            tvMonthYear.setText(monthYear);

        }

        @Override
        protected AmountDO doInBackground(Void... voids) {
            try {
                DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                        .dynamoDBClient(dynamoDBClient)
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .build();
                amountDO = dynamoDBMapper.load(AmountDO.class, Config.SUPERADMIN, thisYear);

                if (amountDO != null) {
                    String todayVal = amountDO.getToday();
                    if (!today.equals(todayVal.substring(0, 2).trim())) {
                        amountDO.setToday(today + " 0");
                        dynamoDBMapper.save(amountDO);
                    }

                    ScanRequest request = new ScanRequest().withTableName(Config.USERSTABLENAME);
                    ScanResult response = dynamoDBClient.scan(request);
                    List<Map<String, AttributeValue>> rows = response.getItems();
                    for(Map<String, AttributeValue> map : rows){
                        String admissionDate = map.get("admissionDone").getS();
                        if (admissionDate.contains("paid")) {
                            try {
                                String date = admissionDate.substring(5);
                                if (monthc.equals(date.substring(0, 3).trim())) {
                                    monthAdm++;
                                    if (today.equals(date.substring(4, 6).trim()))
                                        todayAdm++;
                                }
                            } catch (NumberFormatException e) {
                                Log.d("number_format_exception", e.getMessage());
                                return null;
                            }
                        }
                    }

                }

                return amountDO;
            } catch (AmazonClientException e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(AmountDO amountDO) {
            if (amountDO != null){
                String todayAmount, monthAmount, yearAmount;
                /*todayAmount = amountDO.getToday();
                monthAmount = amountDO.getMonth();
                yearAmount = amountDO.getAnnual();*/
                String todayVal = amountDO.getToday();
                if (today.equals(todayVal.substring(0,2).trim())){
                    todayAmount = String.valueOf(todayVal.substring(3));
                } else {
                    todayAmount = "0";
                }
                String monthVal = amountDO.getMonth();
                if (month.equals(monthVal.substring(0,2).trim())){
                    monthAmount = String.valueOf(monthVal.substring(3));
                } else {
                    monthAmount = "0";
                }
                String annualVal = amountDO.getAnnual();
                if (year.equals(annualVal.substring(0,2).trim())){
                    yearAmount = String.valueOf(annualVal.substring(3));
                } else {
                    yearAmount = "0";
                }
                tvToday.setText(tvToday.getText().toString() + todayAmount);
                tvMonth.setText(tvMonth.getText().toString() + monthAmount);
                tvYear.setText(tvYear.getText().toString() + yearAmount);
                tvTodayAdm.setText(tvTodayAdm.getText().toString() + todayAdm);
                tvMonthAdm.setText(tvMonthAdm.getText().toString() + monthAdm);
            } else {
                tvToday.setText(tvToday.getText().toString() + "0");
                tvMonth.setText(tvMonth.getText().toString() + "0");
                tvYear.setText(tvYear.getText().toString() + "0");
                tvTodayAdm.setText(tvTodayAdm.getText().toString() + "0");
                tvMonthAdm.setText(tvMonthAdm.getText().toString() + "0");
            }
            progressDialog.dismiss();
        }

    }

}
