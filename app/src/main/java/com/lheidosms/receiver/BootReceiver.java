package com.lheidosms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lheidosms.service.LheidoSMSService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String iAction = intent.getAction();
        if(iAction.equals("android.intent.action.BOOT_COMPLETED")){
            Intent i = new Intent(context, LheidoSMSService.class);
            context.startService(i);
        }
    }
}
