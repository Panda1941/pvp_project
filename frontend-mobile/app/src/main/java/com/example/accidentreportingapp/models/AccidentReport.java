package com.example.accidentreportingapp.models;

import java.io.Serializable;
import java.util.Date;

/**
 * The 'Model' in our MVC architecture.
 * This class represents the full data structure of an accident report.
 * Implements Serializable to allow passing objects between Activities.
 */
public class AccidentReport implements Serializable {
    
    // Status constants
    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_ISSUE = "ISSUE";

    private String id;
    private long timestamp;
    private String location;
    private String description;
    private boolean isDraft;
    private String status; // Current status of the report

    // The two parties involved in the accident
    private VehicleSection vehicleA;
    private VehicleSection vehicleB;

    /**
     * Default constructor for Firebase/GSON or manual creation.
     */
    public AccidentReport() {
        this.timestamp = new Date().getTime();
        this.isDraft = true;
        this.status = STATUS_WAITING; // Default status
        this.vehicleA = new VehicleSection();
        this.vehicleB = new VehicleSection();
    }

    /**
     * Convenience constructor for legacy support/quick creation.
     */
    public AccidentReport(String location, String plateA) {
        this();
        this.location = location;
        this.vehicleA.vehicleRegistration = plateA;
    }

    // Getters and Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isDraft() { return isDraft; }
    public void setDraft(boolean draft) { isDraft = draft; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public VehicleSection getVehicleA() { return vehicleA; }
    public void setVehicleA(VehicleSection vehicleA) { this.vehicleA = vehicleA; }

    public VehicleSection getVehicleB() { return vehicleB; }
    public void setVehicleB(VehicleSection vehicleB) { this.vehicleB = vehicleB; }
}
