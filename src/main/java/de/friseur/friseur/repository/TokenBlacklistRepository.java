package de.friseur.friseur.repository;

import de.friseur.friseur.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    Optional<TokenBlacklist> findByToken(String token);

    List<TokenBlacklist> findByUsername(String username);

    List<TokenBlacklist> findByExpiresAtBefore(Instant now);

    void deleteByExpiresAtBefore(Instant now);

    List<TokenBlacklist> findByUsernameAndTokenType(String username, String tokenType);
}
