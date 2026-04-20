package com.nnh.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:4173}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Always allow CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // Public read endpoints (booking form)
                        .requestMatchers(HttpMethod.GET,  "/api/appointments/availability").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/doctors/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/services/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/absences/active").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/appointments").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
                        // H2 dev console
                        .requestMatchers("/h2-console/**").permitAll()
                        // Admin-only
                        .requestMatchers("/api/employees/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/absences/**").hasRole("ADMIN")
                        .requestMatchers("/api/absences/*/approve").hasRole("ADMIN")
                        .requestMatchers("/api/absences/*/reject").hasRole("ADMIN")
                        .requestMatchers("/api/appointments/*/audit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/bills").hasRole("ADMIN")
                        // Admin-only leave actions
                        .requestMatchers("/api/leaves/*/approve").hasRole("ADMIN")
                        .requestMatchers("/api/leaves/*/reject").hasRole("ADMIN")
                        // Authenticated staff
                        .requestMatchers(HttpMethod.POST, "/api/appointments/staff").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/absences").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/absences").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/leaves").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/leaves/approved").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/leaves").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/bills").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bills/**").authenticated()
                        // Everything else requires a valid token
                        .anyRequest().authenticated()
                )
                // Return JSON instead of Spring Security's default HTML error pages
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"success\":false,\"message\":\"Authentication required\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"success\":false,\"message\":\"Access denied\"}");
                        })
                )
                // Allow H2 console frames
                .headers(h -> h.frameOptions(fo -> fo.sameOrigin()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
