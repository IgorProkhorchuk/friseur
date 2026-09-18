package de.friseur.friseur.service;

import de.friseur.friseur.model.TokenBlacklist;
import de.friseur.friseur.repository.TokenBlacklistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Transactional
    public void blacklistToken(String token, Instant expiresAt, String tokenType, String username) {
        Optional<TokenBlacklist> existing = tokenBlacklistRepository.findByToken(token);
        if (existing.isPresent()) {
            TokenBlacklist blacklist = existing.get();
            blacklist.setBlacklistedAt(Instant.now());
            blacklist.setExpiresAt(expiresAt);
            tokenBlacklistRepository.save(blacklist);
            log.debug("Updated existing blacklisted token for user: {}", username);
        } else {
            TokenBlacklist blacklist = new TokenBlacklist(token, expiresAt, tokenType, username);
            tokenBlacklistRepository.save(blacklist);
            log.debug("Blacklisted token for user: {}", username);
        }
    }

    @Transactional
    public boolean isTokenBlacklisted(String token) {
        Optional<TokenBlacklist> blacklist = tokenBlacklistRepository.findByToken(token);
        if (blacklist.isPresent()) {
            TokenBlacklist entry = blacklist.get();
            if (entry.isExpired()) {
                tokenBlacklistRepository.delete(entry);
                log.debug("Removed expired blacklist entry for token");
                return false;
            }
            log.debug("Token found in blacklist");
            return true;
        }
        return false;
    }

    @Transactional
    public void blacklistAllUserTokens(String username) {
        List<TokenBlacklist> userTokens = tokenBlacklistRepository.findByUsername(username);
        log.info("Blacklisting all tokens for user: {}. Found {} tokens to blacklist.", 
                username, userTokens.size());
        
        for (TokenBlacklist token : userTokens) {
            if (!token.isExpired()) {
                token.setBlacklistedAt(Instant.now());
                tokenBlacklistRepository.save(token);
            }
        }
    }

    @Transactional
    public void invalidateRefreshToken(String refreshToken, Instant expiresAt, String username) {
        blacklistToken(refreshToken, expiresAt, "refresh", username);
        log.info("Invalidated refresh token for user: {}", username);
    }

    @Transactional
    public void invalidateAccessToken(String accessToken, Instant expiresAt, String username) {
        blacklistToken(accessToken, expiresAt, "access", username);
        log.info("Invalidated access token for user: {}", username);
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredBlacklistEntries() {
        Instant now = Instant.now();
        long deletedCount = tokenBlacklistRepository.deleteByExpiresAtBefore(now);
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired token blacklist entries", deletedCount);
        }
    }
}
