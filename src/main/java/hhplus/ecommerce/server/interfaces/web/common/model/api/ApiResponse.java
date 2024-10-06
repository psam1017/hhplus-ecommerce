package hhplus.ecommerce.server.interfaces.web.common.model.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

@Getter
public class ApiResponse<T> {

    private final String code;
    private final String message;
    private final String debug;
    private final T data;

    private ApiResponse(ApiStatus apiStatus, String message, String debug, T data){
        this.code = apiStatus.code();
        this.message = message;
        this.debug = debug;
        this.data = data;
    }

    public static <T> ApiResponse<T> of(ApiStatus status, String message, String debug, T data) {
        return new ApiResponse<>(status, message, debug, data);
    }

    public static <T> ApiResponse<T> of(ApiStatus status, T data) {
        return ApiResponse.of(status, status.message(), status.message(), data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.of(ApiStatus.OK, data);
    }

    public static <T> ApiResponse<T> error(ApiStatus status, Class<?> clazz, String debug, T data) {
        return new ApiResponse<>(status, clazz.getSimpleName(), debug, data);
    }

    /*
     * Validation 에서 검증되지 않은 객체에 대한 응답값은 클라이언트 측과 협의할 여지가 있습니다.
     * 파라미터 유효성 검사는 최대한 프론트엔트에서 검증하여 잘못된 값 자체가 서버로 전달되지 않도록 해야 합니다.
     * 하지만 그럼에도 서버에서는 한 번 더 값을 검증해야 하고, 이때의 응답값을 어떻게 처리할지는 프론트엔드와 협의합니다.
     * 현재 예시에서는 유효성 검사에 실패했을 때의 첫 번째 필드 에러 메시지를 응답값으로 사용합니다.
     * 메시징 및 국제화를 사용할 수도 있고, 또는 개발용 서버에서만 디버깅 메시지를 보여주는 등의 처리를 할 수도 있을 겁니다.
     *
     * 모쪼록 클라이언트인 프론트엔드에게 맞춰주도록 합시다.
     */
    public static ApiResponse<Object> badRequest(BindException e) {
        String message;
        String debug;
        if (ObjectUtils.isEmpty(e.getFieldErrors())) {
            message = debug = "유효성 검사에 실패했습니다.";
        } else {
            FieldError error = e.getFieldErrors().get(0);
            message = debug = error.getDefaultMessage();
        }

        return new ApiResponse<>(
                ApiStatus.BAD_REQUEST,
                message,
                debug,
                null
        );
    }

    public static ApiResponse<Object> badRequest(ConstraintViolationException e) {
        String message;
        String debug;
        if (ObjectUtils.isEmpty(e.getConstraintViolations())) {
            message = debug = "유효성 검사에 실패했습니다.";
        } else {
            ConstraintViolation<?> violation = e.getConstraintViolations().iterator().next();
            message = debug = violation.getMessage();
        }

        return new ApiResponse<>(
                ApiStatus.BAD_REQUEST,
                message,
                debug,
                null
        );
    }
}
