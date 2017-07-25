package com.sardinecorp.blissapplication.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sardinecorp.blissapplication.R;
import com.sardinecorp.blissapplication.network.BlissService;
import com.sardinecorp.blissapplication.network.Question;
import com.sardinecorp.blissapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This class is not used on this project
 * If the DeepLink URI were to work, this class would download the data
 * from the server and start a new Question Activity
 */

public class GetQuestionFragment extends Fragment {

    public interface DeepLinkQuestionInterface {
        void deepLinkGoToQuestion(Question question);
        void deepLinkGoToList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_question, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final DeepLinkQuestionInterface interf = (DeepLinkQuestionInterface)getActivity();
        Bundle bundle = getArguments();
        if (bundle == null) {
            interf.deepLinkGoToList();
        } else {
            // retrieve the key from the bundle and get the data from the server
            int id = bundle.getInt(MainActivity.QUESTION_KEY);
            Retrofit client = RetrofitClient.getClient();
            BlissService service = client.create(BlissService.class);
            service.getQuestionFromID(id).enqueue(new Callback<Question>() {
                @Override
                public void onResponse(Call<Question> call, Response<Question> response) {
                    if (response.body() != null) {
                        Question question = response.body();
                        interf.deepLinkGoToQuestion(question);
                    }
                }

                @Override
                public void onFailure(Call<Question> call, Throwable t) {
                    interf.deepLinkGoToList();
                }
            });

        }
    }

}
