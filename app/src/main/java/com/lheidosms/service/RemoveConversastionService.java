package com.lheidosms.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.lheidosms.app.Global;
import com.lheidosms.utils.LheidoContact;
import com.lheidosms.utils.LheidoUtils;
import com.lheidosms.utils.Message;

public class RemoveConversastionService extends Service {
    private static final String SERVICE_TAG = "REMOVE CONVERSATION LOG";
    private Context context;

    @Override
    public void onCreate() {
        Log.v(SERVICE_TAG, "=====> Service start! <=====");
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.v(SERVICE_TAG, "=====> onStartCommand <=====");
        try {
            String id = intent.getStringExtra("conversationId");
            if(id != null && !id.isEmpty()) {
                RemoveConversationTask delete = new RemoveConversationTask(id);
                delete.execTask();
            }
        }catch (Exception e){e.printStackTrace();}
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        Log.v(SERVICE_TAG, "=====> Service done! <=====");
        super.onDestroy();
    }

    private final class RemoveConversationTask extends AsyncTask<Void, Message, Boolean> {

        private final String id;

        public RemoveConversationTask(String id){
            this.id = id;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                int c_index = -1;
                for (LheidoContact c : Global.conversationsList) {
                    if (c.getConversationId().equals(id)) {
                        LheidoUtils.delete_sms(context, c, 0);
                        c_index = Global.conversationsList.indexOf(c);
                        LheidoUtils.Send.notifyDataChanged(context);
                    }
                }
                if(c_index != -1){
                    Global.conversationsList.remove(c_index);
                }
            }catch (Exception e){e.printStackTrace();}
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
