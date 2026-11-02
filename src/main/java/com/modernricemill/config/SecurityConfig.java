package com.modernricemill.config;



import com.modernricemill.security.CustomUserDetailsService;

import com.modernricemill.security.HtmlAccessDeniedHandler;

import com.modernricemill.security.HtmlAuthenticationEntryPoint;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;



@Configuration

@EnableWebSecurity

@EnableMethodSecurity

public class SecurityConfig {



    @Bean

    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }



    @Bean

    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();

    }



    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new org.springframework.security.web.context.HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomUserDetailsService userDetailsService,
            HtmlAuthenticationEntryPoint authenticationEntryPoint,
            HtmlAccessDeniedHandler accessDeniedHandler,
            SecurityContextRepository securityContextRepository)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(

                                "/",

                                "/index.html",

                                "/login.html",

                                "/customer-login.html",

                                "/register.html",

                                "/css/**",

                                "/js/**",

                                "/images/**",

                                "/api/auth/customer/login",

                                "/api/auth/me",

                                "/error"

                        ).permitAll()

                        .requestMatchers("/admin.html").hasRole("ADMIN")

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers(

                                "/products.html",

                                "/cart.html",

                                "/checkout.html",

                                "/tracking.html",

                                "/account.html",

                                "/feedback.html"

                        ).hasRole("USER")

                        .requestMatchers("/api/cart/**", "/api/checkout").hasRole("USER")

                        .requestMatchers("/api/products").hasAnyRole("USER", "ADMIN")

                        .requestMatchers("/api/orders/**", "/api/feedback").hasRole("USER")

                        .requestMatchers("/api/users/**").hasRole("USER")

                        .anyRequest().authenticated()

                )

                .userDetailsService(userDetailsService)

                .exceptionHandling(e -> e

                        .authenticationEntryPoint(authenticationEntryPoint)

                        .accessDeniedHandler(accessDeniedHandler)

                )

                .formLogin(form -> form

                        .loginPage("/login.html")

                        .loginProcessingUrl("/login")

                        .usernameParameter("username")

                        .passwordParameter("password")

                        .successHandler((request, response, authentication) -> {

                            String next = request.getParameter("next");

                            if (next != null && next.startsWith("/") && !next.startsWith("//")) {

                                response.sendRedirect(next);

                                return;

                            }

                            SavedRequestAwareAuthenticationSuccessHandler handler =

                                    new SavedRequestAwareAuthenticationSuccessHandler();

                            handler.setDefaultTargetUrl("/index.html");

                            handler.onAuthenticationSuccess(request, response, authentication);

                        })

                        .failureHandler((request, response, exception) -> {

                            String next = request.getParameter("next");

                            if (next != null && next.startsWith("/") && !next.startsWith("//")) {

                                response.sendRedirect("/login.html?error=1&next=" + java.net.URLEncoder.encode(next, java.nio.charset.StandardCharsets.UTF_8));

                                return;

                            }

                            response.sendRedirect("/login.html?error=1");

                        })

                        .permitAll()

                )

                .logout(logout -> logout

                        .logoutUrl("/logout")

                        .logoutSuccessUrl("/index.html")

                        .invalidateHttpSession(true)

                        .deleteCookies("JSESSIONID")

                        .permitAll()

                );



        return http.build();

    }

}


