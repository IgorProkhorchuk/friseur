package de.friseur.friseur.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.util.StringUtils;

public class RequestParameterRememberMeServices extends TokenBasedRememberMeServices {

    public static final String REMEMBER_ME_PARAMETER = "_spring_security_remember_me";

    public RequestParameterRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
        setParameter(REMEMBER_ME_PARAMETER);
    }

    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response,
                             Authentication successfulAuthentication) {
        if (StringUtils.hasText(request.getParameter(REMEMBER_ME_PARAMETER))) {
            UserDetails userDetails = getUserDetailsService().loadUserByUsername(successfulAuthentication.getName());
            Authentication rememberMeAuthentication = UsernamePasswordAuthenticationToken.authenticated(
                    userDetails,
                    userDetails.getPassword(),
                    userDetails.getAuthorities());
            onLoginSuccess(request, response, rememberMeAuthentication);
        }
    }
}
