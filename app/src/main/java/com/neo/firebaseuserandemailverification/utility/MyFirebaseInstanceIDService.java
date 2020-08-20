package com.neo.firebaseuserandemailverification.utility;


import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.neo.firebaseuserandemailverification.R;

/**
 * enables sending fcm to specific device using token
 */
class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseInstanceIdSer";

    @Override
    public void onTokenRefresh() {       // gets token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefresh: refresh token : " + refreshedToken);

        // sends the token to our firebase realtime db
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        Log.d(TAG, "sendRegistrationToServer: sending token to server: " + refreshedToken);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_messaging_token))                       // field to store the token
                .setValue(refreshedToken);
    }
}
