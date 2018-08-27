package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.amazonaws.services.s3.AmazonS3Client;
import com.sagsaguz.bmtt.adapter.CentresListAdapter;
import com.sagsaguz.bmtt.adapter.EnquiryAdapter;
import com.sagsaguz.bmtt.adapter.SpinnerAdapter;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.Config;
import com.sagsaguz.bmtt.utils.EnquiryDO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AdmissionEnquiriesActivity extends AppCompatActivity {

    private RelativeLayout rlEnquiry;
    private ProgressBar pbEnquiry;
    private TextView tvMessage;
    private ListView lvEnquiries;
    private FloatingActionButton add_enquiry;

    private List<EnquiryDO> enquiryList = new ArrayList<>();
    private List<EnquiryDO> fEnquiryList = new ArrayList<>();
    private EnquiryAdapter enquiryAdapter;

    private List<String> centreList = new ArrayList<>();
    private ArrayAdapter<String> myAdapter;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admission_enquiries_layout);

        getSupportActionBar().setTitle("Admission Enquiries");

        rlEnquiry = findViewById(R.id.rlEnquiry);
        pbEnquiry = findViewById(R.id.pbEnquiry);
        pbEnquiry.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);

        tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setVisibility(View.GONE);
        lvEnquiries = findViewById(R.id.lvEnquiries);

        add_enquiry = findViewById(R.id.add_enquiry);

        enquiryAdapter = new EnquiryAdapter(AdmissionEnquiriesActivity.this, enquiryList);
        lvEnquiries.setAdapter(enquiryAdapter);

        new GetEnquiries().execute();
        new GetCentresList().execute();

        add_enquiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEnquiryDialog();
            }
        });

        lvEnquiries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Intent intent = new Intent(getBaseContext(), EnquiryDetailsActivity.class);
                intent.putExtra("enquiry", enquiryList.get(pos));
                startActivity(intent);
            }
        });

    }

    private void addEnquiryDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.new_enquiry_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final EditText etName = dialog.findViewById(R.id.etName);
        final EditText etPhone = dialog.findViewById(R.id.etPhone);
        final EditText etEmail = dialog.findViewById(R.id.etEmail);
        final EditText etLocation = dialog.findViewById(R.id.etLocation);
        final Spinner centreSpinner = dialog.findViewById(R.id.centreSpinner);
        myAdapter = new SpinnerAdapter(this, R.layout.centre_spinner_item, centreList);
        centreSpinner.setAdapter(myAdapter);

        Button btnAdd = dialog.findViewById(R.id.btnAdd);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(etName.getText().toString()) ||
                        TextUtils.isEmpty(etPhone.getText().toString()) ||
                        TextUtils.isEmpty(etEmail.getText().toString()) ||
                        TextUtils.isEmpty(etLocation.getText().toString())){
                    Toast.makeText(getBaseContext(), "Please enter valid details for all fields.", Toast.LENGTH_SHORT).show();
                } else {
                    Boolean valid = true;
                    for (int i=0; i<enquiryList.size(); i++){
                        if (enquiryList.get(i).getEnqEmail().equals(etEmail.getText().toString()) && enquiryList.get(i).getEnqPhone().equals(etPhone.getText().toString())){
                            valid = false;
                            break;
                        }
                    }

                    if (valid){
                        EnquiryDO enquiryDO = new EnquiryDO();
                        int max=0;
                        for (int i=0; i<enquiryList.size(); i++){
                            if (Integer.parseInt(enquiryList.get(i).getEnqNo()) > max)
                                max = Integer.parseInt(enquiryList.get(i).getEnqNo());
                        }
                        enquiryDO.setEnqNo(String.valueOf(max+1));
                        enquiryDO.setEnqId(etPhone.getText().toString());
                        enquiryDO.setEnqName(etName.getText().toString());
                        enquiryDO.setEnqPhone(etPhone.getText().toString());
                        enquiryDO.setEnqEmail(etEmail.getText().toString());
                        enquiryDO.setEnqLocation(etLocation.getText().toString());
                        enquiryDO.setEnqStatus("open");
                        Calendar myCalendar = Calendar.getInstance();
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
                        enquiryDO.setEnqCreatedDate(df.format(myCalendar.getTime()));
                        enquiryDO.setEnqAssignTo(centreSpinner.getSelectedItem().toString());
                        new AddEnquiry().execute(enquiryDO);
                    } else {
                        Toast.makeText(AdmissionEnquiriesActivity.this, "Enquiry with this details already exists", Toast.LENGTH_SHORT).show();
                    }

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admissions_menu, menu);
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
                List<EnquiryDO> newList = new ArrayList<>();
                enquiryList.clear();
                newList.addAll(fEnquiryList);
                for (EnquiryDO str : newList){
                    String name = str.getEnqName().toLowerCase();
                    if(name.contains(newText)) {
                        enquiryList.add(str);
                    }
                }
                enquiryAdapter.notifyDataSetChanged();
                return true;
            }
        });
        MenuItem showCentres = menu.findItem(R.id.show_centres);
        /*if(adminType.equals("SuperAdmin")){
            showCentres.setVisible(true);
        } else {
            showCentres.setVisible(false);
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.show_centres) {
            showCentresDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showCentresDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.centres_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ListView lvCentres = dialog.findViewById(R.id.lvCentres);
        final List<String> centreNames = new ArrayList<>();

        centreNames.add("All users");
        centreNames.addAll(centreList);

        Collections.sort(centreNames, String.CASE_INSENSITIVE_ORDER);
        CentresListAdapter centresListAdapter = new CentresListAdapter(AdmissionEnquiriesActivity.this, centreNames);
        lvCentres.setAdapter(centresListAdapter);

        lvCentres.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                enquiryList.clear();
                if (centreNames.get(position).equals("All users")) {
                    enquiryList.addAll(fEnquiryList);
                } else {
                    for (int i=0; i<fEnquiryList.size(); i++){
                        if (centreNames.get(position).equals(fEnquiryList.get(i).getEnqAssignTo()))
                            enquiryList.add(fEnquiryList.get(i));
                    }
                }
                enquiryAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    @SuppressLint("StaticFieldLeak")
    private class GetEnquiries extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            if(pbEnquiry!=null)
                pbEnquiry.setVisibility(View.VISIBLE);
            enquiryList.clear();
            fEnquiryList.clear();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            AWSProvider awsProvider = new AWSProvider();
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));

            try {
                ScanResult result = null;
                do{
                    ScanRequest req = new ScanRequest();
                    req.setTableName(Config.ENQUIRYTABLENAME);
                    if(result != null){
                        req.setExclusiveStartKey(result.getLastEvaluatedKey());
                    }
                    result = dynamoDBClient.scan(req);
                    List<Map<String, AttributeValue>> rows = result.getItems();
                    for(Map<String, AttributeValue> map : rows){

                        EnquiryDO enquiryDO = new EnquiryDO();

                        enquiryDO.setEnqNo(map.get("enqNo").getS());
                        enquiryDO.setEnqId(map.get("enqId").getS());
                        enquiryDO.setEnqAssignTo(map.get("enqAssignTo").getS());
                        enquiryDO.setEnqCreatedDate(map.get("enqCreatedDate").getS());
                        enquiryDO.setEnqEmail(map.get("enqEmail").getS());
                        enquiryDO.setEnqLocation(map.get("enqLocation").getS());
                        enquiryDO.setEnqName(map.get("enqName").getS());
                        enquiryDO.setEnqPhone(map.get("enqPhone").getS());
                        enquiryDO.setEnqStatus(map.get("enqStatus").getS());
                        enquiryDO.setEnqFollowUp(map.get("enq").getS());

                        enquiryList.add(enquiryDO);
                    }
                } while(result.getLastEvaluatedKey() != null);

                return true;
            } catch (AmazonClientException e){
                Toast.makeText(AdmissionEnquiriesActivity.this, "Network connection error, Please try again.", Toast.LENGTH_SHORT).show();
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(pbEnquiry!=null)
                pbEnquiry.setVisibility(View.GONE);
            if(result){
                Collections.sort(enquiryList, new NameComparator());
                fEnquiryList.addAll(enquiryList);
                enquiryAdapter.notifyDataSetChanged();
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class AddEnquiry extends AsyncTask<EnquiryDO, Void, Boolean> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(AdmissionEnquiriesActivity.this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Adding Enquiry, please wait...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(EnquiryDO... enquiryDOS) {

            AWSProvider awsProvider = new AWSProvider();
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                    .build();

            try{
                dynamoDBMapper.save(enquiryDOS[0]);
                return true;
            } catch (AmazonClientException e) {
                progressDialog.dismiss();
                Toast.makeText(AdmissionEnquiriesActivity.this, "Network connection error, Please try again.", Toast.LENGTH_SHORT).show();
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result){
                progressDialog.dismiss();
                dialog.dismiss();
                recreate();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetCentresList extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            centreList.clear();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            AWSProvider awsProvider = new AWSProvider();
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));

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
                                centreList.add(map.get("centre").getS());
                            }
                        } catch (NumberFormatException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } while (result.getLastEvaluatedKey() != null);
                return true;
            } catch (AmazonClientException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result){
                new GetCentresList().execute();
            }
        }
    }

    class NameComparator implements Comparator<EnquiryDO>
    {
        @Override
        public int compare(EnquiryDO enquiryDO1, EnquiryDO enquiryDO2) {
            return enquiryDO1.getEnqName().compareTo(enquiryDO2.getEnqName());
        }
    }

}
