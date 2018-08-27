package com.sagsaguz.bmtt.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

    private Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        timer.schedule(timerTask, 2000, 2 * 1000);
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            Log.e("LOG", "Running");
        }
    };

    @Override
    public void onDestroy() {
        try {
            timer.cancel();
            timerTask.cancel();
        } catch (Exception e){
            e.printStackTrace();
        }
        Intent intent = new Intent("com.sagsaguz.bmtt");
        intent.putExtra("STATUS", "Working");
        sendBroadcast(intent);
    }
}
