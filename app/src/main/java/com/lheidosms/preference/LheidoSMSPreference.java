package com.lheidosms.preference;

import android.annotation.TargetApi;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.lheidosms.app.R;


/**
 * Created by lheido on 05/06/14.
 */
public class LheidoSMSPreference extends ActionBarActivity {
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lheidosms_pref);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        PrefPagerAdapter prefPagerAdapter = new PrefPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(prefPagerAdapter);
    }

    private class PrefPagerAdapter extends FragmentPagerAdapter {
        public PrefPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new PrefGeneralFragment();
                case 1:
                    return new PrefListConversationsFragment();
                case 2:
                    return new PrefConversationFragment();
                case 3:
                    return new PrefReceiveFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Général";
                case 1:
                    return "Liste Conversations";
                case 2:
                    return "Conversation";
                case 3:
                    return "Réception";
            }
            return null;
        }
    }
}
