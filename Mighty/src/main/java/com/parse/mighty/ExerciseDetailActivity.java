package com.parse.mighty;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.mighty.classes.Exercise;
import com.parse.mighty.classes.ExerciseLog;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ExerciseDetailActivity extends AppCompatActivity {

    SQLiteDatabase logDatabase;


    Exercise ex;
    String exName;
    String exId;
    long date;

    LinearLayout detailsLayout;

    public int getInt(TextView t) {
        int ret = 0;
        if (!t.getText().toString().matches("")) {
            ret = Integer.parseInt(t.getText().toString());
        }
        return ret;
    }

    public double getDouble(TextView t) {
        double ret = -1;
        if (!t.getText().toString().matches("")) {
            ret = Double.valueOf(t.getText().toString());
        }
        return ret;
    }


    public void addExercise (View view) {
        EditText setsText = (EditText) findViewById(R.id.setsEditText);
        EditText repsText = (EditText) findViewById(R.id.repsEditText);
        EditText loadText = (EditText) findViewById(R.id.loadEditText);
        TextView loadType = (TextView) findViewById(R.id.loadTypeSelector);
        Log.i("empty", String.valueOf(setsText.getText().toString().trim().length()));
//            Toast.makeText(this, "You must enter a value for sets and reps", Toast.LENGTH_SHORT).show();
//        } else {
        ExerciseLog log = new ExerciseLog(exId, exName, ex.getClassification(), ex.getEquipment(), ex.getTarget());
        if (!(setsText.getText().toString().trim().length() == 0 || repsText.getText().toString().trim().length() == 0)) {

            log.addSets(getInt(setsText), getInt(repsText), getDouble(loadText), loadType.getText().toString());
        }
            try {
                Log.i("JSON", log.toJSONString());
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("date", date);
                intent.putExtra("log", log.toJSONString());
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
//        }

    }

    public static String getFormattedDate(Date date){
        Calendar cal=Calendar.getInstance();
        cal.setTime(date);
        //2nd of march 2015
        int day=cal.get(Calendar.DATE);

        if(!((day>10) && (day<19)))
            switch (day % 10) {
                case 1:
                    return new SimpleDateFormat("d'st' 'of' MMMM yyyy").format(date);
                case 2:
                    return new SimpleDateFormat("d'nd' 'of' MMMM yyyy").format(date);
                case 3:
                    return new SimpleDateFormat("d'rd' 'of' MMMM yyyy").format(date);
                default:
                    return new SimpleDateFormat("d'th' 'of' MMMM yyyy").format(date);
            }
        return new SimpleDateFormat("d'th' 'of' MMMM yyyy").format(date);
    }

    public void addHistory(Date date, JSONObject log) {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final View historyView = inflater.inflate(R.layout.history_listview, null);
        final TextView dayOfWeekTextView = (TextView) historyView.findViewById(R.id.dayOfWeek);
        final TextView dateTextView = (TextView) historyView.findViewById(R.id.date);
        final TextView setsTextView = (TextView) historyView.findViewById(R.id.sets);
        final TextView repsTextView = (TextView) historyView.findViewById(R.id.reps);

        try {
            JSONArray sets = log.getJSONArray("sets");
            dayOfWeekTextView.setText(new SimpleDateFormat("EEEE").format(date));
            dateTextView.setText(getFormattedDate(date));

            int totalReps = 0, totalCompleted = 0, count = 0;


            for (int i = 0; i < sets.length(); i++) {
                JSONObject set = sets.getJSONObject(i);
                if(set.has("completed") && set.getDouble("completed") > 0) {
                    totalReps += set.getInt("reps");
                    totalCompleted += set.getDouble("completed");
                    count++;
                }
            }
            if (count >0) {
                setsTextView.setText(count + " sets");

                repsTextView.setText(String.valueOf(totalReps/count) + " reps / " + String.valueOf(totalCompleted/count) + getResources().getString(R.string.load_measurement));

                detailsLayout.addView(historyView);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    public void getHistory() {

        try {
            Cursor c = logDatabase.rawQuery("SELECT * FROM logs WHERE name ='" + exName + "' ORDER BY date DESC", null);
            c.moveToFirst();

            int dateIndex = c.getColumnIndex("date");
            int nameIndex = c.getColumnIndex("name");
            int idIndex = c.getColumnIndex("id");
            int logIndex = c.getColumnIndex("log");
            while (c != null) {
                String name = c.getString(nameIndex);
                String logJSONString = c.getString(logIndex);
                Date date = new Date(c.getLong(dateIndex));

                JSONObject json = new JSONObject(logJSONString);

                Log.i("DATE", getFormattedDate(date));

                Log.i("LOG", json.toString());

                int logId = c.getInt(idIndex);

                addHistory(date, json);
                c.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);
        
        detailsLayout = (LinearLayout) findViewById(R.id.exerciseDetailsLayout);


        logDatabase = this.openOrCreateDatabase("Logs", MODE_PRIVATE, null);


        Intent intent = getIntent();

        exName = intent.getStringExtra("name");
        date = intent.getLongExtra("date", 0);
        getHistory();

        TextView exerciseNameTextView = (TextView) findViewById(R.id.exerciseNameTextView);
        exerciseNameTextView.setText(exName);
        final TextView loadSelector = (TextView) findViewById(R.id.loadTypeSelector);

        // GET EXERCISE DETAILS
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Exercise");
        query.whereMatches("name", exName);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null && object != null) {
                    int id = getResources().getIdentifier(object.getString("target").toLowerCase(), "drawable", getPackageName());
                    ex = new Exercise(object.getString("name"), id, object.getString("classification"), object.getString("equipment"));
                    exId = object.getObjectId();
                }
            }
        });


        ImageView backButton = (ImageView) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Spinner dynamicSpinner = (Spinner) findViewById(R.id.dynamic_spinner);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.load_types,
                        R.layout.dropdown_spinner);


        adapter
                .setDropDownViewResource(R.layout.dropdown_item);

        dynamicSpinner.setAdapter(adapter);

        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                loadSelector.setText(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });


    }
}
