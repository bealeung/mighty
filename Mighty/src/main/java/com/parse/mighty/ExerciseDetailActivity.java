package com.parse.mighty;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.mighty.classes.Exercise;
import com.parse.mighty.classes.ExerciseLog;

import org.w3c.dom.Text;


public class ExerciseDetailActivity extends AppCompatActivity {

    Exercise ex;
    String exName;
    String exId;
    long date;

    public int getInt(TextView t) {
        int ret = 0;
        if (!t.getText().toString().matches("")) {
            ret = Integer.parseInt(t.getText().toString());
        }
        return ret;
    }


    public void addExercise (View view) {
        EditText setsText = (EditText) findViewById(R.id.setsEditText);
        EditText repsText = (EditText) findViewById(R.id.repsEditText);
        EditText percentageText = (EditText) findViewById(R.id.percentageEditText);
        Log.i("empty", String.valueOf(setsText.getText().toString().trim().length()));
        if (setsText.getText().toString().trim().length() == 0 || repsText.getText().toString().trim().length() == 0) {
            Toast.makeText(this, "You must enter a value for sets and reps", Toast.LENGTH_SHORT).show();
        } else {
            ExerciseLog log = new ExerciseLog(exId, exName, ex.getClassification(), ex.getEquipment(), ex.getTarget());
            log.addSets(getInt(setsText), getInt(repsText), getInt(percentageText));
            try {
                Log.i("JSON", log.toJSONString());
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("date", date);
                intent.putExtra("log", log.toJSONString());
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        Intent intent = getIntent();

        exName = intent.getStringExtra("name");
        date = intent.getLongExtra("date", 0);

        TextView exerciseNameTextView = (TextView) findViewById(R.id.exerciseNameTextView);
        exerciseNameTextView.setText(exName);

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


    }
}
