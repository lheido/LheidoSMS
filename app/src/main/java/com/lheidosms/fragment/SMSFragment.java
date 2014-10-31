package com.lheidosms.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.telephony.PhoneNumberUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import com.lheidosms.adapter.ConversationAdapter;
import com.lheidosms.app.MainLheidoSMS;
import com.lheidosms.app.R;
import com.lheidosms.receiver.SmsReceiver;
import com.lheidosms.utils.LheidoContact;
import com.lheidosms.utils.LheidoUtils;
import com.lheidosms.utils.Message;
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
    private Context context;
    private LheidoUtils.UserPref userPref;
    private String name;
    private String phoneContact;
    private int conversationId; // id for database
    private long conversation_nb_sms;
    private JazzyListView liste;
    private int list_conversationId; // id for global conversations list
    public ArrayList<Message> Message_list = new ArrayList<Message>();
    private ConversationAdapter conversationAdapter;
    private BroadcastReceiver mBroadCast;
    private SwipeRefreshLayout swipeLayout;
    private boolean mOnPause = false;
    private IntentFilter filter;

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
                int sdk = android.os.Build.VERSION.SDK_INT;
                if(sdk < Build.VERSION_CODES.HONEYCOMB){
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(Message_list.get(Message_list.size()-1-position).getBody());
                } else{
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("simple text", Message_list.get(Message_list.size()-1-position).getBody());
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(context, R.string.message_copy, Toast.LENGTH_LONG).show();
                return true;
            }
        });
        liste.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                liste.requestFocus();
            }
        });
        conversationAdapter = new ConversationAdapter(context, phoneContact, Message_list);
        liste.setAdapter(conversationAdapter);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
    }

    public void add_sms(String phone, long _id, String body, String sender, int deli,Time t, int position){
        Message sms = new Message(_id, body, sender, deli, t);
        sms.setSender(phone);
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
        add_sms("", new_id, body, s, i, now, i1);
        conversation_nb_sms += 1;
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
        gen_list.execTask();
    }

    public void init_broadcast(){
        mBroadCast = new SmsReceiver(){
            @Override
            public void customReceivedSMS() {
                if(PhoneNumberUtils.compare(phoneContact, phone) && !mOnPause){
                    //on est dans la bonne conversation !
                    Time t = new Time();
                    t.set(date);
                    add_sms(phone, -1L, body, "", 0, t, 0);
                    conversation_nb_sms += 1;
                    liste.smoothScrollToPosition(liste.getBottom());
                    LheidoUtils.Send.newMessageRead(context, list_conversationId, phoneContact);
                }
            }

            @Override
            public void customReceivedMMS() {
//                if(PhoneNumberUtils.compare(phoneContact, phone) && !mOnPause){
//
//                }
            }

            @Override
            public void customNewMessageRead(int position, String phone) {}

            @Override
            public void customDelivered(long _id){
                if(!mOnPause) {
                    int k = 0;
                    boolean find = false;
                    while (!find && k < Message_list.size()) {
                        if (_id == Message_list.get(k).getId()) {
                            find = true;
                            Message_list.get(k).setRead(true);
                            conversationAdapter.notifyDataSetChanged();
                        }
                        k++;
                    }
                }
            }
        };
        filter = new IntentFilter();
        filter.addAction(LheidoUtils.ACTION_RECEIVE_SMS);
        filter.addAction(LheidoUtils.ACTION_SENT_SMS);
        filter.addAction(LheidoUtils.ACTION_DELIVERED_SMS);
        filter.setPriority(2000);
        context.registerReceiver(mBroadCast, filter);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainLheidoSMS) activity).onSectionAttached(getArguments().getString(ARG_CONTACT_NAME));
    }

    @Override
    public void onPause(){
        super.onPause();
        mOnPause = true;
        try {
            context.unregisterReceiver(mBroadCast);
        }catch (Exception e){
            Toast.makeText(context, "Error SMSFragment.onPause unregisterReceiver", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        mOnPause = false;
        try {
            context.registerReceiver(mBroadCast, filter);
        }catch (Exception e){
            Toast.makeText(context, "Error SMSFragment.onResume registerReceiver", Toast.LENGTH_LONG).show();
        }
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
//        Log.v("onResume", "id = "+list_conversationId);
        LheidoUtils.Send.newMessageRead(context, list_conversationId, phoneContact);
    }

    @Override
    public void onRefresh() {
        try {
            long last_id = Message_list.get(Message_list.size() - 1).getId();
            // save index and top position
            final int index = liste.getFirstVisiblePosition();
            View v = liste.getChildAt(Message_list.size() - 1);
            final int top = (v == null) ? 0 : v.getTop();
            final int start_count = liste.getCount();
            LheidoUtils.ConversationTask more = new LheidoUtils.ConversationTask(getActivity(), conversationId, last_id) {
                @Override
                protected void onProgressUpdate(Message... prog) {
                    if (this.act.get() != null) {
                        Message_list.add(prog[0]);
                    }
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    if (act.get() != null) {
                        if (!result)
                            Toast.makeText(context, "Problème génération conversation", Toast.LENGTH_LONG).show();
                        swipeLayout.setRefreshing(false);
                        conversationAdapter.notifyDataSetChanged();
                        int finalposition = index + liste.getCount() - start_count;
                        liste.setSelectionFromTop(finalposition, top);
                    }
                }
            };
            more.execTask();
        }catch (Exception e){
            Toast.makeText(context, "Error onRefresh", Toast.LENGTH_LONG).show();
        }
    }
}
