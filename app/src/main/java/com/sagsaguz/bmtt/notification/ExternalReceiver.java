package com.sagsaguz.bmtt.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sagsaguz.bmtt.LoginActivity;

public class ExternalReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            Bundle extras = intent.getExtras();
            MessageReceivingService.saveToLog(extras, context);
            /*if(!LoginActivity.inBackground){
                //MessageReceivingService.sendToApp(extras, context);
            }
            else{
                MessageReceivingService.saveToLog(extras, context);
            }*/
        }
    }
}

