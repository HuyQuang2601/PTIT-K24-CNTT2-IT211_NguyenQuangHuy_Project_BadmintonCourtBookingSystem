package project.badminton.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.badminton.common.BusinessException;
import project.badminton.court.CourtService;
import project.badminton.court.dto.CourtResponse;

import java.io.IOException;
import java.util.Set;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg");

    private final CloudStorageService cloudStorageService;
    private final CourtService courtService;
    private final long maxFileBytes;

    public FileStorageService(
            CloudStorageService cloudStorageService,
            CourtService courtService,
            @Value("${app.upload.max-file-bytes}") long maxFileBytes
    ) {
        this.cloudStorageService = cloudStorageService;
        this.courtService = courtService;
        this.maxFileBytes = maxFileBytes;
    }

    public String upload(MultipartFile file) throws IOException {
        validate(file);
        return cloudStorageService.upload(file);
    }

    public CourtResponse uploadCourtImage(Long courtId, MultipartFile file) throws IOException {
        String imageUrl = upload(file);
        return courtService.addImage(courtId, imageUrl);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only PNG and JPG images are allowed");
        }
        if (file.getSize() > maxFileBytes) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "File size exceeds allowed limit");
        }
    }
}
