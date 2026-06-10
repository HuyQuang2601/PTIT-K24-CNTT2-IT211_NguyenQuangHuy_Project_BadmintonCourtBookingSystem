package project.badminton.court.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CourtRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 255) String address,
        @Size(max = 1000) String description,
        @NotNull @DecimalMin("0.0") BigDecimal hourlyPrice,
        boolean active,
        List<String> imageUrls
) {
}
