package project.badminton.court.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourtResponse(
        Long id,
        String name,
        String address,
        String description,
        BigDecimal hourlyPrice,
        boolean active,
        List<String> imageUrls,
        Long managerId
) {
}
