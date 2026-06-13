package project.badminton.file;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "cloudinary")
public class CloudinaryStorageService implements CloudStorageService {
    private final Cloudinary cloudinary;
    private final String folder;

    public CloudinaryStorageService(
            @Value("${app.storage.cloudinary.cloud-name}") String cloudName,
            @Value("${app.storage.cloudinary.api-key}") String apiKey,
            @Value("${app.storage.cloudinary.api-secret}") String apiSecret,
            @Value("${app.storage.cloudinary.folder:badminton-courts}") String folder
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
        this.folder = folder;
    }

    @Override
    public String upload(MultipartFile file) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("resource_type", "image", "folder", folder)
        );
        Object secureUrl = result.get("secure_url");
        if (secureUrl == null) {
            throw new IOException("Phản hồi từ Cloudinary không chứa URL bảo mật");
        }
        return secureUrl.toString();
    }
}
