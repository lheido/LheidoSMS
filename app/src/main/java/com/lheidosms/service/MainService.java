package com.lheidosms.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneNumberUtils;
import android.text.format.Time;
import android.widget.Toast;

import com.lheidosms.app.Global;
import com.lheidosms.app.MainLheidoSMS;
import com.lheidosms.app.R;
import com.lheidosms.receiver.MainServiceReceiver;
import com.lheidosms.utils.LheidoContact;
import com.lheidosms.utils.LheidoUtils;
import com.lheidosms.utils.Message;

import java.util.HashMap;

/**
 * Created by lheido on 01/11/14.
 */
public class MainService extends Service {

    private Context mContext;
    private HashMap<String, Integer> notificationsId;
    private MainServiceReceiver mBroadcast;
    private IntentFilter filter;
    private SharedPreferences userPref;
    private boolean activ_notif;
    private boolean vibrate;
    private Vibrator v;

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.v(SERVICE_TAG, "=====> Service start! <=====");
        mContext = getApplicationContext();
        Global.conversationsList.clear();
        loadConversationsList();
        notificationsId = new HashMap<String, Integer>();
        userPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        activ_notif = userPref.getBoolean(LheidoUtils.receiver_notification_key, true);
        vibrate = userPref.getBoolean(LheidoUtils.vibration_key, true);
        v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        // init receiver
        mBroadcast = new MainServiceReceiver() {
            @Override
            protected void customReceiveSms(String new_name, String phone, String body, long date) {
                userPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                activ_notif = userPref.getBoolean(LheidoUtils.receiver_notification_key, true);
                Toast.makeText(mContext, "Sms reçu de " + new_name, Toast.LENGTH_LONG).show();
                if(activ_notif){
                    if(!notificationsId.containsKey(phone))
                        notificationsId.put(phone, notificationsId.size());
                    Intent notificationIntent = new Intent(mContext, MainLheidoSMS.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
                    Intent openAction = new Intent(mContext, MainLheidoSMS.class);
                    openAction.putExtra("name", new_name);
                    openAction.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent openPending = PendingIntent.getActivity(mContext, 0, openAction, PendingIntent.FLAG_CANCEL_CURRENT);
                    showNotification(body, new_name, phone, pIntent, openPending);
                }
                playNotificationSound();
                moveConversationOnTop(phone, true);
                LheidoUtils.Send.receiveNewMessage(mContext);
                Time d = new Time();
                d.set(date);
                LheidoUtils.Send.notifyReceiveSms(mContext, new Message(-1, body, phone, 1, d));
            }

            @Override
            protected void customDeliveredSms(long id) {
                userPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                Toast.makeText(mContext, "Message remis" , Toast.LENGTH_SHORT).show();
                LheidoUtils.Send.notifyDeliveredSms(mContext, id);
                if(userPref.getBoolean("delivered_vibration", true)){
                    try {
                        long[] pattern = { 0, 100, 100, 100, 100, 100, 100, 100, 10 };
                        v.vibrate(pattern, -1);
                    }catch (Exception e){
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            protected void customNewMessageRead(int position, String phone) {
                cancelNotif(phone);
                int i = 0;
                int size = Global.conversationsList.size();
                while(i < size && !PhoneNumberUtils.compare(Global.conversationsList.get(i).getPhone(), phone)) {i++;}
                if(i < size && PhoneNumberUtils.compare(Global.conversationsList.get(i).getPhone(), phone)) {
                    // retrieved position in conversationsList
                    Global.conversationsList.get(i).markNewMessage(false);
                }
                LheidoUtils.Send.notifyDataChanged(mContext);
            }

            @Override
            protected void customSentSms() {

            }

            @Override
            protected void customReceiveMms(String new_name, String phone, String body, long date) {
                Toast.makeText(mContext, "Mms reçu de " + new_name, Toast.LENGTH_LONG).show();
                if(activ_notif){
                    Intent notificationIntent = new Intent(mContext, MainLheidoSMS.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
                    Intent openAction = new Intent(mContext, MainLheidoSMS.class);
                    openAction.putExtra("name", new_name);
                    PendingIntent openPending = PendingIntent.getActivity(mContext, 0, openAction, 0);
                    showNotification("MMS", new_name, phone, pIntent, openPending);
                }
                playNotificationSound();
                //moveConversationOnTop(phone, true);
                LheidoUtils.Send.receiveNewMessage(mContext);
            }

            @Override
            protected void customUserNewMessage(String phone) {
                moveConversationOnTop(phone, false);
                LheidoUtils.Send.receiveNewMessage(mContext);
            }

            @Override
            protected void customCancelVibrator() {
                v.cancel();
            }
        };
        filter = mBroadcast.getIntentFilter(3000);
        getApplicationContext().registerReceiver(mBroadcast, filter);
        //display toast for dev
        Toast.makeText(mContext, "LheidoSMS Service started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
//        Log.v(SERVICE_TAG, "=====> onStartCommand <=====");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
//        Log.v(SERVICE_TAG, "=====> Service done! <=====");
        getApplicationContext().unregisterReceiver(mBroadcast);
        super.onDestroy();
    }

    public void showNotification(String body, String name, String phone, PendingIntent pIntent, PendingIntent openConversationIntent){
        // Create Notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.lheido_sms_icon)
                .setTicker(body)
                .setContentTitle("" + name)
                .setContentText(body)
                .setPriority(2)
                        // Add an Action Button below Notification
                .addAction(R.drawable.send_sms, "Ouvrir", openConversationIntent)
                        // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                        // Dismiss Notification
                .setAutoCancel(true);

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        int id = 0;
        if(notificationsId.containsKey(phone))
            id = notificationsId.get(phone);
        notificationmanager.notify(id, builder.build());
    }

    public void playNotificationSound(){
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        userPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                try {
                    if (vibrate) v.vibrate(1000);
                }catch (Exception e){
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                break;
            case AudioManager.RINGER_MODE_NORMAL:

                if(userPref.getBoolean(LheidoUtils.receiver_ringtone_key, true)) {
                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(mContext, notification);
                        r.play();
                    } catch (Exception e) {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public void cancelNotif(String phone){
//        Log.v("LHEIDO SMS LOG", notificationsId.keySet().toString());
        try {
            NotificationManager notificationmanager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            for(String p : notificationsId.keySet()){
                if(PhoneNumberUtils.compare(p, phone))
                    notificationmanager.cancel(notificationsId.get(p));
            }
        }catch (Exception ex){ex.printStackTrace();}
    }

    private static void moveConversationOnTop(String phone, boolean mark) {
        // get contact position in conversationList
        int i = 0;
        int size = Global.conversationsList.size();
        while(i < size && !PhoneNumberUtils.compare(Global.conversationsList.get(i).getPhone(), phone)) {i++;}
        if(i < size && PhoneNumberUtils.compare(Global.conversationsList.get(i).getPhone(), phone)) {
            // retrieved position in conversationsList
            LheidoContact c = Global.conversationsList.remove(i);
            c.Nb_sms_Plus();
            if(mark) c.markNewMessage(true);
            Global.conversationsList.add(0, c);
        } else{
            // not in conversationsList
        }
    }

    public void loadConversationsList(){
        final String[] projection = new String[] {"_id", "date", "message_count", "recipient_ids", "read", "type"};
        Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
        Cursor query = mContext.getContentResolver().query(uri, projection, null, null, "date DESC");
        if(query != null) {
            if (query.moveToFirst()) {
                do {
                    Global.conversationsList.add(LheidoUtils.getLConversationInfo(mContext, query));
                    if(Global.conversationsList.size() == 1)
                        LheidoUtils.Send.first(mContext);
                } while (query.moveToNext());
            }
            query.close();
        }
    }
}
