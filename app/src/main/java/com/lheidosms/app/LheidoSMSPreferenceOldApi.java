package com.lheidosms.app;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import java.util.ArrayList;

/**
 * Created by lheido on 06/06/14.
 */
public class LheidoSMSPreferenceOldApi extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lheidosms_pref_old);


        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.settings_list, android.R.layout.simple_spinner_dropdown_item);
        ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
            // Get the same strings provided for the drop-down's ArrayAdapter
            String[] strings = getResources().getStringArray(R.array.settings_list);

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                // Create new fragment from our own Fragment class
                PreferenceOldApi newFragment = new PreferenceOldApi();
                int res = R.xml.pref_general;
                switch (position){
                    case 0: res = R.xml.pref_general; break;
                    case 1: res = R.xml.pref_list_conversations; break;
                    case 2: res = R.xml.pref_conversation; break;
                    case 3: res = R.xml.pref_receive; break;
                }
                newFragment.setPrefResource(res);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                // Replace whatever is in the fragment container with this fragment
                // and give the fragment a tag name equal to the string at the position
                // selected
                ft.replace(R.id.pref_content, newFragment, strings[position]);

                // Apply changes
                ft.commit();
                return true;
            }
        };
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
        }

    }

    private class PreferenceOldApi extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private int res;

        public void setPrefResource(int res){
            this.res = res;
        }

        @Override
        public void onCreate(Bundle paramBundle) {
            super.onCreate(paramBundle);
            addPreferencesFromResource(this.res);
        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        }
    }

}
