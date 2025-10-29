package com.logismart.logismartv2.repository;

import com.logismart.logismartv2.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, String> {

    Optional<Zone> findByName(String name);

    Optional<Zone> findByPostalCode(String postalCode);

    List<Zone> findByNameContainingIgnoreCase(String keyword);

    boolean existsByName(String name);

    boolean existsByPostalCode(String postalCode);

    @Query("SELECT COUNT(dp) FROM DeliveryPerson dp WHERE dp.assignedZone.id = :zoneId")
    Long countDeliveryPersonsByZoneId(@Param("zoneId") String zoneId);

    @Query("SELECT DISTINCT z FROM Zone z WHERE EXISTS (SELECT 1 FROM DeliveryPerson dp WHERE dp.assignedZone.id = z.id)")
    List<Zone> findZonesWithDeliveryPersons();

    @Query("SELECT z FROM Zone z WHERE NOT EXISTS (SELECT 1 FROM DeliveryPerson dp WHERE dp.assignedZone.id = z.id)")
    List<Zone> findZonesWithoutDeliveryPersons();
}
