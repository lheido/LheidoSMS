package com.lheidosms.utils;

/**
 * Created by lheido on 31/10/14.
 */

import com.lheidosms.fragment.MmsFragment;
import com.lheidosms.fragment.SmsBaseFragment;
import com.lheidosms.fragment.SmsFragment;

/**
 * SMS/MMS fragment builder.
 */
public class BuildFragment {
    public static SmsFragment SMS(LheidoContact contact, int position){
        SmsFragment fragment = new SmsFragment();
        SmsBaseFragment.setArgs(fragment, contact, position);
        return fragment;
    }
    public static MmsFragment MMS(LheidoContact contact, int position){
        MmsFragment fragment = new MmsFragment();
        SmsBaseFragment.setArgs(fragment, contact, position);
        return fragment;
    }
}
