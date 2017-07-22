package com.sardinecorp.blissapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sardinecorp.blissapplication.ui.NoInternetFragment;
import com.sardinecorp.blissapplication.utils.ConnectivityStatus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_main) Toolbar mMainToolbar;
    @BindView(R.id.fragment_holder) FrameLayout mFragmentHolder;

    public static final String FRAGMENT_CONNECTIVITY_TAG = "FRAGMENT_CONNECTIVITY_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mMainToolbar);
        mMainToolbar.setTitle("Bliss Application");
        // register the receiver to check internet connection
        this.registerReceiver(mBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    
    // Create a receiver to check the internet connection status
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ConnectivityStatus.isConnected(getApplicationContext())) {
                // TODO: Delete me!
                Toast.makeText(context, "We have internet", Toast.LENGTH_SHORT).show();
                removeNoConnectionFragment();
            } else {
                // TODO: Delete me!
                Toast.makeText(context, "We don't have internet", Toast.LENGTH_SHORT).show();
                addNoConnectionFragment();
            }
        }
    };

    private void removeNoConnectionFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_CONNECTIVITY_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .remove(fragment)
                    .commit();
        }
    }

    private void addNoConnectionFragment() {
        NoInternetFragment fragment = new NoInternetFragment();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .add(R.id.fragment_holder, fragment, FRAGMENT_CONNECTIVITY_TAG)
                .commit();
    }
}
