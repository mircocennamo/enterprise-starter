package it.interno.platform.starter.security.config;

import it.interno.platform.starter.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF since REST APIs are stateless
                .csrf(csrf -> csrf.disable())

                // Set stateless session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorize requests
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to H2 Console (disabling frameOptions security is also
                        // needed)
                        // .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        // Require authentication for all REST API endpoints
                        // .requestMatchers(new AntPathRequestMatcher("/api/**")).authenticated()
                        .anyRequest().permitAll())

                // Required for H2 Console to show correctly in frames
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // Add custom JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
