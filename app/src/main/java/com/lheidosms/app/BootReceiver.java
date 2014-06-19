package com.lheidosms.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
