package com.lheidosms.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class LheidoUtils {

    public static final String ARG_SMS_DELIVERED = "new_sms_delivered";
    public static final String ACTION_RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";
    public static final String ACTION_RECEIVE_MMS = "android.provider.Telephony.MMS_RECEIVED";
    public static final String ACTION_SENT_SMS = "com.lheidosms.app.sent";
    public static final String ACTION_DELIVERED_SMS = "com.lheidosms.app.delivered";
    public static final String ACTION_FIRST = "com.lheidosms.app.first";
    public static final String ACTION_NEW_MESSAGE = "com.lheido.app.new_message";
    public static final String ACTION_NEW_MESSAGE_READ = "com.lheido.app.new_message_read";
    public static final String ACTION_NOTIFY_DATA_CHANGED = "com.lheido.app.notify_data_changed";

    public static final String drawer_start_opened_key = "drawer_start_opened";
    public static final String hide_keyboard_key = "hide_keyboard";
    public static final String first_uppercase_key = "first_upper_letter";
    public static final String vibration_key = "receiver_vibrate";
    public static final String receiver_ringtone_key = "receiver_ringtone";
    public static final String delivered_vibration_key = "receiver_vibrate_delivered";
    public static final String sms_onload_key = "sms_onload";
    public static final String text_size_key = "text_size";
    public static final String hold_message_key = "delete_hold_sms";
    public static final String hold_message_num_key = "limit_hold_sms";
    public static final String conversation_jazzyeffect_key = "conversation_jazzyeffect";
    public static final String list_conversations_jazzyeffect_key = "list_conversations_jazzyeffect";
    public static final String receiver_notification_key = "receiver_notification";
    public static final String conversation_onload_key = "conversation_onload";

    public static class UserPref{
        public int max_conversation = 10;
        public int max_sms = 21;
        public boolean hide_keyboard = true;
        public boolean first_upper = true;
        public boolean vibrate = true;
        public boolean vibrate_delivered = true;
        public float text_size = 13.0F;
        public boolean hold_message = true;
        public int hold_message_num = 500;
        public boolean drawer = false;
        public int conversation_effect = 14;
        public int listConversation_effect = 14;
        UserPref(){}
        public void setUserPref(SharedPreferences pref){
            String pref_nb_conv = pref.getString(conversation_onload_key, "10");
            String pref_nb_sms = pref.getString(sms_onload_key, "42");
            String pref_text_size = pref.getString(text_size_key, "13");
            this.conversation_effect = Integer.parseInt(pref.getString(conversation_jazzyeffect_key, "14"));
            this.listConversation_effect = Integer.parseInt(pref.getString(list_conversations_jazzyeffect_key, "14"));
            this.hold_message = pref.getBoolean(hold_message_key, true);
            this.hold_message_num = Integer.parseInt(pref.getString(hold_message_num_key, "500"));
            try{
                this.max_conversation = Integer.parseInt(pref_nb_conv);
            }catch(Exception ex){
                this.max_conversation = 10000;
            }
            try{
                this.max_sms = Integer.parseInt(pref_nb_sms);
            }catch(Exception ex){
                this.max_sms = 100000;
            }
            try{
                this.text_size = Float.parseFloat(pref_text_size);
            }catch(Exception ex){
                this.text_size = 13.0F;
            }
            this.hide_keyboard = pref.getBoolean(hide_keyboard_key, true);
            this.first_upper = pref.getBoolean(first_uppercase_key, true);
            this.vibrate = pref.getBoolean(vibration_key, true);
            this.vibrate_delivered = pref.getBoolean(delivered_vibration_key, true);
            this.drawer = pref.getBoolean(drawer_start_opened_key, false);
        }
    }

    public static void retrieveContact(Context context, LheidoContact contact, String phone){
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};
        Cursor cur = context.getContentResolver().query(uri, projection, null, null, null);
        if(cur != null){
            if(cur.moveToFirst()){
                try{
                    contact.setId(cur.getLong(cur.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID)));
                }catch(Exception ex){
                    Toast.makeText(context, "Error setId\n" + ex.toString(), Toast.LENGTH_LONG).show();
                }
                try{
                    contact.setName(cur.getString(cur.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)));
                }catch(Exception ex){
                    Toast.makeText(context, "Error setName\n"+ex.toString(), Toast.LENGTH_LONG).show();
                }
                try{
                    contact.setPic();
                }catch(Exception ex){
                    Toast.makeText(context, "Error setPic\n"+ex.toString(), Toast.LENGTH_LONG).show();
                }
            }else
                contact.setName(phone);
            cur.close();
        }
    }

    public static LheidoContact getLConversationInfo(Context context, Cursor query){
        LheidoContact contact = new LheidoContact();
        contact.setConversationId(query.getString(query.getColumnIndex("_id")));
        contact.setNb_sms(query.getString(query.getColumnIndex("message_count")));
        String recipientId = query.getString(query.getColumnIndex("recipient_ids"));
        String[] recipientIds = recipientId.split(" ");
        for (String recipientId1 : recipientIds) {
            Uri ur = Uri.parse("content://mms-sms/canonical-addresses");
            if (!recipientId1.equals("")) {
                Cursor cr = context.getContentResolver().query(ur, new String[]{"*"}, "_id = " + recipientId1, null, null);
                if (cr != null) {
                    while (cr.moveToNext()) {
                        //String id = cr.getString(0);
                        String address = cr.getString(1);
                        contact.setPhone(address);
                        retrieveContact(context, contact, address);
                        //contact.setName(context, address);
                        //contact.setPic(context);
                    }
                    cr.close();
                }
            }
        }
        return contact;
    }


    public static abstract class ConversationListTask extends AsyncTask<Void, LheidoContact, Boolean> {

        protected WeakReference<FragmentActivity> act = null;
        private Context context = null;
        private UserPref userPref = null;

        public ConversationListTask(FragmentActivity activity){
            link(activity);
        }

        @Override
        protected void onPreExecute () {
            if(act.get() != null){
                context = act.get().getApplicationContext();
                userPref = new UserPref();
                userPref.setUserPref(PreferenceManager.getDefaultSharedPreferences(context));
            }
        }

        @Override
        protected void onPostExecute (Boolean result) {
            if (act.get() != null) {
                if(!result)
                    Toast.makeText(context, "Problème génération liste conversations", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(act.get() != null){
                final String[] projection = new String[] {"_id", "date", "message_count", "recipient_ids", "read", "type"};
                Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
                Cursor query = context.getContentResolver().query(uri, projection, null, null, "date DESC");
                if(query != null) {
                    if (query.moveToFirst()) {
                        int i = 0;
                        do {
                            publishProgress(getLConversationInfo(context, query));
                            i = i + 1;
                        } while (i < userPref.max_conversation && query.moveToNext());
                    } else {
                        //mConversationListe.add("Pas de conversations !");
                    }
                    query.close();
                }
                return true;
            }
            return false;
        }

        @Override
        abstract protected void onProgressUpdate (LheidoContact... prog);

        public void link (FragmentActivity pActivity) {
            act = new WeakReference<FragmentActivity>(pActivity);
        }

        public void execConversationListTask(){
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                execute();
            }
        }

    }


    public static abstract class ConversationTask extends AsyncTask<Void, Message, Boolean> {

        private long last_sms = -1;
        protected WeakReference<FragmentActivity> act = null;
        private Context context = null;
        private UserPref userPref = null;
        private int conversationId;
        private final String sms_uri = "content://sms";
        private final String[] projection = {"*"};
        private String selection = "thread_id = ? AND body != ?";
        private ArrayList<String> selectionArgs = new ArrayList<String>();


        public ConversationTask(FragmentActivity activity, int id){
            link(activity);
            conversationId = id;
            selectionArgs.add("" + conversationId);
            selectionArgs.add("LHEIDO_SMS_CONVERSATION_CLEAR");
        }

        public ConversationTask(FragmentActivity activity, int id, long last_id_sms){
            link(activity);
            conversationId = id;
            last_sms = last_id_sms;
            selection = "thread_id = ? AND body != ? AND _id < ?";
            selectionArgs.add("" + conversationId);
            selectionArgs.add("LHEIDO_SMS_CONVERSATION_CLEAR");
            selectionArgs.add("" + last_sms);
        }

        @Override
        protected void onPreExecute () {
            if(act.get() != null){
                context = act.get().getApplicationContext();
                userPref = new UserPref();
                userPref.setUserPref(PreferenceManager.getDefaultSharedPreferences(context));
            }
        }

        @Override
        protected void onPostExecute (Boolean result) {
            if (act.get() != null) {
                if(!result)
                    Toast.makeText(context, "Problème génération conversation", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if(act.get() != null){
                try{
                    Uri uri = Uri.parse(sms_uri);
                    Cursor query = context.getContentResolver().query(
                            uri,
                            projection,
                            selection,
                            selectionArgs.toArray(new String[selectionArgs.size()]),
                            "date DESC");
                    int count = 0;
                    if(query != null){
                        long _id; String body; String type; int status; long date;
                        while(count < userPref.max_sms && query.moveToNext()){
                            _id = query.getLong(query.getColumnIndexOrThrow("_id"));
                            body = query.getString(query.getColumnIndexOrThrow("body"));
                            type = query.getString(query.getColumnIndexOrThrow("type"));
                            status = query.getInt(query.getColumnIndexOrThrow("status"));
                            date = query.getLong(query.getColumnIndexOrThrow("date"));
                            Time t = new Time();
                            t.set(date);
                            Message sms = new Message(_id, body, type, status, t);
                            publishProgress(sms);
                            count += 1;
                        }
                        query.close();
                        if(count == 0 && last_sms == -1){
                            Time now = new Time();
                            now.setToNow();
                            Message sms = new Message(-1L, "Pas de sms", "1", 0, now);
                            publishProgress(sms);
                        }
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }

                return true;
            }
            return false;
        }

        @Override
        abstract protected void onProgressUpdate (Message... prog);

        public void link (FragmentActivity pActivity) {
            act = new WeakReference<FragmentActivity>(pActivity);
        }

        public void execConversationTask(){
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                execute();
            }
        }
    }

    public static long store_sms(Context context, Message sms, long thread_id){
        try {
            ContentValues values = new ContentValues();
            values.put("address", sms.getPhone());
            values.put("body", sms.getBody());
            values.put("read", false);
            values.put("type", sms.isRight() ? 2 : 1);
            values.put("status", 32);
            if(thread_id != -1)
                values.put("thread_id", thread_id);
            values.put("date", sms.getDateNormalize());
            Uri uri_id = context.getContentResolver().insert(Uri.parse("content://sms"), values);
            if(uri_id != null)
                return Long.parseLong(uri_id.toString().substring(14));
        } catch (Exception ex) {
            Toast.makeText(context, "store_sms\n"+ex.toString(), Toast.LENGTH_LONG).show();
        }
        return -1;
    }
}
