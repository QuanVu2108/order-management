package com.ss.repository.query;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.List;

@Builder
@Data
public class UserQuery {
//    String username, String store, String permissionGroup, String position, String email, String fullName
    private String keyword;
    private Collection<String> userNames;
    private String fullName;
    private String store;
    private String permissionGroup;
    private String position;
    private String email;
}
