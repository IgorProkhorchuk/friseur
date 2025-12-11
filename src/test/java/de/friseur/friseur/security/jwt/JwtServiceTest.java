package de.friseur.friseur.security.jwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import de.friseur.friseur.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtDecoder jwtDecoder;
    private JwtProperties properties;
    private UserDetails userDetails;

    private static final String BASE64_SECRET = Base64.getEncoder()
            .encodeToString("0123456789ABCDEF0123456789ABCDEF".getBytes());

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret(BASE64_SECRET);
        properties.setAccessTokenTtl(Duration.ofMinutes(30));
        properties.setRefreshTokenTtl(Duration.ofDays(7));
        properties.setRememberMeRefreshTokenTtl(Duration.ofDays(100));
        properties.setIssuer("test-issuer");
        properties.setSameSite("Lax");
        properties.setSecureCookie(false);

        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(properties.getSecret()), "HmacSHA256");
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        jwtService = new JwtService(jwtEncoder, jwtDecoder, properties);
        userDetails = new User("user@example.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void generateAccessToken_containsSubjectRolesAndExpiresWithinConfiguredWindow() {
        Instant before = Instant.now();
        String token = jwtService.generateAccessToken(userDetails);
        Jwt jwt = jwtDecoder.decode(token);

        assertEquals("user@example.com", jwt.getSubject());
        assertEquals("test-issuer", jwt.getClaimAsString("iss"));
        assertEquals(List.of("ROLE_USER"), jwt.getClaimAsStringList("roles"));
        assertEquals("access", jwt.getClaimAsString("token_type"));

        Instant expectedExpiry = before.plus(properties.getAccessTokenTtl());
        long skewSeconds = Math.abs(jwt.getExpiresAt().getEpochSecond() - expectedExpiry.getEpochSecond());
        assertTrue(skewSeconds <= 5, "Expiry should be within a few seconds of configured TTL");
    }

    @Test
    void generateRefreshToken_setsRememberMeClaimAndCookieTtl() {
        String rememberToken = jwtService.generateRefreshToken(userDetails, true);
        Jwt rememberJwt = jwtDecoder.decode(rememberToken);

        assertEquals("refresh", rememberJwt.getClaimAsString("token_type"));
        assertTrue(Boolean.TRUE.equals(rememberJwt.getClaim("remember_me")));

        ResponseCookie rememberCookie = jwtService.buildRefreshTokenCookie(rememberToken, true);
        assertEquals(properties.getRememberMeRefreshTokenTtl(), rememberCookie.getMaxAge());
        assertTrue(rememberCookie.isHttpOnly());
        assertEquals(properties.isSecureCookie(), rememberCookie.isSecure());
        assertEquals("/", rememberCookie.getPath());
    }

    @Test
    void generateRefreshToken_setsShorterTtlWhenNotRememberMe() {
        String token = jwtService.generateRefreshToken(userDetails, false);
        Jwt jwt = jwtDecoder.decode(token);

        assertEquals("refresh", jwt.getClaimAsString("token_type"));
        assertFalse(jwtService.isRememberMeRefreshToken(token));

        ResponseCookie cookie = jwtService.buildRefreshTokenCookie(token, false);
        assertEquals(properties.getRefreshTokenTtl(), cookie.getMaxAge());
    }

    @Test
    void isTokenValidReturnsFalseForDifferentUser() {
        String token = jwtService.generateAccessToken(userDetails);
        UserDetails otherUser = new User("other@example.com", "pwd", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertFalse(jwtService.isTokenValid(token, otherUser));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }
}
