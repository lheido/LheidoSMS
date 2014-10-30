package com.lheidosms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lheidosms.utils.LheidoContact;
import com.lheidosms.app.R;

import java.util.ArrayList;

public class ContactsListAdapter extends BaseAdapter {
    private final ArrayList<LheidoContact> items;
    private final Context mContext;

    public ContactsListAdapter(Context context, ArrayList<LheidoContact> suggestions) {
        this.items = suggestions;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public LheidoContact getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public View getView(int r, View convertView, ViewGroup parent) {
        LheidoContact contact = (LheidoContact) this.getItem(r);
        Holder holder;
        if(convertView == null)
        {
            holder = new Holder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.auto_complete, parent, false);
            holder.label = (TextView) convertView.findViewById(R.id.contact_name);
            holder.phone = (TextView) convertView.findViewById(R.id.contact_phone);
            holder.type = (TextView) convertView.findViewById(R.id.account_type);
            convertView.setTag(holder);
        }
        else
            holder = (Holder) convertView.getTag();

        holder.label.setText(contact.getName());
        holder.phone.setText(contact.getPhone());
        holder.type.setText(contact.getAccountType());
        return convertView;
    }

    private class Holder {
        public TextView label;
        public TextView phone;
        public TextView type;
    }
}
