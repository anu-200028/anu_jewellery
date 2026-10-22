package com.example.anu_jewellery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

import com.example.anu_jewellery.filter.JwtAuthenticationFilter;
import com.example.anu_jewellery.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    // Completely bypass security checks for favicon so it never throws 403
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/favicon.ico");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider authenticationProvider) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // 1. Completely public paths (wishlist.html added here)
            		.requestMatchers(
            			    "/", "/index.html",
            			    "/signup.html", "/login.html", "/admin-login.html",
            			    "/customer-home.html", "/view-cart.html",
            			    "/customer-payment.html", "/wishlist.html",
            			    "/admin-home.html",
            			    "/add-product.html", "/all-products-admin.html",
            			    "/edit-product.html", "/view-product-admin.html","/api/metals/**",
            			    "/css/**", "/js/**","/images/**"
            			).permitAll()
                .requestMatchers("/auth/**", "/hello").permitAll()
                
                // 2. Public GET access for viewing products
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/products/**").permitAll()

                // 3. Product modification rules for ADMIN
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                
                // 4. Customer cart and backend payment API rules
             // 4. Customer cart, payment API, and cart REST API rules
                .requestMatchers("/customer/cart", "/customer/cart/**", "/api/cart/**").hasAnyAuthority("USER", "ROLE_USER", "ADMIN", "ROLE_ADMIN", "CUSTOMER", "ROLE_CUSTOMER")
                .requestMatchers("/customer/payment/**", "/api/payment/**").hasAnyAuthority("USER", "ROLE_USER", "ADMIN", "ROLE_ADMIN", "CUSTOMER", "ROLE_CUSTOMER")
                
                // 5. Admin and Customer general path controls
                .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .requestMatchers("/customer/**").hasAnyAuthority("USER", "ROLE_USER", "ADMIN", "ROLE_ADMIN", "CUSTOMER", "ROLE_CUSTOMER")
                
                // 6. Catch-all requirement
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}