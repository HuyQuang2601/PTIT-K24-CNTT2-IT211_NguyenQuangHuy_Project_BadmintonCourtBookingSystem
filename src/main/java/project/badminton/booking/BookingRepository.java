package project.badminton.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import project.badminton.user.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn(
            Long courtId,
            LocalDate bookingDate,
            Long timeSlotId,
            Collection<BookingStatus> statuses
    );

    boolean existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusInAndIdNot(
            Long courtId,
            LocalDate bookingDate,
            Long timeSlotId,
            Collection<BookingStatus> statuses,
            Long id
    );

    List<Booking> findByCustomerOrderByBookingDateDescCreatedAtDesc(User customer);

    List<Booking> findByBookingDateAndStatus(LocalDate bookingDate, BookingStatus status);

    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);
}
