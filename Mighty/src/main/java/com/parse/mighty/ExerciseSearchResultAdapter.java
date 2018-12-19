package com.parse.mighty;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.mighty.classes.Exercise;

import java.util.ArrayList;

public class ExerciseSearchResultAdapter extends BaseAdapter {
    Context context;
    ArrayList<Exercise> exercises;
    LayoutInflater inflater;

    public ExerciseSearchResultAdapter(Context applicationContext, ArrayList<Exercise> exercises) {
        this.context = context;
        this.exercises = exercises;
        inflater = (LayoutInflater.from(applicationContext));
    }


    @Override
    public int getCount() {
        return exercises.size();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.search_listview, null);
        TextView name = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView details = (TextView) convertView.findViewById(R.id.detailsTextView);
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        Exercise ex = exercises.get(position);
        name.setText(ex.getName());
        details.setText(ex.getDetails());
        icon.setImageResource(ex.getImageId());
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }
}
