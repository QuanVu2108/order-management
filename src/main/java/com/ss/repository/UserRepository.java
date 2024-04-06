package com.ss.repository;

import com.ss.model.PermissionGroupModel;
import com.ss.model.UserModel;
import com.ss.repository.query.UserQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    UserModel findByUsername(String username);

    @Query("SELECT DISTINCT user FROM UserModel user " +
            " LEFT JOIN user.permissionGroup permissionGroup " +
            " LEFT JOIN user.stores store " +
            " WHERE 1=1 " +
            " AND ((:#{#query.keyword} is null) or (upper(user.username) like :#{#query.keyword}) or (upper(user.fullName) like :#{#query.keyword}))" +
            " AND (:#{#query.userNames} is null or user.username in :#{#query.userNames} ) " +
            " AND (:#{#query.fullName} is null or UPPER(user.fullName) like :#{#query.fullName} ) " +
            " AND (:#{#query.position} is null or UPPER(user.position) like :#{#query.position} ) " +
            " AND (:#{#query.email} is null or UPPER(user.email) like :#{#query.email} ) " +
            " AND (:#{#query.store} is null or store.name = :#{#query.store}) " +
            " AND (:#{#query.permissionGroup} is null or UPPER(permissionGroup.name) like :#{#query.permissionGroup} )"
    )
    Page<UserModel> search(UserQuery query, Pageable pageable);

    List<UserModel> findByPermissionGroup(PermissionGroupModel permissionGroup);

    List<UserModel> findByUsernameLikeOrFullNameLike(String userName, String fullName);

    @Query("SELECT DISTINCT user FROM UserModel user " +
            " LEFT JOIN user.permissionGroup permissionGroup " +
            " LEFT JOIN user.stores store " +
            " WHERE 1=1 " +
            " AND ((:#{#query.keyword} is null) or (upper(user.username) like :#{#query.keyword}) or (upper(user.fullName) like :#{#query.keyword}))" +
            " AND (:#{#query.userNames} is null or user.username in :#{#query.userNames} ) " +
            " AND (:#{#query.fullName} is null or UPPER(user.fullName) like :#{#query.fullName} ) " +
            " AND (:#{#query.position} is null or UPPER(user.position) like :#{#query.position} ) " +
            " AND (:#{#query.email} is null or UPPER(user.email) like :#{#query.email} ) " +
            " AND (:#{#query.store} is null or store.name = :#{#query.store}) " +
            " AND (:#{#query.permissionGroup} is null or UPPER(permissionGroup.name) like :#{#query.permissionGroup} )"
    )
    List<UserModel> searchList(UserQuery query);
}
