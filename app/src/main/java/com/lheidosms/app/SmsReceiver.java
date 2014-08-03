package com.lheidosms.app;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public abstract class SmsReceiver extends BroadcastReceiver {
    protected Context context;
    protected boolean activ_notif;
    protected boolean vibrate;
    protected Vibrator v;
    protected String body;
    protected String phone;
    protected String new_name;
    protected long date;
    protected Map<String, Integer> notificationsId = new HashMap<String, Integer>();
    protected SharedPreferences userPref;

    public void showNotification(String body, String name, String phone, PendingIntent pIntent){
        // Create Notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.lheido_sms_icon)
            .setTicker(body)
            .setContentTitle("" + name)
            .setContentText(body)
            // Add an Action Button below Notification
            //.addAction(R.drawable.ic_launcher, "Ouvrir la conversation", pIntent)
            // Set PendingIntent into Notification
            .setContentIntent(pIntent)
            // Dismiss Notification
            .setAutoCancel(true);

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        int id = 0;
        if(notificationsId.containsKey(phone))
            id = notificationsId.get(phone);
        notificationmanager.notify(id, builder.build());
    }

    public void playNotificationSound(){
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences userPref = PreferenceManager.getDefaultSharedPreferences(context);
        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                try {
                    if (vibrate) v.vibrate(1000);
                }catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                break;
            case AudioManager.RINGER_MODE_NORMAL:

                if(userPref.getBoolean(LheidoUtils.receiver_ringtone_key, true)) {
                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(context, notification);
                        r.play();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public void cancelNotif(String phone){
//        Log.v("LHEIDO SMS LOG", notificationsId.keySet().toString());
        try {
            NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            for(String p : notificationsId.keySet()){
                if(PhoneNumberUtils.compare(p, phone))
                    notificationmanager.cancel(notificationsId.get(p));
            }
        }catch (Exception ex){ex.printStackTrace();}
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        context = c;
        userPref = PreferenceManager.getDefaultSharedPreferences(context);
        activ_notif = userPref.getBoolean(LheidoUtils.receiver_notification_key, true);
        vibrate = userPref.getBoolean(LheidoUtils.vibration_key, true);
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        String iAction = intent.getAction();
        if(iAction.equals(LheidoUtils.ACTION_CANCEL_VIBRATOR)){
            v.cancel();
        }else if(iAction.equals(LheidoUtils.ACTION_RECEIVE_SMS)){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for(int i = 0; i<pdus.length; i++){
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                if(messages.length > -1){
                    body = "";
                    for(SmsMessage x : messages){
                        body += x.getMessageBody();
                    }
                    date = messages[0].getTimestampMillis();
                    phone = messages[0].getDisplayOriginatingAddress();
                    new_name = LheidoContact.getContactName(context, phone);
//                    Log.v("LHEIDO SMS LOG", "phone = "+phone);
                    if(!notificationsId.containsKey(phone))
                        notificationsId.put(phone, notificationsId.size());
//                    Log.v("LHEIDO SMS LOG", notificationsId.toString());
                    customReceivedSMS();
                }
            }
        } else if(iAction.equals(LheidoUtils.ACTION_DELIVERED_SMS)){
            switch(getResultCode()){
                case Activity.RESULT_OK:
                    long _id = intent.getExtras().getLong(LheidoUtils.ARG_SMS_DELIVERED, -1);
                    if(_id != -1){
                        ContentValues values = new ContentValues();
                        values.put("status", 0);
                        try{
                            context.getContentResolver().update(Uri.parse("content://sms/" + _id), values, null, null);
                            customDelivered(_id);
                        }catch (Exception ex){
                            Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                default:
                    Toast.makeText(context, "Erreur, message non remis", Toast.LENGTH_SHORT).show();
                    try {
                        if (vibrate) v.vibrate(2000);
                    }catch (Exception e){
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        } else if(iAction.equals(LheidoUtils.ACTION_SENT_SMS)){
            switch(getResultCode()){
                case Activity.RESULT_OK:
                    //Toast.makeText(context, "Le message a certainement dû être envoyé à quelqu'un..." , Toast.LENGTH_SHORT).show();
                    //v.vibrate(1000);
                    break;
                default:
                    Toast.makeText(context, "Erreur, le message n'a pas était envoyé", Toast.LENGTH_SHORT).show();
                    try {
                        if (vibrate) v.vibrate(2000);
                    }catch (Exception e){
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        } else if(iAction.equals(LheidoUtils.ACTION_NEW_MESSAGE_READ)){
            int position = intent.getIntExtra("position", 0);
            String phone = intent.getStringExtra("phone");
            customNewMessageRead(position, phone);
        } else if(iAction.equals(LheidoUtils.ACTION_RECEIVE_MMS)){
            customReceivedMMS();
        }
    }

    public abstract void customReceivedSMS();
    public abstract void customReceivedMMS();
    public abstract void customNewMessageRead(int position, String phone);
    public abstract void customDelivered(long id);


}
