package accidentreporting.model;

import jakarta.persistence.*;

@Entity
@Table(name = "damages")
public class Damage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // free-form area description (e.g., "front-left bumper", "rear-right door")
    private String area;
    private String severity; // optional severity descriptor

    public Damage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
