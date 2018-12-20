package com.parse.mighty;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SearchExerciseActivity extends AppCompatActivity {
    ListView resultsListView;
    ListView tabbedResultsListView;
    EditText searchEditText;
    ArrayList<Exercise> searchResults = new ArrayList<>();
    ArrayList<Exercise> recentResults = new ArrayList<>();
    ArrayList<Exercise> favouriteResults = new ArrayList<>();
    ExerciseSearchResultAdapter searchAdapter;
    ExerciseSearchResultAdapter tabbedAdapter;
    TextView recentTextView;
    TextView favouritesTextView;
    ImageView favouritesUnderline;
    ImageView recentUnderline;
    SQLiteDatabase logDatabase;
    DecimalFormat df = new DecimalFormat("###.##");


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
        tabbedAdapter = new ExerciseSearchResultAdapter(getApplicationContext(), favouriteResults);
        tabbedResultsListView.setAdapter(tabbedAdapter);
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
        Log.i("RECENT TAB", String.valueOf(recentResults.size()));
        tabbedAdapter = new ExerciseSearchResultAdapter(getApplicationContext(), recentResults);
        tabbedResultsListView.setAdapter(tabbedAdapter);
    }


    public String getDetails(JSONArray sets ) {
        try {
            String details = sets.length() + "x" + sets.getJSONObject(0).getInt("reps") + " @ ";
            if (sets.getJSONObject(0).has("load")) {
                details += df.format(sets.getJSONObject(0).getDouble("load"));
                details += " " + sets.getJSONObject(0).getString("type");
            }
            return details;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public void getRecent() {
        try {
            Cursor c = logDatabase.rawQuery("SELECT * FROM logs ORDER BY date DESC LIMIT 10", null);
            if (c != null) {
                c.moveToFirst();
                int nameIndex = c.getColumnIndex("name");
                int logIndex = c.getColumnIndex("log");
                Log.i("GETTING RECENT", "hi");

                while (c != null) {
                    Log.i("EXERCISE NAME ", c.getString(nameIndex));
                    String name = c.getString(nameIndex);
                    String JSON = c.getString(logIndex);
                    JSONObject logObj = new JSONObject(JSON);
                    JSONArray sets = logObj.getJSONArray("sets");

                    int id = getResources().getIdentifier(logObj.getString("equipment").toLowerCase(), "drawable", getPackageName());
                    Exercise ex = new Exercise(name, id, getDetails(sets));
                    recentResults.add(ex);
                    Log.i("numm", "hi");

                    c.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getSearch(String searchTerm) {

        if (searchTerm == "" || searchTerm.length() == 0 || searchTerm.matches("")) {
            resultsListView.setAdapter(searchAdapter);
            searchResults.clear();
            searchAdapter.notifyDataSetChanged();
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
                    searchAdapter.notifyDataSetChanged();
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
        final ConstraintLayout mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);
        final ConstraintLayout searchLayout = (ConstraintLayout) findViewById(R.id.searchLayout);

        ImageView backButton = (ImageView) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        date = intent.getLongExtra("date", 0);

        resultsListView = (ListView) findViewById(R.id.resultsListView);
        tabbedResultsListView = (ListView) findViewById(R.id.tabbedResultsListView);
        searchAdapter = new ExerciseSearchResultAdapter(getApplicationContext(), searchResults);
        tabbedAdapter = new ExerciseSearchResultAdapter(getApplicationContext(), recentResults);
        resultsListView.setAdapter(searchAdapter);
        tabbedResultsListView.setAdapter(tabbedAdapter);

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
                if (s.toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "EMPTY", Toast.LENGTH_SHORT).show();
                    searchResults.clear();
                    searchAdapter.notifyDataSetChanged();
                } else {
                    getSearch(s.toString());

                }
            }
        });
        final ConstraintLayout searchBarLayout = (ConstraintLayout) findViewById(R.id.searchBarLayout);

        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    searchBarLayout.animate().y(20).setDuration(200);
                    ImageView titleBar = (ImageView) findViewById(R.id.titleBarColour);
                    titleBar.animate().y(-100).setDuration(200);
                    ImageView searchImageView = (ImageView) findViewById(R.id.searchImageView);
                    searchImageView.setImageResource(R.drawable.arrow_back_nav_black);
                    mainLayout.setVisibility(View.INVISIBLE);
                    searchLayout.setVisibility(View.VISIBLE);
                    searchImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            searchBarLayout.animate().y(110).setDuration(200);
                            ImageView titleBar = (ImageView) findViewById(R.id.titleBarColour);
                            titleBar.animate().y(0).setDuration(200);
                            ImageView searchImageView = (ImageView) findViewById(R.id.searchImageView);
                            searchImageView.setImageResource(R.drawable.search_black);
                            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            mainLayout.setVisibility(View.VISIBLE);
                            searchLayout.setVisibility(View.INVISIBLE);
                            searchEditText.clearFocus();
                        }

                    });
                }
            }
        });


        tabbedResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), ExerciseDetailActivity.class);

                // TODO: fix when favourites
                intent.putExtra("name", recentResults.get(position).getName());

                intent.putExtra("date", date);
                startActivity(intent);
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
