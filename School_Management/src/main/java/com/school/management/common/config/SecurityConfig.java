package com.school.management.common.config;

import com.school.management.auth.filter.JwtAuthenticationFilter;
import com.school.management.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Full JWT Security Configuration.
 *
 * URL Permission Rules:
 * - PUBLIC : /auth/**, /public/**, Swagger, Actuator
 * - ADMIN : /admin/**
 * - TEACHER : /teacher/**
 * - STUDENT : /student/**
 * - ADMIN or TEACHER: /teacher/attendance/**, /teacher/marks/**
 * - ANY AUTHENTICATED: /notices/**, /timetable/**, /leave/**
 *
 * All sessions are STATELESS (no HttpSession — JWT only).
 * 
 * @EnableMethodSecurity enables @PreAuthorize for fine-grained method-level
 *                       control.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // ── Beans ─────────────────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── Security Filter Chain ────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth

                        // ── Completely Public ─────────────────────────────────────────
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // ── Admin Only ────────────────────────────────────────────────
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ── Teacher: can mark attendance, enter marks, view students ──
                        .requestMatchers("/teacher/**").hasAnyRole("TEACHER", "ADMIN")

                        // ── Student: can view own data ────────────────────────────────
                        .requestMatchers("/student/**").hasAnyRole("STUDENT", "ADMIN")

                        // ── Timetable: all authenticated users can view ───────────────
                        .requestMatchers(HttpMethod.GET, "/timetable/**").authenticated()

                        // ── Notices: all authenticated users can read ─────────────────
                        .requestMatchers(HttpMethod.GET, "/notices/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/notices").authenticated()

                        // ── Leave: teachers and students can apply and view own ───────
                        .requestMatchers("/leave/**").authenticated()

                        // All others require authentication
                        .anyRequest().authenticated())

                // Add JWT filter before Spring's username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
