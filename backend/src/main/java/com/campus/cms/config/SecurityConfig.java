package com.campus.cms.config;

import com.campus.cms.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/courses", "/courses/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/classes", "/classes/**").authenticated()
                .requestMatchers("/teachers/**").hasRole("admin")
                .requestMatchers("/students/**").hasRole("admin")
                .requestMatchers(HttpMethod.POST, "/courses").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/courses/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/courses/**").hasRole("admin")
                .requestMatchers(HttpMethod.POST, "/classes").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/classes/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/classes/**").hasRole("admin")
                .requestMatchers("/dashboard/**").hasRole("admin")
                .requestMatchers("/enrollments/**").hasAnyRole("student", "admin")
                .requestMatchers(HttpMethod.POST, "/grades").hasRole("teacher")
                .requestMatchers(HttpMethod.PUT, "/grades/**").hasRole("teacher")
                .requestMatchers(HttpMethod.GET, "/grades").authenticated()
                .requestMatchers("/timetable/**").hasRole("student")
                .requestMatchers("/profile/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(fo -> fo.disable()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
