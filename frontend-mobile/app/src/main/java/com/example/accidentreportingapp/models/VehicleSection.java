package com.example.accidentreportingapp.models;

import java.io.Serializable;

/**
 * Represents one side of the accident (Vehicle A or Vehicle B).
 * Contains policy holder, vehicle, insurance, and driver information.
 */
public class VehicleSection implements Serializable {
    
    // Policy Holder / Insured
    public String insuredName;
    public String insuredAddress;
    public String insuredPostalCode;
    public String insuredCountry;
    public String insuredContact;

    // Vehicle Info
    public String vehicleMakeType;
    public String vehicleRegistration;
    public String vehicleCountry;

    // Insurance Company Info
    public String insuranceName;
    public String policyNumber;
    public String greenCardNumber;
    public String insuranceAgency;
    public String insuranceContact;
    public boolean coversDamage;

    // Driver Info
    public String driverName;
    public String driverDob;
    public String driverAddress;
    public String driverCountry;
    public String driverContact;
    public String licenseNumber;
    public String licenseCategory;
    public String licenseExpiry;

    // Circumstances (per vehicle)
    public boolean isParkedStopped;
    public boolean isLeavingParking;
    public boolean isReversing;

    public VehicleSection() {
    }
}
