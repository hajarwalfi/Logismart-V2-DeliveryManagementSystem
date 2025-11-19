package com.logismart.logismartv2.repository;

import com.logismart.logismartv2.entity.DeliveryPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPersonRepository extends JpaRepository<DeliveryPerson, String> {

    Optional<DeliveryPerson> findByPhone(String phone);

    boolean existsByPhone(String phone);

    List<DeliveryPerson> findByFirstNameAndLastName(String firstName, String lastName);

    @Query("SELECT dp FROM DeliveryPerson dp WHERE LOWER(dp.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(dp.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DeliveryPerson> searchByName(@Param("keyword") String keyword);

    List<DeliveryPerson> findByAssignedZoneId(String zoneId);

    @Query("SELECT dp FROM DeliveryPerson dp WHERE dp.assignedZone IS NULL")
    List<DeliveryPerson> findUnassignedDeliveryPersons();

    List<DeliveryPerson> findByVehicle(String vehicle);

    @Query("SELECT dp FROM DeliveryPerson dp WHERE dp.vehicle IS NULL OR dp.vehicle = ''")
    List<DeliveryPerson> findWithoutVehicle();

    @Query("SELECT dp FROM DeliveryPerson dp LEFT JOIN FETCH dp.assignedZone WHERE dp.id = :id")
    Optional<DeliveryPerson> findByIdWithZone(@Param("id") String id);

    @Query("SELECT dp FROM DeliveryPerson dp LEFT JOIN FETCH dp.assignedZone")
    List<DeliveryPerson> findAllWithZones();

    @Query("SELECT COUNT(p) FROM Parcel p WHERE p.deliveryPerson.id = :deliveryPersonId AND p.status = 'IN_TRANSIT'")
    Long countActiveParcels(@Param("deliveryPersonId") String deliveryPersonId);

    @Query("SELECT COUNT(p) FROM Parcel p WHERE p.deliveryPerson.id = :deliveryPersonId AND p.status = 'DELIVERED'")
    Long countDeliveredParcels(@Param("deliveryPersonId") String deliveryPersonId);

    @Query("SELECT dp FROM DeliveryPerson dp WHERE NOT EXISTS (SELECT 1 FROM Parcel p WHERE p.deliveryPerson.id = dp.id AND p.status = 'IN_TRANSIT')")
    List<DeliveryPerson> findAvailableDeliveryPersons();

    @Query("SELECT DISTINCT dp FROM DeliveryPerson dp INNER JOIN Parcel p ON p.deliveryPerson.id = dp.id WHERE p.status = 'IN_TRANSIT'")
    List<DeliveryPerson> findBusyDeliveryPersons();

    @Query("SELECT dp FROM DeliveryPerson dp WHERE dp.assignedZone.id = :zoneId AND NOT EXISTS (SELECT 1 FROM Parcel p WHERE p.deliveryPerson.id = dp.id AND p.status = 'IN_TRANSIT')")
    List<DeliveryPerson> findAvailableInZone(@Param("zoneId") String zoneId);

    @Query("SELECT dp FROM DeliveryPerson dp LEFT JOIN Parcel p ON p.deliveryPerson.id = dp.id AND p.status = 'DELIVERED' GROUP BY dp.id ORDER BY COUNT(p) DESC")
    List<DeliveryPerson> findTopPerformers();
}
