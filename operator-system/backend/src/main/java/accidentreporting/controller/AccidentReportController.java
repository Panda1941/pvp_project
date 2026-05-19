package accidentreporting.controller;

import accidentreporting.dto.*;
import accidentreporting.model.AccidentReport;
import accidentreporting.model.Driver;
import accidentreporting.model.Vehicle;
import accidentreporting.model.Photo;
import accidentreporting.model.Damage;
import accidentreporting.model.Witness;
import accidentreporting.repository.AccidentReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class AccidentReportController {

    private final AccidentReportRepository repo;
    private final Logger logger = LoggerFactory.getLogger(AccidentReportController.class);

    @Value("${cache.logging.enabled:true}")
    private boolean cacheLoggingEnabled;

    public AccidentReportController(AccidentReportRepository repo) { this.repo = repo; }

    @GetMapping
    @Cacheable("reportList")
    public List<AccidentReportDto> list() {
        if (cacheLoggingEnabled) logger.info("DB fetch: list reports");
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Cacheable(value = "reports", key = "#id")
    public ResponseEntity<AccidentReportDto> getOne(@PathVariable Long id) {
        if (cacheLoggingEnabled) logger.info("DB fetch: report {}", id);
        return repo.findById(id).map(r -> ResponseEntity.ok(toDto(r))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @CacheEvict(value = {"reportList", "reports"}, allEntries = true)
    public ResponseEntity<AccidentReportDto> create(@RequestBody AccidentReport dto) {
        AccidentReport saved = repo.save(dto);
        if (cacheLoggingEnabled) logger.info("Created report {} and evicted caches", saved.getId());
        return ResponseEntity.ok(toDto(saved));
    }

    @PutMapping("/{id}/status")
    @CacheEvict(value = {"reportList", "reports"}, allEntries = true)
    public ResponseEntity<AccidentReportDto> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        if (request == null || request.status == null || request.status < 0 || request.status > 2) {
            return ResponseEntity.badRequest().build();
        }
        return repo.findById(id)
                .map(report -> {
                    report.setStatus(request.status);
                    if (request.status != null) {
                        report.setIsDraft(false);
                    }
                    AccidentReport saved = repo.save(report);
                    if (cacheLoggingEnabled) logger.info("Updated status for report {} and evicted caches", saved.getId());
                    return ResponseEntity.ok(toDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"reportList", "reports"}, allEntries = true)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        var existing = repo.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repo.delete(existing.get());
        if (cacheLoggingEnabled) logger.info("Deleted report {} and evicted caches", id);
        return ResponseEntity.noContent().build();
    }

    public static class StatusUpdateRequest {
        public Integer status;
    }

    private AccidentReportDto toDto(AccidentReport r) {
        AccidentReportDto dto = new AccidentReportDto();
        dto.id = r.getId();
        dto.timestamp = r.getTimestamp();
        dto.location = r.getLocation();
        dto.description = r.getDescription();
        dto.isDraft = r.getIsDraft();
        dto.status = r.getStatus();
        dto.address = r.getAddress();
        dto.latitude = r.getLatitude();
        dto.longitude = r.getLongitude();
        dto.signatureA = r.getSignatureA();
        dto.signatureB = r.getSignatureB();

        if (r.getVehicleA() != null) dto.vehicleA = toVehicleDto(r.getVehicleA());
        if (r.getVehicleB() != null) dto.vehicleB = toVehicleDto(r.getVehicleB());

        dto.photos = r.getPhotos().stream().map(this::toPhotoDto).collect(Collectors.toList());
        dto.damages = r.getDamages().stream().map(this::toDamageDto).collect(Collectors.toList());
        dto.witnesses = r.getWitnesses().stream().map(this::toWitnessDto).collect(Collectors.toList());

        return dto;
    }

    private VehicleDto toVehicleDto(Vehicle v) {
        VehicleDto d = new VehicleDto();
        d.id = v.getId();
        d.vehicleRegistration = v.getVehicleRegistration();
        d.vehicleMakeType = v.getVehicleMakeType();
        d.vehicleCountry = v.getVehicleCountry();
        d.insuranceName = v.getInsuranceName();
        d.policyNumber = v.getPolicyNumber();
        d.contactPhone = v.getContactPhone();
        if (v.getDriver() != null) d.driver = toDriverDto(v.getDriver());
        return d;
    }

    private DriverDto toDriverDto(Driver driver) {
        DriverDto d = new DriverDto();
        d.id = driver.getId();
        d.name = driver.getName();
        d.firstName = driver.getFirstName();
        d.lastName = driver.getLastName();
        d.dob = driver.getDob();
        d.country = driver.getCountry();
        d.street = driver.getStreet();
        d.houseNumber = driver.getHouseNumber();
        d.apartment = driver.getApartment();
        d.city = driver.getCity();
        d.postalCode = driver.getPostalCode();
        d.contact = driver.getContact();
        d.personalId = driver.getPersonalId();
        d.licenseNumber = driver.getLicenseNumber();
        d.licenseCategory = driver.getLicenseCategory();
        d.licenseExpiry = driver.getLicenseExpiry();
        return d;
    }

    private PhotoDto toPhotoDto(Photo p) {
        PhotoDto d = new PhotoDto();
        d.id = p.getId();
        d.url = p.getUrl();
        d.description = p.getDescription();
        return d;
    }

    private DamageDto toDamageDto(Damage p) {
        DamageDto d = new DamageDto();
        d.id = p.getId();
        d.area = p.getArea();
        d.severity = p.getSeverity();
        d.vehicleTarget = p.getVehicleTarget();
        return d;
    }

    private WitnessDto toWitnessDto(Witness w) {
        WitnessDto d = new WitnessDto();
        d.id = w.getId();
        d.firstName = w.getFirstName();
        d.lastName = w.getLastName();
        d.phone = w.getPhone();
        return d;
    }
}
