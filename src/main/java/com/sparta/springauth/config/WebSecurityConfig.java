package com.sparta.springauth.config;

import com.sparta.springauth.jwt.JwtAuthorizationFilter;
import com.sparta.springauth.jwt.JwtAuthenticationFilter;
import com.sparta.springauth.jwt.JwtUtil;
import com.sparta.springauth.security.UserDetailsServiceImpl;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Spring Security 지원을 가능하게 함
@EnableMethodSecurity(securedEnabled = true) //메서드 수준에서 보안설정을 가능하게 함
public class WebSecurityConfig {
    // JWT 토근의 생성 및 검증을 담당하는 유틸 클래스
    private final JwtUtil jwtUtil;
    // Spring Security 에서 사용자 정보를 로드하는 서비스 구현체
    private final UserDetailsServiceImpl userDetailsService;
    // 인증 설정을 위한 구성 클래스입니다. 여기서 인증 매니저를 가져옵니다.
    private final AuthenticationConfiguration authenticationConfiguration;

    public WebSecurityConfig(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService, AuthenticationConfiguration authenticationConfiguration) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authenticationConfiguration = authenticationConfiguration;
    }
    // 인증 과정을 처리하는 매니저로 AuthenticationConfiguration에서 가져옴
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    //사용자가 로그인을 시도할 때 자격 증명을 확인하고, 성공 시 JWT 토근을 생성하여 응답합니다.
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);
        filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
        return filter;
    }
    //JWT 토근을 검증하여 요청이 인증된 사용자의 요청인지를 확인합니다.
    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF 설정 비활성화
        // CSRF 보호는 주로 브라우저를 대상으로 하는 공격을 방지하기 위해 사용되며, RESTful API에서 사용 안함
        http.csrf((csrf) -> csrf.disable());

        // 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정
        // 서버가 세션을 생성하거나 유지하지 않도록 합니다. 대신 JWT 토큰을 사용해 사용자 상태를 관리
        http.sessionManagement((sessionManagement) ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
        // 정적 리소스에 대한 접근을 허용
        // /api/user/** 로 시작하는 경로에 대한 접근을 허용하여 로그인 페이지나 회원가입 요청 처리
        // 그 외의 모든 요청은 인증된 사용자만 접근할 수 있도록 설정합니다.
        http.authorizeHttpRequests((authorizeHttpRequests) ->
                authorizeHttpRequests
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // resources 접근 허용 설정
                        .requestMatchers("/api/user/**").permitAll() // '/api/user/'로 시작하는 요청 모두 접근 허가
                        .anyRequest().authenticated() // 그 외 모든 요청 인증처리
        );
        // 로그인 경로를 로그인 페이지로 설정하며, 이 경로는 인증 없이 접근할 수 있습니다.
        http.formLogin((formLogin) ->
                formLogin
                        .loginPage("/api/user/login-page").permitAll()
        );

        // 필터 관리
        //JwtAuthorizationFilter는 JwtAuthenticationFilter보다 먼저 실행되도록 설정됩니다.
        //이 필터는 요청에 포함된 JWT 토큰을 검증하여 사용자를 인증합니다.
        http.addFilterBefore(jwtAuthorizationFilter(), JwtAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // 접근 불가 페이지
        http.exceptionHandling((exceptionHandling) ->
                exceptionHandling
                        // "접근 불가" 페이지 URL 설정
                        .accessDeniedPage("/forbidden.html")
        );

        return http.build();
    }
}