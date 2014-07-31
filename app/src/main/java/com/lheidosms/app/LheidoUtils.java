package com.lheidosms.app;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
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
    public static final String ACTION_USER_NEW_MESSAGE = "com.lheido.app.user_new_message";

    public static final String drawer_start_opened_key = "drawer_start_opened";
    public static final String hide_keyboard_key = "hide_keyboard";
    public static final String first_uppercase_key = "first_upper_letter";
    public static final String vibration_key = "receiver_vibrate";
    public static final String receiver_ringtone_key = "receiver_ringtone";
    public static final String delivered_vibration_key = "receiver_vibrate_delivered";
    public static final String sms_onload_key = "sms_onload";
    public static final String text_size_key = "text_size";
    public static final String old_message_key = "delete_old_sms";
    public static final String old_message_num_key = "limit_old_sms";
    public static final String conversation_jazzyeffect_key = "conversation_jazzyeffect";
    public static final String list_conversations_jazzyeffect_key = "list_conversations_jazzyeffect";
    public static final String receiver_notification_key = "receiver_notification";
    public static final String conversation_onload_key = "conversation_onload";


    public static class Send {
        public static void receiveNewMessage(Context context){
            Intent i = new Intent(ACTION_NEW_MESSAGE);
            context.sendBroadcast(i);
        }
        public static void first(Context context){
            Intent i = new Intent(ACTION_FIRST);
            context.sendBroadcast(i);
        }

        public static void notifyDataChanged(Context context) {
            Intent i = new Intent(ACTION_NOTIFY_DATA_CHANGED);
            context.sendBroadcast(i);
        }

        public static void newMessageRead(Context context, int position, String phone) {
            Intent i = new Intent(ACTION_NEW_MESSAGE_READ);
            i.putExtra("position", position);
            i.putExtra("phone", phone);
            context.sendBroadcast(i);
        }

        public static void userNewMessage(Context context, String phoneContact) {
            Intent i = new Intent(ACTION_USER_NEW_MESSAGE);
            i.putExtra("phone", phoneContact);
            context.sendBroadcast(i);
        }
    }

    public static class UserPref{
        public int max_conversation = 10;
        public int max_sms = 21;
        public boolean hide_keyboard = true;
        public boolean first_upper = true;
        public boolean vibrate = true;
        public boolean vibrate_delivered = true;
        public float text_size = 13.0F;
        public boolean old_message = true;
        public int old_message_num = 500;
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
            this.old_message = pref.getBoolean(old_message_key, true);
            this.old_message_num = Integer.parseInt(pref.getString(old_message_num_key, "500"));
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
    public static void delete_sms(Context context, LheidoContact lcontact, int count){
        Uri uri = Uri.parse("content://sms");
        String[] projection = {"*"};
        String selection = "thread_id = ?";
        String[] selectionArgs = {""+lcontact.getConversationId()};
        Cursor cr = context.getContentResolver().query(uri, projection, selection, selectionArgs, "date DESC");
        if(cr != null){
            ArrayList<Long> list_id_delete = new ArrayList<Long>();
            long c = 0;
            while(cr.moveToNext()){
                if(c >= count)
                    list_id_delete.add(cr.getLong(cr.getColumnIndexOrThrow("_id")));
                c ++;
            }
            cr.close();
            int i = 0;
            for(Long id : list_id_delete){
                i += 1;
                context.getContentResolver().delete(Uri.parse("content://sms/"+id), selection, selectionArgs);
            }
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


    public static String getMessageCount(Context context, String id){
        String res = null;
        try {
            final String[] projection = new String[]{"_id", "message_count"};
            Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
            Cursor query = context.getContentResolver().query(uri, projection, null, null, "date DESC");
            if(query != null){
                boolean find = false;
                while(query.moveToNext() && !find) {
                    if(query.getString(query.getColumnIndex("_id")).equals(id)) {
                        res = query.getString(query.getColumnIndex("message_count"));
//                        Log.v("getMessageCount", "find, nb_sms = "+res);
                        find = true;
                    }
                }
                query.close();
            }
        }catch(Exception e){
            Log.v("getMessageCount", "Erreur");
            e.printStackTrace();
        }
        return res;
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
                        long _id; String body; String type; int status; long date; String sender;
                        while(count < userPref.max_sms && query.moveToNext()){
                            _id = query.getLong(query.getColumnIndexOrThrow("_id"));
                            body = query.getString(query.getColumnIndexOrThrow("body"));
//                            type = query.getString(query.getColumnIndexOrThrow("type"));
//                            Log.v("LOG type", "type = "+type+", body = "+body);
                            sender = query.getString(query.getColumnIndexOrThrow("address"));
                            if(sender == null) sender = getUserPhone(context);
                            status = query.getInt(query.getColumnIndexOrThrow("status"));
                            date = query.getLong(query.getColumnIndexOrThrow("date"));
                            Time t = new Time();
                            t.set(date);
                            Message sms = new Message(_id, body, sender, status, t);
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

    public static abstract class MMSTask extends AsyncTask<Void, Message, Boolean>{

        private final String mms_uri = "content://mms";
        private final String[] projection = {"*"};
        private long last_sms = -1;
        private String selection = "thread_id = ?";
        private ArrayList<String> selectionArgs = new ArrayList<String>();

        private final int conversationId;
        private UserPref userPref;
        private Context context;
        protected WeakReference<FragmentActivity> act;

        public MMSTask(FragmentActivity activity, int conversationId){
            link(activity);
            this.conversationId = conversationId;
            selectionArgs.add(""+conversationId);
        }

        public MMSTask(FragmentActivity activity, int conversationId, long last_id) {
            link(activity);
            this.conversationId = conversationId;
            last_sms = last_id;
            selection = "thread_id = ? AND _id < ?";
            selectionArgs.add("" + conversationId);
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
                Cursor allMms = context.getContentResolver().query(
                        Uri.parse(mms_uri),
                        projection,
                        selection,
                        selectionArgs.toArray(new String[selectionArgs.size()]),
                        "date DESC");

                if(allMms != null){
//                    for (int i = 0; i < allMms.getColumnCount(); i++) {
//                        Log.v("LheidoSMS Log MMS", ""+allMms.getColumnName(i));
//                    }
                    int count = 0;
                    while(count < userPref.max_sms && allMms.moveToNext()){
                        long mmsId = allMms.getLong(allMms.getColumnIndexOrThrow("_id"));
                        int read = allMms.getInt(allMms.getColumnIndexOrThrow("read"));
                        String senderAdd = getAddressNumber(mmsId);
                        if(senderAdd == null) senderAdd = getUserPhone(context);
//                        Log.v("LHEIDO SMS LOG MMS", "_id = "+mmsId+",\n sender = "+senderAdd);
                        Message mms = getMMSData(mmsId, senderAdd);
                        long date = allMms.getLong(allMms.getColumnIndex("date"));
                        Time t = new Time();
                        t.set(date);
                        mms.setDate(t);
                        publishProgress(mms);
                        //add_sms(_id, string, type, read, t, 1, liste);
                        count += 1;
                    }
                    allMms.close();
                    if(count == 0 && last_sms == -1){
                        Time now = new Time();
                        now.setToNow();
                        Message sms = new Message(-1L, "Pas de mms", "1", 0, now);
                        publishProgress(sms);
                    }
                }
                return true;
            }
            return false;
        }

        private String getAddressNumber(long id) {
            String selectionAdd = "msg_id=" + id;
            String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
            Uri uriAddress = Uri.parse(uriStr);
            Cursor cAdd = context.getContentResolver().query(uriAddress, null,
                    selectionAdd, null, null);
            String name = null;
            if (cAdd != null) {
                if (cAdd.moveToFirst()) {
                    do {
                        String number = cAdd.getString(cAdd.getColumnIndex("address"));
                        if (number != null) {
                            try {
                                Long.parseLong(number.replace("-", ""));
                                name = number;
                            } catch (NumberFormatException nfe) {
                                if (name == null) {
                                    name = number;
                                }
                            }
                        }
                    } while (cAdd.moveToNext());
                }
                cAdd.close();
            }
            return name;
        }

        private Message getMMSData(long mmsId, String sender) {
            Message mms = null;
            String selectionPart = "mid=" + mmsId;
            Uri uri = Uri.parse("content://mms/part");
            try{
                mms = new Message();
                Cursor cPart = context.getContentResolver().query(uri, new String[] {"*"}, selectionPart, null, null);
                if(cPart != null) {
//                    for (int i = 0; i < cPart.getColumnCount(); i++) {
//                        Log.v("LheidoSMS Log MMS content://mms/part", ""+cPart.getColumnName(i));
//                    }
                    if (cPart.moveToFirst()) {
                        do {
                            mms.setId(mmsId);
                            mms.setSender(sender);
                            String partId = cPart.getString(cPart.getColumnIndex("_id"));
                            String type = cPart.getString(cPart.getColumnIndex("ct"));
                            if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                                    "image/gif".equals(type) || "image/jpg".equals(type) ||
                                    "image/png".equals(type)) {
                                mms.setUriPicture(getMmsImageUri(partId));
                            }
                            if ("text/plain".equals(type)) {
                                String data = cPart.getString(cPart.getColumnIndex("_data"));
                                String body;
                                if (data != null) {
                                    body = getMmsText(partId);
                                } else {
                                    body = cPart.getString(cPart.getColumnIndex("text"));
                                }
                                mms.setBody(body);
                            }
                        } while (cPart.moveToNext());
                        cPart.close();
                    }
                }
            }catch(Exception ex){ex.printStackTrace();}
            return mms;
        }

        private String getMmsText(String partId) {
            Uri partURI = Uri.parse("content://mms/part/" + partId);
            InputStream is = null;
            StringBuilder sb = new StringBuilder();
            try {
                is = context.getContentResolver().openInputStream(partURI);
                if (is != null) {
                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                    String temp = reader.readLine();
                    while (temp != null) {
                        sb.append(temp);
                        temp = reader.readLine();
                    }
                }
            } catch (IOException e) {e.printStackTrace();}
            finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {e.printStackTrace();}
                }
            }
            return sb.toString();
        }

        private Uri getMmsImageUri(String partId) {
            return Uri.parse("content://mms/part/" + partId);
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

    public static long store_sms(Context context, String phoneContact, Message sms, long thread_id){
        try {
            ContentValues values = new ContentValues();
            values.put("address", sms.getSender());
            values.put("body", sms.getBody());
            values.put("read", false);
            values.put("type", (!PhoneNumberUtils.compare(phoneContact, sms.getSender())) ? 2 : 1);
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

    public static boolean isRight(Context context, String sender){
        TelephonyManager telemamanger = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return PhoneNumberUtils.compare(telemamanger.getLine1Number(), sender);
    }

    public static String getUserPhone(Context context){
        TelephonyManager telemamanger = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telemamanger.getLine1Number();
    }

    public static abstract class LheidoDialog extends Dialog {
        /**
         *
         * @param context   : Context android.
         * @param ressource : layout.
         * @param title     : dialog title.
         */
        public LheidoDialog(Context context, int ressource, String title) {
            super(context);
            setContentView(ressource);
            setTitle(title);
            customInit();
            Button ok = (Button) findViewById(R.id.ok_button);
            if(ok != null){
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customOk();
                        dismiss();
                    }
                });
            }
            Button cancel = (Button) findViewById(R.id.cancel_button);
            if(cancel != null){
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customCancel();
                        cancel();
                    }
                });
            }
        }

        public abstract void customInit();

        public abstract void customCancel();

        public abstract void customOk();
    }

    public static String getNewThreadID(Context context){
        int new_id = 0;
        final String[] projection = new String[] {"_id"};
        Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
        Cursor query = context.getContentResolver().query(uri, projection, null, null, "date DESC");
        if(query != null){
            while(query.moveToNext()) {
                int tmp = query.getInt(query.getColumnIndexOrThrow("_id")) + 1;
                if(tmp > new_id) {
                    new_id = tmp;
                }
            }
//            Log.v("LHEIDO SMS LOG", last);
//            Log.v("LHEIDO SMS LOG", "nb rows = " + query.getCount());
            query.close();
        }
        return ""+new_id;
    }

    public static Long getOrCreateThreadId(Context context, String phone){
        try{
            Uri threadIdUri = Uri.parse("content://mms-sms/threadID");
            Uri.Builder builder = threadIdUri.buildUpon();
            String[] recipients = {phone};
            for(String recipient : recipients){
                builder.appendQueryParameter("recipient", recipient);
            }
            Uri uri = builder.build();
            Long threadId = 0L;
            Cursor cursor = context.getContentResolver().query(uri, new String[]{"_id"}, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        threadId = cursor.getLong(0);
                    }
                } finally {
                    cursor.close();
                }
                Log.v("threadId TEST", "threadId = "+threadId);
                return threadId;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1L;
    }

    public static abstract class GetContacts extends AsyncTask<Void, LheidoContact, Boolean>{

        private WeakReference<MainLheidoSMS> act;
        private Context context;

        public GetContacts(MainLheidoSMS activity){
            link(activity);
        }

        @Override
        protected void onPreExecute () {
            if(act.get() != null){
                context = act.get().getApplicationContext();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String[] projection = {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER,

            };
            if(act.get() != null){
                Cursor c = context.getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);
                if(c != null){
                    while(c.moveToNext()){
                        String phone = null;
                        String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        int hasPhone = c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if(hasPhone > 0) {
                            String[] pr = {
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            };
                            Cursor cur = context.getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    pr, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+id,
                                    null, null);
                            if(cur != null) {
                                while(cur.moveToNext()) {
                                    phone = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                }
                                cur.close();
                                LheidoContact contact = new LheidoContact();
                                contact.setId(Long.parseLong(id));
                                contact.setName(name);
                                contact.setPhone(phone);
                                contact.setPic();
                                publishProgress(contact);
                            }
                        }
                    }
                    c.close();
                    return true;
                }
            }
            return false;
        }

        @Override
        abstract protected void onProgressUpdate (LheidoContact... prog);

        @Override
        protected void onPostExecute (Boolean result) {
            if (act.get() != null) {
                if(!result)
                    Toast.makeText(context, "Problème GetContacts", Toast.LENGTH_LONG).show();
            }
        }

        public void link (MainLheidoSMS pActivity) {
            act = new WeakReference<MainLheidoSMS>(pActivity);
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
