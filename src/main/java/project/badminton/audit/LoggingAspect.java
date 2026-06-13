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
            LOGGER.info("[HIỆU NĂNG - THÀNH CÔNG] chức_năng={} thời_gian_ms={}", operation, elapsedMillis(startedAt));
            return result;
        } catch (Throwable throwable) {
            LOGGER.warn("[HIỆU NĂNG - THẤT BẠI] chức_năng={} thời_gian_ms={} loại_lỗi={}",
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
        String message = "[KIỂM TOÁN - THÀNH CÔNG] Khách hàng " + username
                + " đã đặt sân " + result.courtName()
                + " vào ngày " + result.bookingDate()
                + ", khung giờ " + result.timeRange();
        auditLogService.save("TAO_LICH_DAT_SAN", username, message, true);
    }

    @AfterThrowing(pointcut = "execution(* project.badminton.booking.BookingService.createBooking(..))", throwing = "exception")
    public void logBookingFailure(JoinPoint joinPoint, Exception exception) {
        BookingCreateRequest request = requestFrom(joinPoint);
        String username = usernameFrom(joinPoint);
        String target = request == null
                ? "sân/khung giờ không xác định"
                : "mã sân=" + request.courtId() + ", ngày=" + request.bookingDate() + ", mã khung giờ=" + request.timeSlotId();
        String message = "[KIỂM TOÁN - THẤT BẠI] Khách hàng " + username
                + " đã thử đặt " + target
                + " nhưng thất bại vì " + exception.getMessage();
        auditLogService.save("TAO_LICH_DAT_SAN", username, message, false);
    }

    private String usernameFrom(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        return args.length > 0 && args[0] instanceof String username ? username : "ẩn danh";
    }

    private BookingCreateRequest requestFrom(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        return args.length > 1 && args[1] instanceof BookingCreateRequest request ? request : null;
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
