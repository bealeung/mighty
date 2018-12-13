package com.parse.mighty;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ExerciseLogView extends LinearLayout {

    public ExerciseLogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExerciseLogView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.log_listview, this);
        // setup all your Views from here with calls to getViewById(...);
    }

}