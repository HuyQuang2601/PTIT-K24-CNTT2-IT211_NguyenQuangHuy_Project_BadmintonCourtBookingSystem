package project.badminton.timeslot;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.badminton.common.BusinessException;
import project.badminton.timeslot.dto.TimeSlotRequest;
import project.badminton.timeslot.dto.TimeSlotResponse;

import java.util.Comparator;
import java.util.List;

@Service
public class TimeSlotService {
    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<TimeSlotResponse> activeTimeSlots() {
        return timeSlotRepository.findByActiveTrueOrderByStartTimeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TimeSlotResponse> all() {
        return timeSlotRepository.findAll().stream()
                .sorted(Comparator.comparing(TimeSlot::getStartTime))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TimeSlotResponse create(TimeSlotRequest request) {
        validate(request);
        TimeSlot timeSlot = new TimeSlot();
        apply(request, timeSlot);
        return toResponse(timeSlotRepository.save(timeSlot));
    }

    @Transactional
    public TimeSlotResponse update(Long id, TimeSlotRequest request) {
        validate(request);
        TimeSlot timeSlot = findById(id);
        apply(request, timeSlot);
        return toResponse(timeSlot);
    }

    @Transactional
    public void delete(Long id) {
        if (!timeSlotRepository.existsById(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy khung giờ");
        }
        timeSlotRepository.deleteById(id);
    }

    public TimeSlot findById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy khung giờ"));
    }

    private void validate(TimeSlotRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Thời gian kết thúc phải sau thời gian bắt đầu");
        }
    }

    private void apply(TimeSlotRequest request, TimeSlot timeSlot) {
        timeSlot.setStartTime(request.startTime());
        timeSlot.setEndTime(request.endTime());
        timeSlot.setActive(request.active());
    }

    private TimeSlotResponse toResponse(TimeSlot timeSlot) {
        return new TimeSlotResponse(timeSlot.getId(), timeSlot.getStartTime(), timeSlot.getEndTime(), timeSlot.isActive());
    }
}
