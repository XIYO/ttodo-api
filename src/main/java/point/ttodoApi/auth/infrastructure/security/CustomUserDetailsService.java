package point.ttodoApi.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Security UserDetailsService 구현체
 * UUID를 username으로 사용하여 표준 User 객체 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username (UUID): {}", username);
        
        try {
            UUID userId = UUID.fromString(username);
            
            // UUID로 사용자 조회 (존재 여부 확인)
            userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + username));
            
            // Spring Security의 표준 User 객체 생성 및 반환
            // UUID를 username으로 사용, 패스워드는 JWT 인증이므로 빈 문자열
            return new User(
                userId.toString(),
                "",
                true,  // enabled
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid UUID format: " + username);
        }
    }
}