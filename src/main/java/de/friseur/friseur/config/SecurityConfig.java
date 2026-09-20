package de.friseur.friseur.config;


import de.friseur.friseur.security.CustomAuthenticationSuccessHandler;
import de.friseur.friseur.security.RequestParameterRememberMeServices;
import de.friseur.friseur.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final String rememberMeKey;
    private final int rememberMeTokenValiditySeconds;
    private final boolean rememberMeSecureCookie;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          @Value("${security.remember-me.key}") String rememberMeKey,
                          @Value("${security.remember-me.token-validity-seconds:2592000}") int rememberMeTokenValiditySeconds,
                          @Value("${security.remember-me.secure-cookie:false}") boolean rememberMeSecureCookie) {
        this.userDetailsService = userDetailsService;
        this.rememberMeKey = rememberMeKey;
        this.rememberMeTokenValiditySeconds = rememberMeTokenValiditySeconds;
        this.rememberMeSecureCookie = rememberMeSecureCookie;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider authenticationProvider,
                                                   RequestParameterRememberMeServices rememberMeServices) throws Exception {
        http
                .authenticationProvider(authenticationProvider)
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
                .rememberMe(rememberMe -> rememberMe
                        .rememberMeServices(rememberMeServices)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
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
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public RequestParameterRememberMeServices rememberMeServices() {
        RequestParameterRememberMeServices rememberMeServices =
                new RequestParameterRememberMeServices(rememberMeKey, userDetailsService);
        rememberMeServices.setCookieName("remember-me");
        rememberMeServices.setTokenValiditySeconds(rememberMeTokenValiditySeconds);
        rememberMeServices.setUseSecureCookie(rememberMeSecureCookie);
        return rememberMeServices;
    }

    /**
     * A custom success handler that redirects the user to the admin page if the user is an admin.
     * Otherwise, the user is redirected to the home page.
     * @return
     */
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

}
