package hhplus.ecommerce.server.interfaces.common.api;

public abstract class ApiException extends RuntimeException {

    protected ApiException(String message) {
        super(message);
    }
}
