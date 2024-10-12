package hhplus.ecommerce.server.interfaces.common.exception;

import hhplus.ecommerce.server.interfaces.common.api.ApiException;
import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RestControllerAdvice("hhplus.ecommerce.server.interfaces.controller")
public class WebControllerAdvice {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex, HttpServletRequest request) {

        log.error("[ApiException handle] URI = {}", request.getRequestURI());
        log.error("[reason] {}", ex.getResponse().getMessage());

        return new ResponseEntity<>(ex.getResponse(), BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(BindException ex, HttpServletRequest request) {

        log.error("[BindException handle] URI = {}", request.getRequestURI());

        ApiResponse<Object> body = ApiResponse.badRequest(ex);
        log.error("[reason] {}", body.getMessage());

        return new ResponseEntity<>(body, BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {

        log.error("[ConstraintViolationException handle] URI = {}", request.getRequestURI());

        ApiResponse<Object> body = ApiResponse.badRequest(ex);
        log.error("[reason] {}", body.getMessage());

        return new ResponseEntity<>(body, BAD_REQUEST);
    }
}
