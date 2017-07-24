package com.sardinecorp.blissapplication.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Created by Gonçalo on 22/07/2017.
 */

public interface BlissService {
    String BASE_URL = "https://private-anon-7820b60ac1-blissrecruitmentapi.apiary-mock.com";

    @GET("/health")
    Call<APIStatus> getStatus();

    @GET("/questions")
    Call<List<Question>> getQuestions(@Query("limit") int limit, @Query("offset") int offset, @Query("filter") String filter);

    @GET("question_id")
    Call<Question> getQuestionFromID(@Query("question_id") int id);

    // TOOD: put here a json object to be updated
    @PUT("question_id")
    Call<Question> updateAnswer();
}
