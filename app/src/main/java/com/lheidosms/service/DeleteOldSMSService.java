package com.lheidosms.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.lheidosms.app.Global;
import com.lheidosms.utils.LheidoContact;
import com.lheidosms.utils.LheidoUtils;
import com.lheidosms.utils.Message;

public class DeleteOldSMSService extends Service {
    private static final String SERVICE_TAG = "DELETE SERVICE LOG";
    private Context context;

    @Override
    public void onCreate() {
//        Log.v(SERVICE_TAG, "=====> Service start! <=====");
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
//        Log.v(SERVICE_TAG, "=====> onStartCommand <=====");
        DeleteAllOldTask delete = new DeleteAllOldTask();
        delete.execTask();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
//        Log.v(SERVICE_TAG, "=====> Service done! <=====");
        super.onDestroy();
    }

    private final class DeleteAllOldTask extends AsyncTask<Void, Message, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            LheidoUtils.UserPref userPref = new LheidoUtils.UserPref();
            userPref.setUserPref(PreferenceManager.getDefaultSharedPreferences(context));
            for(LheidoContact c : Global.conversationsList){
                if(c.getNb_sms() > userPref.old_message_num){
                    LheidoUtils.delete_sms(context, c, userPref.old_message_num);
                    int i = Global.conversationsList.indexOf(c);
                    if(i != -1) {
                        String nb_sms_updated = LheidoUtils.getMessageCount(context, c.getConversationId());
                        if (nb_sms_updated != null) {
                            Global.conversationsList.get(i).setNb_sms(nb_sms_updated);
                            LheidoUtils.Send.notifyDataChanged(context);
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute (Boolean result) {
            stopSelf();
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
