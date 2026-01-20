package com.logismart.logismartv2.repository;

import com.logismart.logismartv2.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, String> {

    Optional<Recipient> findByPhone(String phone);

    Optional<Recipient> findByEmail(String email);

    List<Recipient> findByFirstNameAndLastName(String firstName, String lastName);

    @Query("SELECT r FROM Recipient r WHERE LOWER(r.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Recipient> searchByName(@Param("keyword") String keyword);

    List<Recipient> findByAddressContainingIgnoreCase(String keyword);


    @Query("SELECT COUNT(p) FROM Parcel p WHERE p.recipient.id = :recipientId")
    Long countParcelsByRecipientId(@Param("recipientId") String recipientId);

    @Query("SELECT r FROM Recipient r WHERE (SELECT COUNT(p) FROM Parcel p WHERE p.recipient.id = r.id) >= :minParcels")
    List<Recipient> findFrequentRecipients(@Param("minParcels") Long minParcels);

    List<Recipient> findByPhoneContaining(String keyword);
}
