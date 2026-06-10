package project.badminton.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import project.badminton.booking.dto.BookingCreateRequest;
import project.badminton.booking.dto.BookingResponse;

@Aspect
@Component
public class LoggingAspect {
    private final AuditLogRepository auditLogRepository;

    public LoggingAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @AfterReturning(pointcut = "execution(* project.badminton.booking.BookingService.createBooking(..))", returning = "result")
    public void logBookingSuccess(JoinPoint joinPoint, BookingResponse result) {
        BookingCreateRequest request = requestFrom(joinPoint);
        String username = usernameFrom(joinPoint);
        String message = "[AUDIT - SUCCESS] Customer " + username
                + " booked court " + result.courtName()
                + " on " + result.bookingDate()
                + ", time slot " + result.timeRange();
        save("BOOKING_CREATE", username, message, true);
    }

    @AfterThrowing(pointcut = "execution(* project.badminton.booking.BookingService.createBooking(..))", throwing = "exception")
    public void logBookingFailure(JoinPoint joinPoint, Exception exception) {
        BookingCreateRequest request = requestFrom(joinPoint);
        String username = usernameFrom(joinPoint);
        String target = request == null
                ? "unknown court/time slot"
                : "courtId=" + request.courtId() + ", date=" + request.bookingDate() + ", timeSlotId=" + request.timeSlotId();
        String message = "[AUDIT - FAILED] Customer " + username
                + " attempted booking " + target
                + " but failed because " + exception.getMessage();
        save("BOOKING_CREATE", username, message, false);
    }

    private String usernameFrom(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        return args.length > 0 && args[0] instanceof String username ? username : "anonymous";
    }

    private BookingCreateRequest requestFrom(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        return args.length > 1 && args[1] instanceof BookingCreateRequest request ? request : null;
    }

    private void save(String action, String username, String message, boolean success) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setUsername(username);
        auditLog.setMessage(message);
        auditLog.setSuccess(success);
        auditLogRepository.save(auditLog);
    }
}
