package com.neo.firebaseuserandemailverification.issues;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.neo.firebaseuserandemailverification.ChangePhotoDialog;
import com.neo.firebaseuserandemailverification.R;
import com.neo.firebaseuserandemailverification.models.Project;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * shows details about a project when it's selected from Projects fragment and allows editing of project
 */
public class ProjectDetailsActivity extends AppCompatActivity implements
        View.OnClickListener,
        ChangePhotoDialog.OnPhotoReceivedListener {

    private static final String TAG = "ProjectDetailsActivity";
    private static final int REQUEST_CODE = 1234;

    //widgets
    private CircleImageView mAvatar;
    private EditText mProjectName, mProjectDescription;
    private TextView mProjectCreatedDate;
    private Button mSave;

    //vars
    private Project mProject;
    private Uri mSelectedImageUri;
    private boolean mStoragePermissions;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);
        mProjectName = findViewById(R.id.project_name);
        mProjectDescription = findViewById(R.id.project_description);
        mAvatar = findViewById(R.id.avatar);
        mProjectCreatedDate = findViewById(R.id.project_created_date);
        mSave = findViewById(R.id.btn_save);

        mAvatar.setOnClickListener(this);
        mSave.setOnClickListener(this);

        getSelectedProject();
        setupActionBar();
        setProjectDetails();
        verifyStoragePermissions();
    }


    /**
     * updates activity widgets with prop from property obj received
     */
    private void setProjectDetails() {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.default_avatar);

        Glide.with(this)
                .load(mProject.getAvatar())
                .apply(requestOptions)
                .into(mAvatar);

        mProjectName.setText(mProject.getName());
        mProjectDescription.setText(mProject.getDescription());
        mProjectCreatedDate.setText(mProject.getTime_created().toString());

        hideSoftKeyboard();
    }


    /**
     * gets selected project obj using intent passed to startActivity
     */
    private void getSelectedProject() {
        if (getIntent().hasExtra(getString(R.string.intent_project))) {
            mProject = getIntent().getParcelableExtra(getString(R.string.intent_project));
        }
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if (mProject != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setTitle(mProject.getName());
            } else {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }


    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.avatar: {
                if(mStoragePermissions){
                    ChangePhotoDialog dialog = new ChangePhotoDialog();
                    dialog.show(getSupportFragmentManager(), getString(R.string.dialog_change_photo));
                }else{
                    verifyStoragePermissions();
                }
                break;
            }

            case R.id.btn_save: {
                IssuesPhotoUploader uploader = new IssuesPhotoUploader(this, mProject.getProject_id(), "", null);
                uploader.uploadNewPhoto(mSelectedImageUri);
                updateProject();
                break;
            }
        }
    }


    /**
     * called when save button is pressed to save in db
     */
    private void updateProject() {

        if (mProjectName.equals("") || mProjectDescription.equals("")) {
            Toast.makeText(this, "fill in the fields", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            HashMap<String, Object> updates = new HashMap<>();
            updates.put(getString(R.string.field_name), mProjectName.getText().toString());
            updates.put(getString(R.string.field_description), mProjectDescription.getText().toString());

            db.collection(getString(R.string.collection_projects))
                    .document(mProject.getProject_id())
                    .update(updates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: updated.");
                                Toast.makeText(ProjectDetailsActivity.this, "saved", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "onComplete: update failed.");
                            }
                        }
                    });
        }
    }

    /**
     * Generalized method for asking permission. Can pass any array of permissions
     */
    public void verifyStoragePermissions(){
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2] ) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    ProjectDetailsActivity.this,
                    permissions,
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void getImagePath(Uri imagePath) {
        if( !imagePath.toString().equals("")){
            mSelectedImageUri = imagePath;
            Log.d(TAG, "getImagePath: got the image uri: " + mSelectedImageUri);

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.default_avatar);

            Glide.with(this)
                    .load(imagePath)
                    .apply(requestOptions)
                    .into(mAvatar);

        }
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {

    }
}














