package com.lheidosms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;

/**
 * Created by lheido on 01/11/14.
 */
public abstract class LheidoBaseReceiver extends BroadcastReceiver {
    protected Context mContext;
    protected String iAction;
    protected ArrayList<String> mActionsList;

    public LheidoBaseReceiver(){
        mActionsList = initActions();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        iAction = intent.getAction();
        receive(intent);
    }

    public ArrayList<String> getActionsList() {
        return mActionsList;
    }

    public IntentFilter getIntentFilter(int priority){
        IntentFilter filter = new IntentFilter();
        filter.setPriority(priority != -1 ? priority : 2000);
        for(String action : mActionsList){
            filter.addAction(action);
        }
        return filter;
    }

    protected abstract ArrayList<String> initActions();

    protected abstract void receive(Intent intent);
}
