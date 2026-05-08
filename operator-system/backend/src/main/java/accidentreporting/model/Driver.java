package accidentreporting.model;

import jakarta.persistence.*;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String firstName;
    private String lastName;
    private String dob; // ISO date string for simplicity
    // Address breakdown
    private String country;
    private String street;
    private String houseNumber;
    private String apartment;
    private String city;
    private String postalCode;

    private String contact; // phone or contact string

    private String personalId; // personal code or national id

    private String licenseNumber;
    private String licenseCategory;
    private String licenseExpiry;

    public Driver() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getHouseNumber() { return houseNumber; }
    public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }

    public String getApartment() { return apartment; }
    public void setApartment(String apartment) { this.apartment = apartment; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getPersonalId() { return personalId; }
    public void setPersonalId(String personalId) { this.personalId = personalId; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getLicenseCategory() { return licenseCategory; }
    public void setLicenseCategory(String licenseCategory) { this.licenseCategory = licenseCategory; }

    public String getLicenseExpiry() { return licenseExpiry; }
    public void setLicenseExpiry(String licenseExpiry) { this.licenseExpiry = licenseExpiry; }
}
