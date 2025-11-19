package com.logismart.logismartv2.repository;

import com.logismart.logismartv2.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findByName(String name);

    boolean existsByName(String name);

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByCategory(String category);

    List<Product> findByCategoryContainingIgnoreCase(String keyword);

    @Query("SELECT p FROM Product p WHERE p.category IS NULL OR p.category = ''")
    List<Product> findProductsWithoutCategory();

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL AND p.category <> '' ORDER BY p.category")
    List<String> findAllDistinctCategories();

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM Product p WHERE p.weight BETWEEN :minWeight AND :maxWeight")
    List<Product> findByWeightRange(@Param("minWeight") BigDecimal minWeight, @Param("maxWeight") BigDecimal maxWeight);

    List<Product> findByPriceLessThanEqual(BigDecimal maxPrice);

    List<Product> findByPriceGreaterThanEqual(BigDecimal minPrice);

    List<Product> findByWeightLessThanEqual(BigDecimal maxWeight);

    @Query("SELECT COALESCE(SUM(pp.quantity), 0) FROM ParcelProduct pp WHERE pp.product.id = :productId")
    Long countTotalQuantityShipped(@Param("productId") String productId);

    @Query("SELECT p FROM Product p LEFT JOIN ParcelProduct pp ON pp.product.id = p.id GROUP BY p.id ORDER BY COALESCE(SUM(pp.quantity), 0) DESC")
    List<Product> findMostPopularProducts();

    @Query("SELECT p FROM Product p WHERE NOT EXISTS (SELECT 1 FROM ParcelProduct pp WHERE pp.product.id = p.id)")
    List<Product> findNeverShippedProducts();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByNameOrCategory(@Param("keyword") String keyword);
}
