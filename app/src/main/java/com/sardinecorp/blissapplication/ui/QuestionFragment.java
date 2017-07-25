package com.sardinecorp.blissapplication.ui;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.Space;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sardinecorp.blissapplication.R;
import com.sardinecorp.blissapplication.network.BlissService;
import com.sardinecorp.blissapplication.network.Choice;
import com.sardinecorp.blissapplication.network.Question;
import com.sardinecorp.blissapplication.network.RetrofitClient;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Gonçalo on 24/07/2017.
 */

public class QuestionFragment extends Fragment {

    private Question mQuestion;
    private boolean mVoted;

    @BindView(R.id.question_item_title)
    TextView mTitle;
    @BindView(R.id.question_item_image)
    ImageView mImage;
    @BindView(R.id.questions_group)
    RadioGroup mQuestionsGroup;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);
        ButterKnife.bind(this, view);
        mQuestion = new Question();
        // Get the object
        Bundle jsonObject = getArguments();
        if (jsonObject != null) {
            mQuestion = new Gson().fromJson(jsonObject.getString(MainActivity.QUESTION_KEY), Question.class);
            populateViews();
        }
        mVoted = false;
        // set up the share button on the fragment
        setHasOptionsMenu(true);
        return view;
    }

    @OnClick(R.id.question_post_button)
    public void submitAnswerToServer() {
        if (!mVoted) {
            submitAnswer();
        } else {
            Toast.makeText(getContext(), "You have already voted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateViews() {
        mTitle.setText(mQuestion.getQuestion());
        Picasso.with(getContext()).load(mQuestion.getImageUrl()).into(mImage);
        List<Choice> choices = mQuestion.getChoices();
        for (Choice choice :
                choices) {
            RadioButton radio = new RadioButton(getContext());
            radio.setText(choice.getChoice());
            radio.setTextSize(30);
            radio.setPadding(dpToPx(30), 0, 0, 0);
            mQuestionsGroup.addView(radio);

            // this Space is a way to replace margins
            mQuestionsGroup.addView(getSpace(4));

            // add a view to delimit the questions
            View view = new View(getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(2)));
            view.setBackgroundColor(Color.GRAY);
            mQuestionsGroup.addView(view);

            mQuestionsGroup.addView(getSpace(4));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem shareItem = menu.add("Share");
        shareItem.setIcon(android.R.drawable.ic_menu_share);
        // todo: change share icon to be opaque
        shareItem.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        shareItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            MainActivity.addShareDialogFragment(getActivity().getSupportFragmentManager(),
                    "Share Question "+mQuestion.getId(),
                    "blissrecruitment://questions?question_id="+mQuestion.getId());
        }
        return true;
    }

    private void submitAnswer() {
        Retrofit client = RetrofitClient.getClient();
        BlissService service = client.create(BlissService.class);
        // update to the new answer
        int questionId = mQuestionsGroup.getCheckedRadioButtonId() - 1;
        if (questionId < 0) {
            Toast.makeText(getContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
        } else {
            int choicesVotes = mQuestion.getChoices().get(questionId).getVotes();
            mQuestion.getChoices().get(questionId).setVotes(choicesVotes + 1);
            service.updateAnswer(mQuestion.getId(), mQuestion)
                    .enqueue(new Callback<Question>() {
            @Override
            public void onResponse(Call<Question> call, Response<Question> response) {
                Log.d("RESPONSE", response.raw().toString());
            }

            @Override
            public void onFailure(Call<Question> call, Throwable t) {
                t.printStackTrace();
            }
        });
        }
    }

    private Space getSpace(int height) {
        Space space = new Space(getContext());
        space.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(height)));
        return space;
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }


}
