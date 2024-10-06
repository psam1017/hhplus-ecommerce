package hhplus.ecommerce.server.interfaces.web.common.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import hhplus.ecommerce.server.interfaces.web.common.model.api.ApiResponse;
import hhplus.ecommerce.server.interfaces.web.common.model.api.ApiStatus;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.springframework.http.HttpStatus.*;

/**
 * -- Caution --
 * ResponseEntityExceptionHandler basically returns a ResponseEntity with org.springframework.http.ProblemDetail, Representation for an RFC 7807 problem details in the body.
 * if you extend ResponseEntityExceptionHandler and return a custom problem details in the body instead of ProblemDetail, You may not standardize your API as Http API.
 */
@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final ObjectMapper objectMapper;

    /**
     * You may need to return body in ResponseEntity as String, if an Exception is instance of ErrorResponse(Not Always).
     * refer to 'writeWithMessageConverters' method in org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor to find out the reason.
     */
    private String writeJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.error("Failed to create error messages.", e);
            return "Failed to create error messages.";
        }
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        HttpServletRequest httpServletRequest = ((ServletWebRequest) request).getRequest();
        log.error("[HttpRequestMethodNotSupportedException handle] URI = {} {}", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());

        String debug = ex.getMethod() + " 는 지원하지 않는 메소드입니다. 다음 메소드를 사용해보세요. " + ex.getSupportedHttpMethods();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.METHOD_NOT_ALLOWED, ApiStatus.METHOD_NOT_ALLOWED.message(), debug, null);

        return createResponseEntity(body, METHOD_NOT_ALLOWED);
    }

    private ResponseEntity<Object> createResponseEntity(ApiResponse<Object> body, HttpStatus status) {
        return new ResponseEntity<>(writeJson(body), status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[HttpMediaTypeNotSupportedException handle] URI = " + ((ServletWebRequest)request).getRequest().getRequestURI());

        String debug = ex.getContentType() + " 는 지원하지 않는 미디어 타입입니다. 다음 미디어 타입으로 보내주세요. " + ex.getSupportedMediaTypes();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.UNSUPPORTED_MEDIA_TYPE, ApiStatus.UNSUPPORTED_MEDIA_TYPE.message(), debug, null);

        return createResponseEntity(body, UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[HttpMediaTypeNotAcceptableException handle] URI = " + ((ServletWebRequest)request).getRequest().getRequestURI());

        String debug = "제공할 수 없는 미디어 타입입니다. 다음 미디어 타입으로 받아주세요. " + ex.getSupportedMediaTypes();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.NOT_ACCEPTABLE, ApiStatus.NOT_ACCEPTABLE.message(), debug, null);

        return createResponseEntity(body, NOT_ACCEPTABLE);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[MissingPathVariableException handle] URI = " + ((ServletWebRequest)request).getRequest().getRequestURI());

        String debug = "필수 경로 매개변수를 보내주세요. " + ex.getVariableName();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.BAD_REQUEST, ApiStatus.BAD_REQUEST.message(), debug, null);

        return createResponseEntity(body, BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[MissingServletRequestParameterException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI());

        String debug = "필수 쿼리 매개변수를 보내주세요. " + ex.getParameterName();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.BAD_REQUEST, ApiStatus.BAD_REQUEST.message(), debug, null);

        return createResponseEntity(body, BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[MissingServletRequestPartException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI());

        String debug = "필수 멀티파트 파일을 보내주세요. " + ex.getRequestPartName();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.BAD_REQUEST, ApiStatus.BAD_REQUEST.message(), debug, null);

        return createResponseEntity(body, BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[ServletRequestBindingException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI(), ex);

        String debug = "Http 통신에 필요한 필수 Header 가 없습니다. 혹시 Header 설정을 잘못한 건 아닌지 확인해보세요.";
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.PRECONDITION_REQUIRED, ApiStatus.PRECONDITION_REQUIRED.message(), debug, null);

        return createResponseEntity(body, PRECONDITION_REQUIRED);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[MethodArgumentNotValidException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI(), ex);

        ApiResponse<Object> body = ApiResponse.badRequest(ex);
        return createResponseEntity(body, BAD_REQUEST);
    }

    /**
     * It may be because you didn't use @Valid, or BindingResult. Check the handler that raised this exception.
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[HandlerMethodValidationException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI(), ex);

        String debug = "백엔드에서 유효성 검사에 실패했습니다. 백엔드 코드에 오타가 있을 수 있으니 바로 노드블랙 백엔드 개발자한테 알려주세요." + ex.getMessage();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.INTERNAL_SERVER_ERROR, ApiStatus.INTERNAL_SERVER_ERROR.message(), debug, null);

        return createResponseEntity(body, INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[NoHandlerFoundException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI());

        String debug = "존재하지 않는 API(또는 리소스)입니다. " + ex.getHttpMethod() + " /" + ex.getRequestURL();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.NOT_FOUND, ApiStatus.NOT_FOUND.message(), debug, null);

        return createResponseEntity(body, NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        HttpServletRequest httpServletRequest = ((ServletWebRequest) request).getRequest();
        log.error("[NoResourceFoundException handle] URI = {} {}", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());

        String debug = "존재하지 않는 API(또는 리소스)입니다. " + ex.getHttpMethod() + " /" + ex.getResourcePath();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.NOT_FOUND, ApiStatus.NOT_FOUND.message(), debug, null);

        return createResponseEntity(body, NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[AsyncRequestTimeoutException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI());

        String debug = "백엔드 서버에 싱크가 잠시 안 맞는 것 같습니다. 잠시 후 시도해보고 만약 문제가 생기면 백엔드 개발자한테 알려주세요.";
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.SERVICE_UNAVAILABLE, ApiStatus.SERVICE_UNAVAILABLE.message(), debug, null);

        return createResponseEntity(body, SERVICE_UNAVAILABLE);
    }

    @Override
    protected ResponseEntity<Object> handleErrorResponseException(ErrorResponseException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[ErrorResponseException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI(), ex);

        String debug = "백엔드 내부적으로 예외가 발생했습니다. 노드블랙 백엔드 개발자에게 바로 알려주세요." + ex.getMessage();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.INTERNAL_SERVER_ERROR, ApiStatus.INTERNAL_SERVER_ERROR.message(), debug, null);

        return createResponseEntity(body, INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[MaxUploadSizeExceededException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI());

        String debug = "전송 가능한 최대 파일 용량을 초과했습니다. 최대 용량은 " + ex.getMaxUploadSize() / 1024 / 1024 + "MB 입니다.";
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.BAD_REQUEST, ApiStatus.BAD_REQUEST.message(), debug, null);

        return createResponseEntity(body, BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[ConversionNotSupportedException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI(), ex);

        String debug = "백엔드 내부적으로 예외가 발생했습니다. 노드블랙 백엔드 개발자에게 바로 알려주세요." + ex.getMessage();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.INTERNAL_SERVER_ERROR, ApiStatus.INTERNAL_SERVER_ERROR.message(), debug, null);

        return createResponseEntity(body, INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[TypeMismatchException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI());

        String debug;
        if (ex.getRequiredType() == null) {
            debug = ex.getPropertyName() + " 의 타입을 확인해주세요.";
        } else {
            debug = ex.getPropertyName() + " 의 타입은 다음과 같습니다. " + ex.getRequiredType().getName();
        }
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.BAD_REQUEST, ApiStatus.BAD_REQUEST.message(), debug, null);

        return createResponseEntity(body, BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[HttpMessageNotReadableException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI());

        String debug = "Http 요청 메시지를 파싱하지 못 했습니다. Request Body 를 확인해보세요.";
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.BAD_REQUEST, ApiStatus.BAD_REQUEST.message(), debug, null);

        return createResponseEntity(body, BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        log.error("[HttpMessageNotWritableException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI(), ex);

        String debug = "Http 응답 메시지를 파싱하지 못 했습니다. 즉시 백엔드 개발자한테 알려주세요." + ex.getMessage();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.INTERNAL_SERVER_ERROR, ApiStatus.INTERNAL_SERVER_ERROR.message(), debug, null);

        return createResponseEntity(body, INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodValidationException(MethodValidationException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        log.error("[MethodValidationException handle] URI = {}", ((ServletWebRequest)request).getRequest().getRequestURI(), ex);

        String debug = "유효성 검사와 관련해서 예외가 발생한 것 같습니다. 즉시 백엔드 개발자한테 알려주세요." + ex.getMessage();
        ApiResponse<Object> body = ApiResponse.of(ApiStatus.INTERNAL_SERVER_ERROR, ApiStatus.INTERNAL_SERVER_ERROR.message(), debug, null);

        return createResponseEntity(body, INTERNAL_SERVER_ERROR);
    }
}
