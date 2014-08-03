package com.lheidosms.app;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

public class LheidoContact {
    private String name_ = null;
    private String lastsms_ = null;
    private long nb_sms_ = -1;
    private String phone_ = null;
    private String conversation_id_ = null;
    private Uri pic;
    private long id;
    private boolean mark_new_messsage = false;
    private String accountType = null;

    public LheidoContact(){
        // Empty constructor
    }
    /**
     *
     * @param name            : contact name.
     * @param phone           : contact phone number
     * @param nb_sms          : sms count
     * @param conversation_id : conversation id to fetch database
     */
    public LheidoContact(String name, String phone, long nb_sms, String conversation_id){
        this.name_ = name;
        this.phone_ = phone;
        this.nb_sms_ = nb_sms;
        this.conversation_id_ = conversation_id;
    }
    public static String getContactName(Context context, String address){
        String res = "";
        Cursor cur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if(cur != null){
            String name = "";
            while(name.equals("") && cur.moveToNext()){
                String phone = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                //Log.v("LHEIDO SMS LOG", phone + ", " + address);
                if(name.equals("") && PhoneNumberUtils.compare(phone, address)){
                    name = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    res += name;
                }
            }
            if(name.equals(""))
                res += address;
            cur.close();
        }
        return res;
    }

    public LheidoContact newInstance(){
        LheidoContact c = new LheidoContact();
        c.setName(this.name_);
        c.setPhone(this.phone_);
        c.setId(this.id);
        c.setConversationId(this.conversation_id_);
        c.setPic();
        c.setNb_sms(""+this.nb_sms_);
        c.setAccountType(this.accountType);
        return c;
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

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountType() {
        return this.accountType;
    }
}
