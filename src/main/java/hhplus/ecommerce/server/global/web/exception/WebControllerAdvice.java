package hhplus.ecommerce.server.global.web.exception;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import hhplus.ecommerce.server.infrastructure.jwt.IllegalTokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import hhplus.ecommerce.server.global.web.api.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import hhplus.ecommerce.server.global.web.api.ApiResponse;
import hhplus.ecommerce.server.global.web.api.ApiStatus;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class WebControllerAdvice {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex, HttpServletRequest request) {

        log.error("[ApiException handle] URI = {}", request.getRequestURI());
        log.error("[reason] {}", ex.getResponse().getDebug());

        return new ResponseEntity<>(ex.getResponse(), OK);
    }

    @ExceptionHandler(IllegalTokenException.class)
    public ResponseEntity<Object> handleIllegalTokenException(IllegalTokenException ex) {
        return new ResponseEntity<>(ex.getResponse(), UNAUTHORIZED);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(BindException ex, HttpServletRequest request) {

        log.error("[BindException handle] URI = {}", request.getRequestURI());

        ApiResponse<Object> body = ApiResponse.badRequest(ex);
        log.error("[reason] {}", body.getDebug());

        return new ResponseEntity<>(body, BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {

        log.error("[ConstraintViolationException handle] URI = {}", request.getRequestURI());

        ApiResponse<Object> body = ApiResponse.badRequest(ex);
        log.error("[reason] {}", body.getDebug());

        return new ResponseEntity<>(body, BAD_REQUEST);
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<Object> handleUnrecognizedPropertyException(UnrecognizedPropertyException ex, HttpServletRequest request) {

        log.error("[UnrecognizedPropertyException handle] URI = {}", request.getRequestURI(), ex);

        String debug = ex.getPropertyName() + " 라는 필드는 없습니다.";
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.BAD_REQUEST, ApiStatus.BAD_REQUEST.message(), debug, null);

        return new ResponseEntity<>(body, BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException ex, HttpServletRequest request) {

        log.error("[InvalidFormatException handle] URI = {}", request.getRequestURI(), ex);

        String debug = ex.getValue() + " 의 포맷이 이상합니다. 한 번 확인해보세요.";
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.BAD_REQUEST, ApiStatus.BAD_REQUEST.message(), debug, null);

        return new ResponseEntity<>(body, BAD_REQUEST);
    }

    @ExceptionHandler(JacksonException.class)
    public ResponseEntity<ApiResponse<Object>> handleJacksonException(JacksonException ex, HttpServletRequest request) {

        log.error("[JacksonException handle] URI = {}", request.getRequestURI(), ex);

        String debug = "JSON 파싱 과정에서 예외가 발생했습니다. 백엔드 개발자에게 문의해주세요.";
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.BAD_REQUEST, ApiStatus.BAD_REQUEST.message(), debug, null);

        return new ResponseEntity<>(body,  BAD_REQUEST);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(MultipartException ex, HttpServletRequest request) {

        log.error("[MultipartException handle] URI = {}", request.getRequestURI(), ex);

        String debug = "멀티파트 파일과 관련하여 예외가 발생했습니다. 혹시 모르니 멀티파트 파일을 제대로 보냈는지도 확인해보세요.";
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.INTERNAL_SERVER_ERROR, ApiStatus.INTERNAL_SERVER_ERROR.message(), debug, null);

        return new ResponseEntity<>(body, INTERNAL_SERVER_ERROR);
    }
}
