package com.sardinecorp.blissapplication.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Gonçalo on 22/07/2017.
 */

public class RetrofitClient {
    public static Retrofit getClient() {
        return new Retrofit.Builder()
                .baseUrl(BlissService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
