package com.logismart.logismartv2.repository;

import com.logismart.logismartv2.entity.Parcel;
import com.logismart.logismartv2.entity.ParcelPriority;
import com.logismart.logismartv2.entity.ParcelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, String>, JpaSpecificationExecutor<Parcel> {

    

    List<Parcel> findByStatus(ParcelStatus status);

    List<Parcel> findByPriority(ParcelPriority priority);

    List<Parcel> findByStatusAndPriority(ParcelStatus status, ParcelPriority priority);

    Long countByStatus(ParcelStatus status);

    

    List<Parcel> findBySenderClientId(String senderClientId);

    List<Parcel> findByRecipientId(String recipientId);

    List<Parcel> findByDeliveryPersonId(String deliveryPersonId);

    List<Parcel> findByZoneId(String zoneId);

    Long countByZoneId(String zoneId);

    @Query("SELECT p FROM Parcel p WHERE p.deliveryPerson IS NULL")
    List<Parcel> findUnassignedParcels();

    @Query("SELECT p FROM Parcel p WHERE p.zone IS NULL")
    List<Parcel> findParcelsWithoutZone();

    

    List<Parcel> findByDestinationCity(String city);

    List<Parcel> findByDestinationCityContainingIgnoreCase(String keyword);

    @Query("SELECT DISTINCT p.destinationCity FROM Parcel p ORDER BY p.destinationCity")
    List<String> findAllDistinctDestinationCities();

    

    @Query("SELECT p FROM Parcel p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Parcel> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Parcel p WHERE CAST(p.createdAt AS DATE) = CURRENT_DATE")
    List<Parcel> findCreatedToday();

    @Query("SELECT p FROM Parcel p WHERE p.createdAt >= :startDate")
    List<Parcel> findCreatedInLastDays(@Param("startDate") LocalDateTime startDate);

    

    @Query("SELECT p FROM Parcel p " +
            "LEFT JOIN FETCH p.senderClient " +
            "LEFT JOIN FETCH p.recipient " +
            "LEFT JOIN FETCH p.deliveryPerson " +
            "LEFT JOIN FETCH p.zone " +
            "WHERE p.id = :id")
    Optional<Parcel> findByIdWithRelationships(@Param("id") String id);

    @Query("SELECT DISTINCT p FROM Parcel p " +
            "LEFT JOIN FETCH p.senderClient " +
            "LEFT JOIN FETCH p.recipient " +
            "LEFT JOIN FETCH p.deliveryPerson " +
            "LEFT JOIN FETCH p.zone")
    List<Parcel> findAllWithRelationships();

    

    List<Parcel> findBySenderClientIdAndStatus(String senderClientId, ParcelStatus status);

    List<Parcel> findByDeliveryPersonIdAndStatus(String deliveryPersonId, ParcelStatus status);

    List<Parcel> findByZoneIdAndStatus(String zoneId, ParcelStatus status);

    

    @Query("SELECT p FROM Parcel p WHERE p.priority = 'EXPRESS' AND p.status <> 'DELIVERED'")
    List<Parcel> findHighPriorityPending();

    @Query("SELECT p FROM Parcel p WHERE p.status = 'CREATED'")
    List<Parcel> findReadyForPickup();

    @Query("SELECT p FROM Parcel p WHERE p.status = 'IN_STOCK'")
    List<Parcel> findInStock();

    @Query("SELECT p FROM Parcel p WHERE p.status = 'IN_TRANSIT'")
    List<Parcel> findInTransit();

    @Query("SELECT p FROM Parcel p WHERE p.status = 'DELIVERED'")
    List<Parcel> findDelivered();

    Long countBySenderClientId(String senderClientId);

    Long countByRecipientId(String recipientId);

    @Query("SELECT DISTINCT p FROM Parcel p INNER JOIN p.parcelProducts pp WHERE pp.product.id = :productId")
    List<Parcel> findByProductId(@Param("productId") String productId);

    @Query("SELECT p FROM Parcel p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Parcel> searchByDescription(@Param("keyword") String keyword);

    @Query("SELECT p FROM Parcel p WHERE p.createdAt < :threshold AND p.status <> 'DELIVERED'")
    List<Parcel> findOverdueParcels(@Param("threshold") LocalDateTime threshold);
}
