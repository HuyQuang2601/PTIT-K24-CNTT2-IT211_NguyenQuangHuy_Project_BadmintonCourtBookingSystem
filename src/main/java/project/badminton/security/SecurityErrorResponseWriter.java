package project.badminton.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class SecurityErrorResponseWriter {
    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
                {"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}
                """.formatted(
                LocalDateTime.now(),
                status.value(),
                escape(errorName(status)),
                escape(message),
                escape(request.getRequestURI())
        ).trim());
    }

    private String errorName(HttpStatus status) {
        return switch (status) {
            case UNAUTHORIZED -> "Chưa xác thực";
            case FORBIDDEN -> "Không có quyền truy cập";
            default -> "Lỗi bảo mật";
        };
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
