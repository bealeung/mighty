package com.parse.mighty;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
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
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.mighty.classes.Exercise;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchExerciseActivity extends AppCompatActivity {
    ListView resultsListView;
    EditText searchEditText;
    ArrayList<Exercise> searchResults = new ArrayList<>();
    ArrayList<Exercise> recentResults = new ArrayList<>();
    ArrayList<Exercise> favouriteResults = new ArrayList<>();
    ExerciseSearchResultAdapter adapter;
    TextView recentTextView;
    TextView favouritesTextView;
    ImageView favouritesUnderline;
    ImageView recentUnderline;
    SQLiteDatabase logDatabase;

    long date;
    String currTab;



    public void getFavourites(View view) {
        if (currTab == "favourites") {
            return;
        }
        favouritesTextView.setAlpha(1);
        recentTextView.setAlpha((float)0.3);
        favouritesUnderline.setVisibility(View.VISIBLE);
        recentUnderline.setVisibility(View.INVISIBLE);
        searchResults.clear();
        searchResults.addAll(favouriteResults);
        adapter.notifyDataSetChanged();
        currTab = "favourites";
    }

    public void recentTab(View view) {
        if (currTab == "recent") {
            return;
        }
        recentTextView.setAlpha(1);
        favouritesTextView.setAlpha((float) 0.3);
        recentUnderline.setVisibility(View.VISIBLE);
        favouritesUnderline.setVisibility(View.INVISIBLE);
        currTab = "recent";
        searchResults.clear();
        searchResults.addAll(recentResults);
        adapter.notifyDataSetChanged();
    }

    public void getRecent() {
        try {
            Cursor c = logDatabase.rawQuery("SELECT * FROM logs ORDER BY date DESC LIMIT 2", null);
            if (c != null) {
                c.moveToFirst();
                int nameIndex = c.getColumnIndex("name");

                while (c != null) {
                    Log.i("EXERCISE NAME ", c.getString(nameIndex));
                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Exercise");
                    query.whereMatches("name", c.getString(nameIndex), "i");
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null && objects.size() >0) {
                                ParseObject object = objects.get(0);
                                String classification = object.getString("classification");
                                int id = getResources().getIdentifier(object.getString("equipment").toLowerCase(), "drawable", getPackageName());
                                Exercise ex = new Exercise(object.getString("name"), id, classification);
                                recentResults.add(ex);
                                searchResults.add(ex);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                    c.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        searchResults.clear();
                        for (ParseObject object: objects) {
                            String classification = object.getString("classification");
                            int id = getResources().getIdentifier(object.getString("target").toLowerCase(), "drawable", getPackageName());
                            Exercise ex = new Exercise(object.getString("name"), id, classification);
                            searchResults.add(ex);
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

        logDatabase = this.openOrCreateDatabase("Logs", MODE_PRIVATE, null);

        getRecent();

        setContentView(R.layout.activity_search_exercise);

        Intent intent = getIntent();
        date = intent.getLongExtra("date", 0);

        resultsListView = (ListView) findViewById(R.id.resultsListView);
        adapter = new ExerciseSearchResultAdapter(getApplicationContext(), searchResults);
        resultsListView.setAdapter(adapter);

        favouritesTextView = (TextView) findViewById(R.id.favouritesTextView);
        recentTextView = (TextView) findViewById(R.id.recentTextView);
        favouritesUnderline = (ImageView) findViewById(R.id.favouritesUnderline);
        recentUnderline = (ImageView) findViewById(R.id.recentUnderline);



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
                intent.putExtra("name", searchResults.get(position).getName());
                intent.putExtra("date", date);
                startActivity(intent);
            }
        });


    }
}
