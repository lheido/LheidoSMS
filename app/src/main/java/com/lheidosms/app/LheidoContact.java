package com.lheidosms.app;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;


/**
 * Created by lheido on 04/06/14.
 */
public class LheidoContact {
    private String name_ = null;
    private String lastsms_ = null;
    private long nb_sms_ = -1;
    private String phone_ = null;
    private String conversation_id_ = null;
    private Uri pic;
    private long id;
    private boolean mark_new_messsage = false;
    public LheidoContact(){
        // Empty constructor
    }
    public LheidoContact(String name, String phone, long nb_sms, String conversation_id){
        this.name_ = name;
        this.phone_ = phone;
        this.nb_sms_ = nb_sms;
        this.conversation_id_ = conversation_id;
    }
    public String getContactName(Context context, String address){
        String res = "";
        Cursor cur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if(cur != null){
            String name = "";
            while(name.equals("") && cur.moveToNext()){
                String phone = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)).toString();
                //Log.v("LHEIDO SMS LOG", phone + ", " + address);
                if(name.equals("") && PhoneNumberUtils.compare(phone, address)){
                    name = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).toString();
                    res += name;
                    //long id = cur.getLong(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    //this.setId(id);
                    //this.setPic(context);
                }
            }
            if(name.equals(""))
                res += address;
            cur.close();
        }
        return res;
    }

    public String getName(){
        return name_;
    }
    public String getLastsms(){
        return lastsms_;
    }
    public long getNb_sms(){
        return nb_sms_;
    }
    public String getPhone(){
        return phone_;
    }
    public String getConversationId(){
        return conversation_id_;
    }
    public void setNb_sms(String count){
        this.nb_sms_ = Integer.parseInt(count);
    }
    public void setPhone(String address){
        this.phone_ = address;
    }
    public void setName(Context context, String name){
        this.name_ = getContactName(context, name);
    }
    public void setConversationId(String id){
        this.conversation_id_ = id;
    }

    public Uri getPic(){
        return this.pic;
    }

    public void setPic(){
        this.pic = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, this.id);
    }

    public long getId() {
        return this.id;
    }
    public void setId(long id_){
        this.id = id_;
    }
    public void setName(String string) {
        this.name_ = string;
    }

    public void Nb_sms_Plus() {
        this.nb_sms_ += 1;
    }

    public void markNewMessage(Boolean val) {
        this.mark_new_messsage = val;
    }

    public boolean hasNewMessage() {
        return this.mark_new_messsage;
    }
}
