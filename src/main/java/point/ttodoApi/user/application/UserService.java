package point.ttodoApi.user.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.user.application.command.*;
import point.ttodoApi.user.application.event.UserCreatedEvent;
import point.ttodoApi.user.application.result.UserResult;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.shared.error.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
  private static final String MEMBER_ENTITY = "User";
  private final UserRepository UserRepository;
  private final ProfileService profileService;
  private final ApplicationEventPublisher eventPublisher;

  public User createUser(CreateUserCommand command) {
    User user = User.builder()
            .email(command.email())
            .password(command.password())
            .build();
    User savedUser = UserRepository.save(user);

    // Profile에서 nickname 관리 (단일 소스)
    Profile profile = profileService.createProfile(savedUser.getId(), command.nickname());
    if (command.introduction() != null && !command.introduction().isEmpty()) {
      profile.setIntroduction(command.introduction());
      profileService.saveProfile(profile);
    }

    eventPublisher.publishEvent(new UserCreatedEvent(
            savedUser.getId(),
            savedUser.getEmail(),
            command.nickname()  // Profile의 nickname 사용
    ));

    return savedUser;
  }

  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    return UserRepository.findByEmail(email);
  }

  @Transactional(readOnly = true)
  public User findByEmailOrThrow(String email) {
    return UserRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("회원 정보 없음"));
  }

  @Transactional(readOnly = true)
  public Optional<User> findById(UUID userId) {
    return UserRepository.findById(userId);
  }

  @Transactional(readOnly = true)
  public User findByIdOrThrow(UUID userId) {
    return UserRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(MEMBER_ENTITY, userId));
  }

  @Transactional(readOnly = true)
  public User findVerifiedUser(UUID userId) {
    return UserRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(MEMBER_ENTITY, userId));
  }

  public void updateUser(UpdateUserCommand command) {
    User user = UserRepository.findById(command.userId())
            .orElseThrow(() -> new EntityNotFoundException(MEMBER_ENTITY, command.userId()));

    if (command.hasNickname()) {
      Profile profile = profileService.getProfile(user.getId());
      profile.setNickname(command.nickname());
      profileService.saveProfile(profile);
    }
    // introduction은 더 이상 User에서 관리하지 않음 (Profile로 이동)
  }

  @Transactional(readOnly = true)
  public Page<UserResult> getuser(Pageable pageable) {
    return UserRepository.findAll(pageable)
            .map(user -> {
              Profile profile = profileService.getProfile(user.getId());
              return new UserResult(
                      user.getId(),
                      user.getEmail(),
                      profile.getNickname());
            });
  }

  @Transactional(readOnly = true)
  public UserResult getUser(UUID userId) {
    var user = findByIdOrThrow(userId);
    Profile profile = profileService.getProfile(userId);
    return new UserResult(
            user.getId(),
            user.getEmail(),
            profile.getNickname());
  }

  /**
   * User 권한 검증을 위한 엔티티 조회 (Spring Security @PreAuthorize용)
   *
   * @param userId 멤버 ID
   * @return User 엔티티
   */
  @Transactional(readOnly = true)
  public User findUserForAuth(UUID userId) {
    return UserRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(MEMBER_ENTITY, userId));
  }

  /**
   * 사용자 삭제 시 프로필도 함께 제거한다.
   */
  public void deleteUser(UUID userId) {
    // 존재 확인 (예외 시 상위로 전달)
    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(MEMBER_ENTITY, userId));

    // 프로필 선삭제 (응용 계층 캐스케이드)
    profileService.deleteByOwner(user.getId());

    // 사용자 삭제
    UserRepository.delete(user);
  }
}
