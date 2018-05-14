package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.sagsaguz.bmtt.adapter.CentresListAdapter;
import com.sagsaguz.bmtt.adapter.UsersListAdapter;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.BmttAdminsDO;
import com.sagsaguz.bmtt.utils.BmttUsersDO;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.DynamoDBCRUDOperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainBranchActivity extends AppCompatActivity{

    @SuppressLint("StaticFieldLeak")
    private static ProgressBar pbMainBranch;

    private ConstraintLayout clMainBranch;
    private ImageView ivLogout, ivAdd, ivQA, ivNotifications;
    private RelativeLayout rlQA;
    private LinearLayout llBottom;
    @SuppressLint("StaticFieldLeak")
    public static View qaIndicator;
    private View view1, view2;

    @SuppressLint("StaticFieldLeak")
    private static TextView tvMessage;

    private static List<String> userNames = new ArrayList<>();
    private static List<String> uNames = new ArrayList<>();
    private static HashMap<String, String> userCentres = new HashMap<String, String>();
    private static HashMap<String, String> userEmailAddress = new HashMap<String, String>();
    private static HashMap<String, String> userPhone = new HashMap<String, String>();
    private static HashMap<String, String> userPics = new HashMap<String, String>();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_branch_layout);

        mainBranchActivity = MainBranchActivity.this;

        /*getSupportActionBar().setTitle("BMTT");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/

        clMainBranch = findViewById(R.id.clMainBranch);

        ListView lvUsers = findViewById(R.id.lvUsers);
        tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setVisibility(View.GONE);

        ivLogout = findViewById(R.id.ivLogout);
        ivAdd = findViewById(R.id.ivAdd);
        ivQA = findViewById(R.id.ivQA);
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

        adminPreferences = getSharedPreferences("USERDETAILS", MODE_PRIVATE);
        userType = adminPreferences.getString("USERTYPE", "super");
        if(userType.equals("admin")){
            ivAdd.setVisibility(View.GONE);
            ivQA.setVisibility(View.GONE);
            rlQA.setVisibility(View.GONE);
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
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

        usersListAdapter = new UsersListAdapter(MainBranchActivity.this, userNames, userCentres, userPics);
        lvUsers.setAdapter(usersListAdapter);

        if(adminType.equals("SuperAdmin")){
            new ShowAllUsers().execute();
        } else {
            new ShowUserByCentre().execute(centre);
        }

        /*lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                *//*Intent userDetailsIntent = new Intent(MainBranchActivity.this, UserDetailsActivity.class);
                userDetailsIntent.putExtra("EMAIL", userEmailAddress.get(userNames.get(i)));
                userDetailsIntent.putExtra("PHONE", userPhone.get(userNames.get(i)));
                startActivity(userDetailsIntent);*//*

                Intent sharedIntent = new Intent(MainBranchActivity.this, UserDetailsActivity.class);
                sharedIntent.putExtra("EMAIL", userEmailAddress.get(userNames.get(i)));
                sharedIntent.putExtra("PHONE", userPhone.get(userNames.get(i)));
                Pair pair = new Pair<View, String>(UsersListAdapter.getPic(), "profile");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainBranchActivity.this, pair);
                startActivity(sharedIntent, options.toBundle());
            }
        });*/

        lvUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
        });

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
                showAddDialog();
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


    private void showAddDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.centres_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final List<String> userType = new ArrayList<>();
        userType.add("Admin");
        userType.add("User");

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
                    startActivity(new Intent(MainBranchActivity.this, AddUserActivity.class));
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
                        TextUtils.isEmpty(etAdminPassword.getText().toString())){
                    Toast.makeText(mainBranchActivity, "Please enter valid details for all fields.", Toast.LENGTH_SHORT).show();
                } else {
                    new AddAdmin(dynamoDBClient, dynamoDBMapper).execute(etAdminEmail.getText().toString(),
                            etAdminPhone.getText().toString(), etAdminCentre.getText().toString(),
                            etAdminPassword.getText().toString());
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
                                finalCd.get(3).get(v));
                        break;
                    }
                }
                return true;
            }
        });
        dialog.show();
    }

    private void showCentreDetailsDialog(String cName, String cEmail, String cPhone, String cPassword){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.centre_details_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final TextView tvCName = dialog.findViewById(R.id.tvCName);
        tvCName.setText(cName);
        final TextView tvCEmail = dialog.findViewById(R.id.tvCEmail);
        tvCEmail.setText(cEmail);
        final TextView tvCPhone = dialog.findViewById(R.id.tvCPhone);
        tvCPhone.setText(cPhone);
        final EditText etCPassword = dialog.findViewById(R.id.etCPassword);
        etCPassword.setText(cPassword);
        etCPassword.setTag(etCPassword.getKeyListener());
        etCPassword.setKeyListener(null);

        Button btnCancel = dialog.findViewById(R.id.btnCCancel);
        final Button btnEdit = dialog.findViewById(R.id.btnCEdit);
        final Button btnSave = dialog.findViewById(R.id.btnCSave);
        btnSave.setVisibility(View.GONE);

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
                btnSave.setVisibility(View.VISIBLE);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSave.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
                etCPassword.setKeyListener(null);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                                .dynamoDBClient(dynamoDBClient)
                                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                                .build();
                        BmttAdminsDO bmttAdminsDO = new BmttAdminsDO();
                        bmttAdminsDO.setCentre(tvCName.getText().toString());
                        bmttAdminsDO.setEmailId(tvCEmail.getText().toString());
                        bmttAdminsDO.setPhone(tvCPhone.getText().toString());
                        bmttAdminsDO.setPassword(etCPassword.getText().toString());
                        dynamoDBMapper.save(bmttAdminsDO);
                    }
                }).start();
                dialog.dismiss();
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

        return super.onOptionsItemSelected(item);
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
                        password = strings[3];

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
                            if(map.get("emailId").getS().equals(email)) {
                                return "exist";
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
            if (str.equals("exist")){
                Toast.makeText(mainBranchActivity, "Admin with this email address is already exists.", Toast.LENGTH_LONG).show();
            } else if(str.equals("added")){
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Toast.makeText(mainBranchActivity, "Successfully added ", Toast.LENGTH_SHORT).show();
            } else {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }
    }


    private static class GetCentreDetails extends AsyncTask<AmazonDynamoDBClient, Void, List<List<String>>>{
        List<String> centreNames = new ArrayList<>();
        List<String> centreEmail = new ArrayList<>();
        List<String> centrePhone = new ArrayList<>();
        List<String> centrePassword = new ArrayList<>();
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

                position = integers[0];

                dynamoDBMapper.delete(bmttUsersDO);
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
                userNames.remove(position);
                Collections.sort(userNames, String.CASE_INSENSITIVE_ORDER);
                usersListAdapter.notifyDataSetChanged();
            }
        }
    }

}
