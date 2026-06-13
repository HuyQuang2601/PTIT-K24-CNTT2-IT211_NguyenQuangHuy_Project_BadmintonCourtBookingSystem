package project.badminton.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import project.badminton.user.Role;

import java.util.Set;

public record UserRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(min = 3, max = 80, message = "Tên đăng nhập phải có từ 3 đến 80 ký tự") String username,
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng") String email,
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 100, message = "Mật khẩu phải có từ 6 đến 100 ký tự") String password,
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 120, message = "Họ tên không được vượt quá 120 ký tự") String fullName,
        @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự") String phone,
        @NotEmpty(message = "Người dùng phải có ít nhất một vai trò") Set<Role> roles,
        boolean enabled
) {
}
