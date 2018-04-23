package fiu.com.skillcourt.ui.main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import fiu.com.skillcourt.R;
import fiu.com.skillcourt.services.ConnectionService;
import fiu.com.skillcourt.ui.configuration.PadConfigFragment;

/**
 * Created by Joshua Mclendon on 11/2/17.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getName();
    private final static String CONNECTIVITY_ACTION = "CONNECTIVITY_CHANGE";
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar mActionBar;
    public ConnectionService mConnectionService;
    public boolean mBounded = false;
    private boolean mToolBarNavigationListenerIsRegistered = false;
    public BottomNavigationView navigation;
    IntentFilter connectionIntentFilter;
    ConnectionReceiver connectionReceiver;
    String text = "0";
    private TextView mPadConnected;
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Binding service disconnected");
            mBounded = false;
            mConnectionService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Binding service connected");
            mBounded = true;
            ConnectionService.LocalBinder mLocalBinder = (ConnectionService.LocalBinder) service;
            mConnectionService = mLocalBinder.getService();
            // Pads connected will be updated via broadcast connectionReceiver when a pad connects
            // This is just for initial launch of this activity

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fragment fragment = new HomeFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.screen_area, fragment);
        fragmentTransaction.commit();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Log.i(TAG, "Nav height " + navigation.getHeight());

        mDrawer = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        connectionIntentFilter = new IntentFilter();
        connectionIntentFilter.addAction(CONNECTIVITY_ACTION);
        connectionIntentFilter.addAction("PAD_CONNECTION_CHANGE");
        connectionReceiver = new ConnectionReceiver();
        registerReceiver(connectionReceiver, connectionIntentFilter);
        /*
         This is for the initial start up of the activity
         If the device is connected to a network and the service has not
         been bounded or started (which means executed by the broadcast connectionReceiver below)
         then start it and bound the service else alert user
        */
        if (isConnected()) {

            Log.i(TAG, "Device is connected to a network");
            if (!mBounded) {
                //start server socket and broadcasting nsd service
                Intent intent = new Intent(this, ConnectionService.class);
                startService(intent);
            /*
             Reason behind binding the service also is because
             this activity needs to access methods of the service
             when the activity is alive
            */
                bindService(intent, mConnection, BIND_AUTO_CREATE);
            }
        } else {
            Log.e(TAG, "Device is not connected to a network");
            Toast.makeText(this, "You must connect to a network to start playing.", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onBackPressed() {

        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackCount >= 1) {
                getSupportFragmentManager().popBackStack();
                // Change to hamburger icon if at bottom of stack
                if (backStackCount == 1) {
                    showUpButton(false);
                }
            } else {
                super.onBackPressed();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        mPadConnected = (TextView) menu.findItem(R.id.pads_connected).getActionView();
        mPadConnected.setText(getUpdatedPadConnectedText());
        mPadConnected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()) {
                    if (mConnectionService.getPadsConnected() > 1) {
                        Log.i(TAG, "Config Pads");
                        Fragment fragment = new PadConfigFragment();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        startFragmentWithBackButton(fragment, fragmentTransaction, true);
                    } else {
                        Toast.makeText(MainActivity.this, "You must be connected to at least 2 pads.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "You must be connected to a network.", Toast.LENGTH_LONG).show();
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment fragment;
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            showUpButton(false);
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    fragment = new HomeFragment();
                    startBottomNavFragment(fragment, fragmentTransaction);

                    return true;
                case R.id.navigation_stats:

                    fragment = new StatsFragment();
                    startBottomNavFragment(fragment, fragmentTransaction);
                    return true;
                case R.id.navigation_sequences:

                    fragment = new SequencesFragment();
                    startBottomNavFragment(fragment, fragmentTransaction);
                    return true;
            }
            return false;
        }
    };


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(connectionReceiver, connectionIntentFilter);
        Log.i(TAG, "onResume");
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectionReceiver);
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //If there is a network available and the device is connected or connecting then were good
        return (connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isAvailable()
                && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting());

    }

    private String getUpdatedPadConnectedText() {
        if (mBounded) {
            Log.i(TAG, "Get updated pad(s) connected: " + mConnectionService.getPadsConnected());
            text = mConnectionService.getPadsConnected() + "";
        }
        return text;
    }

    private void showUpButton(boolean show) {
        // To keep states of ActionBar and ActionBarDrawerToggle synchronized,
        // when you enable on one, you disable on the other.
        // And as you may notice, the order for this operation is disable first, then enable - VERY VERY IMPORTANT.
        if (show) {
            // Remove hamburger
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            // Show back button
            mActionBar.setDisplayHomeAsUpEnabled(true);
            // when DrawerToggle is disabled i.e. setDrawerIndicatorEnabled(false), navigation icon
            // clicks are disabled i.e. the UP button will not work.
            // We need to add a listener, as in below, so DrawerToggle will forward
            // click events to this listener.
            if (!mToolBarNavigationListenerIsRegistered) {
                mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });

                mToolBarNavigationListenerIsRegistered = true;
            }

        } else {
            // Remove back button
            mActionBar.setDisplayHomeAsUpEnabled(false);
            // Show hamburger
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            // Remove the/any drawer toggle listener
            mDrawerToggle.setToolbarNavigationClickListener(null);
            mToolBarNavigationListenerIsRegistered = false;
        }

    }


    public void startFragmentWithBackButton(Fragment fragment, FragmentTransaction fragmentTransaction, boolean addToBackStack) {
        fragmentTransaction.replace(R.id.screen_area, fragment);
        if(addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
        showUpButton(true);
    }

    public void startBottomNavFragment(Fragment fragment, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.replace(R.id.screen_area, fragment);
        fragmentTransaction.commit();
        showUpButton(false);
    }

    //Used to check a network change, like connecting to wifi or being disconnected
    private class ConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            //If the action is a network change state
            if (intent.getAction().equals(CONNECTIVITY_ACTION)) {
                /*
                 Check if the device is connected to a network this handles the case where the user
                 just pulls down the notification screen and mHitPercentage the wifi button, the reason why
                 this is here is because in this case the activity doesn't get destroyed just
                 paused so therefore the same code in the onCreate method doesn't get executed so
                 we need to execute it here to start the nsd and server service
                */
                if (isConnected()) {
                    Log.i(TAG, "Device is connected to a network");

                    //start server socket and broadcasting nsd service
                    Intent i = new Intent(context, ConnectionService.class);
                    context.startService(i);
                    bindService(i, mConnection, BIND_AUTO_CREATE);
                } else {
                    Log.e(TAG, "Device disconnected from a network");
                    Intent i = new Intent(context, ConnectionService.class);
                    if (mBounded) {
                        unbindService(mConnection);
                        mBounded = false;
                    }
                    stopService(i);
                }
            } else if (intent.getAction().equals("PAD_CONNECTION_CHANGE")) {
                Log.i(TAG, "PAD CONNECTION CHANGE");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPadConnected.setText(getUpdatedPadConnectedText());
                        invalidateOptionsMenu();
                    }
                });
            }
        }
    }
}
