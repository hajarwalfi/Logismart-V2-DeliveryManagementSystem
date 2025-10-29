package com.logismart.logismartv2.repository;

import com.logismart.logismartv2.entity.SenderClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SenderClientRepository extends JpaRepository<SenderClient, String> {

    Optional<SenderClient> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM SenderClient sc WHERE sc.email = :email AND sc.id <> :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") String id);

    Optional<SenderClient> findByPhone(String phone);

    List<SenderClient> findByFirstNameAndLastName(String firstName, String lastName);

    @Query("SELECT sc FROM SenderClient sc WHERE LOWER(sc.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(sc.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SenderClient> searchByName(@Param("keyword") String keyword);

    List<SenderClient> findByEmailContainingIgnoreCase(String keyword);

    List<SenderClient> findByAddressContainingIgnoreCase(String keyword);

    @Query("SELECT COUNT(p) FROM Parcel p WHERE p.senderClient.id = :senderClientId")
    Long countParcelsBySenderClientId(@Param("senderClientId") String senderClientId);

    @Query("SELECT sc FROM SenderClient sc LEFT JOIN Parcel p ON p.senderClient.id = sc.id GROUP BY sc.id ORDER BY COUNT(p) DESC")
    List<SenderClient> findTopSendersByParcelCount();
}
