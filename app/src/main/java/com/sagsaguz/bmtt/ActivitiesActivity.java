package com.sagsaguz.bmtt;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.sagsaguz.bmtt.fragments.Arithmetic;
import com.sagsaguz.bmtt.fragments.EPL;
import com.sagsaguz.bmtt.fragments.Language;
import com.sagsaguz.bmtt.fragments.Sensorial;
import com.sagsaguz.bmtt.utils.AWSProvider;
import com.sagsaguz.bmtt.utils.ActivitiesDO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivitiesActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabParts;

    private String email, phone;

    private ActivitiesDO activitiesDO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activities_main_layout);

        getSupportActionBar().setTitle("Activities");

        Intent intent = getIntent();
        email = intent.getStringExtra("EMAIL");
        phone = intent.getStringExtra("PHONE");

        viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(1);

        tabParts = findViewById(R.id.tabParts);
        tabParts.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));

        new GetActivities().execute();

    }

    private void setUpTab(){
        setupViewPager(viewPager);
        tabParts.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {

        List<String> eplList = Arrays.asList(getResources().getStringArray(R.array.epl));
        List<String> sensorialList = Arrays.asList(getResources().getStringArray(R.array.sensorial));
        List<String> arithmeticList = Arrays.asList(getResources().getStringArray(R.array.arithmetic));
        List<String> languageList = Arrays.asList(getResources().getStringArray(R.array.language));

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(EPL.newInstance(activitiesDO, eplList), "EPL");
        adapter.addFrag(Sensorial.newInstance(activitiesDO, sensorialList), "Sensorial");
        adapter.addFrag(Arithmetic.newInstance(activitiesDO, arithmeticList), "Arithmetic");
        adapter.addFrag(Language.newInstance(activitiesDO, languageList), "Language");
        viewPager.setAdapter(adapter);

    }


    @SuppressLint("StaticFieldLeak")
    private class GetActivities extends AsyncTask<Void, Void, Boolean> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ActivitiesActivity.this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Loading, please wait...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                AWSProvider awsProvider = new AWSProvider();
                AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(awsProvider.getCredentialsProvider(getBaseContext()));
                dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
                DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                        .dynamoDBClient(dynamoDBClient)
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .build();
                activitiesDO = dynamoDBMapper.load(ActivitiesDO.class, email, phone);
                return true;
            } catch (AmazonClientException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            if (result)
                setUpTab();
            else
                Toast.makeText(getBaseContext(), "Failed to Load, please try again.", Toast.LENGTH_LONG).show();
        }
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
