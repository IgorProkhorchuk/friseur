package de.friseur.friseur.security.jwt;

import de.friseur.friseur.config.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final JwtProperties jwtProperties;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, JwtProperties jwtProperties) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        tryAuthenticateWithAccessToken(request);

        // If still not authenticated try with refresh token (renewing both tokens).
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            tryRenewSessionWithRefreshToken(request, response);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/favicon");
    }

    private void tryAuthenticateWithAccessToken(HttpServletRequest request) {
        String token = resolveAccessToken(request);
        if (!StringUtils.hasText(token)) {
            return;
        }

        Optional<String> usernameOpt = jwtService.extractUsername(token);
        if (usernameOpt.isEmpty()) {
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOpt.get());
        if (jwtService.isTokenValid(token, userDetails)) {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }

    private void tryRenewSessionWithRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(request);
        if (!StringUtils.hasText(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            return;
        }

        Optional<String> usernameOpt = jwtService.extractUsername(refreshToken);
        if (usernameOpt.isEmpty()) {
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOpt.get());
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            log.debug("Refresh token is not valid for user {}", usernameOpt.get());
            return;
        }

        boolean rememberMe = jwtService.isRememberMeRefreshToken(refreshToken);

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails, rememberMe);

        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.buildAccessTokenCookie(newAccessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.buildRefreshTokenCookie(newRefreshToken, rememberMe).toString());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return resolveTokenFromCookies(request, jwtProperties.getAccessTokenCookieName());
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        return resolveTokenFromCookies(request, jwtProperties.getRefreshTokenCookieName());
    }

    private String resolveTokenFromCookies(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
