package com.lheidosms.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lheidosms.utils.LheidoUtils;
import com.lheidosms.utils.Message;

import java.util.ArrayList;

/**
 * Created by lheido on 31/10/14.
 */
public abstract class SmsBaseAdapter extends BaseAdapter {
    protected final String mPhoneContact;
    protected LheidoUtils.UserPref userPref;
    protected ArrayList<Message> mList;
    protected Context mContext;

    public SmsBaseAdapter(Context context,String contactPhone, ArrayList<Message> conversation){
        mContext = context;
        mList = conversation;
        mPhoneContact = contactPhone;
        userPref = new LheidoUtils.UserPref();
    }

    public Message getItem(int position) {
        return mList.get(getCount() -1 - position);
    }

    public long getItemId(int position) {
        return position;
    }

    public abstract View getView(int r, View convertView, ViewGroup parent);

    @Override
    public int getCount() {
        return mList.size();
    }

    class SmsBaseViewHolder {
        public RelativeLayout mLayout;
        public TextView mBody;
        public TextView mdate;
        public View mIsRead;
        public ImageView mPict;
    }
}