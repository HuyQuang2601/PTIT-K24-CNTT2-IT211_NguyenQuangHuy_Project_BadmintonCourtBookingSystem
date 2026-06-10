package project.badminton.file;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import project.badminton.common.ApiResponse;
import project.badminton.court.dto.CourtResponse;
import project.badminton.file.dto.FileUploadResponse;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> upload(@RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok("File uploaded successfully", new FileUploadResponse(fileStorageService.upload(file))));
    }

    @PostMapping("/courts/{courtId}/images")
    public ResponseEntity<ApiResponse<CourtResponse>> uploadCourtImage(
            @PathVariable Long courtId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok("Court image uploaded successfully", fileStorageService.uploadCourtImage(courtId, file)));
    }
}
