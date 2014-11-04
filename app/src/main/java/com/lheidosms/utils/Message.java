package com.lheidosms.utils;

import android.net.Uri;
import android.text.format.Time;

public class Message {
    private String sender = null;
    private String body_ = null;
    private Uri img_ = null;
    private Time date_ = null;
    private boolean right = false;
    private boolean read_ = false;
    private long _id = -1;

    public Message(){}

    public Message(long _id, String body, String sender, int deli,Time t){
        if(_id != -1)
            this._id = _id;
        this.body_ = body;
        this.date_ = t;
        this.sender = sender;
        if(deli == 0) this.read_ = true;
        else this.read_ = false;
    }

    public static String formatDate(Time date){
        int time_dd = date.monthDay;
        int time_MM = date.month;
        Time now = new Time();
        now.setToNow();
        int c_dd = now.monthDay;
        int c_MM = now.month;
        if(time_MM == c_MM){
            if(time_dd == c_dd)
                return date.format("%H:%M");
            else
                return date.format("%d/%m/%Y %H:%M");
        }
        return date.format("%d/%m/%Y");
    }

    public void setDate(Time date){
        this.date_ = date;
    }
    public String getDate(){
        return formatDate(this.date_);
    }

    public long getDateNormalize(){
        return this.date_.normalize(false);
    }

    public void setBody(String string) {
        this.body_ = string != null ? string : "";
    }

    public String getBody() {
        return this.body_;
    }

    public Uri getUriPicture() {
        return this.img_;
    }

    public void setUriPicture(Uri pict){
        this.img_ = pict;
    }

    public String getSender(){
        return this.sender;
    }

    public void setSender(String s){
        this.sender = s;
    }

    public boolean isRead() {
        return this.read_;
    }

    public void setRead(boolean b) {
        this.read_ = b;
    }

    public void setId(long sms_id) {
        this._id = sms_id;
    }

    public long getId(){
        return this._id;
    }
}
