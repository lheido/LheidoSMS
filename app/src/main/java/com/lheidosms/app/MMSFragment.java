package com.lheidosms.app;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.telephony.PhoneNumberUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.twotoasters.jazzylistview.JazzyListView;

import java.util.ArrayList;

public class MMSFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String ARG_CONVERSATION_ID = "conversation_id";
    public static final String ARG_CONVERSATION_COUNT = "conversation_count";
    public static final String ARG_CONTACT_PHONE = "contact_phone";
    public static final String ARG_CONVERSATION_NUMBER = "conversation_number";
    public static final String ARG_CONTACT_NAME = "contact_name";
    private ArrayList<Message> Message_list = new ArrayList<Message>();
    private SwipeRefreshLayout swipeLayout;
    private MMSAdapter conversationMmsAdapter;
    private Context context;
    private int conversationId;
    private SmsReceiver mBroadCast;
    private String name;
    private String phoneContact;
    private long conversation_nb_sms;
    private int list_conversationId;
    private LheidoUtils.UserPref userPref;
    private JazzyListView liste;

    public static MMSFragment newInstance(int position, LheidoContact contact) {
        MMSFragment fragment = new MMSFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CONVERSATION_NUMBER, position);
        args.putString(ARG_CONTACT_NAME, contact.getName());
        args.putString(ARG_CONTACT_PHONE, contact.getPhone());
        args.putInt(ARG_CONVERSATION_ID, Integer.parseInt(contact.getConversationId()));
        args.putLong(ARG_CONVERSATION_COUNT, contact.getNb_sms());
        fragment.setArguments(args);
        return fragment;
    }

    public MMSFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.conversation_mms, container, false);
        if(rootView != null) {
            context = getActivity();
            userPref = new LheidoUtils.UserPref();
            userPref.setUserPref(PreferenceManager.getDefaultSharedPreferences(context));
            name = getArguments().getString(ARG_CONTACT_NAME);
            phoneContact = getArguments().getString(ARG_CONTACT_PHONE);
            conversationId = getArguments().getInt(ARG_CONVERSATION_ID);
            list_conversationId = getArguments().getInt(ARG_CONVERSATION_NUMBER);
            liste = (JazzyListView) rootView.findViewById(R.id.list_conversation_mms);
            liste.setTransitionEffect(userPref.conversation_effect);
            liste.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            conversation_nb_sms = getArguments().getLong(ARG_CONVERSATION_COUNT);
            conversationMmsAdapter = new MMSAdapter(context, Message_list);
            liste.setAdapter(conversationMmsAdapter);
            swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
            swipeLayout.setOnRefreshListener(this);
            init_broadcast();
        }
        return rootView;
    }

    public void gen_conversation(){
        LheidoUtils.MMSTask gen_list = new LheidoUtils.MMSTask(getActivity(), conversationId) {
            @Override
            protected void onProgressUpdate(Message... prog) {
                if (this.act.get() != null) {
                    add_sms_(prog[0], 1);
                }
            }
        };
        gen_list.execConversationTask();
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

    public void add_sms_(Message message, int i) {
        if(i != 0){
            Message_list.add(message);
        } else{
            Message_list.add(0, message);
        }
        conversationMmsAdapter.notifyDataSetChanged();
    }

    public void init_broadcast(){
        mBroadCast = new SmsReceiver(){
            @Override
            public void customReceivedSMS() {}

            @Override
            public void customReceivedMMS() {
                if(PhoneNumberUtils.compare(phoneContact, phone)){
                    //on est dans la bonne conversation !
                    Time t = new Time();
                    t.set(date);
                    add_sms(-1L, body, "", 0, t, 0);
                    conversationMmsAdapter.notifyDataSetChanged();
                    conversation_nb_sms += 1;
                    liste.smoothScrollToPosition(liste.getBottom());
                    LheidoUtils.Send.newMessageRead(context, list_conversationId, phoneContact);
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
                        conversationMmsAdapter.notifyDataSetChanged();
                    }
                    k++;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(LheidoUtils.ACTION_RECEIVE_MMS);
//        filter.addAction(LheidoUtils.ACTION_SENT_SMS);
//        filter.addAction(LheidoUtils.ACTION_DELIVERED_SMS);
        filter.setPriority(2000);
        context.registerReceiver(mBroadCast, filter);
    }

    @Override
    public void onDestroyView(){
        context.unregisterReceiver(mBroadCast);
        super.onDestroyView();
    }

    @Override
    public void onResume(){
        super.onResume();
        updateFragment();
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
        final int start_count = liste.getCount();
        LheidoUtils.MMSTask more = new LheidoUtils.MMSTask(getActivity(), conversationId, last_id) {
            @Override
            protected void onProgressUpdate(Message... prog) {
                if (this.act.get() != null) {
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
                    conversationMmsAdapter.notifyDataSetChanged();
                    int finalposition = index + liste.getCount() - start_count;
                    liste.setSelectionFromTop(finalposition, top);
                }
            }
        };
        more.execConversationTask();
    }
}
