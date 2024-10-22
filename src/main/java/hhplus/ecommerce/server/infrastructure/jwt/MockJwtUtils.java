package hhplus.ecommerce.server.infrastructure.jwt;

public class MockJwtUtils implements JwtUtils {
    @Override
    public boolean hasId(String token) {
        return true;
    }

    @Override
    public String getId(String token) {
        return "1";
    }
}
