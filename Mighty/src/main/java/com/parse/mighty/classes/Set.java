package com.parse.mighty.classes;

import java.io.Serializable;

public class Set implements Serializable {
    int reps;
    int percentage;

    public Set (int reps, int percentage) {
        this.reps = reps;
        this.percentage = percentage;
    }

    public int getReps() {
        return reps;
    }
    public int getPercentage() {
        return percentage;
    }


    @Override
    public String toString() {
        return reps + " x " +percentage;
    }

}
