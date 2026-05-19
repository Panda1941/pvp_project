package com.example.accidentreportingapp.models;

import java.io.Serializable;

/**
 * Represents damage to a vehicle.
 */
public class Damage implements Serializable {
    private String area;
    private String severity;
    private int vehicleTarget; // 0 = A, 1 = B

    public Damage() {
    }

    public Damage(String area, String severity, int vehicleTarget) {
        this.area = area;
        this.severity = severity;
        this.vehicleTarget = vehicleTarget;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public int getVehicleTarget() {
        return vehicleTarget;
    }

    public void setVehicleTarget(int vehicleTarget) {
        this.vehicleTarget = vehicleTarget;
    }
}
