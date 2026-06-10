package project.badminton.user.dto;

import project.badminton.user.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
        Set<Role> roles,
        boolean enabled,
        LocalDateTime createdAt
) {
}
