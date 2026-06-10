package project.badminton.file;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "dev", matchIfMissing = true)
public class DevCloudStorageService implements CloudStorageService {
    @Override
    public String upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Empty file cannot be uploaded");
        }
        String extension = extension(file.getOriginalFilename());
        return "https://storage.local/badminton/" + UUID.randomUUID() + extension;
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
