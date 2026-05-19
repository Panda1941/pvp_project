package com.example.accidentreportingapp.models;

import com.example.accidentreportingapp.R;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;


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

    private List<Witness> witnesses;
    private List<Damage> damages;
    private String address;
    private Double latitude;
    private Double longitude;
    private String atFaultVehicle;
    private String signatureA;
    private String signatureB;

    /**
     * Default constructor for Firebase/GSON or manual creation.
     */
    public AccidentReport() {
        this.timestamp = new Date().getTime();
        this.isDraft = true;
        this.status = STATUS_WAITING; // Default status
        this.vehicleA = new VehicleSection();
        this.vehicleB = new VehicleSection();
        this.witnesses = new ArrayList<>();
        this.damages = new ArrayList<>();
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

    public void setTimestamp(String date, String time)
    {
        String dateTime = date + " " + time;

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date parsedDate = format.parse(dateTime);

            if (parsedDate != null) {
                this.timestamp = parsedDate.getTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTimestampAsDate()
    {
        long timestamp = this.timestamp;

        SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        String dateTime = format.format(new Date(timestamp));

        return dateTime;
    }

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

    public List<Witness> getWitnesses() {
        return witnesses;
    }

    public void setWitnesses(List<Witness> witnesses) {
        this.witnesses = witnesses;
    }

    public List<Damage> getDamages() {
        return damages;
    }

    public void setDamages(List<Damage> damages) {
        this.damages = damages;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAtFaultVehicle() {
        return atFaultVehicle;
    }

    public void setAtFaultVehicle(String atFaultVehicle) {
        this.atFaultVehicle = atFaultVehicle;
    }

    public String getSignatureA() {
        return signatureA;
    }

    public void setSignatureA(String signatureA) {
        this.signatureA = signatureA;
    }

    public String getSignatureB() {
        return signatureB;
    }

    public void setSignatureB(String signatureB) {
        this.signatureB = signatureB;
    }

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
