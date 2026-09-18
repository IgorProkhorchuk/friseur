package de.friseur.friseur.config;


import de.friseur.friseur.security.CustomAuthenticationSuccessHandler;
import de.friseur.friseur.security.jwt.JwtAuthenticationFilter;
import de.friseur.friseur.security.jwt.JwtService;
import de.friseur.friseur.service.TokenBlacklistService;
import de.friseur.friseur.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtService jwtService, 
                          JwtProperties jwtProperties, TokenBlacklistService tokenBlacklistService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers("/").permitAll()
                                .requestMatchers("/home").permitAll()
                                .requestMatchers("/datenschutz").permitAll()
                                .requestMatchers("/impressum").permitAll()
                                .requestMatchers("/privacy").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/create-schedule").hasRole("ADMIN")
                                .requestMatchers("/admin-dashboard").hasRole("ADMIN")
                                .requestMatchers("/shop").permitAll()
                                .requestMatchers("/login").permitAll()
                                .requestMatchers("/slots").permitAll()
                                .requestMatchers("/book").authenticated()
                                .requestMatchers("/register").permitAll()
                                .requestMatchers("/success").hasRole("ADMIN")
                                .requestMatchers("/css/**").permitAll()
                                .requestMatchers("/js/**").permitAll()
                                .requestMatchers("/images/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .addLogoutHandler(tokenBlacklistLogoutHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me", jwtProperties.getAccessTokenCookieName(), jwtProperties.getRefreshTokenCookieName())
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .userDetailsService(userDetailsService);

        http.addFilterBefore(jwtAuthenticationFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    /**
     * A custom success handler that redirects the user to the admin page if the user is an admin.
     * Otherwise, the user is redirected to the home page.
     * @return
     */
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler(jwtService);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userDetailsService, jwtProperties);
    }

    @Bean
    public LogoutHandler tokenBlacklistLogoutHandler() {
        return (request, response, authentication) -> {
            if (authentication != null && authentication.getPrincipal() != null) {
                String username = authentication.getName();
                
                String accessToken = resolveTokenFromCookies(request, jwtProperties.getAccessTokenCookieName());
                String refreshToken = resolveTokenFromCookies(request, jwtProperties.getRefreshTokenCookieName());
                
                if (accessToken != null) {
                    Instant accessTokenExpiry = jwtService.extractExpiration(accessToken).orElse(Instant.now());
                    tokenBlacklistService.blacklistToken(accessToken, accessTokenExpiry, "access", username);
                }
                
                if (refreshToken != null) {
                    Instant refreshTokenExpiry = jwtService.extractExpiration(refreshToken).orElse(Instant.now());
                    tokenBlacklistService.blacklistToken(refreshToken, refreshTokenExpiry, "refresh", username);
                }
            }
        };
    }

    private String resolveTokenFromCookies(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(javax.servlet.http.Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

}
