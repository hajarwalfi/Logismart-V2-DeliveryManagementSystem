package com.logismart.logismartv2.repository;

import com.logismart.logismartv2.entity.DeliveryHistory;
import com.logismart.logismartv2.entity.ParcelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryHistoryRepository extends JpaRepository<DeliveryHistory, String> {

    @Query("SELECT h FROM DeliveryHistory h WHERE h.parcel.id = :parcelId ORDER BY h.changedAt ASC")
    List<DeliveryHistory> findByParcelIdOrderByChangedAtAsc(@Param("parcelId") String parcelId);

    @Query("SELECT h FROM DeliveryHistory h WHERE h.parcel.id = :parcelId ORDER BY h.changedAt DESC")
    List<DeliveryHistory> findByParcelIdOrderByChangedAtDesc(@Param("parcelId") String parcelId);

    @Query("SELECT h FROM DeliveryHistory h WHERE h.parcel.id = :parcelId ORDER BY h.changedAt DESC LIMIT 1")
    Optional<DeliveryHistory> findLatestByParcelId(@Param("parcelId") String parcelId);

    @Query("SELECT h FROM DeliveryHistory h WHERE h.parcel.id = :parcelId ORDER BY h.changedAt ASC LIMIT 1")
    Optional<DeliveryHistory> findFirstByParcelId(@Param("parcelId") String parcelId);

    List<DeliveryHistory> findByStatus(ParcelStatus status);

    @Query("SELECT h FROM DeliveryHistory h WHERE h.status = :status AND h.changedAt BETWEEN :startDate AND :endDate")
    List<DeliveryHistory> findByStatusAndDateRange(@Param("status") ParcelStatus status,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT h FROM DeliveryHistory h WHERE h.comment IS NOT NULL AND h.comment <> ''")
    List<DeliveryHistory> findEntriesWithComments();

    @Query("SELECT h FROM DeliveryHistory h WHERE h.status = :status AND CAST(h.changedAt AS DATE) = CURRENT_DATE")
    List<DeliveryHistory> findByStatusToday(@Param("status") ParcelStatus status);

    Long countByStatus(ParcelStatus status);

    @Query("SELECT COUNT(h) FROM DeliveryHistory h WHERE CAST(h.changedAt AS DATE) = CURRENT_DATE")
    Long countChangesToday();

    @Query("SELECT COUNT(h) FROM DeliveryHistory h WHERE h.status = 'DELIVERED' AND CAST(h.changedAt AS DATE) = CURRENT_DATE")
    Long countDeliveriesToday();

    @Query("SELECT h FROM DeliveryHistory h WHERE h.changedAt BETWEEN :startDate AND :endDate ORDER BY h.changedAt DESC")
    List<DeliveryHistory> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT h FROM DeliveryHistory h WHERE h.parcel.id = :parcelId AND h.status = :status")
    Optional<DeliveryHistory> findByParcelIdAndStatus(@Param("parcelId") String parcelId,
                                                       @Param("status") ParcelStatus status);

    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, " +
            "(SELECT h1.changedAt FROM DeliveryHistory h1 WHERE h1.parcel.id = h.parcel.id AND h1.status = 'CREATED'), " +
            "h.changedAt)) " +
            "FROM DeliveryHistory h WHERE h.status = 'DELIVERED'")
    Double calculateAverageDeliveryTimeInHours();

    @Query("SELECT DISTINCT h.parcel.id FROM DeliveryHistory h")
    List<String> findParcelsWithHistory();

    @Query("SELECT COUNT(h) FROM DeliveryHistory h WHERE h.parcel.id = :parcelId")
    Long countByParcelId(@Param("parcelId") String parcelId);

    @Query("SELECT h FROM DeliveryHistory h WHERE h.parcel.id = :parcelId AND h.comment IS NOT NULL AND h.comment <> '' ORDER BY h.changedAt DESC")
    List<DeliveryHistory> findByParcelIdWithComments(@Param("parcelId") String parcelId);

    @Query("SELECT h FROM DeliveryHistory h WHERE LOWER(h.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY h.changedAt DESC")
    List<DeliveryHistory> searchByComment(@Param("keyword") String keyword);
}
