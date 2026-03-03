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
 * Loads User from DB by email for Spring Security authentication.
 * Wraps the User entity into Spring's UserDetails contract.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getStatus() == com.school.management.common.enums.UserStatus.ACTIVE,
                true, true, true,
                buildAuthorities(user));
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(User user) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
}
