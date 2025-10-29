package com.logismart.logismartv2.repository;

import com.logismart.logismartv2.entity.ParcelProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelProductRepository extends JpaRepository<ParcelProduct, String> {

    

    @Query("SELECT pp FROM ParcelProduct pp WHERE pp.parcel.id = :parcelId")
    List<ParcelProduct> findByParcelId(@Param("parcelId") String parcelId);

    @Query("SELECT COUNT(pp) FROM ParcelProduct pp WHERE pp.parcel.id = :parcelId")
    Long countByParcelId(@Param("parcelId") String parcelId);

    @Query("SELECT COALESCE(SUM(pp.quantity * pp.price), 0) FROM ParcelProduct pp WHERE pp.parcel.id = :parcelId")
    BigDecimal calculateTotalValueByParcelId(@Param("parcelId") String parcelId);

    @Query("SELECT pp FROM ParcelProduct pp WHERE pp.parcel.id = :parcelId AND pp.product.id = :productId")
    Optional<ParcelProduct> findByParcelIdAndProductId(@Param("parcelId") String parcelId,
                                                        @Param("productId") String productId);

    

    @Query("SELECT pp FROM ParcelProduct pp WHERE pp.product.id = :productId")
    List<ParcelProduct> findByProductId(@Param("productId") String productId);

    @Query("SELECT COUNT(pp) FROM ParcelProduct pp WHERE pp.product.id = :productId")
    Long countByProductId(@Param("productId") String productId);

    @Query("SELECT COALESCE(SUM(pp.quantity), 0) FROM ParcelProduct pp WHERE pp.product.id = :productId")
    Long calculateTotalQuantityByProductId(@Param("productId") String productId);

    @Query("SELECT COALESCE(SUM(pp.quantity * pp.price), 0) FROM ParcelProduct pp WHERE pp.product.id = :productId")
    BigDecimal calculateTotalRevenueByProductId(@Param("productId") String productId);

    

    @Query("SELECT pp.product.id, pp.product.name, SUM(pp.quantity) as totalQty " +
            "FROM ParcelProduct pp " +
            "GROUP BY pp.product.id, pp.product.name " +
            "ORDER BY totalQty DESC")
    List<Object[]> findMostPopularProducts();

    @Query("SELECT pp.product.id, pp.product.name, SUM(pp.quantity * pp.price) as totalRevenue " +
            "FROM ParcelProduct pp " +
            "GROUP BY pp.product.id, pp.product.name " +
            "ORDER BY totalRevenue DESC")
    List<Object[]> findHighestRevenueProducts();

    @Query("SELECT COALESCE(SUM(pp.quantity * pp.price), 0) FROM ParcelProduct pp")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(pp.quantity), 0) FROM ParcelProduct pp")
    Long calculateTotalItemsShipped();

    @Query("SELECT pp FROM ParcelProduct pp WHERE pp.quantity >= :minQuantity ORDER BY pp.quantity DESC")
    List<ParcelProduct> findBulkOrders(@Param("minQuantity") Integer minQuantity);

    @Query("SELECT pp FROM ParcelProduct pp INNER JOIN pp.product p WHERE pp.price < p.price")
    List<ParcelProduct> findDiscountedProducts();

    @Query("SELECT AVG(pp.price) FROM ParcelProduct pp WHERE pp.product.id = :productId")
    BigDecimal calculateAveragePriceByProductId(@Param("productId") String productId);

    @Query("SELECT pp FROM ParcelProduct pp JOIN FETCH pp.product WHERE pp.parcel.id = :parcelId")
    List<ParcelProduct> findByParcelIdWithProduct(@Param("parcelId") String parcelId);

    @Query("SELECT pp FROM ParcelProduct pp " +
            "JOIN FETCH pp.parcel p " +
            "JOIN FETCH pp.product prod")
    List<ParcelProduct> findAllWithRelationships();

    @Query("SELECT COUNT(DISTINCT pp.product.id) FROM ParcelProduct pp")
    Long countDistinctProducts();

    @Query("SELECT p.id FROM Product p WHERE NOT EXISTS (SELECT 1 FROM ParcelProduct pp WHERE pp.product.id = p.id)")
    List<Long> findUnusedProductIds();

    @Query("SELECT AVG(totalQty) FROM (SELECT SUM(pp.quantity) as totalQty FROM ParcelProduct pp GROUP BY pp.parcel.id) as subquery")
    Double calculateAverageQuantityPerParcel();
}
