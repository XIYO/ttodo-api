package point.zzicback.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;

@Component
@RequiredArgsConstructor
public class AnonMemberInitializer implements ApplicationRunner {
  private final MemberService memberService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) {
    String anonymousEmail = "anon@zzic.com";
    if (memberService.findByEmail(anonymousEmail).isEmpty()) {
      String anonymousPassword = "";
      String anonymousNickname = "익명의 찍찍이";
      CreateMemberCommand command = new CreateMemberCommand(
          anonymousEmail,
          passwordEncoder.encode(anonymousPassword),
          anonymousNickname
      );
      memberService.createMember(command);
    }
  }
}
