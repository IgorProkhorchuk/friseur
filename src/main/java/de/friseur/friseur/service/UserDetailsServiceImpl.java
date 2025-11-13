package de.friseur.friseur.service;

import de.friseur.friseur.model.User;
import de.friseur.friseur.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Bridges the {@link UserRepository} with Spring Security by exposing {@link UserDetails} records.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return userRepository.findByUsername(username)
//                .map(user -> {
//                    UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.getUsername());
//                    builder.password(user.getPassword());
//                    builder.authorities(user.getRoles().stream()
//                            .map(SimpleGrantedAuthority::new)
//                            .collect(Collectors.toSet()));
//                    return builder.build();
//                })
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
//    }

    /**
     * Loads the persistent {@link User} by username, allowing Spring Security to authenticate it.
     *
     * @param username username entered during login
     * @return the user details implementation
     * @throws UsernameNotFoundException when no matching user exists
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
