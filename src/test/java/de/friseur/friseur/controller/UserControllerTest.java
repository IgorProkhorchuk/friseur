package de.friseur.friseur.controller;

import de.friseur.friseur.config.JwtConfig;
import de.friseur.friseur.config.SecurityConfig;
import de.friseur.friseur.security.jwt.JwtService;
import de.friseur.friseur.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserService userService;

    @MockBean
    private de.friseur.friseur.service.UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setupJwtMocks() {
        when(jwtService.generateAccessToken(any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), anyBoolean())).thenReturn("refresh");
        when(jwtService.buildAccessTokenCookie(anyString()))
                .thenReturn(ResponseCookie.from("ACCESS_TOKEN", "access").build());
        when(jwtService.buildRefreshTokenCookie(anyString(), anyBoolean()))
                .thenReturn(ResponseCookie.from("REFRESH_TOKEN", "refresh").build());
    }

    @Test
    void showRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void registerUser_success() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "user")
                        .param("email", "user@example.com")
                        .param("phone", "1234567890")
                        .param("password", "password")
                        .param("confirmPassword", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?success=true"));
    }

    @Test
    void registerUser_failure() throws Exception {
        doThrow(new IllegalArgumentException("Password mismatch")).when(userService).registerUser(anyString(), anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "user")
                        .param("email", "user@example.com")
                        .param("phone", "1234567890")
                        .param("password", "password")
                        .param("confirmPassword", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Password mismatch"));
    }

    @Test
    void showLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void loginUser_success() throws Exception {
        when(userDetailsService.loadUserByUsername("user@example.com"))
                .thenReturn(User.builder()
                        .username("user@example.com")
                        .password(passwordEncoder.encode("password"))
                        .roles("USER")
                        .build());

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "user@example.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    void loginUser_failure() throws Exception {
        when(userDetailsService.loadUserByUsername("user@example.com"))
                .thenReturn(User.builder()
                        .username("user@example.com")
                        .password(passwordEncoder.encode("password"))
                        .roles("USER")
                        .build());

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "user@example.com")
                        .param("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void logout() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }
}
