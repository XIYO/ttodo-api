package point.ttodoApi.user.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.user.application.mapper.UserApplicationMapper;
import point.ttodoApi.user.application.query.UserEmailQuery;
import point.ttodoApi.user.application.query.UserListQuery;
import point.ttodoApi.user.application.query.UserQuery;
import point.ttodoApi.user.application.result.UserResult;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;
import point.ttodoApi.shared.error.BusinessException;
import point.ttodoApi.shared.error.EntityNotFoundException;

import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;

/**
 * 회원 쿼리 서비스
 * TTODO 아키텍처 패턴: Query 처리 전용 서비스 (읽기 작업)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class UserQueryService {
    private static final String MEMBER_ENTITY = "User";
    
    private final UserRepository UserRepository;
    private final UserApplicationMapper mapper;

    /**
     * 회원 ID로 조회
     * TTODO 아키텍처 패턴: Query 기반 회원 조회
     */
    public UserResult getUser(@Valid UserQuery query) {
        User user = findByIdOrThrow(query.userId());
        // TTODO 아키텍처 패턴: Application 매퍼 사용
        return mapper.toResult(user);
    }

    /**
     * 이메일로 회원 조회
     * TTODO 아키텍처 패턴: Query 기반 이메일 검색
     */
    public Optional<UserResult> findUserByEmail(@Valid UserEmailQuery query) {
        return UserRepository.findByEmail(query.email())
                // TTODO 아키텍처 패턴: Application 매퍼 사용
                .map(mapper::toResult);
    }

    /**
     * 이메일로 회원 조회 (예외 발생)
     * TTODO 아키텍처 패턴: Query 기반 이메일 검색 with validation
     */
    public UserResult getUserByEmail(@Valid UserEmailQuery query) {
        User user = UserRepository.findByEmail(query.email())
                .orElseThrow(() -> new BusinessException("회원 정보 없음"));
        // TTODO 아키텍처 패턴: Application 매퍼 사용
        return mapper.toResult(user);
    }

    /**
     * 회원 목록 조회 (페이징)
     * TTODO 아키텍처 패턴: Query 기반 회원 목록 조회
     */
    public Page<UserResult> getuser(@Valid UserListQuery query) {
        return UserRepository.findAll(query.pageable())
                // TTODO 아키텍처 패턴: Application 매퍼 사용
                .map(mapper::toResult);
    }

    /**
     * User 권한 검증을 위한 엔티티 조회 (Spring Security @PreAuthorize용)
     * TTODO 아키텍처 패턴: 도메인 엔티티 직접 반환 (권한 검증 전용)
     */
    public User findUserForAuth(UUID userId) {
        return UserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(MEMBER_ENTITY, userId));
    }

    /**
     * ID로 회원 조회 (내부용)
     */
    private User findByIdOrThrow(UUID userId) {
        return UserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(MEMBER_ENTITY, userId));
    }

    /**
     * 회원 존재 확인 (도메인 엔티티 반환)
     * TTODO 아키텍처 패턴: 검증된 회원 엔티티 반환
     */
    public User findVerifiedUser(UUID userId) {
        return findByIdOrThrow(userId);
    }

    /**
     * 선택적 회원 조회 (Optional 반환)
     * TTODO 아키텍처 패턴: 존재하지 않을 수 있는 회원 조회
     */
    public Optional<User> findUserById(UUID userId) {
        return UserRepository.findById(userId);
    }

    /**
     * 이메일로 회원 엔티티 조회 (Optional 반환)
     * TTODO 아키텍처 패턴: 도메인 엔티티 직접 반환
     */
    public Optional<User> findUserEntityByEmail(String email) {
        return UserRepository.findByEmail(email);
    }
}