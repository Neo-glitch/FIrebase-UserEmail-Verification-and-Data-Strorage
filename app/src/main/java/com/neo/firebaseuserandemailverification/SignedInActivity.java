package com.neo.firebaseuserandemailverification;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.neo.firebaseuserandemailverification.issues.IssuesActivity;
import com.neo.firebaseuserandemailverification.models.Chatroom;
import com.neo.firebaseuserandemailverification.models.User;
import com.neo.firebaseuserandemailverification.utility.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * activity shown when user has been authenticated successfully
 */
public class SignedInActivity extends AppCompatActivity {

    private static final String TAG = "SignedInActivity";

    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    // widgets and UI References

    //vars
    public static boolean isActivityRunning;                                                        // tells if activity is running
    private Boolean mIsAdmin = false;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signedin);
        Log.d(TAG, "onCreate: started.");
        setupFirebaseAuth();
        getUserDetails();
        initFCM();
        isAdmin();
        getPendingIntent();
//        initImageLoader();

        // to test crash
//        ArrayList<String> test = null;
//        test.size();
    }

    /**
     * retrieves intentExtras from the notification and pass to chatRoom activity, which is started
     */
    private void getPendingIntent(){
        Log.d(TAG, "getPendingIntent: checking for pending intents");

        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.intent_chatroom))){
            Log.d(TAG, "getPendingIntent: pending intent detected");

            Chatroom chatroom = intent.getParcelableExtra(getString(R.string.intent_chatroom));
            Intent chatroomIntent = new Intent(this, ChatroomActivity.class);
            chatroomIntent.putExtra(getString(R.string.intent_chatroom), chatroom);
            startActivity(chatroomIntent);
        }
    }


    /**
     * gets device token for cloud messaging
     */
    private void initFCM(){
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "initFCM: token: " + token);
        sendRegistrationToServer(token);
    }

    /**
     * done here also since when user signs in the token will be refreshed for sure
     * and send token to firebase db
     */
    private void sendRegistrationToServer(String refreshedToken) {
        Log.d(TAG, "sendRegistrationToServer: sending token to server: " + refreshedToken);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_messaging_token))                       // field to store the token
                .setValue(refreshedToken);
    }


    /**
     * checks to see if user security level is = 10 i.e admin
     */
    private void isAdmin(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: datasnapshot: " + dataSnapshot);
                DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                int securityLevel = Integer.parseInt(singleSnapshot.getValue(User.class).getSecurity_level());
                if( securityLevel == 10){
                    Log.d(TAG, "onDataChange: user is an admin.");
                    mIsAdmin = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * init universal image loader although not used
     */
    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(SignedInActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }


    /**
     * gets user acct details
     */
    private void getUserDetails(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){               // user is authenticated
            String uid = user.getUid();
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            String properties = "uid: " + uid + "\n" +
                    "name: " + name + "\n" +
                    "email: " + email + "\n" +
                    "photoUrl: " + photoUrl;

            Log.d(TAG, "getUserDetails: properties \n: " + properties);


        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    /**
     * auth state checker
     */
    private void checkAuthenticationState(){
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");
            Intent intent = new Intent(SignedInActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);        // to prevent user from pressing back button and gaining access to signedInActivity # new task
            startActivity(intent);
            finish();
        }else{
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.optionSignOut:
                signOut();
                return true;
            case R.id.optionAccountSettings:
                startActivity(new Intent(SignedInActivity.this, SettingsActivity.class));
                return true;
            case R.id.optionChat:
                startActivity(new Intent(SignedInActivity.this, ChatActivity.class));
                return true;
            case R.id.optionAdmin:
                if(mIsAdmin){
                    startActivity(new Intent(SignedInActivity.this, AdminActivity.class));
                }else{
                    Toast.makeText(this, "You're not an Admin", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.optionIssues:
                startActivity(new Intent(SignedInActivity.this, IssuesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sign out the current user
     */
    private void signOut(){
        Log.d(TAG, "signOut: signing out");
        FirebaseAuth.getInstance().signOut();
    }

    /*
            ----------------------------- Firebase setup ---------------------------------
         */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(SignedInActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
        isActivityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
        isActivityRunning = false;
    }



}
