package com.parse.mighty.classes;

import java.io.Serializable;

public class Set implements Serializable {
    int reps;
    int load;
    String loadType;
    int completed;

    public Set (int reps, int load, String loadType) {
        this.reps = reps;
        this.load = load;
        this.loadType = loadType;
        this.completed = -1;
    }

    public int getReps() {
        return reps;
    }
    public int getLoad() {
        return load;
    }
    public String getLoadType() { return loadType; };

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }


    @Override
    public String toString() {
        return reps + " x " + load;
    }

}
