package com.sardinecorp.blissapplication.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BlissService {
    String BASE_URL = "https://private-anon-7820b60ac1-blissrecruitmentapi.apiary-mock.com";

    @GET("/health")
    Call<APIStatus> getStatus();

    @GET("/questions")
    Call<List<Question>> getQuestions(@Query("limit") int limit, @Query("offset") int offset, @Query("filter") String filter);

    @GET("/question_id")
    Call<Question> getQuestionFromID(@Query("question_id") int id);

    @PUT("/questions/{id}")
    Call<Question> updateAnswer(@Path("id") int id, @Body Question question);
}
