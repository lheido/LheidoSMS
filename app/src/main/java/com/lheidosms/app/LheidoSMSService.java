package com.lheidosms.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Mathilde on 13/06/2014.
 */
public class LheidoSMSService extends Service {
    private static final String SERVICE_TAG = "LHEIDOSMS SERVICE LOG";
    SmsReceiver smsReceiver;
    private ArrayList<LheidoContact> conversations;
    protected Context context;

    @Override
    public void onCreate(){
        Log.v(SERVICE_TAG, "=====> Service start! <=====");
        context = getApplicationContext();
        // load conversations
//        LheidoUtils.ConversationListTask conversationListTask = new LheidoUtils.ConversationListTask(this) {
//            @Override
//            protected void onProgressUpdate(LheidoContact... prog) {
//                if(act != null){
//                    conversations.add(prog[0]);
//                }
//            }
//        };
//        conversationListTask.execConversationListTask();
        // init receiver
        smsReceiver = new SmsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LheidoUtils.ACTION_RECEIVE_SMS);
        filter.addAction(LheidoUtils.ACTION_SENT_SMS);
        filter.addAction(LheidoUtils.ACTION_DELIVERED_SMS);
        filter.setPriority(2000);
        getApplication().registerReceiver(smsReceiver, filter);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        Log.v(SERVICE_TAG, "=====> Service done! <=====");
        getApplication().unregisterReceiver(smsReceiver);
        super.onDestroy();
    }

    public void getConversationsList(){
        ConversationsListTask c = new ConversationsListTask();
        c.execTask();
    }

    private final class ConversationsListTask extends AsyncTask<Void, LheidoContact, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            final String[] projection = new String[] {"_id", "date", "message_count", "recipient_ids", "read", "type"};
            Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
            Cursor query = context.getContentResolver().query(uri, projection, null, null, "date DESC");
            if(query != null) {
                if (query.moveToFirst()) {
                    do {
                        publishProgress(LheidoUtils.getLConversationInfo(context, query));
                    } while (query.moveToNext());
                }
                query.close();
            }
            return true;
        }

        @Override
        protected void onProgressUpdate (LheidoContact... prog){
            conversations.add(prog[0]);
        }

        public void execTask(){
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                execute();
            }
        }
    }

}
