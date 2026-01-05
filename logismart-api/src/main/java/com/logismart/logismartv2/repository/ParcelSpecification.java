package com.logismart.logismartv2.repository;

import com.logismart.logismartv2.entity.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ParcelSpecification {

    public static Specification<Parcel> withFilters(
            ParcelStatus status,
            ParcelPriority priority,
            String zoneId,
            String destinationCity,
            String deliveryPersonId,
            String senderClientId,
            String recipientId,
            Boolean unassignedOnly) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }

            
            if (zoneId != null) {
                Join<Parcel, Zone> zoneJoin = root.join("zone", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(zoneJoin.get("id"), zoneId));
            }

            
            if (destinationCity != null && !destinationCity.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("destinationCity")),
                        "%" + destinationCity.toLowerCase() + "%"
                ));
            }

            
            if (deliveryPersonId != null) {
                Join<Parcel, DeliveryPerson> deliveryPersonJoin = root.join("deliveryPerson", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(deliveryPersonJoin.get("id"), deliveryPersonId));
            }

            
            if (senderClientId != null) {
                Join<Parcel, SenderClient> senderJoin = root.join("senderClient", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(senderJoin.get("id"), senderClientId));
            }

            
            if (recipientId != null) {
                Join<Parcel, Recipient> recipientJoin = root.join("recipient", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(recipientJoin.get("id"), recipientId));
            }

            
            if (unassignedOnly != null && unassignedOnly) {
                predicates.add(criteriaBuilder.isNull(root.get("deliveryPerson")));
            }

            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
