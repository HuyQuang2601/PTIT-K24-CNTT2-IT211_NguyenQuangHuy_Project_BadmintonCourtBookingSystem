package project.badminton.court;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.badminton.common.BusinessException;
import project.badminton.court.dto.CourtRequest;
import project.badminton.court.dto.CourtResponse;
import project.badminton.user.User;
import project.badminton.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourtService {
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;

    public CourtService(CourtRepository courtRepository, UserRepository userRepository) {
        this.courtRepository = courtRepository;
        this.userRepository = userRepository;
    }

    public Page<CourtResponse> search(String keyword, Pageable pageable) {
        String value = keyword == null ? "" : keyword;
        return courtRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(value, value, pageable)
                .map(this::toResponse);
    }

    public List<CourtResponse> activeCourts() {
        return courtRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public CourtResponse get(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public CourtResponse create(String managerUsername, CourtRequest request) {
        User manager = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Manager not found"));
        Court court = new Court();
        apply(request, court);
        court.setManager(manager);
        return toResponse(courtRepository.save(court));
    }

    @Transactional
    public CourtResponse update(Long id, CourtRequest request) {
        Court court = findById(id);
        apply(request, court);
        return toResponse(court);
    }

    @Transactional
    public void delete(Long id) {
        if (!courtRepository.existsById(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Court not found");
        }
        courtRepository.deleteById(id);
    }

    @Transactional
    public CourtResponse addImage(Long courtId, String imageUrl) {
        Court court = findById(courtId);
        court.getImageUrls().add(imageUrl);
        return toResponse(court);
    }

    public Court findById(Long id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Court not found"));
    }

    private void apply(CourtRequest request, Court court) {
        court.setName(request.name());
        court.setAddress(request.address());
        court.setDescription(request.description());
        court.setHourlyPrice(request.hourlyPrice());
        court.setActive(request.active());
        court.setImageUrls(request.imageUrls() == null ? new ArrayList<>() : new ArrayList<>(request.imageUrls()));
    }

    private CourtResponse toResponse(Court court) {
        Long managerId = court.getManager() == null ? null : court.getManager().getId();
        return new CourtResponse(
                court.getId(),
                court.getName(),
                court.getAddress(),
                court.getDescription(),
                court.getHourlyPrice(),
                court.isActive(),
                List.copyOf(court.getImageUrls()),
                managerId
        );
    }
}
