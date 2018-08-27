package com.sagsaguz.bmtt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sagsaguz.bmtt.utils.EnquiryDO;

public class EnquiryDetailsActivity extends AppCompatActivity {

    private RelativeLayout rlUserDetails;
    private TextView name, phone;
    private ListView lvActivities;
    private LinearLayout llOptions;
    private ImageButton ibSMS, ibCall;

    private EnquiryDO enquiryDO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enquiry_details_layout);

        getSupportActionBar().setTitle("Enquiry Details");

        rlUserDetails = findViewById(R.id.rlUserDetails);
        name = findViewById(R.id.user_name);
        phone = findViewById(R.id.user_phone);
        lvActivities = findViewById(R.id.lvActivities);
        llOptions = findViewById(R.id.llOptions);
        ibSMS = findViewById(R.id.ibSMS);
        ibCall = findViewById(R.id.ibCall);

        enquiryDO = (EnquiryDO) getIntent().getSerializableExtra("enquiry");

        name.setText(enquiryDO.getEnqName());
        phone.setText(enquiryDO.getEnqPhone());

    }
}
