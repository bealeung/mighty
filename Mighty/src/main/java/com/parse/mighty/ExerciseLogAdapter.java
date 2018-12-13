package com.parse.mighty;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.mighty.classes.ExerciseLog;

import java.util.ArrayList;

public class ExerciseLogAdapter extends BaseAdapter {
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    Context context;
    ArrayList<ExerciseLog> logs;
    LayoutInflater inflater;

    public ExerciseLogAdapter(Context applicationContext, ArrayList<ExerciseLog> logs) {
        this.context = context;
        this.logs = logs;
        inflater = (LayoutInflater.from(applicationContext));
    }


    @Override
    public int getCount() {
        return logs.size();
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.log_listview, null);
        final TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        final TextView letterTextView = (TextView) convertView.findViewById(R.id.letterTextView);
        ExerciseLog log = logs.get(position);
        Log.i("exercise id", log.getId());
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Exercise");
        try {

            query.getInBackground(log.getId(), new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null && object != null) {
                        String name = object.getString("name");
                        char letter = (char) (64 + position);
                        nameTextView.setText(name);
                        letterTextView.setText(letter);
                        Log.i("Info", name + " " + letter);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


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
