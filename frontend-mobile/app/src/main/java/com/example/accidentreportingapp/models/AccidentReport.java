package com.example.accidentreportingapp.models;

import java.io.Serializable;
import java.util.Date;

/**
 * The 'Model' in our MVC architecture.
 * This class represents the data structure of an accident report.
 * Implements Serializable to allow passing objects between Activities.
 */
public class AccidentReport implements Serializable {
    private String id;
    private long timestamp;
    private String location;
    private String description;
    private String plateNumber;
    private boolean isDraft;

    /**
     * Default constructor for Firebase/GSON or manual creation.
     * Initializes timestamp to current time and sets status as draft.
     */
    public AccidentReport() {
        this.timestamp = new Date().getTime();
        this.isDraft = true;
    }

    /**
     * Convenience constructor for quick report creation.
     * @param location Geographical location or address.
     * @param plateNumber License plate of the involved vehicle.
     */
    public AccidentReport(String location, String plateNumber) {
        this();
        this.location = location;
        this.plateNumber = plateNumber;
    }

    /**
     * Full constructor for AccidentReport.
     * @param id Unique identifier for the report.
     * @param timestamp Time of the accident in milliseconds.
     * @param location Geographical location or address.
     * @param description Details about the accident.
     * @param plateNumber License plate of the involved vehicle.
     * @param isDraft Whether the report is currently a draft.
     */
    public AccidentReport(String id, long timestamp, String location, String description, String plateNumber, boolean isDraft) {
        this.id = id;
        this.timestamp = timestamp;
        this.location = location;
        this.description = description;
        this.plateNumber = plateNumber;
        this.isDraft = isDraft;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean draft) {
        isDraft = draft;
    }
}
