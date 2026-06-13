package project.badminton.court.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CourtRequest(
        @NotBlank(message = "Tên sân không được để trống")
        @Size(max = 120, message = "Tên sân không được vượt quá 120 ký tự") String name,
        @NotBlank(message = "Địa chỉ không được để trống")
        @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự") String address,
        @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự") String description,
        @NotNull(message = "Giá thuê theo giờ không được để trống")
        @DecimalMin(value = "0.0", message = "Giá thuê theo giờ phải lớn hơn hoặc bằng 0") BigDecimal hourlyPrice,
        boolean active,
        List<String> imageUrls
) {
}
