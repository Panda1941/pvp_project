package com.example.accidentreportingapp.models;

import com.example.accidentreportingapp.R;
import java.io.Serializable;
import java.util.Date;

/**
 * The 'Model' in our MVC architecture.
 * This class represents the full data structure of an accident report.
 * Implements Serializable to allow passing objects between Activities.
 */
public class AccidentReport implements Serializable {
    
    // Status constants
    public static final int STATUS_WAITING = 0;
    public static final int STATUS_CONFIRMED = 1;
    public static final int STATUS_ISSUE = 2;

    private String id;
    private long timestamp;
    private String location;
    private String description;
    private boolean isDraft;
    private int status; // Current status of the report (int for localization support)

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

    /*
     * NOTE: `AccidentReport` is the main data model used across the
     * app to represent a submitted or draft report. It implements
     * `Serializable` for simplicity in this demo so objects can be passed
     * between Activities in `Intent` extras. In a production app you may
     * prefer `Parcelable` for better performance when passing many objects
     * or when optimizing memory/IPC behavior.
     *
     * Fields are intentionally public inside `VehicleSection` to keep the
     * model simple; consider adding validation or using builders if you need
     * stronger invariants later.
     */

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

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    /**
     * Returns the string resource ID corresponding to the current status.
     */
    public int getStatusResourceId() {
        switch (status) {
            case STATUS_CONFIRMED:
                return R.string.status_confirmed;
            case STATUS_ISSUE:
                return R.string.status_issue;
            case STATUS_WAITING:
            default:
                return R.string.status_waiting;
        }
    }

    public VehicleSection getVehicleA() { return vehicleA; }
    public void setVehicleA(VehicleSection vehicleA) { this.vehicleA = vehicleA; }

    public VehicleSection getVehicleB() { return vehicleB; }
    public void setVehicleB(VehicleSection vehicleB) { this.vehicleB = vehicleB; }

    @Override
    public String toString() {
        String aReg = (vehicleA != null && vehicleA.vehicleRegistration != null) ? vehicleA.vehicleRegistration : "-";
        String bReg = (vehicleB != null && vehicleB.vehicleRegistration != null) ? vehicleB.vehicleRegistration : "-";
        return "AccidentReport{" +
                "id='" + id + '\'' +
                ", ts=" + timestamp +
                ", loc='" + location + '\'' +
                ", status=" + status +
                ", A=" + aReg +
                ", B=" + bReg +
                '}';
    }
}
