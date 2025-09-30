package point.ttodoApi.common.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import point.ttodoApi.shared.validation.service.DisposableEmailService;
import point.ttodoApi.shared.validation.service.ForbiddenWordService;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;
import point.ttodoApi.profile.infrastructure.persistence.ProfileRepository;

import static org.mockito.Mockito.*;

@TestConfiguration
public class WebMvcSliceConfig {
    
    @Bean
    @Primary
    public DisposableEmailService disposableEmailService() {
        DisposableEmailService mock = mock(DisposableEmailService.class);
        when(mock.isDisposableEmail(any())).thenReturn(false);
        return mock;
    }
    
    @Bean
    @Primary
    public ForbiddenWordService forbiddenWordService() {
        ForbiddenWordService mock = mock(ForbiddenWordService.class);
        when(mock.containsForbiddenWord(any())).thenReturn(false);
        return mock;
    }
    
    @Bean
    @Primary
    public UserRepository userRepository() {
        UserRepository mock = mock(UserRepository.class);
        when(mock.existsByEmail(any())).thenReturn(false);
        return mock;
    }
    
    @Bean
    @Primary
    public ProfileRepository profileRepository() {
        return mock(ProfileRepository.class);
    }
}