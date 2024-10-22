package hhplus.ecommerce.server.interfaces.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice("hhplus.ecommerce.server.interfaces.controller")
public class WebControllerAdvice {

    // API 호출를 호출했을 때 그 내부 로직에서 발생하는 예외들을 관리
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex, HttpServletRequest request) {

        log.info("[ApiException handle] URI = {}", request.getRequestURI());
        log.info("[reason] {}", ex.getMessage(), ex);

        ApiErrorResponse responseBody = new ApiErrorResponse(ex.getCode(), ex.getMessage());
        return new ResponseEntity<>(responseBody, BAD_REQUEST);
    }

    // 유효성 검사에서 실패했을 때 발생하는 예외들을 관리
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(BindException ex, HttpServletRequest request) {

        // 발견되면 프론트엔드에서 유효성 검사를 해달라고 요청하기
        log.error("[BindException handle] URI = {}", request.getRequestURI());

        String message = null;
        if (!ObjectUtils.isEmpty(ex.getFieldErrors())) {
            FieldError error = ex.getFieldErrors().get(0);
            message = error.getDefaultMessage();
        }
        if (ObjectUtils.isEmpty(message)) {
            message = "유효성 검사에 실패했습니다.";
        }
        log.error("[reason] {}", message, ex);

        ApiErrorResponse responseBody = new ApiErrorResponse(BindException.class.getSimpleName(), message);
        return new ResponseEntity<>(responseBody, BAD_REQUEST);
    }

    // 유효성 검사에서 실패했을 때 발생하는 예외들을 관리
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {

        // 발견되면 프론트엔드에서 유효성 검사를 해달라고 요청하기
        log.error("[ConstraintViolationException handle] URI = {}", request.getRequestURI());

        String message = null;
        if (!ObjectUtils.isEmpty(ex.getConstraintViolations())) {
            ConstraintViolation<?> violation = ex.getConstraintViolations().iterator().next();
            message = violation.getMessage();
        }
        if (ObjectUtils.isEmpty(message)) {
            message = "유효성 검사에 실패했습니다.";
        }
        log.error("[reason] {}", message, ex);

        ApiErrorResponse responseBody = new ApiErrorResponse(ConstraintViolationException.class.getSimpleName(), message);
        return new ResponseEntity<>(responseBody, BAD_REQUEST);
    }

    // 예외 메시지를 클라이언트에게서 숨기고, 로깅만 수행
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, HttpServletRequest request) {

        log.error("[Exception handle] URI = {}", request.getRequestURI());
        log.error("[reason] {}", ex.getMessage(), ex);

        ApiErrorResponse responseBody = new ApiErrorResponse(Exception.class.getSimpleName(), "서버에서 오류가 발생했습니다.");
        return new ResponseEntity<>(responseBody, INTERNAL_SERVER_ERROR);
    }
}
