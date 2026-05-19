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
    public boolean hasTrailer;
    public String trailerRegistration;
    public boolean noLicencePlate;
    public String contactPhone;
    public String contactEmail;

    // Insurance Company Info
    public String insuranceName;
    public String policyNumber;
    public String greenCardNumber;
    public String insuranceAgency;
    public String insuranceContact;
    public boolean coversDamage;

    // Driver Info
    public String driverName;
    public String driverFirstName;
    public String driverLastName;
    public String driverDob;
    public String driverStreet;
    public String driverHouseNumber;
    public String driverApartment;
    public String driverCity;
    public String driverPostalCode;
    public String driverCountry;
    public String driverPersonalId;
    public String driverEmail;
    public String driverContact;
    public String licenseNumber;
    public String licenseCategory;
    public String licenseExpiry;
    public Driver driver;

    // Circumstances (per vehicle)
    public boolean isParkedStopped;
    public boolean isLeavingParking;
    public boolean isReversing;
    public boolean isStopping;
    public boolean isStartingOff;
    public boolean isOpeningDoor;
    public boolean isEnteringParking;
    public boolean isEnteringRoundabout;
    public boolean isCirculatingRoundabout;
    public boolean isRearEndSameDirection;
    public boolean isChangingLanes;
    public boolean isOvertaking;
    public boolean isTurningRight;
    public boolean isTurningLeft;
    public boolean isEnteringOppositelane;
    public boolean isFromRightAtIntersection;
    public boolean isFailedToPrioritize;

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
        if (driverName != null) {
            sb.append(driverName);
        } else {
            sb.append(driverFirstName != null ? driverFirstName : "-");
            sb.append(" ");
            sb.append(driverLastName != null ? driverLastName : "-");
        }
        return sb.toString();
    }
}
