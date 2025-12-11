package de.friseur.friseur.security.jwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import de.friseur.friseur.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    private JwtProperties properties;
    private JwtService jwtService;
    private UserDetails user;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;
    private static final String BASE64_SECRET = Base64.getEncoder()
            .encodeToString("0123456789ABCDEF0123456789ABCDEF".getBytes());

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret(BASE64_SECRET);
        properties.setAccessTokenTtl(Duration.ofMinutes(30));
        properties.setRefreshTokenTtl(Duration.ofDays(7));
        properties.setRememberMeRefreshTokenTtl(Duration.ofDays(100));
        properties.setSecureCookie(false);
        properties.setSameSite("Lax");

        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(properties.getSecret()), "HmacSHA256");
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        JwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        jwtService = new JwtService(jwtEncoder, jwtDecoder, properties);

        user = User.withUsername("user@example.com").password("pwd").roles("USER").build();
        userDetailsService = username -> user;

        filter = new JwtAuthenticationFilter(jwtService, userDetailsService, properties);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesWithValidAccessTokenFromCookie() throws Exception {
        String accessToken = jwtService.generateAccessToken(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/slots");
        request.setCookies(new Cookie(properties.getAccessTokenCookieName(), accessToken));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be set");
        assertEquals(user.getUsername(), SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(response.getHeaders("Set-Cookie").isEmpty(), "No new cookies should be issued when access token is valid");
    }

    @Test
    void reissuesTokensAndAuthenticatesWithRefreshToken() throws Exception {
        String refreshToken = jwtService.generateRefreshToken(user, true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/slots");
        request.setCookies(new Cookie(properties.getRefreshTokenCookieName(), refreshToken));

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be set from refresh token");
        assertEquals(user.getUsername(), SecurityContextHolder.getContext().getAuthentication().getName());

        List<String> setCookieHeaders = response.getHeaders("Set-Cookie");
        assertEquals(2, setCookieHeaders.size(), "Access and refresh cookies should be reissued");
        assertTrue(setCookieHeaders.stream().anyMatch(h -> h.startsWith(properties.getAccessTokenCookieName() + "=")));
        assertTrue(setCookieHeaders.stream().anyMatch(h -> h.startsWith(properties.getRefreshTokenCookieName() + "=")));
    }
}
