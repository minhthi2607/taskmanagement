package com.project.taskmanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;
        private final LoginInputValidationFilter loginInputValidationFilter;

        // Mã hoá password khi đăng ký, so khớp password khi đăng nhập.
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // Bật bảo vệ CSRF chống tấn công Cross-Site Request Forgery
                                .csrf(Customizer.withDefaults())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/team/create", "/team/*/edit", "/team/*/delete",
                                                                "/team/*/members/**")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.GET, "/team/accept-invite", "/board/accept-invite")
                                                .authenticated()
                                                .requestMatchers(HttpMethod.GET, "/", "/team/list", "/team/{id}", "/board/*", "/board/*/search")
                                                .permitAll()
                                                .requestMatchers("/auth/**", "/css/**", "/js/**", "/uploads/**",
                                                                "/favicon.ico", "/error")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/auth/login") // trang login tự viết (Thymeleaf)
                                                .loginProcessingUrl("/auth/login") // URL nhận POST khi submit form
                                                                                   // login
                                                .usernameParameter("email") // đăng nhập bằng email, không phải username
                                                .passwordParameter("password")
                                                .defaultSuccessUrl("/", false) // chuyển hướng về trang yêu cầu ban đầu (RequestCache)
                                                .failureUrl("/auth/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/auth/logout")
                                                .logoutSuccessUrl("/auth/login?logout=true")
                                                .permitAll())
                                .userDetailsService(userDetailsService)
                                .addFilterBefore(loginInputValidationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
