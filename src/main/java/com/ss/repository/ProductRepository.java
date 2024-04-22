package com.ss.repository;

import com.ss.model.ProductModel;
import com.ss.model.ProductPropertyModel;
import com.ss.repository.query.ProductQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel, Long> {

    @Query("SELECT DISTINCT e FROM ProductModel e " +
            " WHERE 1=1 " +
            " AND ((:#{#query.code} is null) or (upper(e.code) like :#{#query.code}))" +
            " AND ((:#{#query.number} is null) or (e.id = :#{#query.number}))" +
            " AND ((:#{#query.name} is null) or (upper(e.name) like :#{#query.name}))" +
            " AND ((:#{#query.size} is null) or (upper(e.size) like :#{#query.size}))" +
            " AND ((:#{#query.color} is null) or (upper(e.color) like :#{#query.color}))" +
            " AND ((:#{#query.brand} is null) or (upper(e.brand.name) like :#{#query.brand}))" +
            " AND ((:#{#query.category} is null) or (upper(e.category.name) like :#{#query.category}))" +
            " ORDER BY e.updatedAt DESC " +
            ""
    )
    Page<ProductModel> search(@Param("query") ProductQuery query, Pageable pageable);

    List<ProductModel> findByBrandOrCategory(ProductPropertyModel brand, ProductPropertyModel category);

    long countByCode(String code);

    long countByProductNumber(String productNumber);

    List<ProductModel> findByCodeInOrProductNumberIn(List<String> productCodes, List<String> productNumbers);

    Optional<ProductModel> findByProductNumber(String productNumber);
}
