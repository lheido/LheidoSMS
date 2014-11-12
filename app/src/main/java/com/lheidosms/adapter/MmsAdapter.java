package com.lheidosms.adapter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lheidosms.app.R;
import com.lheidosms.utils.Message;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by lheido on 31/10/14.
 */
public class MmsAdapter extends SmsBaseAdapter {

    public MmsAdapter(Context context, String contactPhone, ArrayList<Message> conversation) {
        super(context, contactPhone, conversation);
    }

    @Override
    public View getView(int r, View convertView, ViewGroup parent) {
        Message message = this.getItem(r);
        SmsBaseViewHolder holder;
        if(convertView == null){
            holder = new SmsBaseViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_mms, parent, false);
            holder.mBody = (TextView) convertView.findViewById(R.id.message);
            holder.mPict = (ImageView) convertView.findViewById(R.id.m_pict);
            holder.mdate = (TextView) convertView.findViewById(R.id.date_message);
            holder.mLayout = (RelativeLayout) convertView.findViewById(R.id.message_relativeLayout);
            holder.mIsRead = (View) convertView.findViewById(R.id.is_read);
            convertView.setTag(holder);
        }
        else
            holder = (SmsBaseViewHolder) convertView.getTag();

        userPref.setUserPref(PreferenceManager.getDefaultSharedPreferences(mContext));

        holder.mBody.setText(message.getBody());
        holder.mBody.setTextSize(userPref.text_size);
        holder.mdate.setText(message.getDate(mContext));
        if(message.getUriPicture() != null) {
            Picasso.with(mContext).load(message.getUriPicture()).fit().centerCrop().into(holder.mPict);
        }
        //RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.mLayout.getLayoutParams();
        if(!PhoneNumberUtils.compare(mPhoneContact, message.getSender())) {
            holder.mLayout.setGravity(GravityCompat.END);
            holder.mLayout.setPadding(42, 0, 0, 0);
            holder.mBody.setBackgroundColor(mContext.getResources().getColor(R.color.grey_mid_high));
            if(message.isRead())
                holder.mIsRead.setBackgroundColor(mContext.getResources().getColor(R.color.read_green));
            else{
                holder.mIsRead.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            }
        }
        else {
            holder.mLayout.setGravity(GravityCompat.START);
            holder.mLayout.setPadding(0, 0, 42, 0);
            holder.mBody.setBackgroundColor(mContext.getResources().getColor(R.color.grey_low));
            holder.mIsRead.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        }
        return convertView;
    }
}
