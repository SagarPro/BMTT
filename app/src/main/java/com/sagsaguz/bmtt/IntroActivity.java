package com.sagsaguz.bmtt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntroActivity extends AppCompatActivity implements View.OnClickListener{

    private String email, phone, name, centre;

    private int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_layout);

        final TextView tvQuotes = findViewById(R.id.tvQuotes);

        Button btnRevision = findViewById(R.id.btnRevision);
        btnRevision.setOnClickListener(this);
        Button btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(this);
        Button btnWebinar = findViewById(R.id.btnWebinar);
        btnWebinar.setOnClickListener(this);
        Button btnAttachments = findViewById(R.id.btnAttachments);
        btnAttachments.setOnClickListener(this);
        Button btnPracticals = findViewById(R.id.btnPracticals);
        btnPracticals.setOnClickListener(this);
        Button btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(this);
        Button btnService = findViewById(R.id.btnService);
        btnService.setOnClickListener(this);
        Button btnLogout = findViewById(R.id.btnLogout);

        final SharedPreferences userPreferences = getSharedPreferences("USERDETAILS", MODE_PRIVATE);
        email = userPreferences.getString("EMAIL", null);
        phone = userPreferences.getString("PHONE", null);
        name = userPreferences.getString("NAME", null);
        centre = userPreferences.getString("CENTRE", null);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = userPreferences.edit();
                editor.putString("LOGIN", "logout");
                editor.apply();
                startActivity(new Intent(getBaseContext(), LoginActivity.class));
                finish();
            }
        });

        final List<String> quotes = Arrays.asList(getResources().getStringArray(R.array.quotes));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvQuotes.setText(quotes.get(i));
                i++;
                if (i == quotes.size())
                    i = 0;
                handler.postDelayed(this, 7000);
            }
        }, 7000);

    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()){
            case R.id.btnRevision:
                intent = new Intent(getBaseContext(), GuidelinesActivity.class);
                break;
            case R.id.btnProfile:
                intent = new Intent(getBaseContext(), ProfileActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("PHONE", phone);
                break;
            case R.id.btnWebinar:
                intent = new Intent(getBaseContext(), WebinarActivity.class);
                intent.putExtra("USERTYPE", "user");
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                break;
            case R.id.btnAttachments:
                intent = new Intent(getBaseContext(), AttachmentsActivity.class);
                intent.putExtra("USERTYPE", "user");
                break;
            case R.id.btnPracticals:
                intent = new Intent(getBaseContext(), ActivitiesActivity.class);
                intent.putExtra("EMAIL", email);
                intent.putExtra("PHONE", phone);
                break;
            case R.id.btnNotification:
                intent = new Intent(getBaseContext(), NotificationActivity.class);
                intent.putExtra("USERTYPE", "user");
                intent.putExtra("EMAIL", email);
                intent.putExtra("CENTRE", centre);
                break;
            case R.id.btnService:
                intent = new Intent(getBaseContext(), QAActivity.class);
                intent.putExtra("USERTYPE", "user");
                intent.putExtra("EMAIL", email);
                intent.putExtra("NAME", name);
                break;
        }
        startActivity(intent);
    }
}
