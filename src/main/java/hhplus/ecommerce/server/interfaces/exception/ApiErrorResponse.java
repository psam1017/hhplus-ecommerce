package hhplus.ecommerce.server.interfaces.exception;

public record ApiErrorResponse(
        String code,
        String message
) {
}
