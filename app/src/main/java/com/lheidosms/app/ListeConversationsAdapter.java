package com.lheidosms.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by lheido on 04/06/14.
 */
public class ListeConversationsAdapter extends BaseAdapter {
    /**
     * Récupérer un item de la liste en fonction de sa position
     * @param position - Position de l'item à récupérer
     * @return l'item récupéré
     */
    //private LayoutInflater mInflater;
    private ArrayList<LheidoContact> mListConv;
    private Context mContext;

    public ListeConversationsAdapter(Context context, int ressource, ArrayList<LheidoContact> list_conversation){
        mContext = context;
        mListConv = list_conversation;
        //mInflater = LayoutInflater.from(mContext);
    }

    public LheidoContact getItem(int position) {
        return mListConv.get(position);
    }

    /**
     * Récupérer l'identifiant d'un item de la liste en fonction de sa position (plutôt utilisé dans le cas d'une
     * base de données, mais on va l'utiliser aussi)
     * @param position - Position de l'item à récupérer
     * @return l'identifiant de l'item
     */
    public long getItemId(int position) {
        return position;
    }

    public View getView(int r, View convertView, ViewGroup parent) {
        LheidoContact contact = (LheidoContact) this.getItem(r);

        ListeConversationViewHolder holder;
        if(convertView == null)
        {
            holder = new ListeConversationViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.conversations_list, parent, false);
            holder.mName = (TextView) convertView.findViewById(R.id.list_conversation_contact_name);
            holder.mCount = (TextView) convertView.findViewById(R.id.list_conversation_count);
            holder.mContactPicture = (ImageView) convertView.findViewById(R.id.contactPict);
            //holder.mLayout = (RelativeLayout) convertView.findViewById(R.id.message_relativeLayout);
            convertView.setTag(holder);
        }
        else
            holder = (ListeConversationViewHolder) convertView.getTag();

        holder.mName.setText(contact.getName());
        holder.mCount.setText(""+contact.getNb_sms());
        if(contact.hasNewMessage())
            holder.mCount.setTextColor(mContext.getResources().getColor(R.color.new_message));
        else
            holder.mCount.setTextColor(mContext.getResources().getColor(R.color.default_message));
        Picasso.with(mContext).load(contact.getPic()).fit().centerCrop().into(holder.mContactPicture);

        return convertView;
    }
    @Override
    public int getCount() {
        return mListConv.size();
    }
    static class ListeConversationViewHolder {
        //public RelativeLayout mLayout;
        public TextView mName;
        public TextView mCount;
        public ImageView mContactPicture;
    }
}
