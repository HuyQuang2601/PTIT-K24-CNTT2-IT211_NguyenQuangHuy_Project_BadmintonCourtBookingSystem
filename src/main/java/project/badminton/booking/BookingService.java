package project.badminton.booking;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.badminton.booking.dto.BookingCreateRequest;
import project.badminton.booking.dto.BookingResponse;
import project.badminton.common.BusinessException;
import project.badminton.court.Court;
import project.badminton.court.CourtRepository;
import project.badminton.court.dto.CourtResponse;
import project.badminton.timeslot.TimeSlot;
import project.badminton.timeslot.TimeSlotService;
import project.badminton.user.User;
import project.badminton.user.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private static final Set<BookingStatus> BLOCKING_STATUSES = Set.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotService timeSlotService;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            TimeSlotService timeSlotService,
            UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.timeSlotService = timeSlotService;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse createBooking(String username, BookingCreateRequest request) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
        Court court = courtRepository.findById(request.courtId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy sân cầu lông"));
        TimeSlot timeSlot = timeSlotService.findById(request.timeSlotId());

        if (!court.isActive() || !timeSlot.isActive()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Sân hoặc khung giờ hiện không hoạt động");
        }
        if (hasConflict(court.getId(), request.bookingDate(), timeSlot.getId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Sân đã được đặt trong khung giờ này");
        }

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setCourt(court);
        booking.setTimeSlot(timeSlot);
        booking.setBookingDate(request.bookingDate());
        booking.setNote(request.note());
        booking.setStatus(BookingStatus.PENDING);
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> customerHistory(String username) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"));
        return bookingRepository.findByCustomerOrderByBookingDateDescCreatedAtDesc(customer).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> pendingBookings() {
        return bookingRepository.findByStatusOrderByCreatedAtDesc(BookingStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> bookingsByDateAndStatus(LocalDate date, BookingStatus status) {
        return bookingRepository.findByBookingDateAndStatus(date, status).stream()
                .filter(booking -> booking.getCourt().isActive())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CourtResponse> availableCourts(LocalDate date, Long timeSlotId) {
        TimeSlot timeSlot = timeSlotService.findById(timeSlotId);
        if (!timeSlot.isActive()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Khung giờ hiện không hoạt động");
        }
        return courtRepository.findByActiveTrue().stream()
                .filter(court -> !hasConflict(court.getId(), date, timeSlotId))
                .map(court -> new CourtResponse(
                        court.getId(),
                        court.getName(),
                        court.getAddress(),
                        court.getDescription(),
                        court.getHourlyPrice(),
                        court.isActive(),
                        List.copyOf(court.getImageUrls()),
                        court.getManager() == null ? null : court.getManager().getId()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse approve(Long id) {
        Booking booking = findBooking(id);
        requireStatus(booking, BookingStatus.PENDING, "Chỉ có thể phê duyệt lịch đặt đang chờ xử lý");
        if (bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusInAndIdNot(
                booking.getCourt().getId(),
                booking.getBookingDate(),
                booking.getTimeSlot().getId(),
                BLOCKING_STATUSES,
                booking.getId()
        )) {
            throw new BusinessException(HttpStatus.CONFLICT, "Sân đã được đặt trong khung giờ này");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse reject(Long id) {
        Booking booking = findBooking(id);
        requireStatus(booking, BookingStatus.PENDING, "Chỉ có thể từ chối lịch đặt đang chờ xử lý");
        booking.setStatus(BookingStatus.REJECTED);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse checkIn(Long id) {
        Booking booking = findBooking(id);
        requireStatus(booking, BookingStatus.CONFIRMED, "Chỉ có thể check-in lịch đặt đã được xác nhận");
        booking.setStatus(BookingStatus.CHECKED_IN);
        return toResponse(booking);
    }

    private boolean hasConflict(Long courtId, LocalDate date, Long timeSlotId) {
        return bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn(courtId, date, timeSlotId, BLOCKING_STATUSES);
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch đặt sân"));
    }

    private void requireStatus(Booking booking, BookingStatus requiredStatus, String message) {
        if (booking.getStatus() != requiredStatus) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
    }

    public BookingResponse toResponse(Booking booking) {
        TimeSlot slot = booking.getTimeSlot();
        return new BookingResponse(
                booking.getId(),
                booking.getCustomer().getId(),
                booking.getCustomer().getFullName(),
                booking.getCourt().getId(),
                booking.getCourt().getName(),
                slot.getId(),
                slot.getStartTime() + "-" + slot.getEndTime(),
                booking.getBookingDate(),
                booking.getStatus(),
                booking.getNote(),
                booking.getCreatedAt()
        );
    }
}
