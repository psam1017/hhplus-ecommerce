package hhplus.ecommerce.server.interfaces.exception;

public abstract class ApiException extends RuntimeException {

    private final String code;

    public ApiException(String message) {
        super(message);
        this.code = this.getClass().getSimpleName();
    }

    public String getCode() {
        return code;
    }
}
