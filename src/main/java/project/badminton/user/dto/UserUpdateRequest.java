package project.badminton.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import project.badminton.user.Role;

import java.util.Set;

public record UserUpdateRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 20) String phone,
        @NotEmpty Set<Role> roles,
        boolean enabled
) {
}
