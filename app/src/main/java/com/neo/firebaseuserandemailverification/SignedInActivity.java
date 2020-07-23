package com.neo.firebaseuserandemailverification;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class SignedInActivity extends AppCompatActivity {

    private static final String TAG = "SignedInActivity";

    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    // widgets and UI References



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signedin);
        Log.d(TAG, "onCreate: started.");
        setupFirebaseAuth();
        getUserDetails();

    }

//    /**
//     * used to set displayName(userName) and photoUri
//     */
//    private void setUserDetails(){
//       FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//       if(user != null){
//           UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
//                   .setDisplayName("Neo")
//                   .setPhotoUri(Uri.parse("https://tr4.cbsistatic.com/hub/i/r/2020/02/24/0c40ec8f-8ebf-4f28-9e2b-5f09d977ce97/resize/1200x/077bf649cbb059fc9662f3b4e6943120")
//                   .buildUpon()
//                   .appendPath("android11hero.jpg").build())
//                   .build();
//
//           user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {  // updates details on firebase
//               @Override
//               public void onComplete(@NonNull Task<Void> task) {
//                   if(task.isSuccessful()){
//                        getUserDetails();
//                   }
//               }
//           });
//
//       }
//    }
//
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
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }



}
