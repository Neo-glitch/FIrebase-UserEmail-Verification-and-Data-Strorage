package com.neo.firebaseuserandemailverification.issues;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.neo.firebaseuserandemailverification.R;
import com.neo.firebaseuserandemailverification.models.Issue;
import com.neo.firebaseuserandemailverification.models.Project;
import com.neo.firebaseuserandemailverification.utility.ResultCodes;

import java.util.ArrayList;


/**
 * Created by User on 4/16/2018.
 */

public class IssuesFragment extends Fragment implements
        View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener
{

    private static final String TAG = "IssuesFragment";

    //widgets
    private ImageView mAddIcon;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Spinner mProjectSpinner;
    public Toolbar mToolbar;


    //vars
    private IIssues mIIssues;
    private ArrayList<Issue> mIssues = new ArrayList<>();                                           // list of issues to be passed to IssuesRV adapter
    private ArrayList<Project> mProjects = new ArrayList<>();
    private IssuesRecyclerViewAdapter mIssuesRecyclerViewAdapter;
    private Project mSelectedProject;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_issues, container, false);
        mAddIcon = view.findViewById(R.id.add_new);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        mProjectSpinner = view.findViewById(R.id.project_spinner);
        mToolbar = view.findViewById(R.id.toolbar);

        mAddIcon.setOnClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        getProjects();

        return view;
    }

    public void updateProjectsList(ArrayList<Project> projects){

        if(mProjects != null){
            if(mProjects.size() > 0){
                mProjects.clear();
            }
        }

        if(projects != null){
            mProjects.addAll(projects);
            initProjectsSpinner();
            initRecyclerView();
        }
    }

    private void initRecyclerView(){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        int[] icons = {R.drawable.ic_task_blue, R.drawable.red_bug};
        mIssuesRecyclerViewAdapter = new IssuesRecyclerViewAdapter(getActivity(), mIssues, icons);
        mRecyclerView.setAdapter(mIssuesRecyclerViewAdapter);
    }

    /**
     * init the project spinner and queries db for issues associated with project
     */
    private void initProjectsSpinner(){

        String[] projects = new String[mProjects.size()];
        for(int i = 0; i < mProjects.size(); i++){
            projects[i] = mProjects.get(i).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, projects);
        mProjectSpinner.setAdapter(adapter);

        mProjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedProject = mProjects.get(i);
                getIssues();                            // queries db for issues collection
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(mProjects.size() > 0){
            mSelectedProject = mProjects.get(0);
        }
    }

    private void getIssues(){
        if(mSelectedProject != null){

            mIIssues.showProgressBar();

            if(mIssues != null){
                if(mIssues.size() > 0){
                    mIssues.clear();
                }
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection(getString(R.string.collection_projects))
                    .document(mSelectedProject.getProject_id())                                         // project id or doc of project assoc with issue
                    .collection(getString(R.string.collection_issues))
                    .orderBy(getString(R.string.field_priority), Query.Direction.DESCENDING)    // the desc order makes issue with highest priority top of list
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                    Issue issue = documentSnapshot.toObject(Issue.class);
                                    mIssues.add(issue);
                                }
                            }
                            else{
                                Toast.makeText(getActivity(), "error getting issues for that project", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onComplete: errors getting issues for project id: " + mSelectedProject.getProject_id());
                            }
                            mIIssues.hideProgressBar();
                            mIssuesRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    });
        }

    }

    private void getProjects(){
        mIIssues.getProjects();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.add_new:{
                //go to NewIssueActivity
                Intent intent = new Intent(getActivity(), NewIssueActivity.class);
                startActivityForResult(intent, ResultCodes.SNACKBAR_RESULT_CODE);
                break;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mIIssues = (IIssues) getActivity();
        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == ResultCodes.SNACKBAR_RESULT_CODE){
            Log.d(TAG, "onActivityResult: building snackbar message.");
            String message = data.getStringExtra(getString(R.string.intent_snackbar_message));
            mIIssues.buildSnackbar(message);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public void onRefresh() {
        getIssues();
        onItemsLoadComplete();
    }

    private void onItemsLoadComplete(){
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
















