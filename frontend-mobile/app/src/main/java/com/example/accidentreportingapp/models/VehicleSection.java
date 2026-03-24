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

    /*
     * NOTE: `VehicleSection` groups together all information related
     * to one side of the accident (insured, vehicle, insurance and driver).
     * The fields are public to keep the model lightweight and easy to use in
     * the demo UI code. If you need stronger guarantees (non-null fields,
     * validation, or immutability) consider adding getters/setters or moving
     * to an immutable/builder pattern.
     */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(insuredName != null ? insuredName : "-");
            sb.append(" |");
            sb.append(vehicleRegistration != null ? vehicleRegistration : "-");
            sb.append(" |");
            sb.append(driverName != null ? driverName : "-");
            return sb.toString();
        }
}
