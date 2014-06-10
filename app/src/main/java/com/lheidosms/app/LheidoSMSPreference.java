package com.lheidosms.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;


/**
 * Created by lheido on 05/06/14.
 */
public class LheidoSMSPreference extends Activity {
    ViewPager mViewPager;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lheidosms_pref);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        PrefPagerAdapter prefPagerAdapter = new PrefPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(prefPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        ActionBar actionBar = getActionBar();
                        if (actionBar != null) {
                            actionBar.setSelectedNavigationItem(position);
                        }
                    }
                });

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.settings_list, android.R.layout.simple_spinner_dropdown_item);
        ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
            // Get the same strings provided for the drop-down's ArrayAdapter
            String[] strings = getResources().getStringArray(R.array.settings_list);

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                /*// Create new fragment from our own Fragment class
                ListContentFragment newFragment = new ListContentFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                // Replace whatever is in the fragment container with this fragment
                // and give the fragment a tag name equal to the string at the position
                // selected
                ft.replace(R.id.fragment_container, newFragment, strings[position]);

                // Apply changes
                ft.commit();*/
                mViewPager.setCurrentItem(position);
                return true;
            }
        };
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
        }
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
