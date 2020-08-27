package com.neo.firebaseuserandemailverification.issues;

import java.util.ArrayList;


/**
 * Created by User on 4/16/2018.
 */

public interface IIssues {

    void showProgressBar();

    void hideProgressBar();

    void buildSnackbar(String message);

    void getProjects();

}
