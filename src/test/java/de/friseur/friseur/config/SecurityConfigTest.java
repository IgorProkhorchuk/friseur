package de.friseur.friseur.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.ProtectedController.class)
@Import({SecurityConfig.class, SecurityConfigTest.ProtectedController.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private de.friseur.friseur.service.UserDetailsServiceImpl userDetailsService;

    private String encodedUserPassword;
    private String encodedAdminPassword;

    @BeforeEach
    void setUpUsers() {
        encodedUserPassword = passwordEncoder.encode("password");
        encodedAdminPassword = passwordEncoder.encode("password");

        when(userDetailsService.loadUserByUsername("user@example.com"))
                .thenAnswer(invocation -> User.builder()
                        .username("user@example.com")
                        .password(encodedUserPassword)
                        .roles("USER")
                        .build());

        when(userDetailsService.loadUserByUsername("admin@example.com"))
                .thenAnswer(invocation -> User.builder()
                        .username("admin@example.com")
                        .password(encodedAdminPassword)
                        .roles("ADMIN")
                        .build());
    }

    @Test
    void loginWithRememberMeCreatesRememberMeCookieWithoutLegacyTokenCookies() throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "user@example.com")
                        .param("password", "password")
                        .param("_spring_security_remember_me", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(cookie().exists("remember-me"))
                .andReturn();

        Cookie rememberMe = result.getResponse().getCookie("remember-me");
        assertNotNull(rememberMe);
        assertTrue(rememberMe.getMaxAge() > 0);

        List<String> setCookieHeaders = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertFalse(setCookieHeaders.stream().anyMatch(header -> header.startsWith("ACCESS_TOKEN=")));
        assertFalse(setCookieHeaders.stream().anyMatch(header -> header.startsWith("REFRESH_TOKEN=")));
    }

    @Test
    void loginWithoutRememberMeDoesNotCreateRememberMeCookie() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "user@example.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(cookie().doesNotExist("remember-me"));
    }

    @Test
    void rememberMeCookieAuthenticatesLaterRequestWithoutSession() throws Exception {
        MvcResult login = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "user@example.com")
                        .param("password", "password")
                        .param("_spring_security_remember_me", "true"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Cookie rememberMe = login.getResponse().getCookie("remember-me");
        assertNotNull(rememberMe);

        mockMvc.perform(get("/book").cookie(rememberMe))
                .andExpect(status().isOk());
    }

    @Test
    void legacyTokenCookiesDoNotAuthenticateARequest() throws Exception {
        mockMvc.perform(get("/book")
                        .cookie(new Cookie("ACCESS_TOKEN", "old-access-token"))
                        .cookie(new Cookie("REFRESH_TOKEN", "old-refresh-token")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void htmxUnauthenticatedRequestReceivesHxRedirectInsteadOfLoginHtml() throws Exception {
        mockMvc.perform(get("/book").header("HX-Request", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("HX-Redirect", "/login"));
    }

    @Test
    void userRoleCannotAccessAdminRoutes() throws Exception {
        mockMvc.perform(get("/admin/dashboard").with(user("user@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRoleCanAccessAdminRoutes() throws Exception {
        mockMvc.perform(get("/admin/dashboard").with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void logoutRequiresCsrfProtection() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutExpiresRememberMeCookie() throws Exception {
        MvcResult login = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "user@example.com")
                        .param("password", "password")
                        .param("_spring_security_remember_me", "true"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Cookie rememberMe = login.getResponse().getCookie("remember-me");
        assertNotNull(rememberMe);

        mockMvc.perform(post("/logout")
                        .with(csrf())
                        .cookie(rememberMe))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(cookie().maxAge("remember-me", 0));
    }

    @RestController
    public static class ProtectedController {

        @GetMapping("/book")
        String book() {
            return "book";
        }

        @GetMapping("/home")
        String home() {
            return "home";
        }

        @GetMapping("/admin/dashboard")
        String adminDashboard() {
            return "admin";
        }
    }
}
