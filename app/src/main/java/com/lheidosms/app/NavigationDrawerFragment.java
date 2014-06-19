package com.lheidosms.app;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.twotoasters.jazzylistview.JazzyListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {
    private static final String DRAWER_LOG = "LheidoSMS Drawer";
    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private JazzyListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private ArrayList<LheidoContact> lheidoConversationListe = new ArrayList<LheidoContact>();
    private ListeConversationsAdapter lConversationsAdapter;
    private LheidoUtils.UserPref userPref;

    private Map<String, Integer> notificationsId = new HashMap<String, Integer>();
    private BroadcastReceiver notificationsBroadcast;
    private BroadcastReceiver mBroadcast ;
    private LheidoUtils.ConversationListTask gen_list;
    private boolean onPauseDrawerOpened = false;

    public NavigationDrawerFragment() {}

    public void updateContact(int position, String count){
//        lheidoConversationListe.get(position).setNb_sms(count);
        Global.conversationsList.get(position).setNb_sms(count);
        lConversationsAdapter.notifyDataSetChanged();
    }

    public void updateContact(String phone){
        int i = 0;
        int size = Global.conversationsList.size();
        while(i < size && !PhoneNumberUtils.compare(Global.conversationsList.get(i).getPhone(), phone)) {i++;}
        if(i < size && PhoneNumberUtils.compare(Global.conversationsList.get(i).getPhone(), phone)) {
            Global.conversationsList.get(i).Nb_sms_Plus();
            lConversationsAdapter.notifyDataSetChanged();
        }
    }

    public void setNotificationsId(Map<String, Integer> notificationsId){
        this.notificationsId.clear();
        this.notificationsId.putAll(notificationsId);
    }

    public void cancelNotification(int position){
        String phone = Global.conversationsList.get(position).getPhone();
        Set<String> keys = this.notificationsId.keySet();
        for(String ph : keys){
            if(PhoneNumberUtils.compare(ph, phone)){
                NotificationManager notificationmanager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationmanager.cancel(this.notificationsId.get(ph));
                Global.conversationsList.get(position).markNewMessage(false);
                lConversationsAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        //selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerListView = (JazzyListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        lConversationsAdapter = new ListeConversationsAdapter(getActivity(), R.layout.conversations_list, Global.conversationsList);
        mDrawerListView.setAdapter(lConversationsAdapter);
        mDrawerListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                return false;
            }
        });
        //mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        userPref = new LheidoUtils.UserPref();
        userPref.setUserPref(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()));

        mBroadcast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String iAction = intent.getAction();
                if(iAction.equals(LheidoUtils.ACTION_FIRST)){
                    selectItem(0);
                }
                else if(iAction.equals(LheidoUtils.ACTION_NEW_MESSAGE)){
                    lConversationsAdapter.notifyDataSetChanged();
                    ((MainLheidoSMS)getActivity()).setCurrentConversation();
                }
                else if(iAction.equals(LheidoUtils.ACTION_NOTIFY_DATA_CHANGED)){
                    lConversationsAdapter.notifyDataSetChanged();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(LheidoUtils.ACTION_FIRST);
        filter.addAction(LheidoUtils.ACTION_NEW_MESSAGE);
        filter.addAction(LheidoUtils.ACTION_NOTIFY_DATA_CHANGED);
        filter.setPriority(3000);
        getActivity().registerReceiver(mBroadcast, filter);
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if ((!mUserLearnedDrawer || userPref.drawer) && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        if(Global.conversationsList.size() > 0){
            selectItem(mCurrentSelectedPosition);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        Log.v(DRAWER_LOG, "SelectItem "+position);
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            //mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null && !onPauseDrawerOpened) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            LheidoContact contact = Global.conversationsList.get(position);
            sendNewMessageRead(position, contact.getPhone());
            Log.v(DRAWER_LOG, "mCallbacks not null, "+position+", "+contact);
            mCallbacks.onNavigationDrawerItemSelected(position, contact);
            if(!onPauseDrawerOpened) getActionBar().setTitle(contact.getName());
        }
    }

    public void sendNewMessageRead(int pos, String phone){
        Intent i = new Intent(LheidoUtils.ACTION_NEW_MESSAGE_READ);
        i.putExtra("position", pos);
        i.putExtra("phone", phone);
        getActivity().sendBroadcast(i);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.action_new_sms) {
            Toast.makeText(getActivity(), "TODO :P", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }


    @Override
    public void onResume(){
        super.onResume();
        updateFragment();
    }

    public void updateFragment(){
        userPref.setUserPref(PreferenceManager.getDefaultSharedPreferences(getActivity()));
        if(mDrawerListView != null){
            mDrawerListView.setTransitionEffect(userPref.listConversation_effect);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        onPauseDrawerOpened = isDrawerOpen();
    }

    @Override
    public void onDestroyView(){
        getActivity().unregisterReceiver(mBroadcast);
        super.onDestroyView();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position, LheidoContact contact);
    }
}
