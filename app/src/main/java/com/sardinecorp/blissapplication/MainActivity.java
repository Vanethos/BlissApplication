package com.sardinecorp.blissapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.airbnb.deeplinkdispatch.DeepLink;
import com.sardinecorp.blissapplication.network.APIStatus;
import com.sardinecorp.blissapplication.network.BlissService;
import com.sardinecorp.blissapplication.network.RetrofitClient;
import com.sardinecorp.blissapplication.ui.LoadingFragment;
import com.sardinecorp.blissapplication.ui.NoInternetFragment;
import com.sardinecorp.blissapplication.ui.QuestionListFragment;
import com.sardinecorp.blissapplication.utils.ConnectivityStatus;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@DeepLink("blissrecruitment://questions")
public class MainActivity extends AppCompatActivity implements LoadingFragment.GoToListFragment{

    @BindView(R.id.toolbar_main) Toolbar mMainToolbar;
    @BindView(R.id.fragment_holder) FrameLayout mFragmentHolder;

    public static final String FRAGMENT_CONNECTIVITY_TAG = "FRAGMENT_CONNECTIVITY_TAG";
    public static final String FRAGMENT_LOADING_TAG = "FRAGMENT_LOADING_TAG";
    public static final String FRAGMENT_LIST_TAG = "FRAGMENT_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mMainToolbar);
        mMainToolbar.setTitle("Bliss Application");
        // Set the LoadingFragment as the initial fragment for the app
        //addLoadingFragment();
        Intent intent = getIntent();
        if (intent.getBooleanExtra(DeepLink.IS_DEEP_LINK, false)) {
            Bundle parameters = intent.getExtras();
            if (parameters != null &&
                    (parameters.getString("question_filter") != null || parameters.getString("question_id") != null)) {
                // TODO: check the links and create appropriate action
                String queryParameter = parameters.getString("question_filter");
                Toast.makeText(this, "queryParameter", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    // Create a receiver to check the internet connection status
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ConnectivityStatus.isConnected(getApplicationContext())) {
                removeNoConnectionFragment();
            } else {
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
        addLoadingFragment();
    }

    private void addNoConnectionFragment() {
        // if there is a server check fragment, remove it
        LoadingFragment loadingFragment = (LoadingFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_LOADING_TAG);
        if (loadingFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(loadingFragment)
                    .commit();
        }
        NoInternetFragment fragment = new NoInternetFragment();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .add(R.id.fragment_holder, fragment, FRAGMENT_CONNECTIVITY_TAG)
                .commit();
    }

    public void addLoadingFragment() {
        Log.d("response", "We created the loading fragment");
        LoadingFragment loadingFragment = (LoadingFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_LOADING_TAG);
        if (loadingFragment == null || loadingFragment.isDetached()) {
            Log.d("MAIN", "========= I WAS CALLED ============");
            LoadingFragment fragment = new LoadingFragment();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .add(R.id.fragment_holder, fragment, FRAGMENT_LOADING_TAG)
                    .commit();
        }
    }

    public void addQuestionFragment() {
        QuestionListFragment fragment = new QuestionListFragment();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_holder, fragment, FRAGMENT_LIST_TAG)
                .commit();
    }



    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        this.registerReceiver(mBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    @Override
    public void GoToListFragment() {
        addQuestionFragment();
    }
}
