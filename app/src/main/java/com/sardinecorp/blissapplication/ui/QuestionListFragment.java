package com.sardinecorp.blissapplication.ui;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sardinecorp.blissapplication.R;
import com.sardinecorp.blissapplication.network.BlissService;
import com.sardinecorp.blissapplication.network.Question;
import com.sardinecorp.blissapplication.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Gonçalo on 22/07/2017.
 */

public class QuestionListFragment extends Fragment {

    List<Question> mQuestions;
    QuestionListAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    @BindView(R.id.question_list_recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.question_list_edittext)
    EditText mEditText;
    @BindView(R.id.question_list_buttons)
    LinearLayout mButtonLayout;
    @BindView(R.id.share_button)
    Button mShareButton;
    @BindView(R.id.dismiss_button)
    Button mDismissButton;
    @BindView(R.id.recyclerview_progress)
    ProgressBar mProgressBar;


    private boolean mLoading = true;
    private int mPastVisiblesItems;
    private int mVisibleItemCount;
    private int mTotalItemCount;
    private int mOffset;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question_list, container, false);
        ButterKnife.bind(this, view);
        mQuestions = new ArrayList<Question>();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new QuestionListAdapter(mQuestions, getContext(), mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mOffset = 0;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // set the visibility of the spinner to visible
        mProgressBar.setVisibility(View.VISIBLE);

        // set the actions on the EditText so that it can search for the filtered questions
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String getText = mEditText.getText().toString();
                    Log.d("RESPONSE", "EditText text: "+getText);
                    if (getText.trim().length()>0){
                        // remove all the items from the list
                        mAdapter.newQuestionList();
                        mAdapter.notifyDataSetChanged();
                        // hide the keyboard
                        mEditText.clearFocus();
                        InputMethodManager in = (InputMethodManager)getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                        // show the hidden bar at the bottom
                        slideToTop(mButtonLayout);
                        // put the progressbar visible
                        if(mProgressBar.getVisibility() == View.GONE) {
                            mProgressBar.setVisibility(View.VISIBLE);
                        }
                        // query the API
                        getQuestions(mOffset, 10, getText);
                    }
                    return true;
                }

                return false;
            }
        });
        getQuestions(mOffset, 10, "");
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // if we are scrolling down
                if (dy > 0) {
                    mVisibleItemCount = mLayoutManager.getChildCount();
                    mTotalItemCount = mLayoutManager.getItemCount();
                    mPastVisiblesItems = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                    if (mLoading && mVisibleItemCount + mPastVisiblesItems >= mTotalItemCount) {
                        mLoading = false;
                        getQuestions(mOffset, 10, "");
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @OnClick(R.id.dismiss_button)
    public void dismissSearch() {
        // hide the bar
        slideToBottom(mButtonLayout);
        // remove all the items from the list
        mAdapter.newQuestionList();
        mAdapter.notifyDataSetChanged();
        // show the progress bar
        if (mProgressBar.getVisibility() == View.GONE) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        // show the list of all questions
        getQuestions(mOffset, 10, "");
    }

    private void getQuestions(int offset, int limit, final String filter) {
        Log.d("RESPONSE", "we are going to retrieve questions");
        Retrofit client = RetrofitClient.getClient();
        BlissService service = client.create(BlissService.class);
        Call<List<Question>> listOfQuestions = service.getQuestions(offset, limit, filter);
        listOfQuestions.enqueue(new Callback<List<Question>>() {
            @Override
            public void onResponse(Call<List<Question>> call, Response<List<Question>> response) {
                // Hide the spinner
                if (mProgressBar.getVisibility()==View.VISIBLE) {
                    mProgressBar.setVisibility(View.GONE);
                }
                // check if we have a valid response
                if (response.body()!=null) {
                    if (filter.trim().length()>0) {
                        mQuestions = response.body();
                        // we reset the offset counter by enterning a new list
                        mOffset = 0;
                    } else {
                        mQuestions.addAll(response.body());
                        mOffset += response.body().size();
                    }
                    mAdapter.updateQuestions(mQuestions);
                    mAdapter.notifyDataSetChanged();
                }
                mLoading = true;
            }

            @Override
            public void onFailure(Call<List<Question>> call, Throwable t) {
                Log.d("RESPONSE", "=== ERROR RECEIVING DATA ===");
                mLoading = true;
            }
        });
    }


    public void slideToBottom(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(300);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    public void slideToTop(final View view){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TranslateAnimation animate = new TranslateAnimation(0,0,view.getHeight(),0);
                animate.setDuration(300);
                view.startAnimation(animate);
                view.setVisibility(View.VISIBLE);
            }
        },1000);

    }

}
