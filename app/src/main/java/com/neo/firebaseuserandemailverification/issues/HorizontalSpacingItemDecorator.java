package com.neo.firebaseuserandemailverification.issues;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * class that helps in spacing horizontal rvView list items
 */
public class HorizontalSpacingItemDecorator extends RecyclerView.ItemDecoration {

    private final int horizontalSpacing;

    public HorizontalSpacingItemDecorator(int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.right = horizontalSpacing;
    }
}
