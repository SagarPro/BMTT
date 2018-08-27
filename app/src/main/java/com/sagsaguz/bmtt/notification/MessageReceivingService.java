package com.sagsaguz.bmtt.notification;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sagsaguz.bmtt.HomePageActivity;
import com.sagsaguz.bmtt.LoginActivity;
import com.sagsaguz.bmtt.MainBranchActivity;
import com.sagsaguz.bmtt.R;
import com.sagsaguz.bmtt.SplashScreenActivity;

public class MessageReceivingService extends Service{
    private GoogleCloudMessaging gcm;

    public void onCreate(){
        super.onCreate();
        gcm = GoogleCloudMessaging.getInstance(getBaseContext());
        SharedPreferences savedValues = PreferenceManager.getDefaultSharedPreferences(this);
        if(savedValues.getBoolean(getString(R.string.first_launch), true)){
            register();
            SharedPreferences.Editor editor = savedValues.edit();
            editor.putBoolean(getString(R.string.first_launch), false);
            editor.apply();
        }
    }

    protected static void saveToLog(Bundle extras, Context context){
        String message = "Notification";
        int category = 0;
        if(extras!=null){
            for(String key: extras.keySet()){
                if(key.equals("default") || key.equals("gcm.notification.body")) {
                    message = extras.getString(key);
                }
            }
        }
        String receivingARN = "null";
        if (message!= null && message.contains("$")) {
            try {
                category = Integer.parseInt(message.substring(0,1));
            } catch (NumberFormatException e){
                category = 0;
            }
            receivingARN = message.substring(1, message.indexOf("$"));
            message = message.substring(message.indexOf("$") + 1);
        }
        SharedPreferences userPref = context.getSharedPreferences("USERDETAILS", MODE_PRIVATE);
        String arn = userPref.getString("ARN", "null");
        if (receivingARN.equals(arn)) {
            if (message != null && message.contains("asked a question.")){
                if (MainBranchActivity.qaIndicator != null)
                    MainBranchActivity.qaIndicator.setVisibility(View.VISIBLE);
                SharedPreferences preferences = context.getSharedPreferences("QAINDICATOR", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("QINDICATOR", true);
                editor.apply();
            } else {
                if (HomePageActivity.notificationIndicator != null)
                    HomePageActivity.notificationIndicator.setVisibility(View.VISIBLE);
                SharedPreferences preferences = context.getSharedPreferences("NOTIFICATIONINDICATOR", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("NINDICATOR", true);
                editor.apply();
            }
            postNotification(new Intent(context, SplashScreenActivity.class), context, message, category);
        }
    }

    protected static void postNotification(Intent intentAction, Context context, String message, int category){

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        @SuppressLint("WrongConstant")
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentAction, Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL);

        String id = "bmtt_notification";
        CharSequence name = "BMTT";
        String subject = "Notification";

        switch (category){
            case 0: subject = "Notification";
                break;
            case 1: subject = "Approved as a BMTT Student. Access granted to TERM 1, part-1";
                break;
            case 2: subject = "Access granted to TERM 1, part-1 & part-2";
                break;
            case 3: subject = "Access granted to TERM 1, part-1 & part-2 and TERM 2";
                break;
            case 4: subject = "Access restricted to BMTT course";
                break;
            case 5: subject = "Successfully received all of your documents";
                break;
            case 6: subject = "Amount paid";
                break;
            case 7: subject = "New Question";
                break;
            case 8: subject = "Pending fees reminder";
                break;
            case 9: subject = "Bmtt Webinar";
                break;
        }

        int importance = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_DEFAULT;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, name,importance);
            mChannel.setDescription(message);

            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);

            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentIntent(pendingIntent).setSmallIcon(R.drawable.icon_bell).setTicker(name).setWhen(0)
                    .setAutoCancel(true).setContentTitle(subject)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentText(message);
            if (isAppIsInBackground(context)){
                builder.setContentIntent(pendingIntent);
            }

            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            if (mNotificationManager != null) {
                mNotificationManager.notify(R.string.notification_number, notification);
            }

            /*RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
            contentView.setImageViewResource(R.id.ivNotificationIcon, R.drawable.star_circle);
            contentView.setTextViewText(R.id.tvNotificationTitle, "BMTT");
            contentView.setTextViewText(R.id.tvNotificationMessage, message);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.icon_bell)
                    .setChannelId(id)
                    .setContent(contentView);
            if (isAppIsInBackground(context)){
                mBuilder.setContentIntent(pendingIntent);
            }

            Notification notification = mBuilder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            if (mNotificationManager != null) {
                mNotificationManager.notify(R.string.notification_number, notification);
            }*/

        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.icon_bell).setTicker(name).setWhen(0)
                    .setAutoCancel(true).setContentTitle(subject)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentText(message);
            if (isAppIsInBackground(context)){
                builder.setContentIntent(pendingIntent);
            }

            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            if (mNotificationManager != null) {
                mNotificationManager.notify(R.string.notification_number, notification);
            }

            /*RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
            contentView.setImageViewResource(R.id.ivNotificationIcon, R.drawable.star_circle);
            contentView.setTextViewText(R.id.tvNotificationTitle, "BMTT");
            contentView.setTextViewText(R.id.tvNotificationMessage, message);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.icon_bell)
                    .setContent(contentView);
            if (isAppIsInBackground(context)) {
                mBuilder.setContentIntent(pendingIntent);
            }

            Notification notification = mBuilder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            if (mNotificationManager != null) {
                mNotificationManager.notify(R.string.notification_number, notification);
            }*/

        }

    }

    private void register() {
        new AsyncTask(){
            protected Object doInBackground(final Object... params) {
                String token;
                try {
                    token = gcm.register(getString(R.string.project_number));
                    Log.i("registrationId", token);
                } 
                catch (IOException e) {
                    Log.i("Registration Error", e.getMessage());
                }
                return true;
            }
        }.execute(null, null, null);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }


    private static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = null;
        if (am != null) {
            runningProcesses = am.getRunningAppProcesses();
        }
        assert runningProcesses != null;
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }

        return isInBackground;
    }

}