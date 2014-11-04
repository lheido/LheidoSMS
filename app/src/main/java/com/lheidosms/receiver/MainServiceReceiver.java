package com.lheidosms.receiver;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.SmsMessage;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.lheidosms.utils.LheidoContact;
import com.lheidosms.utils.LheidoUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lheido on 01/11/14.
 */
public abstract class MainServiceReceiver extends LheidoBaseReceiver {
    private boolean activ_notif;
    private boolean vibrate;
    private Vibrator v;

    @Override
    protected ArrayList<String> initActions() {
        ArrayList<String> actions = new ArrayList<String>();
        actions.add(LheidoUtils.ACTION_RECEIVE_SMS);
        actions.add(LheidoUtils.ACTION_RECEIVE_MMS);
        actions.add(LheidoUtils.ACTION_SENT_SMS);
        actions.add(LheidoUtils.ACTION_DELIVERED_SMS);
        actions.add(LheidoUtils.ACTION_NEW_MESSAGE_READ);
        actions.add(LheidoUtils.ACTION_USER_NEW_MESSAGE);
        actions.add(LheidoUtils.ACTION_CANCEL_VIBRATOR);
        return actions;
    }

    @Override
    protected void receive(Intent intent) {
        if(iAction.equals(LheidoUtils.ACTION_RECEIVE_SMS)){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for(int i = 0; i<pdus.length; i++){
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                String body;
                if(messages.length > -1){
                    body = "";
                    for(SmsMessage x : messages){
                        body += x.getMessageBody();
                    }
                    long date = messages[0].getTimestampMillis();
                    String phone = messages[0].getDisplayOriginatingAddress();
                    String sender = LheidoContact.getContactName(mContext, phone);
                    customReceiveSms(sender, phone, body, date);
                }
            }
        }else if(iAction.equals(LheidoUtils.ACTION_RECEIVE_MMS)){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    final SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }
                    String body;
                    if (messages.length > -1) {
                        body = "";
                        for (SmsMessage x : messages) {
                            body += x.getMessageBody();
                        }
                        long date = messages[0].getTimestampMillis();
                        String phone = messages[0].getDisplayOriginatingAddress();
                        String sender = LheidoContact.getContactName(mContext, phone);
//                        customReceiveMms(sender, phone, body, date);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Time t = new Time();
                    t.setToNow();
//                    customReceiveMms("quelqu'un", "1234", "", t.normalize(false));
                }
            }
        }else if(iAction.equals(LheidoUtils.ACTION_SENT_SMS)){
            switch(getResultCode()){
                case Activity.RESULT_OK:
                    //Toast.makeText(context, "Le message a certainement dû être envoyé à quelqu'un..." , Toast.LENGTH_SHORT).show();
                    //v.vibrate(1000);
                    break;
                default:
                    Toast.makeText(mContext, "Erreur, le message n'a pas était envoyé", Toast.LENGTH_SHORT).show();
                    try {
                        if (vibrate) v.vibrate(2000);
                    }catch (Exception e){
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
            customSentSms();
        }else if(iAction.equals(LheidoUtils.ACTION_DELIVERED_SMS)){
            switch(getResultCode()){
                case Activity.RESULT_OK:
                    Bundle extras = intent.getExtras();
                    long id = extras.getLong(LheidoUtils.ARG_SMS_DELIVERED, -1);
                    if(id != -1){
                        ContentValues values = new ContentValues();
                        values.put("status", 0);
                        try{
                            mContext.getContentResolver().update(Uri.parse("content://sms/" + id), values, null, null);
                            customDeliveredSms(id);
                        }catch (Exception ex){
                            Toast.makeText(mContext, ex.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                default:
                    Toast.makeText(mContext, "Erreur, message non remis", Toast.LENGTH_SHORT).show();
                    try {
                        if (vibrate) v.vibrate(2000);
                    }catch (Exception e){
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }else if(iAction.equals(LheidoUtils.ACTION_NEW_MESSAGE_READ)){
            int position = intent.getIntExtra("position", 0);
            String phone = intent.getStringExtra("phone");
            customNewMessageRead(position, phone);
        }else if(iAction.equals(LheidoUtils.ACTION_USER_NEW_MESSAGE)){
            String phone = intent.getStringExtra("phone");
            customUserNewMessage(phone);
        }else if(iAction.equals(LheidoUtils.ACTION_CANCEL_VIBRATOR)){
            customCancelVibrator();
        }
    }

    protected abstract void customReceiveSms(String new_name, String phone, String body, long date);

    protected abstract void customDeliveredSms(long id);

    protected abstract void customNewMessageRead(int position, String phone);

    protected abstract void customSentSms();

    protected abstract void customReceiveMms(String new_name, String phone, String body, long date);

    protected abstract void customUserNewMessage(String phone);

    protected abstract void customCancelVibrator();
}
