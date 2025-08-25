package com.movietracker.api.config;

import com.movietracker.api.security.JwtAuthenticationFilter;
import com.movietracker.api.security.OAuth2AuthenticationFailureHandler;
import com.movietracker.api.security.OAuth2AuthenticationSuccessHandler;
import com.movietracker.api.security.OAuth2AuthenticationSuccessHandlerV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final OAuth2AuthenticationSuccessHandlerV2 oAuth2AuthenticationSuccessHandlerV2;
    
    @Value("${app.auth.oauth2-enabled:false}")
    private boolean oauth2Enabled;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         @Autowired(required = false) @Lazy OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                         @Autowired(required = false) @Lazy OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
                         @Autowired(required = false) @Lazy OAuth2AuthenticationSuccessHandlerV2 oAuth2AuthenticationSuccessHandlerV2) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.oAuth2AuthenticationSuccessHandlerV2 = oAuth2AuthenticationSuccessHandlerV2;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        HttpSecurity httpSecurity = http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // Disable for API development
            .sessionManagement(session -> {
                if (oauth2Enabled) {
                    // OAuth2 needs sessions for authorization requests
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                } else {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                }
            })
            .authorizeHttpRequests(authz -> {
                authz
                    // Public endpoints
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/health").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/graphql").permitAll()
                    .requestMatchers("/graphiql").permitAll()
                    .requestMatchers("/h2-console/**").permitAll(); // For H2 console in testing
                    
                // Add OAuth2 endpoints only if OAuth2 is enabled
                if (oauth2Enabled) {
                    authz
                        .requestMatchers("/oauth2/**").permitAll() // OAuth2 endpoints
                        .requestMatchers("/login/oauth2/**").permitAll(); // OAuth2 login endpoints
                }
                
                authz.anyRequest().authenticated(); // All other requests require authentication
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // For H2 console
        
        // Disable form login to prevent default /login redirect
        httpSecurity.formLogin(formLogin -> formLogin.disable());
        
        // Only configure OAuth2 login if enabled and handlers are available
        if (oauth2Enabled && oAuth2AuthenticationFailureHandler != null) {
            // Use V2 handler if available (with session management), fallback to V1 handler
            var successHandler = oAuth2AuthenticationSuccessHandlerV2 != null ? 
                oAuth2AuthenticationSuccessHandlerV2 : oAuth2AuthenticationSuccessHandler;
            
            if (successHandler != null) {
                System.out.println("Configuring OAuth2 with handler: " + successHandler.getClass().getSimpleName());
                
                httpSecurity.oauth2Login(oauth2 -> oauth2
                    .successHandler(successHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
                    .authorizationEndpoint(authorization -> 
                        authorization.baseUri("/oauth2/authorization"))
                    .redirectionEndpoint(redirection -> 
                        redirection.baseUri("/login/oauth2/code/*"))
                );
            }
        }
        
        return httpSecurity.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
