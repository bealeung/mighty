/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.mighty;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseAnalytics;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.mighty.classes.ExerciseLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    LinearLayout workoutLinearLayout;
    SQLiteDatabase logDatabase;

    public void showSets(View v) {
        String childId = v.getTag().toString();
        Log.i("Show sets with child ID", childId);
        View logView = workoutLinearLayout.getChildAt(Integer.valueOf(childId));
        LinearLayout logLinearLayout = (LinearLayout) logView.findViewById(R.id.logLinearLayout);
        ImageView showSetsButton = (ImageView) logView.findViewById(R.id.showSetsButton);

        Log.i("number children", String.valueOf(logLinearLayout.getChildCount()));
        int numChildren = logLinearLayout.getChildCount();
        String id = logView.getTag().toString();
        if (numChildren == 1) {
            try {
                Cursor c = logDatabase.rawQuery("SELECT * FROM logs WHERE id =" + id, null);
                if (c != null) {
                    c.moveToFirst();
                    int logIndex = c.getColumnIndex("log");
                    String JSON = c.getString(logIndex);
                    JSONObject logObj = new JSONObject(JSON);
                    String exId = logObj.getString("id");
                    JSONArray sets = logObj.getJSONArray("sets");
                    Log.i("Number of sets", String.valueOf(sets.length()));

                    for (int i = 0; i < sets.length(); i++) {
                        JSONObject currSet = sets.getJSONObject(0);

                        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                        final View setView = inflater.inflate(R.layout.set_listview, null);

                        final TextView repsTextView = (TextView) setView.findViewById(R.id.repsTextView);
                        final TextView percentageTextView = (TextView) setView.findViewById(R.id.percentageTextView);
                        repsTextView.setText(String.valueOf(currSet.getInt("reps")) + " reps");
                        percentageTextView.setText(String.valueOf(currSet.getInt("percentage")) + " %");
                        logLinearLayout.addView(setView);
                    }
                    showSetsButton.setImageResource(R.drawable.arrow_up_black);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showSetsButton.setImageResource(R.drawable.arrow_down_black);

            logLinearLayout.removeViews(1, numChildren-1);

        }
    }

    public void addExercise(View view) {
        Intent intent = new Intent(getApplicationContext(), SearchExerciseActivity.class);
        startActivity(intent);
    }

    public void relabelExerciseOrder() {
        for (int i = 0; i < workoutLinearLayout.getChildCount(); i++) {
            View child = workoutLinearLayout.getChildAt(i);
            final TextView letterTextView = (TextView) child.findViewById(R.id.letterTextView);
            letterTextView.setText(String.valueOf((char) (65+i)));

        }
    }


    public void addLogView(final String name, String JSON, int id) {
        try {
            JSONObject logObj = new JSONObject(JSON);
            String exId = logObj.getString("id");
            JSONArray sets = logObj.getJSONArray("sets");
            int childId = workoutLinearLayout.getChildCount();

            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            final View logView = inflater.inflate(R.layout.log_listview, null);
            final TextView logTextView = (TextView) logView.findViewById(R.id.exerciseTextView);
            final TextView letterTextView = (TextView) logView.findViewById(R.id.letterTextView);
            final TextView detailsTextView = (TextView) logView.findViewById(R.id.detailsTextView);

            final ImageView showSets = (ImageView) logView.findViewById(R.id.showSetsButton);
            showSets.setTag(childId);
            showSets.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSets(v);
                }
            });

            logTextView.setText(name);

//            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Exercise");
//            query.getInBackground(exId, new GetCallback<ParseObject>() {
//                @Override
//                public void done(ParseObject object, ParseException e) {
//                    if (e == null && object != null) {
//                        logTextView.setText(object.getString("name"));
//                        Log.i("Success", name);
//                    }
//                }
//            });

            letterTextView.setText(String.valueOf((char) (65+childId)));
            // TODO: fix hardcoding rep from first set
            detailsTextView.setText(sets.length() + " sets • " + sets.getJSONObject(0).getInt("reps") + " reps • " + sets.getJSONObject(0).getInt("percentage") + "%");
            View clayout = logView.findViewById(R.id.exerciseConstraintLayout);
            clayout.setTag(childId);
            clayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.i("Long click!", v.getTag().toString());
                    int childId = Integer.parseInt(v.getTag().toString());
                    final LinearLayout child = (LinearLayout) workoutLinearLayout.getChildAt(childId);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Are you sure you want to delete?")
                            .setMessage("This will delete all sets for this exercise!")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    logDatabase.execSQL("DELETE FROM logs WHERE id = " + child.getTag().toString());
                                    workoutLinearLayout.removeView(child);
                                    relabelExerciseOrder();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                }
            });
            logView.setTag(id);
            workoutLinearLayout.addView(logView);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void addLogToDatabase(String logJSONString) {
        String date = new Date().toString();
        try {
            JSONObject logJSON = new JSONObject(logJSONString);
            logDatabase.execSQL("INSERT INTO logs (date, name, log) VALUES ('" + date + "', '" + logJSON.getString("name") + "', '" + logJSONString +"')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void populateWorkout() {
        try {
            Cursor c = logDatabase.rawQuery("SELECT * FROM logs", null);

            int dateIndex = c.getColumnIndex("date");
            int nameIndex = c.getColumnIndex("name");
            int idIndex = c.getColumnIndex("id");
            int logIndex = c.getColumnIndex("log");

            c.moveToFirst();

            int count = 0;
            while (c != null) {
                String name = c.getString(nameIndex);
                String logJSONString = c.getString(logIndex);
                int id = c.getInt(idIndex);
                Log.i("Id", String.valueOf(id));
                Log.i("Name", name);
                Log.i("JSON", logJSONString);
                addLogView(name, logJSONString, id);

                c.moveToNext();
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         logDatabase = this.openOrCreateDatabase("Logs", MODE_PRIVATE, null);

//        logDatabase.execSQL("DROP TABLE IF EXISTS logs");

        logDatabase.execSQL("CREATE TABLE IF NOT EXISTS logs (date VARCHAR, name VARCHAR, log VARCHAR, id INTEGER PRIMARY KEY)");
        Intent intent = getIntent();
        String newLog = intent.getStringExtra("log");
        if (newLog != null) {
            addLogToDatabase(newLog);
        }

        workoutLinearLayout = (LinearLayout) findViewById(R.id.workoutLinearLayout);

        populateWorkout();



        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

}