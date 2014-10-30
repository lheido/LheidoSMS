package com.lheidosms.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.lheidosms.app.R;
import com.lheidosms.utils.Message;
import com.twotoasters.jazzylistview.JazzyListView;

import java.util.ArrayList;

/**
 * Created by lheido on 31/10/14.
 */
public class MmsFragment extends SmsBaseFragment {
    @Override
    protected View initRootView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.conversation_mms, container, false);
    }

    @Override
    protected com.twotoasters.jazzylistview.JazzyListView initList(View rootView) {
        return (JazzyListView)rootView.findViewById(R.id.list_conversation_mms);
    }

    @Override
    protected void initBroadcastReceiver() {

    }

    @Override
    protected void initConversationAdapter(Context context, String phoneContact, ArrayList<Message> message_list) {

    }

    @Override
    protected void initListOnItemClick(AdapterView<?> adapterView, View view, int position, long id) {

    }

    @Override
    protected void initListOnItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

    }

    @Override
    protected void load_conversation() {

    }

    @Override
    protected void load_more_conversation(long last_id, int index, int top, int start_count) {

    }
}
