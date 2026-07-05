package org.example.hotelbookingservice.security;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.exception.CustomAccessDenialHandler;
import org.example.hotelbookingservice.exception.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilter {



    private final CustomAccessDenialHandler customAccessDenialHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthFilter authFilter) throws Exception {

        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(exception ->
                        exception.accessDeniedHandler(customAccessDenialHandler)
                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(request -> request
                        // --- 1. TÀI LIỆU API (SWAGGER) ---
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/prometheus"
                        ).permitAll()

                        .requestMatchers("/api/v1/auth/**").permitAll()

                        .requestMatchers(
                                "/api/v1/payments/vnpay/return",
                                "/api/v1/payments/vnpay/ipn",
                                "/api/v1/contact",
                                "/api/v1/contacts"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/uploads/local/*",
                                "/api/v1/news",
                                "/api/v1/news/*",
                                "/api/v1/banners",
                                "/api/v1/hotels",
                                "/api/v1/hotels/*",
                                "/api/v1/hotels/*/policies",
                                "/api/v1/hotels/*/reviews",
                                "/api/v1/hotels/*/reviews/summary",
                                "/api/v1/hotels/*/room-types",
                                "/api/v1/hotels/*/room-types/*",
                                "/api/v1/hotels/*/room-types/available",
                                "/api/v1/hotels/all",
                                "/api/v1/hotels/search",
                                "/api/v1/hotels/{hotelId}"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/rooms/all",
                                "/api/v1/rooms/all-available-rooms",
                                "/api/v1/rooms/search",
                                "/api/v1/rooms/legacy-types",
                                "/api/v1/rooms/{roomId}"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/amenities",
                                "/api/v1/amenities/*",
                                "/api/v1/amenities/{id}",
                                "/api/v1/amenities/all",
                                "/api/v1/amenities/hotel/{hotelId}/room/{roomId}"
                        ).permitAll()


                        .requestMatchers("/room-photos/**").permitAll()

                        .anyRequest().authenticated()
                )
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }
}
