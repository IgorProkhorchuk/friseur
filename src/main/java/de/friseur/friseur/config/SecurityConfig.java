package de.friseur.friseur.config;


import de.friseur.friseur.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request ->
                        request
//                                .requestMatchers("/").permitAll()
//                                .requestMatchers("/home").permitAll()
//                                .requestMatchers("/admin/**").permitAll()
//                                .requestMatchers("/shop").permitAll()
//                                .requestMatchers("/login").permitAll()
//                                .requestMatchers("/slots").permitAll()
//                                .requestMatchers("/register").permitAll()
//                                .requestMatchers("/schedule").permitAll()
//                                .requestMatchers("/success").permitAll()
//                                .requestMatchers("/css/**").permitAll()
//                                .requestMatchers("/js/**").permitAll()
//                                .requestMatchers("/images/**").permitAll()
                                .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                );
        return http.build();
    }

    

}