package com.neo.firebaseuserandemailverification.utility;

import com.neo.firebaseuserandemailverification.models.fcm.FirebaseCloudMessage;

import java.util.Map;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

/**
 * Created by User on 10/26/2017.
 */

public interface FCM {

    @POST("send")           // end point
    Call<ResponseBody> send(
            @HeaderMap Map<String, String> headers,                 // headers of request
            @Body FirebaseCloudMessage message                      // body of request
    );
}