package com.xyzy.config;

import com.xyzy.filter.JwtAuthenticationTokenFilter;
import com.xyzy.handler.AccessDeniedHandlerImpl;
import com.xyzy.handler.AuthenticationEntryPointImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired
    private AuthenticationEntryPointImpl authenticationEntryPoint;

    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF，因为你是前后端分离项目，用 JWT，不用 Session 表单登录
                .csrf(csrf -> csrf.disable())

                // 不通过 Session 获取 SecurityContext
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 接口权限配置
                .authorizeHttpRequests(auth -> auth
                        // 登录接口：未登录用户才能访问
                        .requestMatchers("/login").anonymous()

                        // 退出登录：必须登录后才能访问
                        .requestMatchers("/logout").authenticated()

                        // 发表评论：必须登录
                        .requestMatchers("/comment").authenticated()

                        // 查看个人信息：必须登录
                        .requestMatchers("/user/userInfo").authenticated()

                        // 上传：必须登录
                        .requestMatchers("/upload").authenticated()

                        // 其他接口全部放行
                        .anyRequest().permitAll()
                )

                // 自定义认证失败 / 权限不足处理器
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)

                // 开启跨域
                .cors(cors -> {})

                // 关键：关闭 Spring Security 默认 logout，防止它拦截你的 /logout 接口
                .logout(logout -> logout.disable());

        return http.build();
    }
}