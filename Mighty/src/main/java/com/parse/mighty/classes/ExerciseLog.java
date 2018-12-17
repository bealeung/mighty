package com.parse.mighty.classes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class ExerciseLog implements Serializable {
    String exerciseId;
    String exerciseName;
    ArrayList<Set> sets;

    public ExerciseLog (String id, String name) {
        this.exerciseId = id;
        this.exerciseName = name;
        sets = new ArrayList<>();
    }

    public void addSet(int reps, int percentage) {
        Set s = new Set(reps, percentage, "percentage");
        sets.add(s);
    }
    public void addSets (int num, int reps, int percentage) {
        int count = 0;
        while (count < num) {
            Set s = new Set(reps, percentage, "percentage");
            sets.add(s);
            count++;
        }
    }

    public String getId() {
        return exerciseId;
    }
    public String getName() {
        return exerciseName;
    }

    public int getNumSets() {
        return sets.size();
    }

    public ArrayList<Set> getSets() { return sets; }

    public String toJSONString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", exerciseId);
            obj.put("name", exerciseName);
            JSONArray sets = new JSONArray();
            for (Set s : this.sets) {
                JSONObject set = new JSONObject();
                set.put("reps", s.getReps());
                set.put("load", s.getLoad());
                if (s.getCompleted() != -1) {
                    set.put("completed", s.getCompleted());

                }
                sets.put(set);
            }
            obj.put("sets", sets);
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // TODO: Fix reps (hard coded to first number of reps ATM)
    public int getReps() {
        return sets.get(0).getReps();
    }

    @Override
    public String toString() {

        String ret = exerciseId + ": " + exerciseName + " - ";
        for (Set s: sets) {
            ret += s.getReps() + ", ";
        }
        return  ret;
    }
}
