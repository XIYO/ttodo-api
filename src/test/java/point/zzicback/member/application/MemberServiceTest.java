package point.zzicback.member.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import point.zzicback.common.error.BusinessException;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.application.dto.command.UpdateMemberCommand;
import point.zzicback.member.domain.Member;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(MemberService.class)
class MemberServiceTest {
  @Autowired
  MemberService memberService;

  @Test
  @DisplayName("회원 생성 성공")
  void createMemberSuccess() {
    CreateMemberCommand command = new CreateMemberCommand("test@example.com", "password", "nickname", null);
    memberService.createMember(command);
    Member member = memberService.findByEmailOrThrow("test@example.com");
    assertEquals("test@example.com", member.getEmail());
    assertEquals("password", member.getPassword());
    assertEquals("nickname", member.getNickname());
  }

  @Test
  @DisplayName("소개글과 함께 회원 생성 성공")
  void createMemberWithIntroductionSuccess() {
    CreateMemberCommand command = new CreateMemberCommand("intro@example.com", "password", "nickname", "안녕하세요! 새로운 회원입니다.");
    memberService.createMember(command);
    Member member = memberService.findByEmailOrThrow("intro@example.com");
    assertEquals("intro@example.com", member.getEmail());
    assertEquals("password", member.getPassword());
    assertEquals("nickname", member.getNickname());
    assertEquals("안녕하세요! 새로운 회원입니다.", member.getIntroduction());
  }

  @Test
  @DisplayName("이메일로 회원 조회 성공")
  void findByEmailSuccess() {
    CreateMemberCommand command = new CreateMemberCommand("find@example.com", "password", "nickname", null);
    memberService.createMember(command);
    Member member = memberService.findByEmailOrThrow("find@example.com");
    assertEquals("find@example.com", member.getEmail());
  }

  @Test
  @DisplayName("이메일로 회원 조회 실패 시 예외 발생")
  void findByEmailNotFound() {
    assertThrows(BusinessException.class, () -> memberService.findByEmailOrThrow("notfound@example.com"));
  }

  @Test
  @DisplayName("회원 ID로 회원 조회 성공")
  void findVerifiedMemberSuccess() {
    CreateMemberCommand command = new CreateMemberCommand("verified@example.com", "password", "nickname", null);
    memberService.createMember(command);
    Member created = memberService.findByEmailOrThrow("verified@example.com");
    Member member = memberService.findVerifiedMember(created.getId());
    assertEquals(created.getId(), member.getId());
  }

  @Test
  @DisplayName("회원 ID로 회원 조회 실패 시 예외 발생")
  void findVerifiedMemberNotFound() {
    assertThrows(EntityNotFoundException.class,
        () -> memberService.findVerifiedMember(UUID.randomUUID()));
  }

  @Test
  @DisplayName("회원 정보 업데이트")
  void updateMember() {
    CreateMemberCommand command = new CreateMemberCommand("update@example.com", "password", "nickname", null);
    memberService.createMember(command);
    Member member = memberService.findByEmailOrThrow("update@example.com");
    UpdateMemberCommand updateCommand = new UpdateMemberCommand(member.getId(), "newNickname", "새로운 소개글");
    memberService.updateMember(updateCommand);
    Member updated = memberService.findByEmailOrThrow("update@example.com");
    assertEquals("newNickname", updated.getNickname());
    assertEquals("새로운 소개글", updated.getIntroduction());
  }

  @Test
  @DisplayName("닉네임만 업데이트")
  void updateMemberNicknameOnly() {
    CreateMemberCommand command = new CreateMemberCommand("nickname-only@example.com", "password", "oldNickname", null);
    memberService.createMember(command);
    Member member = memberService.findByEmailOrThrow("nickname-only@example.com");
    UpdateMemberCommand updateCommand = new UpdateMemberCommand(member.getId(), "newNickname", null);
    memberService.updateMember(updateCommand);
    Member updated = memberService.findByEmailOrThrow("nickname-only@example.com");
    assertEquals("newNickname", updated.getNickname());
    assertNull(updated.getIntroduction());
  }

  @Test
  @DisplayName("소개글만 업데이트")
  void updateMemberIntroductionOnly() {
    CreateMemberCommand command = new CreateMemberCommand("intro-only@example.com", "password", "nickname", null);
    memberService.createMember(command);
    Member member = memberService.findByEmailOrThrow("intro-only@example.com");
    UpdateMemberCommand updateCommand = new UpdateMemberCommand(member.getId(), null, "새로운 소개글");
    memberService.updateMember(updateCommand);
    Member updated = memberService.findByEmailOrThrow("intro-only@example.com");
    assertEquals("nickname", updated.getNickname());
    assertEquals("새로운 소개글", updated.getIntroduction());
  }

  @Test
  @DisplayName("이메일로 회원 조회 Optional - 존재하는 경우")
  void findByEmailOptionalExists() {
    CreateMemberCommand command = new CreateMemberCommand("optional@example.com", "password", "nickname", null);
    memberService.createMember(command);
    Optional<Member> member = memberService.findByEmail("optional@example.com");
    assertTrue(member.isPresent());
    assertEquals("optional@example.com", member.get().getEmail());
  }

  @Test
  @DisplayName("이메일로 회원 조회 Optional - 존재하지 않는 경우")
  void findByEmailOptionalNotExists() {
    Optional<Member> member = memberService.findByEmail("nonexistent@example.com");
    assertTrue(member.isEmpty());
  }

  @Test
  @DisplayName("ID로 회원 조회 Optional - 존재하는 경우")
  void findByIdOptionalExists() {
    CreateMemberCommand command = new CreateMemberCommand("id-optional@example.com", "password", "nickname", null);
    memberService.createMember(command);
    Member created = memberService.findByEmailOrThrow("id-optional@example.com");
    Optional<Member> member = memberService.findById(created.getId());
    assertTrue(member.isPresent());
    assertEquals(created.getId(), member.get().getId());
  }

  @Test
  @DisplayName("ID로 회원 조회 Optional - 존재하지 않는 경우")
  void findByIdOptionalNotExists() {
    Optional<Member> member = memberService.findById(UUID.randomUUID());
    assertTrue(member.isEmpty());
  }
}
