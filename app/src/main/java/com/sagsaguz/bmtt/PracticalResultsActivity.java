package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.sagsaguz.bmtt.adapter.PracticalsListAdapter;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.PracticalsDO;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PracticalResultsActivity extends AppCompatActivity {

    private RelativeLayout rlPracticalResults, rlPBottom;
    private ListView lvPracticals;
    private FloatingActionButton fb_addPractical;
    private ProgressBar pbPDetails;
    private TextView tvMessage;
    private EditText etPSearch;

    private Map<String, String> practicalDetails = new HashMap<>();
    private List<String> p_name = new ArrayList<>();
    private List<String> pNameSearch = new ArrayList<>();

    private PracticalsListAdapter practicalsListAdapter;

    private Dialog dialog;
    private String email, phone, userType;
    private Calendar myCalendar;

    private ProgressDialog progressDialog;
    private DynamoDBMapper dynamoDBMapper;

    @SuppressLint("StaticFieldLeak")
    private static PracticalResultsActivity practicalResultsActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.practical_results_layout);

        practicalResultsActivity = PracticalResultsActivity.this;

        Intent intent = getIntent();
        email = intent.getStringExtra("EMAIL");
        phone = intent.getStringExtra("PHONE");
        userType = intent.getStringExtra("USERTYPE");

        rlPracticalResults = findViewById(R.id.rlPracticalResults);
        lvPracticals = findViewById(R.id.lvPracticals);
        fb_addPractical = findViewById(R.id.fb_addPractical);
        pbPDetails = findViewById(R.id.pbPDetails);
        tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setVisibility(View.GONE);
        rlPBottom = findViewById(R.id.rlPBottom);
        rlPBottom.setVisibility(View.GONE);
        etPSearch = findViewById(R.id.etPSearch);

        if (userType.equals("admin"))
            fb_addPractical.setVisibility(View.GONE);

        myCalendar = Calendar.getInstance();

        practicalsListAdapter = new PracticalsListAdapter(getBaseContext(), p_name, practicalDetails);
        lvPracticals.setAdapter(practicalsListAdapter);

        progressDialog = new ProgressDialog(practicalResultsActivity, R.style.MyAlertDialogStyle);

        AWSProvider awsProvider = new AWSProvider();
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        new ShowPracticalDetails(dynamoDBMapper).execute(email, phone);

        fb_addPractical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddPDetails();
            }
        });

        etPSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String text = etPSearch.getText().toString().toLowerCase();
                List<String> newList = new ArrayList<>();
                p_name.clear();
                newList.addAll(pNameSearch);
                for (String str : newList){
                    String name = str.toLowerCase();
                    if(name.contains(text)) {
                        p_name.add(str);
                    }
                }
                practicalsListAdapter.notifyDataSetChanged();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }


    private void showPDetails(PracticalsDO practicalsDO){
        if(practicalsDO != null) {
            p_name.clear();
            practicalDetails.clear();
            practicalDetails = practicalsDO.getDetails();
            if (practicalsDO.getDetails() != null) {
                Set vkeys = practicalsDO.getDetails().keySet();
                for (Object key1 : vkeys) {
                    String key = (String) key1;
                    p_name.add(key);
                }
                Collections.sort(p_name, String.CASE_INSENSITIVE_ORDER);
                practicalsListAdapter = new PracticalsListAdapter(getBaseContext(), p_name, practicalDetails);
                lvPracticals.setAdapter(practicalsListAdapter);
                tvMessage.setVisibility(View.GONE);
            }
        } else {
            tvMessage.setVisibility(View.VISIBLE);
        }
        rlPBottom.setVisibility(View.VISIBLE);
        pNameSearch.clear();
        pNameSearch.addAll(p_name);
    }

    private void showAddPDetails(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_pdetails_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final EditText etPName = dialog.findViewById(R.id.etPName);
        final TextView tvPDate = dialog.findViewById(R.id.tvPDate);
        final TextView tvPTime = dialog.findViewById(R.id.tvPTime);

        tvPDate.setOnClickListener(new View.OnClickListener() {
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
                        tvPDate.setText(sdf.format(myCalendar.getTime()));
                    }
                };
                new DatePickerDialog(practicalResultsActivity, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        tvPTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        myCalendar.set(Calendar.HOUR_OF_DAY, hour);
                        myCalendar.set(Calendar.MINUTE, minute);
                        String myFormat = "hh:mm a";
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                        tvPTime.setText(sdf.format(myCalendar.getTime()));
                    }
                };
                new TimePickerDialog(practicalResultsActivity, time,myCalendar.get(Calendar.HOUR_OF_DAY),
                        myCalendar.get(Calendar.MINUTE), true).show();
            }
        });

        Button btnPCancel = dialog.findViewById(R.id.btnPCancel);
        Button btnPSend = dialog.findViewById(R.id.btnPSend);

        btnPCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnPSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(etPName.getText().toString()) &&
                        !tvPDate.getText().toString().equals("Click here") &&
                        !tvPTime.getText().toString().equals("Click here")) {
                    String date_time = "On " + tvPDate.getText().toString() + " at " + tvPTime.getText().toString();
                    new AddPracticalDetails(dynamoDBMapper, practicalDetails).execute(etPName.getText().toString(), date_time);
                } else {
                    basicSnackBar("Please fill correct details.");
                }
            }
        });

        dialog.show();
    }

    public void basicSnackBar(String message){
        Snackbar snackbar = Snackbar.make(rlPracticalResults, message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(practicalResultsActivity, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(practicalResultsActivity, R.color.colorAccent));
        snackbar.show();
    }

    public void showSnackBar(String message, final String type){
        Snackbar snackbar = Snackbar.make(rlPracticalResults, message, Snackbar.LENGTH_SHORT)
                .setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(type.equals("show"))
                            new ShowPracticalDetails(dynamoDBMapper).execute(email, phone);
                    }
                });
        snackbar.setActionTextColor(ContextCompat.getColor(practicalResultsActivity, R.color.colorAccent));

        View sbView = snackbar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(practicalResultsActivity, R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(practicalResultsActivity, R.color.colorAccent));
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.show();
    }



    private static class ShowPracticalDetails extends AsyncTask<String, Void, Boolean>{

        PracticalsDO practicalsDO = new PracticalsDO();
        DynamoDBMapper dynamoDBMapper;

        ShowPracticalDetails(DynamoDBMapper dynamoDBMapper){
            this.dynamoDBMapper = dynamoDBMapper;
        }

        @Override
        protected void onPreExecute() {
            if(practicalResultsActivity.pbPDetails != null)
                practicalResultsActivity.pbPDetails.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                practicalsDO = dynamoDBMapper.load(PracticalsDO.class, strings[0], strings[1]);
                return true;
            } catch (AmazonClientException e){
                practicalResultsActivity.showSnackBar("Network connection error!!", "show");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(practicalResultsActivity.pbPDetails != null)
                practicalResultsActivity.pbPDetails.setVisibility(View.GONE);
            if(result)
                practicalResultsActivity.showPDetails(practicalsDO);
        }
    }



    private static class AddPracticalDetails extends AsyncTask<String, Void, Boolean> {

        PracticalsDO practicalsDO = new PracticalsDO();
        DynamoDBMapper dynamoDBMapper;
        Map<String, String> pDetails = new HashMap<>();
        String key;

        AddPracticalDetails(DynamoDBMapper dynamoDBMapper, Map<String, String> pDetails){
            this.dynamoDBMapper = dynamoDBMapper;
            this.pDetails = pDetails;
        }

        @Override
        protected void onPreExecute() {
            practicalResultsActivity.progressDialog.setMessage("Sending details, please wait.");
            practicalResultsActivity.progressDialog.show();
            practicalsDO.setEmailId(practicalResultsActivity.email);
            practicalsDO.setPhone(practicalResultsActivity.phone);
        }

        @Override
        protected Boolean doInBackground(String...strings) {
            try {
                key = strings[0];
                pDetails.put(strings[0], strings[1]);
                practicalsDO.setDetails(pDetails);
                dynamoDBMapper.save(practicalsDO);
                return true;
            } catch (AmazonClientException e){
                practicalResultsActivity.showSnackBar("Network connection error!!", "add");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            practicalResultsActivity.progressDialog.dismiss();
            if(practicalResultsActivity.dialog != null && practicalResultsActivity.dialog.isShowing())
                practicalResultsActivity.dialog.dismiss();
            if(result){
                practicalResultsActivity.p_name.add(key);
                practicalResultsActivity.pNameSearch.add(key);
                Collections.sort(practicalResultsActivity.p_name, String.CASE_INSENSITIVE_ORDER);
                practicalResultsActivity.practicalsListAdapter.notifyDataSetChanged();
                practicalResultsActivity.tvMessage.setVisibility(View.GONE);
            }
        }
    }

}
