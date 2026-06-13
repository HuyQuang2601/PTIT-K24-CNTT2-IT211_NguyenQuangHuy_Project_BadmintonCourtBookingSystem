package project.badminton.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.badminton.common.BusinessException;
import project.badminton.court.CourtService;
import project.badminton.court.dto.CourtResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg");

    private final CloudStorageService cloudStorageService;
    private final CourtService courtService;
    private final long maxFileBytes;
    private final int maxFiles;

    public FileStorageService(
            CloudStorageService cloudStorageService,
            CourtService courtService,
            @Value("${app.upload.max-file-bytes}") long maxFileBytes,
            @Value("${app.upload.max-files:5}") int maxFiles
    ) {
        this.cloudStorageService = cloudStorageService;
        this.courtService = courtService;
        this.maxFileBytes = maxFileBytes;
        this.maxFiles = maxFiles;
    }

    public String upload(MultipartFile file) throws IOException {
        validate(file);
        return cloudStorageService.upload(file);
    }

    public CourtResponse uploadCourtImage(Long courtId, MultipartFile file) throws IOException {
        String imageUrl = upload(file);
        return courtService.addImage(courtId, imageUrl);
    }

    public List<String> uploadAll(List<MultipartFile> files) throws IOException {
        validateFiles(files);
        try {
            return files.stream()
                    .map(file -> {
                        try {
                            return cloudStorageService.upload(file);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    })
                    .toList();
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    public CourtResponse uploadCourtImages(Long courtId, List<MultipartFile> files) throws IOException {
        return courtService.addImages(courtId, uploadAll(files));
    }

    private void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cần chọn ít nhất một ảnh");
        }
        if (files.size() > maxFiles) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Chỉ được tải lên tối đa " + maxFiles + " ảnh");
        }
        files.forEach(this::validate);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Vui lòng chọn tệp cần tải lên");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Chỉ chấp nhận ảnh định dạng PNG hoặc JPG");
        }
        if (file.getSize() > maxFileBytes) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Dung lượng tệp vượt quá giới hạn cho phép");
        }
    }
}
