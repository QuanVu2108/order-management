package com.ss.repository;

import com.ss.enums.ProductPropertyType;
import com.ss.model.ProductPropertyModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductPropertyRepository extends JpaRepository<ProductPropertyModel, UUID> {
    List<ProductPropertyModel> findByName(String name);

    @Query(value = "SELECT count(*) FROM product_property_tbl m where m.type = :type", nativeQuery = true)
    long countAllByType(String type);

    @Query("SELECT e FROM ProductPropertyModel e " +
            " WHERE 1=1 " +
            " AND (:type is null or UPPER(e.type) = :type) " +
            " AND (:code is null or UPPER(e.code) like :code) " +
            " AND (:name is null or UPPER(e.name) like :name) "
    )
    Page<ProductPropertyModel> search(String code, String name, ProductPropertyType type, Pageable pageable);
}
