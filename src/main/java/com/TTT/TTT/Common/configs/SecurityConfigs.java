package com.TTT.TTT.Common.configs;


import com.TTT.TTT.Common.auth.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfigs {
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfigs(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain myFilter(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(cors->cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable) //csrf 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) //HTTP basic 비활성화
//                특정 url 패턴에 대해서는 Authentication 객체 요구하지 않음 (인증처리 제외)
                .authorizeHttpRequests(a->a.requestMatchers(
                        "/sms/**","/ttt/user/"
                        , "/ttt/user/create", "/ttt/user/login","ttt/user/refresh-token"
                        ,"ttt/category/all","/connect/**", "/ttt/user/google/doLogin",
                        "/ttt/user/kakao/doLogin", "ttt/user/oauth/create","/ttt/user/checkLoginId","/ttt/user/checkNickName",
                        "/ttt/chat/total/rooms", "/ttt/post/popular/like", "/ttt/post/findAll",
                        "/ttt/post/category/**", "/ttt/user/batchRank", "/ttt/user/rankingfive",
                        "/ttt/chat/room/group/list", "/ttt/post/total/count", "/ttt/user/total/user", "ttt/user/check")
                        .permitAll().anyRequest().authenticated())
                .sessionManagement(s-> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //세션방식을 사용하지 않겠다라는 의미
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("*")); //모든 HTTP 메서드 허용
        configuration.setAllowedHeaders(Arrays.asList("*")); //모든 헤더값 허용
        configuration.setAllowCredentials(true); //자격 증명 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); //모든 url 패턴에 대해 cors 허용 설정
        return source;
    }

    @Bean
    public PasswordEncoder makePassword(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}