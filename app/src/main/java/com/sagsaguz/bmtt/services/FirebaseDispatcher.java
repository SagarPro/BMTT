package com.sagsaguz.bmtt.services;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.sagsaguz.bmtt.notification.MessageReceivingService;

public class FirebaseDispatcher extends JobService {

    RunTask runTask;

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(final JobParameters job) {
        runTask = new RunTask(){
            @Override
            protected void onPostExecute(String s) {
                /*Toast.makeText(getApplicationContext(), "Working here...", Toast.LENGTH_SHORT).show();
                Log.d("Sagardispatcher", "Working");*/
                try {
                    startService(new Intent(getApplicationContext(), MessageReceivingService.class));
                    jobFinished(job, false);
                } catch (IllegalStateException e){
                    Log.d("OREO", "Illegal state exception");
                }
            }
        };
        runTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }

    public  static class RunTask extends AsyncTask<Void,Void,String>
    {
        @Override
        protected String doInBackground(Void... voids) {
            return "true";
        }
    }
}
