package com.aws.sync.entity.firefiles;

import lombok.Data;

@Data
public class SetUserRoleResponse {
    private SetUserRoleData data;

    @Data
    public static class SetUserRoleData {
        private UserRole setUserRole;

    }

    @Data
    public static class UserRole {
        private String name;
        private boolean is_admin;

    }
}
