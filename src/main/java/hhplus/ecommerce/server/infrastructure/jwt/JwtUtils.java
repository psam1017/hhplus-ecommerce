package hhplus.ecommerce.server.infrastructure.jwt;

public interface JwtUtils {

    boolean hasId(String token);
    String getId(String token);
}
