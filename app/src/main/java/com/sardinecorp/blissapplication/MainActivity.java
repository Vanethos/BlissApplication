package com.sardinecorp.blissapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.sardinecorp.blissapplication.utils.ConnectivityStatus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_main)
    Toolbar mMainToolbar;

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
                Toast.makeText(context, "We have internet", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "We don't have internet", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
