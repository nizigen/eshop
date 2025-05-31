package com.example.eshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  @SuppressWarnings("deprecation")
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize -> authorize
            // 公共访问路径
            .requestMatchers(
                "/",
                "/home",
                "/products/**",
                "/auth/**",
                "/error",
                "/css/**",
                "/js/**",
                "/images/**",
                "/webjars/**",
                "/favicon.ico")
            .permitAll()
            // 用户相关路径需要认证
            .requestMatchers(
                "/user/**", // 所有用户相关页面
                "/user/orders/**", // 订单相关
                "/user/points/**", // 积分相关
                "/user/wallet/**", // 钱包相关
                "/user/profile/**" // 个人资料相关
            ).authenticated()
            // 购物车需要认证
            .requestMatchers("/cart/**").authenticated()
            // 默认其他所有请求都需要认证
            .anyRequest().authenticated())
        .formLogin(form -> form
            .loginPage("/auth/login") // 登录页面路径
            .loginProcessingUrl("/auth/login") // 登录处理路径
            .defaultSuccessUrl("/", true) // 登录成功后跳转
            .failureUrl("/auth/login?error=true") // 登录失败后跳转
            .permitAll())
        .logout(logout -> logout
            .logoutUrl("/auth/logout") // 登出处理路径
            .logoutSuccessUrl("/auth/login?logout=true") // 登出成功后跳转
            .permitAll())
        .csrf(csrf -> csrf.disable()); // 禁用CSRF保护

    return http.build();
  }
}