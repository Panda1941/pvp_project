package accidentreporting.model;

import jakarta.persistence.*;

@Entity
@Table(name = "accident_reports")
public class AccidentReport {

    public static final int STATUS_WAITING = 0;
    public static final int STATUS_CONFIRMED = 1;
    public static final int STATUS_ISSUE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long timestamp;
    private String location;
    @Column(length = 2000)
    private String description;
    private Boolean isDraft;
    private Integer status;

    // Address and geolocation
    private String address;
    private Double latitude;
    private Double longitude;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String signatureA;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String signatureB;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vehicle_a_id")
    private Vehicle vehicleA;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vehicle_b_id")
    private Vehicle vehicleB;

    // Witnesses, photos and damage lists
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "report_id")
    private java.util.List<Witness> witnesses;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "report_id")
    private java.util.List<Photo> photos;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "report_id")
    private java.util.List<Damage> damages;

    public AccidentReport() {
        this.timestamp = System.currentTimeMillis();
        this.isDraft = true;
        this.status = STATUS_WAITING;
        this.witnesses = new java.util.ArrayList<>();
        this.photos = new java.util.ArrayList<>();
        this.damages = new java.util.ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsDraft() { return isDraft; }
    public void setIsDraft(Boolean isDraft) { this.isDraft = isDraft; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Vehicle getVehicleA() { return vehicleA; }
    public void setVehicleA(Vehicle vehicleA) { this.vehicleA = vehicleA; }

    public Vehicle getVehicleB() { return vehicleB; }
    public void setVehicleB(Vehicle vehicleB) { this.vehicleB = vehicleB; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public java.util.List<Witness> getWitnesses() { return witnesses; }
    public void setWitnesses(java.util.List<Witness> witnesses) { this.witnesses = witnesses; }

    public java.util.List<Photo> getPhotos() { return photos; }
    public void setPhotos(java.util.List<Photo> photos) { this.photos = photos; }

    public java.util.List<Damage> getDamages() { return damages; }
    public void setDamages(java.util.List<Damage> damages) { this.damages = damages; }

    public String getSignatureA() { return signatureA; }
    public void setSignatureA(String signatureA) { this.signatureA = signatureA; }

    public String getSignatureB() { return signatureB; }
    public void setSignatureB(String signatureB) { this.signatureB = signatureB; }
}
