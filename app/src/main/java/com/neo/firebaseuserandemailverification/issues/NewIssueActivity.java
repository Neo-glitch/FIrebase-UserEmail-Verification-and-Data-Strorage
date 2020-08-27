package com.neo.firebaseuserandemailverification.issues;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.neo.firebaseuserandemailverification.R;
import com.neo.firebaseuserandemailverification.models.Issue;
import com.neo.firebaseuserandemailverification.models.Project;
import com.neo.firebaseuserandemailverification.utility.ResultCodes;
import com.neo.firebaseuserandemailverification.utility.SpinnerResource;

import java.util.ArrayList;


/**
 * Created by User on 4/17/2018.
 */

public class NewIssueActivity extends AppCompatActivity implements
        View.OnClickListener,
        View.OnTouchListener {

    private static final String TAG = "NewIssueActivity";
    private static final int REQUEST_CODE = 1234;

    //widgets
    private Spinner mIssueTypeSpinner, mPrioritySpinner;
    private AutoCompleteTextView mAssignToProject;                              // autoCompleteTv holds projectName issue is associated with
    private EditText mSummary,                                                  // title of issue
            mDescription;
    private ImageView mClose;
    private TextView mCreate;
    private ProgressBar mProgressBar;


    //vars
    private boolean mStoragePermissions;
    private ArrayList<Project> mProjects = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_issue);
        mIssueTypeSpinner = findViewById(R.id.issue_type_spinner);
        mPrioritySpinner = findViewById(R.id.priority_spinner);
        mAssignToProject = findViewById(R.id.assign_to_project);
        mClose = findViewById(R.id.close);
        mCreate = findViewById(R.id.create);
        mSummary = findViewById(R.id.issue_summary);
        mDescription = findViewById(R.id.issue_description);
        mProgressBar = findViewById(R.id.progress_bar);

        mClose.setOnClickListener(this);
        mCreate.setOnClickListener(this);
        mAssignToProject.setOnTouchListener(this);

        setupActionBar();
        initIssueTypeSpinner();
        initPrioritySpinner();
        initProjectAutoCompleteTextView();
    }

    /**
     * queries needed fireStore and get list of project to be associated with autoComplete tv
     */
    private void initProjectAutoCompleteTextView() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference projectsRef = db.collection(getString(R.string.collection_projects));


        // does querying of collectionRef and get needed values(docs)
        projectsRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: got projects");
                            int i = 0;
                            String[] projects = new String[task.getResult().size()];
                            for (QueryDocumentSnapshot document : task.getResult()) {   // gets each doc in doc list or collection
                                Project project = document.toObject(Project.class);
                                mProjects.add(project);
                                projects[i] = project.getName();
                                i++;
                            }

                            ArrayAdapter<String> adapter =
                                    new ArrayAdapter<String>(NewIssueActivity.this, android.R.layout.simple_list_item_1, projects);
                            mAssignToProject.setAdapter(adapter);
                            initTextWatcher();
                        } else {
                            Toast.makeText(NewIssueActivity.this, "error getting projects", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: error getting project names.", task.getException());
                        }
                    }
                });
    }


    /**
     * makes sure user enters something in autoCompleteTextView else error is show on tv
     */
    private void initTextWatcher() {
        mAssignToProject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                    mAssignToProject.setError(getString(R.string.select_a_project));
                } else {
                    mAssignToProject.setError(null);
                }
            }
        });
    }


    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }


    /**
     * sets values for issueTypeSpinner
     */
    private void initIssueTypeSpinner() {
        final String[] issueTypes = SpinnerResource.issue_type_spinner;
        int[] issueImages = SpinnerResource.issue_type_images_spinner;

        final SpinnerAdapter adapter = new SpinnerAdapter(this, issueTypes, issueImages);
        mIssueTypeSpinner.setAdapter(adapter);

        mIssueTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.setSelectedText(issueTypes[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    /**
     * sets values for prioritySpinner
     */
    private void initPrioritySpinner() {
        final String[] priorityLevels = SpinnerResource.issue_priorities_spinner;
        int[] priorityImages = SpinnerResource.issue_priority_images_spinner;

        final SpinnerAdapter adapter = new SpinnerAdapter(this, priorityLevels, priorityImages);
        mPrioritySpinner.setAdapter(adapter);

        mPrioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.setSelectedText(priorityLevels[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void createNewIssue() {

        hideSoftKeyboard();

        if (mAssignToProject.getText().toString().equals("")) {
            mAssignToProject.setError(getString(R.string.select_a_project));
        } else if (mSummary.getText().toString().equals("")) {
            mSummary.setError(getString(R.string.required));
        } else {
            showProgressBar();

//             Find the Project id, by checking if project assigned matches one of projects docs in db and get the project id of project
            String temp = "";
            for (Project project : mProjects) {
                if (project.getName().equals(mAssignToProject.getText().toString())) {
                    temp = project.getProject_id();
                    break;
                }
            }
            final String projectId = temp;

            if (projectId.equals("")) {
                Toast.makeText(this, "select a valid project", Toast.LENGTH_SHORT).show();
                mAssignToProject.setError(getString(R.string.select_a_project));
                hideProgressBar();
            } else {
                // get the document reference
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                DocumentReference newIssueRef = db
                        .collection(getString(R.string.collection_projects))
                        .document(projectId)
                        .collection(getString(R.string.collection_issues))
                        .document();

                // Get user id of issue reporter or creator
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Create a new id for the issue
                final String issueId = newIssueRef.getId();

                // Create the issue and add send to database
                Issue issue = new Issue();
                issue.setAssignee("none");
                issue.setDescription(mDescription.getText().toString());
                issue.setIssue_id(issueId);
                issue.setIssue_type(((SpinnerAdapter) mIssueTypeSpinner.getAdapter()).getSelectedText());
                issue.setPriority(Issue.getPriorityInteger(((SpinnerAdapter) mPrioritySpinner.getAdapter()).getSelectedText()));
                issue.setReporter(userId);
                issue.setStatus(Issue.IDLE);
                issue.setSummary(mSummary.getText().toString());
                issue.setProject_id(projectId);

                newIssueRef.set(issue).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressBar();
                        Intent intent = new Intent();
                        intent.putExtra(getString(R.string.intent_snackbar_message), getString(R.string.created_new_issue));
                        setResult(ResultCodes.SNACKBAR_RESULT_CODE, intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBar();
                        Snackbar.make(getCurrentFocus().getRootView(), getString(R.string.failed_to_create_new_issue), Snackbar.LENGTH_LONG).show();
                    }
                });
            }


        }
    }


    private void showProgressBar() {
        if (mProgressBar != null) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (mProgressBar != null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.create: {
                createNewIssue();
                break;
            }

            case R.id.close: {
                finish();
                break;
            }
        }
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
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.assign_to_project: {
                // case shows keyboard and shows suggestions in autoTv without user entering text
                showSoftKeyboard(v);
                mAssignToProject.showDropDown();
                return true;
            }

        }
        return false;
    }
}





















