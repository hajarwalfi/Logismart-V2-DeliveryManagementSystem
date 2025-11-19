package com.logismart.logismartv2.dto.deliveryperson;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPersonResponseDTO {

    private String id;

    private String firstName;

    private String lastName;

    private String fullName;

    private String phone;

    private String vehicle;

    private Boolean hasVehicle;

    private String assignedZoneId;

    private String assignedZoneName;

    private Boolean hasAssignedZone;

    public DeliveryPersonResponseDTO(String id, String firstName, String lastName,
                                      String phone, String vehicle,
                                      String assignedZoneId, String assignedZoneName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.phone = phone;
        this.vehicle = vehicle;
        this.hasVehicle = vehicle != null && !vehicle.trim().isEmpty();
        this.assignedZoneId = assignedZoneId;
        this.assignedZoneName = assignedZoneName;
        this.hasAssignedZone = assignedZoneId != null;
    }
}
