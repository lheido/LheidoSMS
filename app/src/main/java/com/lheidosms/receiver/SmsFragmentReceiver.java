package com.lheidosms.receiver;

import android.content.Intent;
import android.os.Bundle;

import com.lheidosms.utils.LheidoUtils;

import java.util.ArrayList;

/**
 * Created by lheido on 01/11/14.
 */
public abstract class SmsFragmentReceiver extends LheidoBaseReceiver {
    @Override
    protected ArrayList<String> initActions() {
        ArrayList<String> actions = new ArrayList<String>();
        actions.add(LheidoUtils.ACTION_NOTIFY_RECEIVE_SMS);
        actions.add(LheidoUtils.ACTION_NOTIFY_DELIVERED_SMS);
        return actions;
    }

    @Override
    protected void receive(Intent intent) {
        if(iAction.equals(LheidoUtils.ACTION_NOTIFY_RECEIVE_SMS)){
            String body = intent.getStringExtra(LheidoUtils.Send.SMS_ATTR_BODY);
            String sender = intent.getStringExtra(LheidoUtils.Send.SMS_ATTR_SENDER);
            long id = intent.getLongExtra(LheidoUtils.Send.SMS_ATTR_ID, -1);
            long date = intent.getLongExtra(LheidoUtils.Send.SMS_ATTR_DATE, -1);
            boolean isRead = intent.getBooleanExtra(LheidoUtils.Send.SMS_ATTR_READ, false);
            customNotifyReceive(id, sender, body, date, isRead);
        }else if(iAction.equals(LheidoUtils.ACTION_NOTIFY_DELIVERED_SMS)){
            Bundle extras = intent.getExtras();
            customNotifyDelivered(extras.getLong(LheidoUtils.Send.DELIVERED_ID));
        }
    }

    protected abstract void customNotifyDelivered(long id);

    protected abstract void customNotifyReceive(long id, String sender, String body, long date, boolean isRead);
}
