package com.eliascampos.usermanagement.user.dto;

import com.eliascampos.usermanagement.user.User;
import com.eliascampos.usermanagement.user.UserRole;

import java.time.Instant;

public record UserResponse(
        String id,
        String name,
        String email,
        UserRole role,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
