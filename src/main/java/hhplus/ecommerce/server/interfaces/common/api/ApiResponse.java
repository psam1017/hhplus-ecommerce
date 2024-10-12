package hhplus.ecommerce.server.interfaces.common.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

@Getter
public class ApiResponse<T> {

    private final String message;
    private final T data;

    private ApiResponse(String message, T data){
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("OK", data);
    }

    public static ApiResponse<Object> error(String meesage) {
        return new ApiResponse<>(meesage, null);
    }

    public static ApiResponse<Object> badRequest(BindException e) {
        String message;
        if (ObjectUtils.isEmpty(e.getFieldErrors())) {
            message = "유효성 검사에 실패했습니다.";
        } else {
            FieldError error = e.getFieldErrors().get(0);
            message = error.getDefaultMessage();
        }

        return new ApiResponse<>(
                message,
                null
        );
    }

    public static ApiResponse<Object> badRequest(ConstraintViolationException e) {
        String message;
        if (ObjectUtils.isEmpty(e.getConstraintViolations())) {
            message = "유효성 검사에 실패했습니다.";
        } else {
            ConstraintViolation<?> violation = e.getConstraintViolations().iterator().next();
            message = violation.getMessage();
        }

        return new ApiResponse<>(
                message,
                null
        );
    }
}
