package de.friseur.friseur.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * Base64 encoded secret used to sign tokens.
     */
    @NotBlank
    private String secret;

    /**
     * Access token validity (defaults to 30 minutes).
     */
    private Duration accessTokenTtl = Duration.ofMinutes(30);

    /**
     * Refresh token validity when remember-me is not requested (defaults to 7 days).
     */
    private Duration refreshTokenTtl = Duration.ofDays(7);

    /**
     * Refresh token validity when remember-me is requested (defaults to 100 days to preserve existing behaviour).
     */
    private Duration rememberMeRefreshTokenTtl = Duration.ofDays(100);

    /**
     * Issuer value placed into every token.
     */
    private String issuer = "friseur";

    /**
     * Whether cookies should be marked as secure. Set to true in production behind HTTPS.
     */
    private boolean secureCookie = false;

    private String accessTokenCookieName = "ACCESS_TOKEN";
    private String refreshTokenCookieName = "REFRESH_TOKEN";
    private String sameSite = "Lax";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public Duration getRememberMeRefreshTokenTtl() {
        return rememberMeRefreshTokenTtl;
    }

    public void setRememberMeRefreshTokenTtl(Duration rememberMeRefreshTokenTtl) {
        this.rememberMeRefreshTokenTtl = rememberMeRefreshTokenTtl;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public boolean isSecureCookie() {
        return secureCookie;
    }

    public void setSecureCookie(boolean secureCookie) {
        this.secureCookie = secureCookie;
    }

    public String getAccessTokenCookieName() {
        return accessTokenCookieName;
    }

    public void setAccessTokenCookieName(String accessTokenCookieName) {
        this.accessTokenCookieName = accessTokenCookieName;
    }

    public String getRefreshTokenCookieName() {
        return refreshTokenCookieName;
    }

    public void setRefreshTokenCookieName(String refreshTokenCookieName) {
        this.refreshTokenCookieName = refreshTokenCookieName;
    }

    public String getSameSite() {
        return sameSite;
    }

    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }
}
