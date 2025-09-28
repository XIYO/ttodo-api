package point.ttodoApi.user.application;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.shared.error.EntityNotFoundException;
import point.ttodoApi.user.application.command.*;
import point.ttodoApi.user.application.event.UserCreatedEvent;
import point.ttodoApi.user.application.mapper.UserApplicationMapper;
import point.ttodoApi.user.application.result.UserResult;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;

/**
 * 회원 명령 서비스
 * TTODO 아키텍처 패턴: Command 처리 전용 서비스 (쓰기 작업)
 */
@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class UserCommandService {
  private static final String MEMBER_ENTITY = "User";

  private final UserRepository UserRepository;
  private final ProfileService profileService;
  private final ApplicationEventPublisher eventPublisher;
  private final UserApplicationMapper mapper;
  private final PasswordEncoder passwordEncoder;

  /**
   * 회원 생성
   * TTODO 아키텍처 패턴: Command 기반 회원 생성 처리
   */
  public UserResult createUser(@Valid CreateUserCommand command) {
    // TTODO 아키텍처 패턴: User 도메인에서 패스워드 암호화 처리
    // TTODO 규칙: DTO 간 변환은 무조건 매퍼로
    CreateUserCommand encryptedCommand = mapper.toEncryptedCommand(
            command,
            passwordEncoder.encode(command.password())
    );

    User user = mapper.toEntity(encryptedCommand);
    User savedUser = UserRepository.save(user);

    // Create profile for the new user with nickname and introduction
    Profile profile = profileService.createProfile(savedUser.getId(), command.nickname());
    if (command.introduction() != null && !command.introduction().isEmpty()) {
      profile.setIntroduction(command.introduction());
      profileService.saveProfile(profile);
    }

    eventPublisher.publishEvent(new UserCreatedEvent(
            savedUser.getId(),
            savedUser.getEmail(),
            command.nickname()
    ));

    // TTODO 아키텍처 패턴: Application 매퍼 사용
    return mapper.toResult(savedUser, command.nickname());
  }

  /**
   * 회원 정보 수정
   * TTODO 아키텍처 패턴: Command 기반 회원 정보 수정 처리
   */
  public UserResult updateUser(@Valid UpdateUserCommand command) {
    User user = UserRepository.findById(command.userId())
            .orElseThrow(() -> new EntityNotFoundException(MEMBER_ENTITY, command.userId()));

    // nickname은 이제 Profile에서 관리
    // Profile 업데이트는 ProfileService를 통해 처리
    if (command.hasNickname()) {
      Profile profile = profileService.getProfile(command.userId());
      profile.setNickname(command.nickname());
      profileService.saveProfile(profile);
    }

    User savedUser = UserRepository.save(user);

    // Profile에서 nickname 가져오기
    Profile profile = profileService.getProfile(command.userId());

    // TTODO 아키텍처 패턴: Application 매퍼 사용
    return mapper.toResult(savedUser, profile.getNickname());
  }
}