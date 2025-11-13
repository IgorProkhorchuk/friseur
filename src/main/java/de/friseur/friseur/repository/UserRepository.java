package de.friseur.friseur.repository;

import de.friseur.friseur.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository encapsulating persistence operations for {@link User} entities.
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Searches for a user by username, returning an {@link Optional} so callers can handle absence.
     *
     * @param userName unique username
     * @return optional user record
     */
    Optional<User> findByUsername(String userName);
}
