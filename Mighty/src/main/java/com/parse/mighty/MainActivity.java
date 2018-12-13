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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

    ArrayList<ExerciseLog> workout = new ArrayList<>();
    LinearLayout workoutLinearLayout;
    SQLiteDatabase logDatabase;

    public void addExercise(View view) {
        Intent intent = new Intent(getApplicationContext(), SearchExerciseActivity.class);
        startActivity(intent);
    }

    public ArrayList<ExerciseLog> fakeData() {
        ArrayList<ExerciseLog> workout = new ArrayList<>();

        ExerciseLog bench = new ExerciseLog("SCk4a0I73a", "Barbell Bench Press");
        bench.addSets(10,2, 80);
        workout.add(bench);

        ExerciseLog inclineBench = new ExerciseLog("EWQJnGKYbj", "Incline Barbell Bench Press");
        inclineBench.addSets(2,12, 0);
        workout.add(inclineBench);

        ExerciseLog tricep = new ExerciseLog("W19nsk3ssd", "Straight Bar Tricep Pushdown");
        tricep.addSets(1,20, 0);
        workout.add(tricep);

        return workout;
    }


    public void addLogView(final String name, String JSON, int count, int id) {
        try {
            JSONObject logObj = new JSONObject(JSON);
            String exId = logObj.getString("id");
            JSONArray sets = logObj.getJSONArray("sets");

            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            final View convertView = inflater.inflate(R.layout.log_listview, null);
            final TextView logTextView = (TextView) convertView.findViewById(R.id.exerciseTextView);
            final TextView letterTextView = (TextView) convertView.findViewById(R.id.letterTextView);
            final TextView detailsTextView = (TextView) convertView.findViewById(R.id.detailsTextView);

            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Exercise");
            query.getInBackground(exId, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null && object != null) {
                        logTextView.setText(object.getString("name"));
                        Log.i("Success", name);
                    }
                }
            });

            letterTextView.setText(String.valueOf((char) (65+count)));
            // TODO: fix hardcoding rep from first set
            detailsTextView.setText(sets.length() + " sets • " + sets.getJSONObject(0).getInt("reps") + " reps ");

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
//                    TextView letterTextView = (TextView) v.findViewById(R.id.letterTextView);
                    Log.i("Long click!", v.getTag().toString());
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Are you sure you want to delete?")
                            .setMessage("This will delete all sets for this exercise!")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    logDatabase.execSQL("DELETE FROM logs WHERE id = " + v.getTag().toString());
                                    workoutLinearLayout.removeView(v);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();


                    return true;
                }
            });
            convertView.setTag(id);
            workoutLinearLayout.addView(convertView);

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
                addLogView(name, logJSONString, count, id);

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