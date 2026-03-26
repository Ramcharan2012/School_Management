package com.school.management.auth.service;

import com.school.management.user.entity.User;
import com.school.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Loads User from DB by email OR username (multi-identifier support).
 * Spring Security's AuthenticationManager calls this with the resolved email
 * (AuthService resolves identifier → email before calling authenticate()).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Called by Spring Security with the email resolved from the identifier.
     * Tries email first, then username as fallback.
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with identifier: " + identifier));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Spring Security principal = email
                user.getPassword(),
                user.getStatus() == com.school.management.common.enums.UserStatus.ACTIVE,
                true, true, true,
                buildAuthorities(user));
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(User user) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
}
