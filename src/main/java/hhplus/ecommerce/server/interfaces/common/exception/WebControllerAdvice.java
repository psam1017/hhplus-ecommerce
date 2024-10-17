package hhplus.ecommerce.server.interfaces.common.exception;

import hhplus.ecommerce.server.interfaces.common.api.ApiException;
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

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RestControllerAdvice("hhplus.ecommerce.server.interfaces.controller")
public class WebControllerAdvice {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex, HttpServletRequest request) {

        log.error("[ApiException handle] URI = {}", request.getRequestURI());
        log.error("[reason] {}", ex.getMessage());

        return new ResponseEntity<>(Map.of("error", ex.getMessage()), BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(BindException ex, HttpServletRequest request) {

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

        return new ResponseEntity<>(Map.of("error", message), BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {

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

        return new ResponseEntity<>(Map.of("error", message), BAD_REQUEST);
    }
}
