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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(MemberService.class)
class MemberServiceTest {
  @Autowired
  MemberService memberService;

  @Test
  @DisplayName("회원 생성 성공")
  void createMemberSuccess() {
    CreateMemberCommand command = new CreateMemberCommand("test@example.com", "password", "nickname");
    memberService.createMember(command);
    Member member = memberService.findByEmail("test@example.com");
    assertEquals("test@example.com", member.getEmail());
    assertEquals("password", member.getPassword());
    assertEquals("nickname", member.getNickname());
  }

  @Test
  @DisplayName("중복 이메일 회원 생성 시 예외 발생")
  void createMemberDuplicateEmail() {
    CreateMemberCommand command = new CreateMemberCommand("dup@example.com", "password", "nickname");
    memberService.createMember(command);
    CreateMemberCommand duplicateCommand = new CreateMemberCommand("dup@example.com", "password2", "nickname2");
    BusinessException exception = assertThrows(BusinessException.class, () -> memberService.createMember(duplicateCommand));
    assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
  }

  @Test
  @DisplayName("이메일로 회원 조회 성공")
  void findByEmailSuccess() {
    CreateMemberCommand command = new CreateMemberCommand("find@example.com", "password", "nickname");
    memberService.createMember(command);
    Member member = memberService.findByEmail("find@example.com");
    assertEquals("find@example.com", member.getEmail());
  }

  @Test
  @DisplayName("이메일로 회원 조회 실패 시 예외 발생")
  void findByEmailNotFound() {
    BusinessException exception = assertThrows(BusinessException.class, () -> memberService.findByEmail("notfound@example.com"));
    assertEquals("회원 정보 없음", exception.getMessage());
  }

  @Test
  @DisplayName("회원 ID로 회원 조회 성공")
  void findVerifiedMemberSuccess() {
    CreateMemberCommand command = new CreateMemberCommand("verified@example.com", "password", "nickname");
    memberService.createMember(command);
    Member created = memberService.findByEmail("verified@example.com");
    Member member = memberService.findVerifiedMember(created.getId());
    assertEquals(created.getId(), member.getId());
  }

  @Test
  @DisplayName("회원 ID로 회원 조회 실패 시 예외 발생")
  void findVerifiedMemberNotFound() {
    EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
        () -> memberService.findVerifiedMember(java.util.UUID.randomUUID()));
    assertTrue(exception.getMessage().contains("Member"));
  }

  @Test
  @DisplayName("회원 정보 업데이트")
  void updateMember() {
    CreateMemberCommand command = new CreateMemberCommand("update@example.com", "password", "nickname");
    memberService.createMember(command);
    Member member = memberService.findByEmail("update@example.com");
    UpdateMemberCommand updateCommand = new UpdateMemberCommand(member.getId(), "newNickname");
    memberService.updateMember(updateCommand);
    Member updated = memberService.findByEmail("update@example.com");
    assertEquals("newNickname", updated.getNickname());
  }
}
