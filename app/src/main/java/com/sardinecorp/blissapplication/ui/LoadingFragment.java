package com.sardinecorp.blissapplication.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sardinecorp.blissapplication.R;
import com.sardinecorp.blissapplication.network.APIStatus;
import com.sardinecorp.blissapplication.network.BlissService;
import com.sardinecorp.blissapplication.network.RetrofitClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoadingFragment extends Fragment {

    public interface LoadingFragmentInterface {
        void GoToNextFragment();
    }

    @BindView(R.id.status_check_text)
    TextView mStatusText;
    @BindView(R.id.status_spinner)
    ProgressBar mStatusProgress;
    @BindView(R.id.status_error_text)
    TextView mStatusErrorText;
    @BindView(R.id.status_error_button)
    Button mStatusErrorButton;
    @BindView(R.id.status_bad_health_imageview)
    ImageView mStatusBadHealthImageView;
    @BindView(R.id.status_ok_text)
    TextView mStatusOkText;
    @BindView(R.id.status_good_health_imageview)
    ImageView mStatusGoodHealthImageView;
    @BindView(R.id.loading_background)
    RelativeLayout mBackground;

    private int mAnimationDelay = 1000;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        ButterKnife.bind(this, view);
        // at the start of the view, the error message and button are hidden
        initializeViews();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        checkServerStatus();
    }



    @OnClick(R.id.status_error_button)
    public void reCheckStatus() {
        serverRecheck();
    }

    private void checkServerStatus() {
        // create the call to the network to check server status
        Retrofit client = RetrofitClient.getClient();
        BlissService status = client.create(BlissService.class);

        Call<APIStatus> test = status.getStatus();
        test.enqueue(new Callback<APIStatus>() {
            @Override
            public void onResponse(Call<APIStatus> call, Response<APIStatus> response) {
                Log.d("response", "API RESPONSE: "+response.message());
                if (response.message().equals("OK")) {
                    serverUp();
                } else {
                    serverDown();
                }
            }

            @Override
            public void onFailure(Call<APIStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void serverUp() {
        AnimatorSet animateServerUp = new AnimatorSet();
        animateServerUp.playSequentially(animateCheckStatus(true), animateGoodStatus(false));
        // Create a listener so that when the animation is ended we go to the next fragment
        animateServerUp.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // Delay the code for 1 second
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoadingFragmentInterface fragmentInterface = (LoadingFragmentInterface)getActivity();
                        // if we are no longer on the fragment, then this will result in NULL
                        if (fragmentInterface != null) {
                            fragmentInterface.GoToNextFragment();
                        }
                    }
                }, 1000);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animateServerUp.start();
    }

    private void serverDown() {
        AnimatorSet animateServerDown = new AnimatorSet();
        animateServerDown.playSequentially(animateCheckStatus(true), animateErrorStatus(false));
        animateServerDown.start();
    }

    private void serverRecheck() {
        AnimatorSet animateServerDown = new AnimatorSet();
        animateServerDown.playSequentially(animateErrorStatus(true), animateCheckStatus(false));
        animateServerDown.start();
        checkServerStatus();
    }

    private AnimatorSet animateCheckStatus(Boolean hide) {
        int i = hide ? 0 : 1;
        AnimatorSet set = new AnimatorSet();
        AnimatorSet progress = animateScaleView(mStatusProgress, i);
        AnimatorSet text = animateScaleView(mStatusText, i);
        if (!hide) {
            ObjectAnimator bg = animateBackground("retry");
            set.playTogether(progress, text, bg);
        } else {
            set.playTogether(progress, text);
            set.setStartDelay(mAnimationDelay);
        }
        return set;
    }


    private AnimatorSet animateErrorStatus(Boolean hide) {
        int i = hide ? 0 : 1;
        AnimatorSet set = new AnimatorSet();
        // disable or enable the button, depending on the action
        mStatusErrorButton.setEnabled(!hide);
        AnimatorSet button = animateScaleView(mStatusErrorButton, i);
        AnimatorSet text = animateScaleView(mStatusErrorText, i);
        AnimatorSet image = animateScaleView(mStatusBadHealthImageView, i);
        if (!hide) {
            // animate background color change
            ObjectAnimator bg = animateBackground("error");
            set.playTogether(button, text, image, bg);
        } else {
            set.playTogether(button, text, image);
            set.setStartDelay(mAnimationDelay);
        }
        return set;
    }

    private AnimatorSet animateGoodStatus(Boolean hide) {
        int i = hide ? 0 : 1;
        AnimatorSet set = new AnimatorSet();
        AnimatorSet text = animateScaleView(mStatusOkText, i);
        AnimatorSet image = animateScaleView(mStatusGoodHealthImageView, i);
        if (!hide) {
            // animate background color change
            ObjectAnimator bg = animateBackground("ok");
            set.playTogether(text, image, bg);
        } else {
            set.playTogether(text, image);
            set.setStartDelay(mAnimationDelay);
        }
        return set;
    }

    private void initializeViews() {
        mStatusErrorButton.setEnabled(false);
        mStatusErrorButton.setScaleX(0);
        mStatusErrorButton.setScaleY(0);
        mStatusErrorText.setScaleX(0);
        mStatusErrorText.setScaleY(0);
        mStatusBadHealthImageView.setScaleY(0);
        mStatusBadHealthImageView.setScaleX(0);
        mBackground.setBackgroundColor(Color.WHITE);
    }

    private AnimatorSet animateScaleView(View view, int i) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, i);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, i);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        return set;
    }

    private ObjectAnimator animateBackground(String type) {
        int startColor = -1;
        int endColor = -1;
        switch(type) {
            case "error":
                startColor = Color.WHITE;
                endColor = getActivity().getResources().getColor(R.color.badHealthBackground);
                break;
            case "ok":
                startColor = Color.WHITE;
                endColor = getActivity().getResources().getColor(R.color.goodHealthBackground);
                break;
            case "retry":
                startColor = getActivity().getResources().getColor(R.color.badHealthBackground);
                endColor = Color.WHITE;
        }
        return new ObjectAnimator().ofArgb(mBackground, "backgroundColor", startColor, endColor);
    }
}
