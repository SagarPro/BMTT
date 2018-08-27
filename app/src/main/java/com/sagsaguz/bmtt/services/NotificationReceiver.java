package com.sagsaguz.bmtt.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("STOP", "ohhh");
        context.startService(new Intent(context, NotificationService.class));
    }
}
