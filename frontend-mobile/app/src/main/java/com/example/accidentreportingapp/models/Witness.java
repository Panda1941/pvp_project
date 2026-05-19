package com.example.accidentreportingapp.models;

import java.io.Serializable;

/**
 * Represents a witness to the accident.
 */
public class Witness implements Serializable {
    private String firstName;
    private String lastName;
    private String phone;

    public Witness() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
