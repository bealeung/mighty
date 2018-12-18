/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.mighty;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    LinearLayout workoutLinearLayout;
    SQLiteDatabase logDatabase;
    JSONObject currLogs = new JSONObject();
    Date currDate;
    SimpleDateFormat curFormatter = new SimpleDateFormat("dd/MM/yyyy");

    public void prevDay (View v) {
        Calendar c = Calendar.getInstance();
        c.setTime(currDate);
        c.add(Calendar.DATE, -1);
        currDate = c.getTime();
        updateDateDisplay();
        populateWorkout();
    }

    public void nextDay (View v) {
        Calendar c = Calendar.getInstance();
        c.setTime(currDate);
        c.add(Calendar.DATE, 1);
        currDate = c.getTime();
        updateDateDisplay();
        populateWorkout();
    }


    public void showSets(View v) {
        String childId = v.getTag().toString();
        Log.i("Show sets with child ID", childId);
        View logView = workoutLinearLayout.getChildAt(Integer.valueOf(childId));
        LinearLayout logLinearLayout = (LinearLayout) logView.findViewById(R.id.logLinearLayout);
        ImageView showSetsButton = (ImageView) logView.findViewById(R.id.showSetsButton);

        Log.i("number children", String.valueOf(logLinearLayout.getChildCount()));
        int numChildren = logLinearLayout.getChildCount();
        final String id = logView.getTag().toString();
        if (numChildren == 1) {
            try {
                Cursor c = logDatabase.rawQuery("SELECT * FROM logs WHERE id =" + id, null);
                if (c != null) {
                    c.moveToFirst();
                    int logIndex = c.getColumnIndex("log");
                    String JSON = c.getString(logIndex);
                    final JSONObject logObj = new JSONObject(JSON);
                    String exId = logObj.getString("id");
                    final JSONArray sets = logObj.getJSONArray("sets");
                    Log.i("Number of sets", String.valueOf(sets.length()));

                    for (int i = 0; i < sets.length(); i++) {
                        final int setId = i;
                        final JSONObject currSet = sets.getJSONObject(i);

                        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                        final View setView = inflater.inflate(R.layout.set_listview, null);

                        final TextView repsTextView = (TextView) setView.findViewById(R.id.repsTextView);
                        final TextView loadTextView = (TextView) setView.findViewById(R.id.loadTextView);
                        final EditText weightEditText = (EditText) setView.findViewById(R.id.enterWeightEditText);
                        repsTextView.setText(String.valueOf(currSet.getInt("reps")) + " reps");
                        loadTextView.setText(String.valueOf(currSet.getInt("load")) + " %");
                        if (currSet.has("completed")) {
                            weightEditText.setText(String.valueOf(currSet.getInt("completed")));
                        }
                        weightEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                Log.i("log id", String.valueOf(id));
                                try {
                                    currSet.put("completed", s);
                                    sets.put(setId, currSet);
                                    logObj.put("sets", sets);
                                    ContentValues cv = new ContentValues();
                                    cv.put("log", logObj.toString());
                                    logDatabase.update("logs", cv, "id=" + id, null);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        setView.setTag(setId);
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
        intent.putExtra("date", currDate.getTime());
        startActivity(intent);
    }

    public void relabelExerciseOrder() {
        for (int i = 0; i < workoutLinearLayout.getChildCount(); i++) {
            View child = workoutLinearLayout.getChildAt(i);
            final TextView letterTextView = (TextView) child.findViewById(R.id.letterTextView);
            letterTextView.setText(String.valueOf((char) (65+i)));

        }
    }


    public void addLogView(final String name, String JSON, int logId) {
        try {
            JSONObject logObj = new JSONObject(JSON);
            currLogs.putOpt(String.valueOf(logId), logObj);
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

            letterTextView.setText(String.valueOf((char) (65+childId)));
            // TODO: fix hardcoding rep from first set
            detailsTextView.setText(sets.length() + " sets • " + sets.getJSONObject(0).getInt("reps") + " reps • " + sets.getJSONObject(0).getInt("load") + "%");
            View clayout = logView.findViewById(R.id.exerciseConstraintLayout);
            clayout.setTag(childId);
            clayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ExerciseDetailActivity.class);
                    intent.putExtra("name", name);
                    startActivity(intent);
                }
            });
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
            logView.setTag(logId);
            workoutLinearLayout.addView(logView);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void addLogToDatabase(String logJSONString) {
        try {
            JSONObject logJSON = new JSONObject(logJSONString);
            logDatabase.execSQL("INSERT INTO logs (date, name, log) VALUES ('" + currDate.getTime() + "', '" + logJSON.getString("name") + "', '" + logJSONString +"')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void populateWorkout() {

        // TODO: Change workout name at top
        workoutLinearLayout.removeAllViewsInLayout();
        Log.i("CALENDAR", String.valueOf(currDate));
        String startStr = currDate.getDate() + "/" + String.valueOf(currDate.getMonth()+1) + "/" + String.valueOf(currDate.getYear()+1900);
        String endStr = String.valueOf(currDate.getDate()+1) + "/" + String.valueOf(currDate.getMonth()+1) + "/" + String.valueOf(currDate.getYear()+1900);
        Log.i("START", startStr);
        Log.i("END", endStr);

        try {
            Date startObj = curFormatter.parse(startStr);
            Date endObj = curFormatter.parse(endStr);
            Log.i("PARSED", String.valueOf(startObj) + " " + String.valueOf(endObj));
            Cursor c = logDatabase.rawQuery("SELECT * FROM logs WHERE date > " + startObj.getTime() + " AND date < " + endObj.getTime(), null);
            int dateIndex = c.getColumnIndex("date");
            int nameIndex = c.getColumnIndex("name");
            int idIndex = c.getColumnIndex("id");
            int logIndex = c.getColumnIndex("log");

            c.moveToFirst();

            int count = 0;
            while (c != null) {
                Log.i("LOG DATE", c.getString(dateIndex));
                String name = c.getString(nameIndex);
                String logJSONString = c.getString(logIndex);
                int logId = c.getInt(idIndex);
                addLogView(name, logJSONString, logId);
                c.moveToNext();
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateDateDisplay() {
        int year = currDate.getYear()+1990;
        int month = currDate.getMonth()+1;
        int day = currDate.getDate();
        TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        dateTextView.setText(day + "/" + month);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logDatabase = this.openOrCreateDatabase("Logs", MODE_PRIVATE, null);


        Intent intent = getIntent();

        long date = intent.getLongExtra("date", 0);

        if (date != 0) {
            currDate = new Date(date);
        } else {
            currDate = Calendar.getInstance().getTime();
        }
        updateDateDisplay();

//        logDatabase.execSQL("DROP TABLE IF EXISTS logs");

        logDatabase.execSQL("CREATE TABLE IF NOT EXISTS logs (date INTEGER, name VARCHAR, log VARCHAR, id INTEGER PRIMARY KEY)");
        String newLog = intent.getStringExtra("log");
        if (newLog != null) {
            addLogToDatabase(newLog);
        }

        workoutLinearLayout = (LinearLayout) findViewById(R.id.workoutLinearLayout);

        populateWorkout();



        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

}