package com.logismart.logismartv2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_person")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    
    
    @Size(max = 50, message = "Vehicle information must not exceed 50 characters")
    @Column(name = "vehicle", nullable = true, length = 50)
    private String vehicle;

    
    
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_zone_id", nullable = true)
    private Zone assignedZone;

    
    
    
    @OneToMany(mappedBy = "deliveryPerson", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Parcel> parcels = new ArrayList<>();

    public DeliveryPerson(String id, String firstName, String lastName, String phone, String vehicle) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.vehicle = vehicle;
        this.parcels = new ArrayList<>();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasVehicle() {
        return vehicle != null && !vehicle.trim().isEmpty();
    }

    public boolean isAssignedToZone() {
        return assignedZone != null;
    }

    public boolean hasAssignedZone() {
        return assignedZone != null;
    }

    public String getZoneName() {
        return assignedZone != null ? assignedZone.getName() : "Unassigned";
    }

    public int getParcelCount() {
        return parcels != null ? parcels.size() : 0;
    }
}
