package accidentreporting.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Policy Holder / Insured
    private String insuredName;
    private String insuredAddress;
    private String insuredPostalCode;
    private String insuredCountry;
    private String insuredContact;

    // Vehicle Info
    private String vehicleMakeType;
    @Column(nullable = false)
    private String vehicleRegistration;
    private String vehicleCountry;

    // Insurance
    private String insuranceName;
    private String policyNumber;
    private String greenCardNumber;
    private String insuranceAgency;
    private String insuranceContact;
    private Boolean coversDamage;

    // Contact fields specifically for vehicle owner/driver
    private String contactPhone;
    private String contactEmail;

    // Trailer info
    private Boolean hasTrailer;
    private String trailerRegistration;

    // If there is no licence plate (e.g., temporary or missing)
    private Boolean noLicencePlate;

    // Circumstances
    private Boolean isParkedStopped;
    private Boolean isLeavingParking;
    private Boolean isReversing;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    public Vehicle() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInsuredName() { return insuredName; }
    public void setInsuredName(String insuredName) { this.insuredName = insuredName; }

    public String getInsuredAddress() { return insuredAddress; }
    public void setInsuredAddress(String insuredAddress) { this.insuredAddress = insuredAddress; }

    public String getInsuredPostalCode() { return insuredPostalCode; }
    public void setInsuredPostalCode(String insuredPostalCode) { this.insuredPostalCode = insuredPostalCode; }

    public String getInsuredCountry() { return insuredCountry; }
    public void setInsuredCountry(String insuredCountry) { this.insuredCountry = insuredCountry; }

    public String getInsuredContact() { return insuredContact; }
    public void setInsuredContact(String insuredContact) { this.insuredContact = insuredContact; }

    public String getVehicleMakeType() { return vehicleMakeType; }
    public void setVehicleMakeType(String vehicleMakeType) { this.vehicleMakeType = vehicleMakeType; }

    public String getVehicleRegistration() { return vehicleRegistration; }
    public void setVehicleRegistration(String vehicleRegistration) { this.vehicleRegistration = vehicleRegistration; }

    public String getVehicleCountry() { return vehicleCountry; }
    public void setVehicleCountry(String vehicleCountry) { this.vehicleCountry = vehicleCountry; }

    public String getInsuranceName() { return insuranceName; }
    public void setInsuranceName(String insuranceName) { this.insuranceName = insuranceName; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getGreenCardNumber() { return greenCardNumber; }
    public void setGreenCardNumber(String greenCardNumber) { this.greenCardNumber = greenCardNumber; }

    public String getInsuranceAgency() { return insuranceAgency; }
    public void setInsuranceAgency(String insuranceAgency) { this.insuranceAgency = insuranceAgency; }

    public String getInsuranceContact() { return insuranceContact; }
    public void setInsuranceContact(String insuranceContact) { this.insuranceContact = insuranceContact; }

    public Boolean getCoversDamage() { return coversDamage; }
    public void setCoversDamage(Boolean coversDamage) { this.coversDamage = coversDamage; }

    public Boolean getIsParkedStopped() { return isParkedStopped; }
    public void setIsParkedStopped(Boolean isParkedStopped) { this.isParkedStopped = isParkedStopped; }

    public Boolean getIsLeavingParking() { return isLeavingParking; }
    public void setIsLeavingParking(Boolean isLeavingParking) { this.isLeavingParking = isLeavingParking; }

    public Boolean getIsReversing() { return isReversing; }
    public void setIsReversing(Boolean isReversing) { this.isReversing = isReversing; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public Boolean getHasTrailer() { return hasTrailer; }
    public void setHasTrailer(Boolean hasTrailer) { this.hasTrailer = hasTrailer; }

    public String getTrailerRegistration() { return trailerRegistration; }
    public void setTrailerRegistration(String trailerRegistration) { this.trailerRegistration = trailerRegistration; }

    public Boolean getNoLicencePlate() { return noLicencePlate; }
    public void setNoLicencePlate(Boolean noLicencePlate) { this.noLicencePlate = noLicencePlate; }
}
