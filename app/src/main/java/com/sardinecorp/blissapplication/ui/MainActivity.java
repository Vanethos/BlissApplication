package com.sardinecorp.blissapplication.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.FrameLayout;

import com.airbnb.deeplinkdispatch.DeepLink;
import com.google.gson.Gson;
import com.sardinecorp.blissapplication.DeepLinkHandler;
import com.sardinecorp.blissapplication.R;
import com.sardinecorp.blissapplication.network.Question;
import com.sardinecorp.blissapplication.utils.ConnectivityStatus;

import butterknife.BindView;
import butterknife.ButterKnife;

@DeepLink("blissrecruitment://questions")
public class MainActivity extends AppCompatActivity implements LoadingFragment.LoadingFragmentInterface,
        QuestionListAdapter.GoToQuestion, GetQuestionFragment.DeepLinkQuestionInterface, ShareDialogFragment.ShareEmailListener,
        QuestionFragment.SetUpNavigation{

    @BindView(R.id.toolbar_main)
    Toolbar mMainToolbar;
    @BindView(R.id.fragment_holder)
    FrameLayout mFragmentHolder;

    public static final String FRAGMENT_CONNECTIVITY_TAG = "FRAGMENT_CONNECTIVITY_TAG";
    public static final String FRAGMENT_LOADING_TAG = "FRAGMENT_LOADING_TAG";
    public static final String FRAGMENT_LIST_TAG = "FRAGMENT_LIST_TAG";
    public static final String FRAGMENT_LIST_WITH_FILTER_TAG = "FRAGMENT_LIST_WITH_FILTER_TAG";
    public static final String FRAGMENT_LIST_WITHOUT_FILTER_TAG = "FRAGMENT_LIST_WITHOUT_FILTER_TAG";
    public static final String FRAGMENT_QUESTION_TAG = "FRAGMENT_QUESTION_TAG";
    public static final String FRAGMENT_DIALOG_TAG = "FRAGMENT_DIALOG_TAG";
    public static final String DIALOG_TITLE = "DIALOG_TITLE";
    public static final String DIALOG_LINK = "DIALOG_LINK";
    public static final String QUESTION_KEY = "QUESTION_KEY";
    public static final String QUESTION_FILTER_KEY = "QUESTION_FILTER_KEY";
    public static final int EMAIL_REQUESTCODE = 47;

    private String mGoToFragment;
    Fragment mSavedFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mMainToolbar);
        mMainToolbar.setTitle("Bliss Application");

        // initialize the parameters to be used on the URI
        mGoToFragment = FRAGMENT_LIST_TAG;

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            mGoToFragment = bundle.getString(DeepLinkHandler.FRAGMENT_TO_GO);
        }
        Log.d("DEEPLINK", "DEEPLINK: "+intent.getBooleanExtra(DeepLink.IS_DEEP_LINK, false));

    }

    // Create a receiver to check the internet connection status
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityStatus.isConnected(getApplicationContext())) {
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
        saveCurrentFragment();
        // check if there is a share fragment on the screen
        ShareDialogFragment dialogFragment = (ShareDialogFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_DIALOG_TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }

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
                .replace(R.id.fragment_holder, fragment, FRAGMENT_CONNECTIVITY_TAG)
                .commit();
    }

    public void addLoadingFragment() {
        LoadingFragment loadingFragment = (LoadingFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_LOADING_TAG);
        if (loadingFragment == null || loadingFragment.isDetached()) {
            LoadingFragment fragment = new LoadingFragment();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_holder, fragment, FRAGMENT_LOADING_TAG)
                    .commit();
        }
    }

    public void addQuestionsListFragment(Bundle bundle) {
        QuestionListFragment fragment = new QuestionListFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_holder, fragment, FRAGMENT_LIST_TAG)
                .commit();
    }

    public void addQuestionFragment(Bundle bundle) {
        QuestionFragment fragment = new QuestionFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .addToBackStack(FRAGMENT_QUESTION_TAG)
                .replace(R.id.fragment_holder, fragment, FRAGMENT_QUESTION_TAG)
                .commit();
    }

    public static void addShareDialogFragment(FragmentManager manager, String message, String link) {
        Fragment fragment = manager.findFragmentByTag(MainActivity.FRAGMENT_DIALOG_TAG);
        if (fragment == null) {
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.DIALOG_TITLE, message);
            bundle.putString(MainActivity.DIALOG_LINK, link);
            ShareDialogFragment dialogFragment = new ShareDialogFragment();
            dialogFragment.setArguments(bundle);
            dialogFragment.show(manager, MainActivity.FRAGMENT_DIALOG_TAG);
        }
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
    public void GoToNextFragment() {
        if (mGoToFragment!=null) {
            Bundle bundle = new Bundle();
            switch (mGoToFragment) {
                case FRAGMENT_LIST_WITHOUT_FILTER_TAG:
                    bundle.putString(QUESTION_FILTER_KEY, "");

                    break;
                case FRAGMENT_LIST_WITH_FILTER_TAG:
                    break;
                case FRAGMENT_QUESTION_TAG:
                    break;
                default:
                    addQuestionsListFragment(null);
                    break;
            }
            mGoToFragment = null;
        } else {
            if (mSavedFragment != null) {
                // if we are in a question fragment, we want to add up navigation
                if (mSavedFragment instanceof QuestionFragment) {
                    getSupportFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.fragment_holder, mSavedFragment, null)
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_holder, mSavedFragment, null)
                            .commit();
                }

            } else {
                addQuestionsListFragment(null);
            }

        }

    }

    @Override
    public void goToQuestion(int position) {
        QuestionListFragment fragment = (QuestionListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST_TAG);
        if (fragment != null) {

            Bundle bundle = getBundleFromQuestion(fragment.mQuestions.get(position));
            addQuestionFragment(bundle);
        }
    }

    @Override
    public void deepLinkGoToQuestion(Question question) {
        addQuestionFragment(getBundleFromQuestion(question));
    }

    @Override
    public void deepLinkGoToList() {
        addQuestionFragment(null);
    }

    // serialize into gson
    private Bundle getBundleFromQuestion(Question question) {
        Bundle bundle = new Bundle();
        bundle.putString(QUESTION_KEY, new Gson().toJson(question));
        return bundle;
    }

    // we have finished typing the email on the share fragment, now we are going to send it
    @Override
    public void inFinishEmail(String email, String link) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Super duper e-mail link dispatcher");
        intent.putExtra(Intent.EXTRA_TEXT, "Dear Madam or Sir. \n\n We are here to present you this incredible link for your BlissApplications Application!\n"+link+"\nKind Regards,\n");
        startActivity(Intent.createChooser(intent, "Send Email"));
        startActivityForResult(Intent.createChooser(intent, "Send Email"), EMAIL_REQUESTCODE);
    }

    private void saveCurrentFragment() {
        mSavedFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
    }

    @Override
    public void setupUpNavigation() {
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    @Override
    public void removeUpNavigation() {
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

}
