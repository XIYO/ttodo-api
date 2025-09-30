package point.ttodoApi.shared.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import point.ttodoApi.shared.config.auth.properties.JwtProperties;
import point.ttodoApi.auth.infrastructure.security.CustomUserDetailsService;
import point.ttodoApi.shared.config.auth.SecurityTestConfig;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;
import point.ttodoApi.auth.application.TokenService;
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.auth.presentation.mapper.AuthPresentationMapper;
import point.ttodoApi.auth.application.AuthCommandService;
import point.ttodoApi.auth.application.AuthQueryService;
import point.ttodoApi.user.application.UserCommandService;
import point.ttodoApi.user.application.UserQueryService;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.shared.validation.sanitizer.ValidationUtils;
import point.ttodoApi.shared.config.properties.AppProperties;
import point.ttodoApi.auth.presentation.CookieService;
import point.ttodoApi.shared.validation.service.DisposableEmailService;
import point.ttodoApi.shared.validation.service.ForbiddenWordService;

import static org.mockito.Mockito.*;

/**
 * 테스트 공통 설정
 * Controller 테스트에 필요한 기본 Bean들을 제공
 */
@TestConfiguration
@Import(SecurityTestConfig.class)
public class TestCommonConfig {
    
    @MockBean
    private JwtDecoder jwtDecoder;
    
    @MockBean
    private JwtEncoder jwtEncoder;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private RedisTemplate<String, Object> redisTemplate;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return new CustomUserDetailsService(userRepository);
    }
    
    @MockBean
    private JwtProperties jwtProperties;
    
    @MockBean
    private AuthPresentationMapper authPresentationMapper;
    
    @MockBean
    private AuthCommandService authCommandService;
    
    @MockBean
    private AuthQueryService authQueryService;
    
    @MockBean
    private UserCommandService userCommandService;
    
    @MockBean
    private UserQueryService userQueryService;
    
    @MockBean
    private ProfileService profileService;
    
    @MockBean
    private AppProperties appProperties;
    
    @MockBean
    private CookieService cookieService;
    
    @MockBean
    private TokenService tokenService;
    
    @MockBean
    private ErrorMetricsCollector errorMetricsCollector;
    
    // Mock validation services that the validators depend on
    @MockBean
    private DisposableEmailService disposableEmailService;
    
    @MockBean 
    private ForbiddenWordService forbiddenWordService;
    
    @MockBean
    private ValidationUtils validationUtils;
    
    // Also add policy factory mocks needed by ValidationUtils
    @MockBean
    @Qualifier("htmlSanitizerPolicy")
    private org.owasp.html.PolicyFactory htmlSanitizerPolicy;
    
    @MockBean
    @Qualifier("strictHtmlSanitizerPolicy")
    private org.owasp.html.PolicyFactory strictHtmlSanitizerPolicy;
    
    @MockBean
    @Qualifier("noHtmlPolicy") 
    private org.owasp.html.PolicyFactory noHtmlPolicy;
    
    @Bean
    @SuppressWarnings("unchecked")
    public ValueOperations<String, Object> valueOperations() {
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        return valueOps;
    }
}