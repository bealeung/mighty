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
import android.widget.Toast;

import com.parse.ParseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    LinearLayout workoutLinearLayout;
    SQLiteDatabase logDatabase;
    SQLiteDatabase recordsDatabase;
    JSONObject currLogs = new JSONObject();
    Date currDate;
    SimpleDateFormat curFormatter = new SimpleDateFormat("dd/MM/yyyy");
    DecimalFormat df = new DecimalFormat("###.##");
    int[] repPercentageArray;



    // https://www.brianmac.co.uk/maxload.htm
    public double estimateRM (double weight, int reps) {
        return weight/( 1.0278 - ( 0.0278 * reps ) );
    }


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

    public int getRepMax(String name) {
        int repMax= 0;
        try {
            Cursor c = recordsDatabase.rawQuery("SELECT * FROM records WHERE exercise = '" + name + "' ORDER BY repMax DESC", null);
            if (c.moveToNext()) {
                int rmIndex = c.getColumnIndex("repMax");
                repMax = c.getInt(rmIndex);
                Log.i("REP MAX", String.valueOf(repMax));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return repMax;
    }


    public void showSets(View v, final String name) {
        final String childId = v.getTag().toString();
        Log.i("Show sets with child ID", childId);
        View logView = workoutLinearLayout.getChildAt(Integer.valueOf(childId));
        LinearLayout logLinearLayout = (LinearLayout) logView.findViewById(R.id.logLinearLayout);
        ImageView showSetsButton = (ImageView) logView.findViewById(R.id.showSetsButton);

        int repMax = getRepMax(name);

        Log.i("number children", String.valueOf(logLinearLayout.getChildCount()));
        int numChildren = logLinearLayout.getChildCount();
        final String id = logView.getTag().toString();
        if (numChildren == 1) {
            try {
                // GET CURR RECORD
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
                        final TextView idTextView = (TextView) setView.findViewById(R.id.childId);
                        idTextView.setText(String.valueOf(setId+1));
                        final TextView repsTextView = (TextView) setView.findViewById(R.id.repsTextView);
                        final TextView loadTextView = (TextView) setView.findViewById(R.id.loadTextView);
                        final EditText weightEditText = (EditText) setView.findViewById(R.id.enterWeightEditText);


                        final int reps = currSet.getInt("reps");
                        repsTextView.setText(String.valueOf(reps) + " reps");
                        loadTextView.setText(displaySetLoad(currSet));


                        String hint = "0";
                        if (currSet.has("load") && currSet.getDouble("load") != -1) {
                            String type = currSet.getString("type");
                            Double load = currSet.getDouble("load");
                            Log.i("HAS LOAD", type);

                            if (type.matches("kg")) {
                                hint = String.valueOf(df.format(load));
                            } else if (type.matches("%RM")) {
                                hint = String.valueOf(df.format(repMax*load/100));
                            } else if (type.matches("RM")) {
                                if (load <= 12) {
                                    double percentage = repPercentageArray[load.intValue()-1];
                                    hint = String.valueOf(df.format(repMax*percentage/100));
                                }
                            }
                            Log.i("HINT", hint);

                        } else {
                            if (reps <= 12) {
                                double percentage = repPercentageArray[reps-1];
                                hint = String.valueOf(df.format(repMax*percentage/100));
                            }
                        }
                        weightEditText.setHint(hint);


                        if (currSet.has("completed") && currSet.getDouble("completed") != -1) {
                            weightEditText.setText(String.valueOf(df.format(currSet.getDouble("completed"))));
                        }
                        weightEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean b) {
                                String weightValue = weightEditText.getText().toString();
                                if (!weightValue.matches("") && reps <= 12 && !b) {
                                    int logId = Integer.valueOf(childId);
                                    Double repMaxDouble = estimateRM(Double.valueOf(weightValue), reps );
                                    int currRepMax = repMaxDouble.intValue();
                                    Cursor c = logDatabase.rawQuery("SELECT * FROM logs WHERE id = " + id, null);
                                    if (c.moveToNext()) {
                                        int idIndex = c.getColumnIndex("recordId");
                                        int setIndex = c.getColumnIndex("recordSet");
                                        int recordId = c.getInt(idIndex);
                                        int recordSet = c.getInt(setIndex);
                                        if (recordId != -1 && setId == recordSet) {
                                            recordsDatabase.execSQL("DELETE FROM records WHERE id = " + recordId);
                                        }
                                    }
                                    if (currRepMax >= getRepMax(name)) {

                                        ContentValues values = new ContentValues();
                                        values.put("date",currDate.getTime());
                                        values.put("exercise",name);
                                        values.put("logId", logId);
                                        values.put("repMax", currRepMax);
                                        values.put("numReps", reps);
                                        int recordId = (int) recordsDatabase.insert("Records", null, values);
                                        ContentValues cv = new ContentValues();
                                        cv.put("recordId", recordId);
                                        cv.put("recordSet", setId);
                                        logDatabase.update("logs", cv, "id=" + id, null);
                                        Log.i("NEW RECORD LOG ID", String.valueOf(id));

                                        Log.i("REPMAX", String.valueOf(currRepMax));
                                    }
                                }
                            }
                        });
                        weightEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                                Log.i("log id", String.valueOf(id));
                                try {
                                    if (s.length() > 0) {
                                        currSet.put("completed", Double.valueOf(s.toString()));

                                    } else {
                                        currSet.put("completed", Double.valueOf(-1));
                                    }
                                    sets.put(setId, currSet);
                                    logObj.put("sets", sets);
                                    ContentValues cv = new ContentValues();
                                    cv.put("log", logObj.toString());
                                    logDatabase.update("logs", cv, "id=" + id, null);
                                    updateCompletion(Integer.parseInt(childId), isCompleted(sets));
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
                    showSetsButton.setImageResource(R.drawable.expand_less_red);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showSetsButton.setImageResource(R.drawable.expand_more_red);
            View child = workoutLinearLayout.getChildAt(Integer.parseInt(childId));
            TextView detailsTextView = child.findViewById(R.id.detailsTextView);
            detailsTextView.setVisibility(View.VISIBLE);
            logLinearLayout.removeViews(1, numChildren-1);

        }
    }

    public void updateCompletion(int childId, boolean completed) {
        View v = workoutLinearLayout.getChildAt(childId);
        ImageView circle = (ImageView) v.findViewById(R.id.setIcon);
        TextView letter = (TextView) v.findViewById(R.id.letterTextView);

        if (completed) {
            circle.setImageResource(R.drawable.circle_red);
            letter.setTextColor(getResources().getColor(R.color.white));
        } else {
            circle.setImageResource(R.drawable.circle_outline_red);
            letter.setTextColor(getResources().getColor(R.color.Main_Red));
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

    public String displaySetLoad(JSONObject currSet) {
        String ret = "";
        try {
            if (currSet.has("load")) {
                if (currSet.getDouble("load") == -1) {
                    ret = "-";
                } else {
                    ret = String.valueOf(df.format(currSet.getDouble("load")));
                    ret += " " + currSet.getString("type");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String getDetails(JSONArray sets ) {
        try {
            String details = sets.length() + "x" + sets.getJSONObject(0).getInt("reps");
            if (sets.getJSONObject(0).has("load") && sets.getJSONObject(0).getDouble("load") != -1) {
                details +=  " @ " + df.format(sets.getJSONObject(0).getDouble("load"));
                details += " " + sets.getJSONObject(0).getString("type");
            }
            return details;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public int getNumCompleted(JSONArray sets) {
        int num = 0;
        try {
            for (int i = 0; i < sets.length(); i++) {
                if (sets.getJSONObject(i).has("completed") && sets.getJSONObject(i).getDouble("completed") != -1 ) {
                    num++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    public boolean isCompleted(JSONArray sets) {
        if (sets.length() == getNumCompleted(sets)) {
            return true;
        }
        return false;
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

            // getCompleted


            showSets.setTag(childId);
            showSets.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detailsTextView.setVisibility(View.GONE);
                    showSets(v, name);
                }
            });

            logTextView.setText(name);

            letterTextView.setText(String.valueOf((char) (65+childId)));
            // TODO: fix hardcoding rep from first set
            detailsTextView.setText(getDetails(sets));
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
            updateCompletion(childId, isCompleted(sets));


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
        TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        int year = currDate.getYear();
        int month = currDate.getMonth()+1;
        int day = currDate.getDate();
        Date today = Calendar.getInstance().getTime();
        if (today.getYear() == year
                && today.getMonth() == month-1
                && today.getDate() == day) {
            dateTextView.setText("Today");
        } else {
            dateTextView.setText(day + "/" + month);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repPercentageArray = getResources().getIntArray(R.array.rep_percentage);
        logDatabase = this.openOrCreateDatabase("Logs", MODE_PRIVATE, null);
        recordsDatabase = this.openOrCreateDatabase("Records", MODE_PRIVATE, null);


        Intent intent = getIntent();

        long date = intent.getLongExtra("date", 0);

        if (date != 0) {
            currDate = new Date(date);
        } else {
            currDate = Calendar.getInstance().getTime();
        }
        updateDateDisplay();

//        logDatabase.execSQL("DROP TABLE IF EXISTS logs");
//        recordsDatabase.execSQL("DROP TABLE IF EXISTS records");
        logDatabase.execSQL("CREATE TABLE IF NOT EXISTS logs (date INTEGER, name VARCHAR, log VARCHAR, recordIdid INTEGER PRIMARY KEY)");

        recordsDatabase.execSQL("CREATE TABLE IF NOT EXISTS records (date INTEGER, exercise VARCHAR, logID INTEGER, repMax INTEGER, numReps INTEGER, id INTEGER PRIMARY KEY)");
//        logDatabase.execSQL("ALTER TABLE logs ADD COLUMN recordSet INTEGER DEFAULT -1");

        String newLog = intent.getStringExtra("log");
        if (newLog != null) {
            addLogToDatabase(newLog);
        }

        workoutLinearLayout = (LinearLayout) findViewById(R.id.workoutLinearLayout);

        populateWorkout();



        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

}