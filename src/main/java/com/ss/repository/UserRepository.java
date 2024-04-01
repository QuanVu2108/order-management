package com.ss.repository;

import com.ss.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    UserModel findByUsername(String username);

    @Query("SELECT DISTINCT user FROM UserModel user " +
            " LEFT JOIN user.permissionGroupModel permissionGroup " +
            " LEFT JOIN user.stores store " +
            " WHERE 1=1 " +
            " AND (:username is null or UPPER(user.username) like :username ) " +
            " AND (:position is null or UPPER(user.position) like :position ) " +
            " AND (:email is null or UPPER(user.email) like :email ) " +
            " AND (:fullName is null or UPPER(user.fullName) like :fullName ) " +
            " AND (:store is null or store.name = :store) " +
            " AND (:permissionGroup is null or UPPER(permissionGroup.name) like :permissionGroup )"
    )
    Page<UserModel> search(String username, String store, String permissionGroup, String position, String email, String fullName, Pageable pageable);
}
