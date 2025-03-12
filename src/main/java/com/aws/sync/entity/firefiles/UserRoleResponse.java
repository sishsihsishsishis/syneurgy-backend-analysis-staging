package com.aws.sync.entity.firefiles;

import lombok.Data;

@Data
public class UserRoleResponse {
    private UserRoleData data;
    @Data
    public static class UserRoleData {
        private UserRole userRole;
    }

    @Data
    public static class UserRole {
        private String name;
        private boolean is_admin;
    }
}
