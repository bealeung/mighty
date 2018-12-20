package com.parse.mighty.classes;

import java.util.ArrayList;

public class Exercise {
    String id;
    String name;
    String target;
    String equipment;
    int imageId;
    String classification;
    String details;

    public Exercise(String name, int imageId) {
        this.name = name;
        this.imageId = imageId;
    }
    public Exercise(String name, int imageId, String classification, String equipment) {
        this.name = name;
        this.imageId = imageId;
        this.classification = classification;
        this.equipment = equipment;
    }

    public Exercise(String name, int imageId, String details) {
        this.name = name;
        this.imageId = imageId;
        this.details = details;
    }

    public String getName() {
        return name;
    }
    public int getImageId() {
        return imageId;
    }
    public String getDetails() { return details; }
    public String getTarget() { return target; }
    public String getEquipment() { return equipment; }
    public String getClassification() { return classification; }

}
