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
@Table(name = "zone")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "Zone name is required")
    @Size(max = 100, message = "Zone name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Postal code is required")
    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;


    @OneToMany(mappedBy = "assignedZone", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryPerson> deliveryPersons = new ArrayList<>();

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Parcel> parcels = new ArrayList<>();

    public Zone(String id, String name, String postalCode) {
        this.id = id;
        this.name = name;
        this.postalCode = postalCode;
        this.deliveryPersons = new ArrayList<>();
        this.parcels = new ArrayList<>();
    }
}
