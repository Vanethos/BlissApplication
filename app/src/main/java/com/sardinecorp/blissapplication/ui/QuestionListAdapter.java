package com.sardinecorp.blissapplication.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.sardinecorp.blissapplication.R;
import com.sardinecorp.blissapplication.network.Question;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Gonçalo on 23/07/2017.
 */

public class QuestionListAdapter extends RecyclerView.Adapter<QuestionListAdapter.QuestionHolder> {

    private List<Question> mQuestions;
    private Context mContext;
    private LinearLayoutManager mLinearLayoutManager;
    private int mLastItem;

    public QuestionListAdapter (List<Question> questions, Context context, LinearLayoutManager linearLayoutManager) {
        mQuestions = questions;
        mContext = context;
        mLinearLayoutManager = linearLayoutManager;
        mLastItem = 0;
    }


    @Override
    public QuestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.question_item, parent, false);
        return new QuestionHolder(view);
    }

    @Override
    public void onBindViewHolder(QuestionHolder holder, int position) {
        holder.bindQuestion(mQuestions.get(position));
        setAnimation(holder.itemView, position);
    }



    @Override
    public int getItemCount() {
        return mQuestions.size();
    }

    public void updateQuestions(List<Question> questionList) {
        mQuestions = questionList;
    }

    public void newQuestionList() {
        mQuestions = new ArrayList<>();
        mLastItem = 0;
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > mLastItem) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.push_left_in);
            viewToAnimate.startAnimation(animation);
            mLastItem = position;
        }
    }

    public class QuestionHolder  extends RecyclerView.ViewHolder {

        @BindView(R.id.question_item_image)
        ImageView mQuestionImage;
        @BindView(R.id.question_item_title)
        TextView mQuestionText;

        public QuestionHolder (View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindQuestion (Question question) {
            mQuestionText.setText(question.getQuestion());
            //TODO: picasso for image binding
            Picasso.with(mContext).load(question.getThumbUrl()).into(mQuestionImage);
        }
    }
}
