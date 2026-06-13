package project.badminton.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập tài nguyên này", request);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleCloudStorage(IOException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ lưu trữ đám mây tạm thời không khả dụng. Vui lòng thử lại sau.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống đã xảy ra lỗi nội bộ", request);
    }

    private String formatFieldError(FieldError error) {
        return error.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorName(status),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(response);
    }

    private String errorName(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Yêu cầu không hợp lệ";
            case UNAUTHORIZED -> "Chưa xác thực";
            case FORBIDDEN -> "Không có quyền truy cập";
            case NOT_FOUND -> "Không tìm thấy tài nguyên";
            case CONFLICT -> "Xung đột dữ liệu";
            case SERVICE_UNAVAILABLE -> "Dịch vụ không khả dụng";
            case INTERNAL_SERVER_ERROR -> "Lỗi hệ thống";
            default -> "Yêu cầu thất bại";
        };
    }
}
