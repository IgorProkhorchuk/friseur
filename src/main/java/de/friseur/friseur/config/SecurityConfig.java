package de.friseur.friseur.config;


import de.friseur.friseur.security.CustomAuthenticationSuccessHandler;
import de.friseur.friseur.security.jwt.JwtAuthenticationFilter;
import de.friseur.friseur.security.jwt.JwtService;
import de.friseur.friseur.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtService jwtService, JwtProperties jwtProperties) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
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
                                .requestMatchers(HttpMethod.GET, "/slots").permitAll()
                                .requestMatchers(HttpMethod.POST, "/slots").authenticated()
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
                        .invalidateHttpSession(true)
                        .addLogoutHandler((request, response, authentication) -> {
                            response.addHeader(HttpHeaders.SET_COOKIE,
                                    jwtService.clearCookie(jwtProperties.getAccessTokenCookieName()).toString());
                            response.addHeader(HttpHeaders.SET_COOKIE,
                                    jwtService.clearCookie(jwtProperties.getRefreshTokenCookieName()).toString());
                        })
                        .deleteCookies("JSESSIONID", "remember-me", jwtProperties.getAccessTokenCookieName(), jwtProperties.getRefreshTokenCookieName())
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if ("true".equalsIgnoreCase(request.getHeader("HX-Request"))) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setHeader("HX-Redirect", "/login");
                                return;
                            }
                            new LoginUrlAuthenticationEntryPoint("/login").commence(request, response, authException);
                        })
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
        return new BCryptPasswordEncoder(12);
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

}
