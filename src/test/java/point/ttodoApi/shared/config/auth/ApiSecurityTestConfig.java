package point.ttodoApi.shared.config.auth;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import point.ttodoApi.shared.config.auth.properties.JwtProperties;
import point.ttodoApi.shared.validation.service.DisposableEmailService;
import point.ttodoApi.shared.validation.service.ForbiddenWordService;

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
}