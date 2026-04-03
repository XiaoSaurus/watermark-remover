package com.watermark.config;

import com.watermark.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/api/video/parse",        // 视频解析接口公开
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                // 历史记录：GET 查询允许匿名（未登录返回空），写操作需要认证
                .requestMatchers(HttpMethod.GET, "/api/parse-history").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/history").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/parse-history/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/parse-history").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/history/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/history").authenticated()
                .requestMatchers("/api/video/download").permitAll()     // 视频下载公开（代理下载）
                .requestMatchers("/api/avatar/**").permitAll()          // 头像相关公开
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
