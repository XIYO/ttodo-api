package point.ttodoApi.shared.config.auth;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import point.ttodoApi.shared.config.auth.properties.JwtProperties;
import point.ttodoApi.shared.validation.service.DisposableEmailService;
import point.ttodoApi.shared.validation.service.ForbiddenWordService;

import java.io.Serializable;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class ApiSecurityTestConfig {
    
    @Bean
    @Primary
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
    
    @Bean
    @Primary
    public DisposableEmailService disposableEmailService() {
        DisposableEmailService mock = mock(DisposableEmailService.class);
        org.mockito.Mockito.when(mock.isDisposableEmail(org.mockito.ArgumentMatchers.any())).thenReturn(false);
        return mock;
    }
    
    @Bean
    @Primary
    public ForbiddenWordService forbiddenWordService() {
        ForbiddenWordService mock = mock(ForbiddenWordService.class);
        org.mockito.Mockito.when(mock.containsForbiddenWord(org.mockito.ArgumentMatchers.any())).thenReturn(false);
        return mock;
    }
    
    @Bean
    @Primary
    public JwtProperties jwtProperties() {
        return mock(JwtProperties.class);
    }

    @Bean
    @Primary
    public PermissionEvaluator permissionEvaluator() {
        return new PermissionEvaluator() {
            @Override
            public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
                // 테스트 환경에서는 모든 권한 허용
                return true;
            }

            @Override
            public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
                // 테스트 환경에서는 모든 권한 허용
                return true;
            }
        };
    }

    @Bean
    @Primary
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(PermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }
}