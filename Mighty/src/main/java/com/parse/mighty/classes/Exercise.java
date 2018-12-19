package com.parse.mighty.classes;

import java.util.ArrayList;

public class Exercise {
    String id;
    String name;
    String target;
    int imageId;
    String classification;
    ArrayList<String> details;

    public Exercise(String name, int imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public Exercise(String name, int imageId, String classification) {
        this.name = name;
        this.imageId = imageId;
        this.classification = classification;
    }

    public Exercise(String id, String name, String target, int imageId, String classficiation, ArrayList<String> details) {
        this.id = id;
        this.name = name;
        this.target = target;
        this.imageId = imageId;
        this.classification = classification;
        this.details = details;
    }

    public String getName() {
        return name;
    }
    public int getImageId() {
        return imageId;
    }
    public String getDetails() { return classification; }

}
