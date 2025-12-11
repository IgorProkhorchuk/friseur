package de.friseur.friseur.security.jwt;

import de.friseur.friseur.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, jwtProperties.getAccessTokenTtl(), TOKEN_TYPE_ACCESS, null);
    }

    public String generateRefreshToken(UserDetails userDetails, boolean rememberMe) {
        Duration ttl = rememberMe ? jwtProperties.getRememberMeRefreshTokenTtl() : jwtProperties.getRefreshTokenTtl();
        return generateToken(userDetails, ttl, TOKEN_TYPE_REFRESH, rememberMe);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return userDetails.getUsername().equals(jwt.getSubject())
                    && jwt.getExpiresAt() != null
                    && jwt.getExpiresAt().isAfter(Instant.now());
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Optional<String> extractUsername(String token) {
        try {
            return Optional.ofNullable(jwtDecoder.decode(token).getSubject());
        } catch (JwtException e) {
            log.debug("Failed to extract username from token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return TOKEN_TYPE_REFRESH.equals(jwt.getClaimAsString(TOKEN_TYPE_CLAIM));
        } catch (JwtException e) {
            log.debug("Token is not a valid refresh token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRememberMeRefreshToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return Boolean.TRUE.equals(jwt.getClaim("remember_me"));
        } catch (JwtException e) {
            log.debug("Failed to determine remember-me flag from token: {}", e.getMessage());
            return false;
        }
    }

    public ResponseCookie buildAccessTokenCookie(String token) {
        return ResponseCookie.from(jwtProperties.getAccessTokenCookieName(), token)
                .httpOnly(true)
                .secure(jwtProperties.isSecureCookie())
                .path("/")
                .sameSite(jwtProperties.getSameSite())
                .maxAge(jwtProperties.getAccessTokenTtl())
                .build();
    }

    public ResponseCookie buildRefreshTokenCookie(String token, boolean rememberMe) {
        Duration ttl = rememberMe ? jwtProperties.getRememberMeRefreshTokenTtl() : jwtProperties.getRefreshTokenTtl();
        return ResponseCookie.from(jwtProperties.getRefreshTokenCookieName(), token)
                .httpOnly(true)
                .secure(jwtProperties.isSecureCookie())
                .path("/")
                .sameSite(jwtProperties.getSameSite())
                .maxAge(ttl)
                .build();
    }

    public ResponseCookie clearCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(jwtProperties.isSecureCookie())
                .path("/")
                .sameSite(jwtProperties.getSameSite())
                .maxAge(Duration.ZERO)
                .build();
    }

    private String generateToken(UserDetails userDetails, Duration ttl, String tokenType, Boolean rememberMe) {
        Instant now = Instant.now();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .id(UUID.randomUUID().toString());

        if (rememberMe != null) {
            claimsBuilder.claim("remember_me", rememberMe);
        }

        JwtClaimsSet claims = claimsBuilder.build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
