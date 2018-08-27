package com.sagsaguz.bmtt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class GuidelinesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guidelines_layout);

        final LinearLayout llCourseSyllabus = findViewById(R.id.llCourseSyllabus);
        final TextView tvDialogTitle = findViewById(R.id.tvDialogTitle);
        tvDialogTitle.setText("Course Syllabus");
        final TextView tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setText(getString(R.string.course_syllabus));
        final ScrollView svCourseSyllabus = findViewById(R.id.svCourseSyllabus);
        final Button btnStartCourse = findViewById(R.id.btnStartCourse);
        btnStartCourse.setText("Next");
        btnStartCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (tvDialogTitle.getText().toString()){
                    case "Course Syllabus":
                        llCourseSyllabus.setBackgroundColor(getResources().getColor(R.color.yellow));
                        btnStartCourse.setText("Next");
                        tvDialogTitle.setText("Course Guidelines\n Level 1");
                        tvDialogTitle.setTextColor(getResources().getColor(R.color.darkGrey));
                        String level1 = getResources().getString(R.string.course_guidelines_level_1);
                        tvMessage.setText(Html.fromHtml(level1));
                        break;
                    case "Course Guidelines\n Level 1":
                        btnStartCourse.setText("Start Course");
                        tvDialogTitle.setText("Course Guidelines\n Level 2");
                        String level2 = getResources().getString(R.string.course_guidelines_level_2);
                        tvMessage.setText(Html.fromHtml(level2));
                        break;
                    case "Course Guidelines\n Level 2":
                        startActivity(new Intent(getBaseContext(), HomePageActivity.class));
                        finish();
                        break;
                }
                svCourseSyllabus.scrollTo(0,0);
            }
        });

    }
}
