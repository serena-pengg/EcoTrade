package com.example.Secondhand.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Static resources and public pages
                .requestMatchers(
                    "/",
                    "/home",
                    "/register",
                    "/login",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/vendor/**",
                    "/search-results",
                    "/about",
                    "/contact",
                    "/uploads/**",
                    "/search",
                    "/api/chat/send",
                    "/item/**",
                    "/error",
                    "/favicon.ico",
                    "/payment-info",
                    "/address/save",
                    "/cart/order-success"
                ).permitAll()
                
                // Protected endpoints
                .requestMatchers(
                    "/profile/**",
                    "/cart/**",
                    "/api/products/**",
                    "/api/cart/**",
                    "/api/orders/**",
                    "/api/reviews/**",
                    "/address/delete/**"
                ).authenticated()
                
                // Any other request
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400) // 24 hours
                .userDetailsService(userDetailsService)
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
                .and()
                .sessionFixation().migrateSession()
                .invalidSessionUrl("/login?invalid=true")
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/api/chat/send",
                    "/api/cart/**",
                    "/api/orders/**",
                    "/address/delete/**",
                    "/address/set-default/**"
                )
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/login?accessDenied=true")
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin()
                .cacheControl().disable()
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
} 