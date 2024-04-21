package com.ss.repository;

import com.ss.model.StoreModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<StoreModel, UUID> {

    @Query("SELECT e FROM StoreModel e " +
            " WHERE 1=1 " +
            " AND (:keyword is null or UPPER(e.name) like :keyword or UPPER(e.phoneNumber) like :keyword or UPPER(e.address) like :keyword  )"
    )
    Page<StoreModel> search(String keyword, Pageable pageable);

    List<StoreModel> findByName(String name);

    List<StoreModel> findByNameIn(List<String> storeNames);
}
