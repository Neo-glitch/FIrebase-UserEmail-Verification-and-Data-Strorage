package com.neo.firebaseuserandemailverification.utility;

import com.neo.firebaseuserandemailverification.models.fcm.FirebaseCloudMessage;

import java.util.Map;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;


/**
 * Interface used by retrofit to make post requests to firebase to trigger cloud Messaging
 */
public interface FCM {

    @POST("send")           // end point
    Call<ResponseBody> send(
            @HeaderMap Map<String, String> headers,                 // headers of request
            @Body FirebaseCloudMessage message                      // body of request
    );
}
