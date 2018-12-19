package com.parse.mighty;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.mighty.classes.Exercise;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchExerciseActivity extends AppCompatActivity {
    ListView resultsListView;
    EditText searchEditText;
    ArrayList<Exercise> exercises = new ArrayList<>();
    ExerciseSearchResultAdapter adapter;
    long date;


    public void getFavourites(View view) {

    }

    public void getRecent(View view) {

    }


    public void getSearch(String searchTerm) {


        if (searchTerm == "" || searchTerm.length() == 0) {
            resultsListView.setAdapter(adapter);
        } else {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Exercise");
            query.whereMatches("name", searchTerm, "i");
            Log.i("Search", searchTerm);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null && objects.size() > 0) {
                        exercises.clear();
                        for (ParseObject object: objects) {
                            int id = getResources().getIdentifier(object.getString("target").toLowerCase(), "drawable", getPackageName());
                            Exercise ex = new Exercise(object.getString("name"), id);
                            exercises.add(ex);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_exercise);

        Intent intent = getIntent();
        date = intent.getLongExtra("date", 0);

        resultsListView = (ListView) findViewById(R.id.resultsListView);
        adapter = new ExerciseSearchResultAdapter(getApplicationContext(), exercises);
        resultsListView.setAdapter(adapter);

        searchEditText = (EditText) findViewById(R.id.searchEditText);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getSearch(s.toString());
            }
        });
        final ConstraintLayout searchBarLayout = (ConstraintLayout) findViewById(R.id.searchBarLayout);

        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                searchBarLayout.animate().y(20).setDuration(200);
                ImageView titleBar = (ImageView) findViewById(R.id.titleBarColour);
                titleBar.animate().y(-100).setDuration(200);
                ImageView searchImageView = (ImageView) findViewById(R.id.searchImageView);
                searchImageView.setImageResource(R.drawable.arrow_back_nav_black);

            }
        });




        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), ExerciseDetailActivity.class);
                intent.putExtra("name", exercises.get(position).getName());
                intent.putExtra("date", date);
                startActivity(intent);
            }
        });


    }
}
