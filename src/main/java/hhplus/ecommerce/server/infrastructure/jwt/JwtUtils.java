package hhplus.ecommerce.server.infrastructure.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

public class JwtUtils {

    private final Key signingKey;

    public static final long ACCESS_EXPIRATION = 1000 * 60 * 60;
    public static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    public JwtUtils(String secretKey) {
        this.signingKey = getSignInKey(secretKey);
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String subject, long jwtExpiration) {
        return generateToken(subject, jwtExpiration, Map.of());
    }

    public String generateToken(String subject, long jwtExpiration, Map<String, Object> extraClaims) {
        return buildToken(extraClaims, subject, jwtExpiration);
    }

    public String generateDefaultAccessToken(String subject, Map<String, Object> extraClaims) {
        return generateToken(subject, ACCESS_EXPIRATION, extraClaims);
    }

    public String generateDefaultRefreshToken(String subject, Map<String, Object> extraClaims) {
        return buildToken(extraClaims, subject, REFRESH_EXPIRATION);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // millisecond
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Optional<JwtStatus> hasInvalidStatus(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.of(JwtStatus.BLANK);
        }
        try {
            extractSubject(token);
        } catch (ExpiredJwtException e) {
            return Optional.of(JwtStatus.EXPIRED);
        } catch (SignatureException e) {
            return Optional.of(JwtStatus.ILLEGAL_SIGNATURE);
        } catch (UnsupportedJwtException e) {
            return Optional.of(JwtStatus.UNSUPPORTED);
        } catch (MalformedJwtException e) {
            return Optional.of(JwtStatus.MALFORMED);
        }
        return Optional.empty();
    }
}
