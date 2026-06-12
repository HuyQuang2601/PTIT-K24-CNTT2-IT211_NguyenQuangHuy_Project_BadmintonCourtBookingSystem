package project.badminton.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import project.badminton.booking.dto.BookingCreateRequest;
import project.badminton.booking.dto.BookingResponse;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    private final AuditLogService auditLogService;

    public LoggingAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("execution(public * project.badminton..*Controller.*(..)) || execution(public * project.badminton..*Service.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        String operation = joinPoint.getSignature().toShortString();
        try {
            Object result = joinPoint.proceed();
            LOGGER.info("[PERFORMANCE - SUCCESS] operation={} durationMs={}", operation, elapsedMillis(startedAt));
            return result;
        } catch (Throwable throwable) {
            LOGGER.warn("[PERFORMANCE - FAILED] operation={} durationMs={} errorType={}",
                    operation,
                    elapsedMillis(startedAt),
                    throwable.getClass().getSimpleName());
            throw throwable;
        }
    }

    @AfterReturning(pointcut = "execution(* project.badminton.booking.BookingService.createBooking(..))", returning = "result")
    public void logBookingSuccess(JoinPoint joinPoint, BookingResponse result) {
        BookingCreateRequest request = requestFrom(joinPoint);
        String username = usernameFrom(joinPoint);
        String message = "[AUDIT - SUCCESS] Customer " + username
                + " booked court " + result.courtName()
                + " on " + result.bookingDate()
                + ", time slot " + result.timeRange();
        auditLogService.save("BOOKING_CREATE", username, message, true);
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
        auditLogService.save("BOOKING_CREATE", username, message, false);
    }

    private String usernameFrom(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        return args.length > 0 && args[0] instanceof String username ? username : "anonymous";
    }

    private BookingCreateRequest requestFrom(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        return args.length > 1 && args[1] instanceof BookingCreateRequest request ? request : null;
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
