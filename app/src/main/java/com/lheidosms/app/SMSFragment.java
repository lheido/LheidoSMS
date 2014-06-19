package com.lheidosms.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.InputType;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import com.twotoasters.jazzylistview.JazzyListView;

/**
 * A placeholder fragment containing a simple view.
 */
public class SMSFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String ARG_CONVERSATION_ID = "conversation_id";
    public static final String ARG_CONVERSATION_COUNT = "conversation_count";
    public static final String ARG_CONTACT_PHONE = "contact_phone";
    public static final String ARG_CONVERSATION_NUMBER = "conversation_number";
    public static final String ARG_CONTACT_NAME = "contact_name";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private EditText sms_body;
    private FragmentActivity context;
    private LheidoUtils.UserPref userPref;
    private String name;
    protected String phoneContact;
    private int conversationId; // id for database
    private long conversation_nb_sms;
    private JazzyListView liste;
    private int list_conversationId;
    public ArrayList<Message> Message_list = new ArrayList<Message>();
    private ConversationAdapter conversationAdapter;
    private String mem_body = null;
    private BroadcastReceiver mBroadCast;
    private SwipeRefreshLayout swipeLayout;
    private int lastVisibleChild;
    public int progress=0;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SMSFragment newInstance(int position, LheidoContact contact) {
        SMSFragment fragment = new SMSFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CONVERSATION_NUMBER, position);
        args.putString(ARG_CONTACT_NAME, contact.getName());
        args.putString(ARG_CONTACT_PHONE, contact.getPhone());
        args.putInt(ARG_CONVERSATION_ID, Integer.parseInt(contact.getConversationId()));
        args.putLong(ARG_CONVERSATION_COUNT, contact.getNb_sms());
        fragment.setArguments(args);
        return fragment;
    }

    public SMSFragment() {
    }

    public String getPhoneContact(){
        return phoneContact;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.conversation, container, false);
        init(rootView);
        init_broadcast();
        return rootView;
    }

    private void init(View rootView){
        context = getActivity();
        userPref = new LheidoUtils.UserPref();
        userPref.setUserPref(PreferenceManager.getDefaultSharedPreferences(context));
        name = getArguments().getString(ARG_CONTACT_NAME);
        phoneContact = getArguments().getString(ARG_CONTACT_PHONE);
        conversationId = getArguments().getInt(ARG_CONVERSATION_ID);
        conversation_nb_sms = getArguments().getLong(ARG_CONVERSATION_COUNT);
        liste = (JazzyListView) rootView.findViewById(R.id.list_conversation);
        liste.setTransitionEffect(userPref.conversation_effect);
        liste.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        list_conversationId = getArguments().getInt(ARG_CONVERSATION_NUMBER);
        liste.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                return false;
            }
        });
        conversationAdapter = new ConversationAdapter(context, R.layout.message, Message_list);
        liste.setAdapter(conversationAdapter);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
    }

    public void add_sms(long _id, String body, String type, int deli,Time t, int position){
        Message sms = new Message();
        if(_id != -1)
            sms.setId(_id);
        sms.setBody(body);
        sms.setDate(t);
        if(type.equals("2")){
            sms.setRight(true);
            if(deli == 0)
                sms.setRead(true);
            else sms.setRead(false);
        }
        add_sms_(sms, position);
    }

    public void add_sms_(Message sms, int position){
        if(position != 0){
            Message_list.add(sms);
        } else{
            Message_list.add(0, sms);
        }
        conversationAdapter.notifyDataSetChanged();
    }

    public void userAddSms(long new_id, String body, String s, int i, Time now, int i1){
        add_sms(new_id, body, s, i, now, i1);
        conversationAdapter.notifyDataSetChanged();
        conversation_nb_sms += 1;
        NavigationDrawerFragment navDrawer = (NavigationDrawerFragment) context.getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        navDrawer.updateContact(list_conversationId, ""+conversation_nb_sms);
        liste.smoothScrollToPosition(liste.getBottom());
    }

    public void gen_conversation(){
        LheidoUtils.ConversationTask gen_list = new LheidoUtils.ConversationTask(getActivity(), conversationId) {
            @Override
            protected void onProgressUpdate(Message... prog) {
                if (this.act.get() != null) {
                    add_sms_(prog[0], 1);
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (act.get() != null) {
                    if (!result)
                        Toast.makeText(context, "Problème génération conversation", Toast.LENGTH_LONG).show();
                    else {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                            liste.smoothScrollToPosition(liste.getBottom());
                        }
                    }
                }
            }
        };
        gen_list.execConversationTask();
    }



//    public void init_sms_body(View rootView){
//        sms_body = (EditText) rootView.findViewById(R.id.send_body);
//        sms_body.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if(hasFocus){
//                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
//                        liste.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
//                }else {
//                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
//                        liste.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
//                    InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                }
//            }
//        });
//        liste.setOnItemClickListener(new ListView.OnItemClickListener(){
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
////                sms_body.clearFocus();
//                liste.requestFocus();
//            }
//        });
//        if(mem_body != null) sms_body.setText(mem_body);
//        sms_body.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES|InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
//        if(!userPref.first_upper)
//            sms_body.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
//        sms_body.setSingleLine(false);
//    }

//    public void init_send_button(View rootView){
//        ImageButton send_button = (ImageButton) rootView.findViewById(R.id.send_button);
//        final ProgressBar noSendBar = (ProgressBar) rootView.findViewById(R.id.no_send_bar);
//        noSendBar.setVisibility(ProgressBar.GONE);
//        noSendBar.setMax(100);
//        send_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(userPref.hide_keyboard){
//                    InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                }
//                String body;
//                if(sms_body.getText() != null) {
//                    body = sms_body.getText().toString();
//                }else
//                    body = "";
//                if(body.length() > 0){
//                    //progressbar
////                    noSendBar.setVisibility(ProgressBar.VISIBLE);
////                    new CountDownTimer(5000, 1) {
////                        public void onTick(long millisUntilFinished) {
////                            progress += 1;
////                            noSendBar.setProgress(progress);
////                        }
////                        public void onFinish() {
////                            noSendBar.setVisibility(ProgressBar.GONE);
////                            progress = 0;
////                        }
////                    }.start();
//                    //
//                    Message new_sms = new Message();
//                    new_sms.setBody(body);
//                    new_sms.setRight(true);
//                    new_sms.setRead(false);
//                    Time now = new Time();
//                    now.setToNow();
//                    new_sms.setDate(now);
//                    long new_id = LheidoUtils.store_sms(context, new_sms, conversationId);
//                    add_sms(new_id, body, "2", 32, now, 0);
//                    conversationAdapter.notifyDataSetChanged();
//                    conversation_nb_sms += 1;
//                    NavigationDrawerFragment navDrawer = (NavigationDrawerFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
//                    navDrawer.updateContact(list_conversationId, ""+conversation_nb_sms);
//                    sms_body.setText(R.string.empty_sms);
//                    SmsManager manager = SmsManager.getDefault();
//                    ArrayList<String> bodyPart = manager.divideMessage(body);
//                    if(bodyPart.size() > 1){
//                        ArrayList<PendingIntent> piSent = new ArrayList<PendingIntent>();
//                        ArrayList<PendingIntent> piDelivered = new ArrayList<PendingIntent>();
//                        for(int i = 0; i < bodyPart.size(); i++){
//                            Intent ideli = new Intent(LheidoUtils.ACTION_DELIVERED_SMS);
//                            ideli.putExtra(LheidoUtils.ARG_SMS_DELIVERED, new_id);
//                            piSent.add(PendingIntent.getBroadcast(context, 0, new Intent(LheidoUtils.ACTION_SENT_SMS) , 0));
//                            piDelivered.add(PendingIntent.getBroadcast(context, 0, ideli , PendingIntent.FLAG_UPDATE_CURRENT));
//                        }
//                        manager.sendMultipartTextMessage(phoneContact, null, bodyPart , piSent, piDelivered);
//                    }
//                    else {
//                        Intent ideli = new Intent(LheidoUtils.ACTION_DELIVERED_SMS);
//                        ideli.putExtra(LheidoUtils.ARG_SMS_DELIVERED, new_id);
//                        PendingIntent piSent = PendingIntent.getBroadcast(context, 0, new Intent(LheidoUtils.ACTION_SENT_SMS) , 0);
//                        PendingIntent piDelivered = PendingIntent.getBroadcast(context, 0, ideli, PendingIntent.FLAG_UPDATE_CURRENT);
//                        manager.sendTextMessage(phoneContact, null, body, piSent, piDelivered);
//                    }
//                    liste.smoothScrollToPosition(liste.getBottom());
//                    sms_body.clearFocus();
//                } else{
//                    Toast.makeText(context, R.string.empty_message, Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//
//        send_button.setOnLongClickListener(new View.OnLongClickListener() {
//
//            @Override
//            public boolean onLongClick(View v) {
//                Toast.makeText(context, ""+conversationId+"\n"+phoneContact+"\n"+name, Toast.LENGTH_LONG).show();
//                return true;
//            }
//        });
//    }

    public void init_broadcast(){
        mBroadCast = new SmsReceiver(){
            @Override
            public void customReceivedSMS() {
                if(PhoneNumberUtils.compare(phoneContact, phone)){
                    //on est dans la bonne conversation !
                    Time t = new Time();
                    t.set(date);
                    add_sms(-1L, body, "", 0, t, 0);
                    conversationAdapter.notifyDataSetChanged();
                    conversation_nb_sms += 1;
                    liste.smoothScrollToPosition(liste.getBottom());
                    Intent i = new Intent(LheidoUtils.ACTION_NEW_MESSAGE_READ);
                    i.putExtra("position", list_conversationId);
                    i.putExtra("phone", phoneContact);
                    context.sendBroadcast(i);
                }
            }

            @Override
            public void customReceivedMMS() {
                if(PhoneNumberUtils.compare(phoneContact, phone)){

                }
            }

            @Override
            public void customNewMessageRead(int position, String phone) {}

            @Override
            public void customDelivered(long _id){
                int k = 0;
                boolean find = false;
                while(!find && k < Message_list.size()){
                    if(_id == Message_list.get(k).getId()){
                        find = true;
                        Message_list.get(k).setRead(true);
                        conversationAdapter.notifyDataSetChanged();
                    }
                    k++;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(LheidoUtils.ACTION_RECEIVE_SMS);
        filter.addAction(LheidoUtils.ACTION_SENT_SMS);
        filter.addAction(LheidoUtils.ACTION_DELIVERED_SMS);
        filter.setPriority(2000);
        context.registerReceiver(mBroadCast, filter);
    }

    @Override
    public void onDestroyView(){
        context.unregisterReceiver(mBroadCast);
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainLheidoSMS) activity).onSectionAttached(getArguments().getString(ARG_CONTACT_NAME));
    }

    @Override
    public void onResume(){
        super.onResume();
        updateFragment();
    }

    @Override
    public void onPause(){
        if(sms_body != null){
            if(sms_body.getText() != null)
                mem_body = sms_body.getText().toString();
            else mem_body = "";
        }
        super.onPause();
    }

    public void updateFragment(){
        userPref.setUserPref(PreferenceManager.getDefaultSharedPreferences(context));
        if(liste != null) {
            if (Message_list != null) {
                Message_list.clear();
                gen_conversation();
            }
            liste.setTransitionEffect(userPref.conversation_effect);
        }
    }

    @Override
    public void onRefresh() {
        long last_id = Message_list.get(Message_list.size() - 1).getId();
        // save index and top position
        final int index = liste.getFirstVisiblePosition();
        View v = liste.getChildAt(Message_list.size()-1);
        final int top = (v == null) ? 0 : v.getTop();
        lastVisibleChild = index;
        final int start_count = liste.getCount();
        Log.v("LHEIDO SMS LOG", "top "+top);
        LheidoUtils.ConversationTask more = new LheidoUtils.ConversationTask(getActivity(), conversationId, last_id) {
            @Override
            protected void onProgressUpdate(Message... prog) {
                if (this.act.get() != null) {
//                    add_sms(prog[0], 1);
                    Message_list.add(prog[0]);
                }
            }
            @Override
            protected void onPreExecute () {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute (Boolean result) {
                if (act.get() != null) {
                    if(!result)
                        Toast.makeText(context, "Problème génération conversation", Toast.LENGTH_LONG).show();
                    swipeLayout.setRefreshing(false);
                    conversationAdapter.notifyDataSetChanged();
                    int finalposition = index + liste.getCount() - start_count;
                    liste.setSelectionFromTop(finalposition, top);
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            more.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            more.execute();
        }

    }
}
