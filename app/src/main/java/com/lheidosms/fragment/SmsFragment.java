package com.lheidosms.fragment;

import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.lheidosms.adapter.ConversationAdapter;
import com.lheidosms.app.R;
import com.lheidosms.receiver.SmsReceiver;
import com.lheidosms.utils.LheidoUtils;
import com.lheidosms.utils.Message;
import com.twotoasters.jazzylistview.JazzyListView;


/**
 * Created by lheido on 31/10/14.
 */
public class SmsFragment extends SmsBaseFragment {

    @Override
    protected View initRootView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.conversation, container, false);
    }

    @Override
    protected com.twotoasters.jazzylistview.JazzyListView initList(View rootView) {
        return (JazzyListView)rootView.findViewById(R.id.list_conversation);
    }

    @Override
    protected void initBroadcastReceiver() {
        mBroadCast = new SmsReceiver(){
            @Override
            public void customReceivedSMS() {
                if(PhoneNumberUtils.compare(phoneContact, phone) && !mOnPause){
                    //on est dans la bonne conversation !
                    Time t = new Time();
                    t.set(date);
                    add_(phone, -1L, body, "", 0, t, 0);
                    conversation_nb_sms += 1;
                    liste.smoothScrollToPosition(liste.getBottom());
                    LheidoUtils.Send.newMessageRead(context, list_conversationId, phoneContact);
                }
            }

            @Override
            public void customReceivedMMS() {}

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
        filter.addAction(LheidoUtils.ACTION_RECEIVE_SMS);
        filter.addAction(LheidoUtils.ACTION_SENT_SMS);
        filter.addAction(LheidoUtils.ACTION_DELIVERED_SMS);
    }

    @Override
    protected void initConversationAdapter() {
        conversationAdapter = new ConversationAdapter(context, phoneContact, Message_list);
    }

    @Override
    protected void initListOnItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        liste.requestFocus();
    }

    @Override
    protected void initListOnItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
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
    }

    @Override
    protected void load_conversation() {
        LheidoUtils.ConversationTask loadTask = new LheidoUtils.ConversationTask(getActivity(), conversationId) {
            @Override
            protected void onProgressUpdate(Message... prog) {
                if (this.act.get() != null) {
                    add__(prog[0], 1);
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
        loadTask.execTask();
    }

    @Override
    protected void load_more_conversation(final long last_id, final int index, final int top, final int start_count) {
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
                if (this.act.get() != null) {
                    if (!result)
                        Toast.makeText(context, "Problème génération conversation", Toast.LENGTH_LONG).show();
                    else {
                        swipeLayout.setRefreshing(false);
                        conversationAdapter.notifyDataSetChanged();
                        int finalposition = index + liste.getCount() - start_count;
                        liste.setSelectionFromTop(finalposition, top);
                    }
                }
            }
        };
        more.execTask();
    }
}
