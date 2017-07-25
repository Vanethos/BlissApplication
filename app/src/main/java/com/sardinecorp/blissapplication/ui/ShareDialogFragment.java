package com.sardinecorp.blissapplication.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.sardinecorp.blissapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ShareDialogFragment extends DialogFragment implements TextView.OnEditorActionListener {

    public interface ShareEmailListener {
        void inFinishEmail(String email, String link);
    }

    @BindView(R.id.dialog_fragment_edittext)
    TextView mEmail;
    @BindView(R.id.dialog_fragment_title)
    TextView mTitle;

    private String mTitleText;
    private String mLinkToSend;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_share, container, false);
        ButterKnife.bind(this, view);
        mEmail.setOnEditorActionListener(this);
        mEmail.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        Bundle bundle = getArguments();
        if (bundle!=null) {
            mTitleText = bundle.getString(MainActivity.DIALOG_TITLE);
            mLinkToSend = bundle.getString(MainActivity.DIALOG_LINK);

        } else {
            // set the "default" parameters
            mTitleText = "Share Questions";
            mLinkToSend = "blissrecruitment://questions";
        }
        mTitle.setText(mTitleText);

        return view;
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (mEmail.getText().toString().trim().length() > 0) {
            ShareEmailListener listener = (ShareEmailListener) getActivity();
            listener.inFinishEmail(mEmail.getText().toString(), mLinkToSend);
            this.dismiss();
        }
        return true;
    }


}
