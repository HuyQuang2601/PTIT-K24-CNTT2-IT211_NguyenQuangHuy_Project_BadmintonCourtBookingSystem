package project.badminton.file.dto;

import java.util.List;

public record MultiFileUploadResponse(
        List<String> urls
) {
}
