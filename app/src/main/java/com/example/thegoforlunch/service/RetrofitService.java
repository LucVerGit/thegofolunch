package com.example.thegoforlunch.service;

import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class which configures a {@link Retrofit} service.
 */
public class RetrofitService {


    private static final String TAG = RetrofitService.class.getSimpleName();
    private static final String BASE_URL = "https://maps.googleapis.com/";
    private static final Retrofit RETROFIT = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(AddLoggingInterceptor.setLogging())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static <S> S createService(Class<S> serviceClass) {
        Log.d(TAG, "createService");





        return RETROFIT.create(serviceClass);
    }

}
