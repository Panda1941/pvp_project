package accidentreporting.dto;

import java.util.List;

public class AccidentReportDto {
    public Long id;
    public Long timestamp;
    public String location;
    public String description;
    public Boolean isDraft;
    public Integer status;
    public String address;
    public Double latitude;
    public Double longitude;
    public String signatureA;
    public String signatureB;

    public VehicleDto vehicleA;
    public VehicleDto vehicleB;
    public List<WitnessDto> witnesses;
    public List<PhotoDto> photos;
    public List<DamageDto> damages;

    public AccidentReportDto() {}
}
